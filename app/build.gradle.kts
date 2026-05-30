import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    alias(libs.plugins.secrets.gradle.plugin)
}
secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

// secrets.properties(gitignored) 우선, 없으면 local.defaults.properties 로 폴백해서 읽는다.
// secrets-gradle-plugin 은 manifestPlaceholder 만 주입하므로, R.string 으로 소비되는 값은 여기서 resValue 로 생성한다.
val localSecrets = Properties().apply {
    rootProject.file("local.defaults.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    rootProject.file("secrets.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun secretOrEmpty(key: String): String = localSecrets.getProperty(key).orEmpty()

android {
    namespace = "com.example.smartee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartee"
        minSdk = 32
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // maps_api_key(Places): strings.xml 하드코딩 제거 → secrets.properties / local.defaults.properties 에서 주입 (#97)
        // default_web_client_id 는 google-services 플러그인이 google-services.json 에서 자동 생성하므로 여기서 만들지 않는다(resValue ↔ google-services generated 중복 리소스 방지)
        resValue("string", "maps_api_key", secretOrEmpty("MAPS_API_KEY"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.material)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose) // 새로고침 기능

    implementation(libs.androidx.material)
    implementation(libs.firebase.common.ktx)
    implementation(libs.places)
    implementation(libs.material)
    implementation(libs.firebase.ml.modeldownloader)
    implementation(libs.tensorflow.lite)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.map.sdk)
    implementation(libs.naver.map.compose)
    implementation(libs.material3) // 새로고침 기능
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")             // 로그인 기능
    implementation("com.google.firebase:firebase-firestore-ktx")       // DB 기능
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0") // 이게 가장 중요!
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // ViewModel 등을 쓸 경우
    // 아래는 Compose 쓸 때 기본적으로 같이 사용합니다
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation ("com.google.android.gms:play-services-location:21.0.1")
    // ✅ JSON 파싱을 위한 Gson
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.naver.maps:map-sdk:3.21.0")
    implementation(libs.naver.map.compose)
    implementation("androidx.compose.material3:material3:1.2.1")

}