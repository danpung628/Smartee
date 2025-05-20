// update-ink.js
const admin = require('firebase-admin');
const serviceAccount = require('../../service-account-key.json'); // 서비스 계정 키 파일 경로

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function addRandomInkLevels() {
  try {
    const studiesSnapshot = await db.collection('studies').get();
    
    const batch = db.batch();
    let count = 0;
    
    studiesSnapshot.forEach(doc => {
      const randomInkLevel = Math.floor(Math.random() * 101);
      batch.update(doc.ref, { minInkLevel: randomInkLevel });
      count++;
    });
    
    await batch.commit();
    console.log(`${count}개 스터디에 랜덤 잉크레벨 추가 완료`);
  } catch (error) {
    console.error('Error:', error);
  }
  process.exit(0);
}

addRandomInkLevels();