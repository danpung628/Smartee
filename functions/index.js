const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {VertexAI} = require("@google-cloud/vertexai");

admin.initializeApp();

// Vertex AI 설정
const projectId = "smartee-319d0"; // Firebase 프로젝트 ID
const location = "us-central1"; // Vertex AI API 위치
const modelId = "text-bison"; // 사용할 모델 ID

// Vertex AI 클라이언트 초기화
const vertexAi = new VertexAI({project: projectId, location: location});
const genAiModel = vertexAi.preview.getGenerativeModel({model: modelId});

// 스터디 추천 함수
exports.recommendStudy = functions.https.onCall(async (data, context) => {
  try {
    if (!context.auth) {
      console.log("인증 없음 - 무시하고 진행");
      // 인증 에러를 던지지 않고 계속 진행
    }

    // 사용자 정보
    const {userCategories, userInkLevel} = data;

    if (!userCategories || !Array.isArray(userCategories)) {
      throw new functions.https.HttpsError(
          "invalid-argument",
          "카테고리 목록이 필요합니다",
      );
    }

    // Firestore에서 스터디 목록 가져오기
    const studiesSnapshot = await admin.firestore()
        .collection("studies")
        .where("minInkLevel", "<=", userInkLevel)
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

    // AI 추천을 위한 프롬프트 작성
    const prompt = `
사용자 프로필:
- 관심 카테고리: ${userCategories.join(", ")}
- 잉크 레벨: ${userInkLevel}

다음 스터디 목록 중에서 이 사용자에게 가장 적합한 스터디를 하나만 추천해 주세요:

${studies.map((study) => `
ID: ${study.id}
제목: ${study.title}
카테고리: ${study.category}
필요 잉크 레벨: ${study.minInkLevel}
설명: ${study.description || "설명 없음"}
현재 인원: ${study.currentMemberCount}/${study.maxMemberCount}
인기도: 좋아요 ${study.likeCount || 0}개, 댓글 ${study.commentCount || 0}개
`).join("\n")}

추천 근거를 간략히 설명한 후, JSON 형식으로 다음과 같이 결과를 반환해 주세요:
{"recommendedStudyId": "추천하는 스터디의 ID", "reason": "추천 이유"}
`;

    // Vertex AI 모델 호출
    const result = await genAiModel.generateContent({
      contents: [{role: "user", parts: [{text: prompt}]}],
    });
    const response = result.response;
    const textResponse = response.candidates[0].content.parts[0].text;

    // JSON 추출
    const jsonMatch = textResponse.match(/{[\s\S]*}/);
    if (!jsonMatch) {
      console.error("응답에서 JSON을 찾을 수 없음:", textResponse);
      throw new Error("응답 파싱 실패");
    }

    // JSON 파싱
    const recommendation = JSON.parse(jsonMatch[0]);

    // 추천된 스터디 상세 정보 가져오기
    let recommendedStudy = null;
    if (recommendation.recommendedStudyId) {
      const studyDoc = await admin.firestore()
          .collection("studies")
          .doc(recommendation.recommendedStudyId)
          .get();

      if (studyDoc.exists) {
        recommendedStudy = {
          id: studyDoc.id,
          ...studyDoc.data(),
        };
      }
    }

    return {
      recommendedStudyId: recommendation.recommendedStudyId,
      reason: recommendation.reason,
      recommendedStudy: recommendedStudy,
    };
  } catch (error) {
    console.error("추천 오류:", error);
    throw new functions.https.HttpsError("internal", "추천 처리 중 오류 발생", error);
  }
});
