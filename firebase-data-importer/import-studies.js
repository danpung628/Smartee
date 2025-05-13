// import-studies.js
const admin = require('firebase-admin');
const fs = require('fs');

// 서비스 계정 키 파일 불러오기
const serviceAccount = require('./serviceAccountKey.json');

// Firebase 초기화
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Firestore 인스턴스 가져오기
const db = admin.firestore();

// 스터디 데이터 준비
const categories = ['어학', '프로그래밍', '자격증', '취미', '운동', '독서', '취업', '자유'];
const addresses = ['서울', '군자동', '구의제3동', '휘경동'];
const titles = [
  '안드로이드 스터디', 'Kotlin 심화반', '코틀린 기초반', 
  '알고리즘 스터디', 'CS 스터디', '취업 준비', 
  '영어 회화반', '자바 스터디', 'Flutter 입문', 'React 스터디',
  'Spring Boot 스터디', 'Node.js 스터디', '파이썬 데이터 분석',
  '머신러닝 기초', '풀스택 개발자 되기', '토익 900점 목표',
  'AWS 자격증 취득반', '프론트엔드 개발', '백엔드 개발',
  'Vue.js 스터디', '영어 독서모임', '취준생 모임'
];
const descriptions = [
  '함께 안드로이드 개발을 공부해요. 매주 목요일 저녁에 만나서 진행합니다.',
  '코틀린 심화 개념을 배우는 스터디입니다. 기초 지식이 필요해요.',
  '알고리즘 문제를 매주 5개씩 풉니다. 코딩테스트 준비하시는 분들 환영합니다.',
  '취업 준비를 위한 스터디입니다. 면접 대비와 포트폴리오를 함께 준비해요.',
  '기초부터 차근차근 배워봐요. 초보자도 환영합니다!',
  '주 2회 만나서 함께 공부합니다. 진도는 함께 정해요.',
  '실전 프로젝트를 통해 실력을 향상시키는 스터디입니다.',
  '경력자들의 노하우를 공유하는 스터디입니다.',
  '최신 트렌드와 기술을 함께 공부해요.',
  '자격증 취득을 목표로 하는 스터디입니다.',
  '영어 회화 실력을 향상시키는 모임입니다. 매주 다른 주제로 대화를 나눕니다.',
  '책을 함께 읽고 토론하는 독서 모임입니다.'
];

// 현재 시간에서 랜덤하게 과거 시간 생성 (최대 30일 전)
function getRandomPastDate() {
  const now = new Date();
  const randomDaysAgo = Math.floor(Math.random() * 30);
  now.setDate(now.getDate() - randomDaysAgo);
  return admin.firestore.Timestamp.fromDate(now);
}

// 스터디 데이터 생성
const createStudyData = (index) => {
  return {
    category: categories[Math.floor(Math.random() * categories.length)],
    dateTimestamp: getRandomPastDate(),
    title: titles[Math.floor(Math.random() * titles.length)],
    description: descriptions[Math.floor(Math.random() * descriptions.length)],
    address: addresses[Math.floor(Math.random() * addresses.length)],
    currentMemberCount: Math.floor(Math.random() * 5) + 1,
    maxMemberCount: Math.floor(Math.random() * 6) + 5,
    commentCount: Math.floor(Math.random() * 20),
    likeCount: Math.floor(Math.random() * 30),
    thumbnailModel: `https://picsum.photos/200/300?random=${index}`
  };
};

// 대량의 스터디 데이터 생성 및 Firestore에 추가
async function addStudiesToFirestore(count) {
  console.log(`${count}개의 스터디 데이터를 추가합니다...`);
  
  // 한 번에 최대 500개까지 배치 처리 가능
  const batchSize = 500;
  const batches = [];
  
  for (let i = 0; i < count; i += batchSize) {
    const batch = db.batch();
    const currentBatchSize = Math.min(batchSize, count - i);
    
    for (let j = 0; j < currentBatchSize; j++) {
      const docRef = db.collection('studies').doc();
      batch.set(docRef, createStudyData(i + j));
    }
    
    batches.push(batch.commit());
    console.log(`배치 ${batches.length}: ${currentBatchSize}개 문서 처리 중...`);
  }
  
  await Promise.all(batches);
  console.log(`${count}개의 스터디 데이터가 성공적으로 추가되었습니다.`);
}

// 스크립트 실행
const numberOfStudies = process.argv[2] ? parseInt(process.argv[2]) : 50;
addStudiesToFirestore(numberOfStudies)
  .then(() => {
    console.log('스크립트 실행 완료');
    process.exit(0);
  })
  .catch(error => {
    console.error('오류 발생:', error);
    process.exit(1);
  });