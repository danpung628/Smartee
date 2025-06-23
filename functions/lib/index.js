"use strict";
// smartee/functions/src/index.ts 최종 수정본
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.processStudySettlement = exports.recommendStudy = void 0;
const scheduler_1 = require("firebase-functions/v2/scheduler");
const https_1 = require("firebase-functions/v2/https");
const admin = __importStar(require("firebase-admin"));
const logger = __importStar(require("firebase-functions/logger"));
const vertexai_1 = require("@google-cloud/vertexai");
admin.initializeApp();
const db = admin.firestore();
// ------------------------------------------------------------------
// AI 스터디 추천 함수 (v2 문법)
// ------------------------------------------------------------------
const projectId = "smartee-319d0";
const location = "us-central1";
const vertexAi = new vertexai_1.VertexAI({ project: projectId, location: location });
const genAiModel = vertexAi.getGenerativeModel({ model: "gemini-2.0-flash" });
exports.recommendStudy = (0, https_1.onCall)(async (request) => {
    var _a, _b, _c, _d, _e;
    logger.info("recommendStudy called with data:", request.data);
    if (!request.auth) {
        throw new https_1.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }
    const { categories, inkLevel } = request.data;
    if (!categories || !Array.isArray(categories) || categories.length === 0) {
        throw new https_1.HttpsError("invalid-argument", "카테고리 목록이 필요합니다.");
    }
    try {
        const studiesSnapshot = await db
            .collection("studies")
            .where("minInkLevel", "<=", inkLevel)
            .get();
        if (studiesSnapshot.empty) {
            return { recommendedStudyId: null, message: "조건에 맞는 스터디가 없습니다" };
        }
        const studies = studiesSnapshot.docs.map((doc, index) => {
            const studyData = doc.data();
            return {
                index: index + 1,
                id: doc.id,
                title: studyData.title || "제목 없음",
                category: studyData.category || "",
                minInkLevel: studyData.minInkLevel || 0,
                description: studyData.description || "설명 없음",
                currentMemberCount: studyData.currentMemberCount || 0,
                maxMemberCount: studyData.maxMemberCount || 0,
                likeCount: studyData.likeCount || 0,
                commentCount: studyData.commentCount || 0,
            };
        });
        const prompt = `
      사용자 프로필:
      - 관심 카테고리: ${categories.join(", ")}
      - 잉크 레벨: ${inkLevel}

      다음 스터디 목록 중에서 이 사용자에게 가장 적합한 스터디를 하나만 선택해주세요:
      ${studies.map((s) => `
      [${s.index}] ID: ${s.id}, 제목: ${s.title}, 카테고리: ${s.category}, 필요 잉크: ${s.minInkLevel}
      `).join("\n")}

      중요: 반드시 위 목록에 있는 번호([1]-[${studies.length}]) 중 하나만 선택하고,
      추천 이유를 JSON 형식으로 다음과 같이 반환해 주세요:
      {"selectedIndex": 2, "reason": "추천 이유"}
    `;
        const aiRequest = { contents: [{ role: "user", parts: [{ text: prompt }] }] };
        const result = await genAiModel.generateContent(aiRequest);
        const textResponse = (_e = (_d = (_c = (_b = (_a = result.response.candidates) === null || _a === void 0 ? void 0 : _a[0]) === null || _b === void 0 ? void 0 : _b.content) === null || _c === void 0 ? void 0 : _c.parts) === null || _d === void 0 ? void 0 : _d[0]) === null || _e === void 0 ? void 0 : _e.text;
        if (!textResponse)
            throw new Error("AI 응답에서 텍스트를 찾을 수 없습니다.");
        const jsonMatch = textResponse.match(/{[\s\S]*}/);
        if (!jsonMatch)
            throw new Error("AI 응답에서 JSON을 찾을 수 없습니다.");
        const aiResponse = JSON.parse(jsonMatch[0]);
        const selectedIndex = aiResponse.selectedIndex;
        if (selectedIndex && Number.isInteger(selectedIndex) &&
            selectedIndex >= 1 && selectedIndex <= studies.length) {
            const recommendedStudyId = studies[selectedIndex - 1].id;
            const studyDoc = await db.collection("studies").doc(recommendedStudyId).get();
            if (!studyDoc.exists) {
                throw new https_1.HttpsError("not-found", "추천된 스터디를 찾을 수 없습니다.");
            }
            return {
                recommendedStudyId: recommendedStudyId,
                reason: aiResponse.reason || "사용자에게 적합한 스터디입니다.",
                recommendedStudy: Object.assign({ id: studyDoc.id }, studyDoc.data()),
            };
        }
        else {
            throw new https_1.HttpsError("internal", "AI가 유효한 추천을 생성하지 못했습니다.");
        }
    }
    catch (error) {
        logger.error("추천 오류:", error);
        if (error instanceof https_1.HttpsError)
            throw error;
        throw new https_1.HttpsError("internal", "추천 처리 중 오류 발생", error);
    }
});
// ------------------------------------------------------------------
// 스터디 종료 정산 및 뱃지 부여 함수 (v2 문법)
// ------------------------------------------------------------------
exports.processStudySettlement = (0, scheduler_1.onSchedule)("0 0 * * *", async (_event) => {
    var _a;
    // ▼▼▼ 'functions.logger' -> 'logger'로 모두 수정 ▼▼▼
    logger.info("스터디 정산 스케줄러 실행", { structuredData: true });
    const now = admin.firestore.Timestamp.now();
    const studiesToSettleQuery = db.collection("studies")
        .where("endDate", "<", now.toDate().toISOString().split("T")[0])
        .where("settlementCompleted", "==", false);
    const studiesSnapshot = await studiesToSettleQuery.get();
    if (studiesSnapshot.empty) {
        logger.info("정산할 스터디가 없습니다.");
        return;
    }
    for (const studyDoc of studiesSnapshot.docs) {
        const studyId = studyDoc.id;
        const studyData = studyDoc.data();
        const participantIds = studyData.participantIds || [];
        logger.info(`'${studyData.title}' (ID: ${studyId}) 스터디 정산 시작...`);
        const meetingsQuery = db.collection("meetings")
            .where("parentStudyId", "==", studyId);
        const meetingsSnapshot = await meetingsQuery.get();
        const totalMeetings = meetingsSnapshot.size;
        if (totalMeetings === 0) {
            logger.info("모임이 없어 정산을 건너뜁니다.");
            await studyDoc.ref.update({ settlementCompleted: true });
            continue;
        }
        for (const userId of participantIds) {
            let attendedCount = 0;
            for (const meetingDoc of meetingsSnapshot.docs) {
                const attendanceRef = db.collection("meetings").doc(meetingDoc.id)
                    .collection("attendance").doc(userId);
                const attendanceDoc = await attendanceRef.get();
                if (attendanceDoc.exists && ((_a = attendanceDoc.data()) === null || _a === void 0 ? void 0 : _a.isPresent) === true) {
                    attendedCount++;
                }
            }
            const attendanceRate = (totalMeetings > 0) ?
                (attendedCount / totalMeetings) * 100 : 0;
            logger.info(`- 참여자 ${userId}: ${attendedCount}/${totalMeetings} (${attendanceRate.toFixed(2)}%) 출석`);
            let inkReward = 0;
            let penReward = 0;
            if (attendanceRate >= 100) {
                inkReward = 10;
                penReward = 2;
            }
            else if (attendanceRate >= 90) {
                inkReward = 5;
                penReward = 1;
            }
            else if (attendanceRate >= 70) {
                inkReward = 2;
                penReward = 0;
            }
            const userRef = db.collection("users").doc(userId);
            try {
                await db.runTransaction(async (transaction) => {
                    const userDoc = await transaction.get(userRef);
                    if (!userDoc.exists) {
                        return;
                    }
                    const userData = userDoc.data();
                    const newCompletedCount = ((userData === null || userData === void 0 ? void 0 : userData.completedStudiesCount) || 0) + 1;
                    const newPerfectAttendanceCount = (attendanceRate >= 100) ?
                        ((userData === null || userData === void 0 ? void 0 : userData.perfectAttendanceCount) || 0) + 1 :
                        ((userData === null || userData === void 0 ? void 0 : userData.perfectAttendanceCount) || 0);
                    const earnedBadgeIds = new Set((userData === null || userData === void 0 ? void 0 : userData.earnedBadgeIds) || []);
                    let newBadgeEarned = false;
                    if (newCompletedCount === 1 && !earnedBadgeIds.has("first_study_complete")) {
                        earnedBadgeIds.add("first_study_complete");
                        newBadgeEarned = true;
                    }
                    if (newCompletedCount === 5 && !earnedBadgeIds.has("five_studies_complete")) {
                        earnedBadgeIds.add("five_studies_complete");
                        newBadgeEarned = true;
                    }
                    if (attendanceRate >= 100 && !earnedBadgeIds.has("perfect_attendance_1")) {
                        earnedBadgeIds.add("perfect_attendance_1");
                        newBadgeEarned = true;
                    }
                    const updatePayload = {
                        completedStudiesCount: newCompletedCount,
                        perfectAttendanceCount: newPerfectAttendanceCount,
                        ink: admin.firestore.FieldValue.increment(inkReward),
                        pen: admin.firestore.FieldValue.increment(penReward),
                    };
                    if (newBadgeEarned) {
                        updatePayload.earnedBadgeIds = Array.from(earnedBadgeIds);
                    }
                    transaction.update(userRef, updatePayload);
                    logger.info(`  ㄴ ${userId} 정산 및 뱃지 처리 완료`);
                });
            }
            catch (error) {
                logger.error(`사용자 ${userId} 정산/뱃지 처리 중 오류:`, error);
            }
        }
        await studyDoc.ref.update({ settlementCompleted: true });
        logger.info(`'${studyData.title}' 스터디 정산 완료.`);
    }
});
//# sourceMappingURL=index.js.map