const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {VertexAI} = require("@google-cloud/vertexai");

admin.initializeApp();

// Vertex AI 설정
const projectId = "smartee-319d0"; // Firebase 프로젝트 ID
const location = "us-central1"; // Vertex AI API 위치

// Vertex AI 클라이언트 초기화 (최신 API로 업데이트)
const vertexAi = new VertexAI({project: projectId, location: location});
const genAiModel = vertexAi.getGenerativeModel({model: "gemini-2.0-flash"});

// 스터디 추천 함수
exports.recommendStudy = functions.https.onCall(async (req) => {
  console.log("recommendStudy called with data:", req.data);
  try {
    if (!req.auth) {
      console.log("인증 없음 - 무시하고 진행");
      // 인증 에러를 던지지 않고 계속 진행
    }

    // 사용자 정보
    const { categories, inkLevel } = req.data;
    console.log("Parsed categories:", categories, "Parsed inkLevel:", inkLevel);

    if (!categories || !Array.isArray(categories)) {
      throw new functions.https.HttpsError(
          "invalid-argument",
          "카테고리 목록이 필요합니다",
      );
    }

    // Firestore에서 스터디 목록 가져오기
    const studiesSnapshot = await admin.firestore()
        .collection("studies")
        .where("minInkLevel", "<=", inkLevel)
        .get();

    if (studiesSnapshot.empty) {
      return {
        recommendedStudyId: null,
        message: "조건에 맞는 스터디가 없습니다",
      };
    }

    // 스터디 목록 구성
    const studies = [];
    studiesSnapshot.forEach((doc) => {
      studies.push({
        id: doc.id,
        ...doc.data(),
      });
    });

    // 인덱스 기반 스터디 목록 생성 (1부터 시작하는 인덱스 부여)
    const indexedStudies = studies.map((study, index) => ({
      index: index + 1,
      id: study.id,
      ...study
    }));

    // 유효한 스터디 ID 목록 생성 (검증용)
    const validStudyIds = studies.map(study => study.id);

    // AI 추천을 위한 프롬프트 작성 (인덱스 기반 선택 방식으로 수정)
    const prompt = `
사용자 프로필:
- 관심 카테고리: ${categories.join(", ")}
- 잉크 레벨: ${inkLevel}

다음 스터디 목록 중에서 이 사용자에게 가장 적합한 스터디를 하나만 선택해주세요:

${indexedStudies.map((study) => `
[${study.index}]
ID: ${study.id}
제목: ${study.title}
카테고리: ${study.category}
필요 잉크 레벨: ${study.minInkLevel}
설명: ${study.description || "설명 없음"}
현재 인원: ${study.currentMemberCount}/${study.maxMemberCount}
인기도: 좋아요 ${study.likeCount || 0}개, 댓글 ${study.commentCount || 0}개
`).join("\n")}

중요: 반드시 위 목록에 있는 번호([1]-[${studies.length}]) 중 하나만 선택하세요.
위 목록에 없는 ID나 번호를 생성하지 마세요.

선택한 번호와 추천 이유를 JSON 형식으로 다음과 같이 반환해 주세요:
{"selectedIndex": 2, "reason": "추천 이유"}
`;

    // Vertex AI 모델 호출 (최신 API 방식으로 변경)
    const request = {
      contents: [
        {
          role: 'user',
          parts: [{ text: prompt }]
        }
      ]
    };
    const result = await genAiModel.generateContent(request);
    const candidates = result.response.candidates;
    const textResponse = candidates?.[0]?.content?.parts?.[0]?.text;
    if (!textResponse) {
      console.error("응답에서 JSON 텍스트를 찾을 수 없음:", JSON.stringify(result.response));
      throw new Error("응답 파싱 실패");
    }

    // JSON 추출
    const jsonMatch = textResponse.match(/{[\s\S]*}/);

    if (!jsonMatch) {
      console.error("응답에서 JSON을 찾을 수 없음:", textResponse);
      throw new Error("응답 파싱 실패");
    }

    // JSON 파싱
    const aiResponse = JSON.parse(jsonMatch[0]);
    console.log("AI 응답:", aiResponse);

    // 선택된 인덱스 검증 및 추천 스터디 ID 설정
    let recommendedStudyId = null;
    let reason = aiResponse.reason || "사용자에게 적합한 스터디입니다.";

    // 인덱스 검증
    if (aiResponse.selectedIndex && 
        Number.isInteger(aiResponse.selectedIndex) && 
        aiResponse.selectedIndex >= 1 && 
        aiResponse.selectedIndex <= indexedStudies.length) {
      // 유효한 인덱스면 해당 스터디 ID 가져오기
      recommendedStudyId = indexedStudies[aiResponse.selectedIndex - 1].id;
      console.log(`유효한 인덱스 ${aiResponse.selectedIndex}가 선택됨, 스터디 ID: ${recommendedStudyId}`);
    } else {
      console.error("AI가 유효하지 않은 인덱스를 반환함:", aiResponse.selectedIndex);
      throw new functions.https.HttpsError(
        "invalid-argument",
        "AI가 유효하지 않은 인덱스를 반환했습니다. 다시 시도해주세요."
      );
    }

    // 직접 일치하는 ID를 사용한 경우 (구형 응답 형식)를 위한 대체 검증
    if (!recommendedStudyId && aiResponse.recommendedStudyId) {
      if (validStudyIds.includes(aiResponse.recommendedStudyId)) {
        recommendedStudyId = aiResponse.recommendedStudyId;
        console.log(`AI가 직접 ID를 반환함: ${recommendedStudyId}`);
      } else {
        console.error("AI가 유효하지 않은 ID 반환:", aiResponse.recommendedStudyId);
        throw new functions.https.HttpsError(
          "invalid-argument",
          "AI가 유효하지 않은 스터디 ID를 반환했습니다. 다시 시도해주세요."
        );
      }
    }

    // ID가 없는 경우 처리
    if (!recommendedStudyId) {
      console.error("추천 ID를 추출할 수 없음:", aiResponse);
      throw new functions.https.HttpsError(
        "internal",
        "추천 ID를 추출할 수 없습니다. 다시 시도해주세요."
      );
    }

    // 추천된 스터디 상세 정보 가져오기
    let recommendedStudy = null;
    const studyDoc = await admin.firestore()
        .collection("studies")
        .doc(recommendedStudyId)
        .get();

    if (studyDoc.exists) {
      recommendedStudy = {
        id: studyDoc.id,
        ...studyDoc.data(),
      };
    } else {
      console.error(`ID ${recommendedStudyId}에 해당하는 스터디를 찾을 수 없음`);
      throw new functions.https.HttpsError(
        "not-found",
        "추천된 스터디를 찾을 수 없습니다. 다시 시도해주세요."
      );
    }

    console.log("추천 요청 결과:", JSON.stringify({
  recommendedStudyId,
  hasRecommendedStudy: recommendedStudy !== null
}));
    return {
      recommendedStudyId: recommendedStudyId,
      reason: reason,
      recommendedStudy: recommendedStudy,
    };
  } catch (error) {
    console.error("추천 오류:", error);
    throw new functions.https.HttpsError("internal", "추천 처리 중 오류 발생", error);
  }
});