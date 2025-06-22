import {onSchedule} from "firebase-functions/v2/scheduler";
import * as admin from "firebase-admin";
import * as functions from "firebase-functions";

admin.initializeApp();
const db = admin.firestore();

export const processStudySettlement = onSchedule("0 0 * * *", async (_event) => {
  functions.logger.info("스터디 정산 스케줄러 실행", {structuredData: true});

  const now = admin.firestore.Timestamp.now();
  const studiesToSettleQuery = db.collection("studies")
    .where("endDate", "<", now.toDate().toISOString().split("T")[0])
    .where("settlementCompleted", "==", false);

  const studiesSnapshot = await studiesToSettleQuery.get();
  if (studiesSnapshot.empty) {
    functions.logger.info("정산할 스터디가 없습니다.");
    return;
  }

  for (const studyDoc of studiesSnapshot.docs) {
    const studyId = studyDoc.id;
    const studyData = studyDoc.data();
    const participantIds = studyData.participantIds || [];

    functions.logger.info(`'${studyData.title}' (ID: ${studyId}) 스터디 정산 시작...`);

    const meetingsQuery = db.collection("meetings")
      .where("parentStudyId", "==", studyId);
    const meetingsSnapshot = await meetingsQuery.get();
    const totalMeetings = meetingsSnapshot.size;

    if (totalMeetings === 0) {
      functions.logger.info("모임이 없어 정산을 건너뜁니다.");
      await studyDoc.ref.update({settlementCompleted: true});
      continue;
    }

    for (const userId of participantIds) {
      let attendedCount = 0;
      for (const meetingDoc of meetingsSnapshot.docs) {
        const attendanceRef = db.collection("meetings").doc(meetingDoc.id)
          .collection("attendance").doc(userId);
        const attendanceDoc = await attendanceRef.get();
        if (attendanceDoc.exists && attendanceDoc.data()?.isPresent === true) {
          attendedCount++;
        }
      }

      const attendanceRate = (totalMeetings > 0) ?
        (attendedCount / totalMeetings) * 100 : 0;
      functions.logger.info(
        `- 참여자 ${userId}: ${attendedCount}/${totalMeetings} (${attendanceRate.toFixed(2)}%) 출석`
      );

      let inkReward = 0;
      let penReward = 0;
      if (attendanceRate >= 100) {
        inkReward = 10;
        penReward = 2;
      } else if (attendanceRate >= 90) {
        inkReward = 5;
        penReward = 1;
      } else if (attendanceRate >= 70) {
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

          const newCompletedCount = (userData?.completedStudiesCount || 0) + 1;
          const newPerfectAttendanceCount = (attendanceRate >= 100) ?
            (userData?.perfectAttendanceCount || 0) + 1 :
            (userData?.perfectAttendanceCount || 0);

          const earnedBadgeIds = new Set(userData?.earnedBadgeIds || []);

          // ▼▼▼ 'var'를 'let'으로 수정 ▼▼▼
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

          const updatePayload: {[key: string]: any} = {
            completedStudiesCount: newCompletedCount,
            perfectAttendanceCount: newPerfectAttendanceCount,
            ink: admin.firestore.FieldValue.increment(inkReward),
            pen: admin.firestore.FieldValue.increment(penReward),
          };
          if (newBadgeEarned) {
            updatePayload.earnedBadgeIds = Array.from(earnedBadgeIds);
          }

          transaction.update(userRef, updatePayload);
          functions.logger.info(`  ㄴ ${userId} 정산 및 뱃지 처리 완료`);
        });
      } catch (error) {
        functions.logger.error(`사용자 ${userId} 정산/뱃지 처리 중 오류:`, error);
      }
    }

    await studyDoc.ref.update({settlementCompleted: true});
    functions.logger.info(`'${studyData.title}' 스터디 정산 완료.`);
  }

});