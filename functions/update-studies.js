const admin = require("firebase-admin");
const serviceAccount = require("../../serviceAccountKey.json"); // 서비스 계정 키 파일 경로

// Firebase 초기화
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const studiesCollection = db.collection("studies");

// 랜덤 데이터 생성 헬퍼 함수들
function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getRandomElement(array) {
  return array[Math.floor(Math.random() * array.length)];
}

function getRandomBoolean() {
  return Math.random() > 0.5;
}

function getRandomDate(start, end) {
  return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
}

function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatTime(hours, minutes) {
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
}

// 카테고리 목록
const categories = [
  "개발", "디자인", "마케팅", "외국어", "취업", "자격증",
  "공무원", "독서", "영상편집", "음악", "요리", "운동"
];

// 주소 목록
const addresses = [
  "서울 강남구", "서울 서초구", "서울 송파구", "서울 마포구", "서울 용산구",
  "서울 종로구", "서울 성동구", "서울 강동구", "서울 영등포구", "경기 성남시",
  "경기 부천시", "경기 수원시", "인천 남동구", "인천 서구", "부산 해운대구"
];

// 요일 목록
const days = ["월", "화", "수", "목", "금", "토", "일"];

// 썸네일 모델 목록
const thumbnailModels = [
  "study_default", "study_coffee", "study_books", "study_coding",
  "study_language", "study_music", "study_art", "study_fitness"
];

// 벌칙 목록
const punishments = [
  "다음 모임 간식 사기", "벌금 5천원", "다음 모임 발표 준비하기",
  "커피 한 잔 사기", "다음 스터디 장소 예약하기", "없음",
  "문서 정리하기", "간단한 과제 추가", "벌금 만원", "청소 담당"
];

// 스터디 제목 생성
function generateStudyTitle() {
  const prefixes = ["함께하는", "열정적인", "성장하는", "실전", "초보자를 위한", "심화", "주말", "평일"];
  const topics = ["코딩", "영어회화", "디자인", "면접준비", "독서", "토익", "자격증", "알고리즘", "프로젝트"];
  const suffixes = ["스터디", "모임", "클래스", "그룹", "챌린지"];

  return `${getRandomElement(prefixes)} ${getRandomElement(topics)} ${getRandomElement(suffixes)}`;
}

// 스터디 설명 생성
function generateStudyDescription() {
  const intros = [
    "안녕하세요! 함께 성장하는",
    "진지하게 배우는",
    "즐겁게 공부하는",
    "서로 도움을 주고받는",
    "체계적으로 학습하는"
  ];

  const activities = [
    "매주 과제를 해오고 함께 리뷰합니다.",
    "실전 문제를 풀고 해결 방법을 공유합니다.",
    "각자 준비한 내용을 발표하고 피드백을 주고받습니다.",
    "실습 위주로 진행되는 스터디입니다.",
    "기초부터 차근차근 배워가는 스터디입니다."
  ];

  const outros = [
    "함께 성장해요!",
    "관심 있는 분들의 많은 참여 바랍니다.",
    "열정 있는 분들을 기다립니다.",
    "초보자도 환영합니다!",
    "경험자들의 노하우를 공유해요."
  ];

  return `${getRandomElement(intros)} 스터디입니다. ${getRandomElement(activities)} ${getRandomElement(outros)}`;
}

// 랜덤 정기 시간 생성
function generateRegularTime() {
  const startHour = getRandomInt(9, 20);
  const startMinute = getRandomElement([0, 30]);
  const duration = getRandomElement([1, 1.5, 2, 2.5, 3]);

  const endHour = Math.floor(startHour + duration);
  const endMinute = (startMinute + (duration % 1) * 60) % 60;

  return `${formatTime(startHour, startMinute)}~${formatTime(endHour, endMinute)}`;
}

// 비정기 날짜/시간 생성
function generateIrregularDateTimes(count) {
  const result = [];
  const now = new Date();
  const sixMonthsLater = new Date();
  sixMonthsLater.setMonth(now.getMonth() + 6);

  for (let i = 0; i < count; i++) {
    const randomDate = getRandomDate(now, sixMonthsLater);
    const startHour = getRandomInt(9, 20);
    const startMinute = getRandomElement([0, 30]);
    const duration = getRandomElement([1, 1.5, 2, 2.5, 3]);

    const endHour = Math.floor(startHour + duration);
    const endMinute = (startMinute + (duration % 1) * 60) % 60;

    const formattedDate = formatDate(randomDate);
    const timeSlot = `${formatTime(startHour, startMinute)}~${formatTime(endHour, endMinute)}`;

    result.push(`${formattedDate} ${timeSlot}`);
  }

  return result;
}

// 정기 요일 선택
function selectRandomDays() {
  const numberOfDays = getRandomInt(1, 3); // 1~3일 선택
  const selectedDays = [];
  const shuffledDays = [...days].sort(() => 0.5 - Math.random());

  for (let i = 0; i < numberOfDays; i++) {
    selectedDays.push(shuffledDays[i]);
  }

  return selectedDays;
}

// 스터디 업데이트 함수
async function updateStudies() {
  try {
    const snapshot = await studiesCollection.get();
    console.log(`총 ${snapshot.size}개의 스터디 문서를 업데이트합니다...`);

    const batch = db.batch();
    let count = 0;

    snapshot.forEach(doc => {
      const isRegular = getRandomBoolean();
      const maxMemberCount = getRandomInt(4, 20);
      const currentMemberCount = getRandomInt(1, maxMemberCount);

      // 시작일/종료일 설정
      const now = new Date();
      const oneMonthLater = new Date();
      oneMonthLater.setMonth(now.getMonth() + 1);

      const sixMonthsLater = new Date();
      sixMonthsLater.setMonth(now.getMonth() + 6);

      const startDate = formatDate(getRandomDate(now, oneMonthLater));
      const endDate = formatDate(getRandomDate(oneMonthLater, sixMonthsLater));

      const studyData = {
        title: generateStudyTitle(),
        category: getRandomElement(categories),
        // 랜덤한 등록일 설정 (최근 6개월 내)
        dateTimestamp: admin.firestore.Timestamp.fromDate(
          getRandomDate(new Date(Date.now() - 180 * 24 * 60 * 60 * 1000), new Date())
        ),
        startDate: startDate,
        endDate: endDate,
        isRegular: isRegular,
        regularDays: isRegular ? selectRandomDays() : [],
        regularTime: isRegular ? generateRegularTime() : "",
        irregularDateTimes: !isRegular ? generateIrregularDateTimes(getRandomInt(3, 8)) : [],
        currentMemberCount: currentMemberCount,
        maxMemberCount: maxMemberCount,
        isOffline: getRandomBoolean(),
        minInkLevel: getRandomInt(10, 80),
        penCount: getRandomInt(1, 5),
        punishment: getRandomElement(punishments),
        description: generateStudyDescription(),
        address: getRandomElement(addresses),
        commentCount: getRandomInt(0, 30),
        likeCount: getRandomInt(0, 50),
        thumbnailModel: getRandomElement(thumbnailModels)
      };

      batch.update(doc.ref, studyData);
      count++;

      // Firestore는 한 배치에 최대 500개 작업만 허용
      if (count % 450 === 0) {
        batch.commit();
        console.log(`${count}개 문서 업데이트 완료`);
        batch = db.batch(); // 새 배치 시작
      }
    });

    // 남은 배치 커밋
    if (count % 450 !== 0) {
      batch.commit();
    }
    console.log(`총 ${count}개 스터디 문서 업데이트 완료!`);

  } catch (error) {
    console.error("스터디 업데이트 중 오류 발생:", error);
  }
}

// 스크립트 실행
updateStudies()
  .then(() => {
    console.log("스터디 데이터 업데이트가 완료되었습니다.");
    process.exit(0);
  })
  .catch(error => {
    console.error("업데이트 중 오류 발생:", error);
    process.exit(1);
  });