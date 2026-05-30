plugins {
    id("smartee.android.library")
}

android {
    namespace = "com.example.smartee.model"
}

dependencies {
    // StudyData 등이 @DocumentId·Timestamp 를 공개 API 로 노출하므로 api 로 전파
    api(libs.firebase.firestore.ktx)
}
