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

// secrets.properties(gitignore 대상) 우선, 없으면 local.defaults.properties 로 폴백해서 읽는다.
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
    lint {
        // NotificationPermission: transitive Glide(NotificationTarget) 로 인한 false positive.
        // 앱은 알림을 게시하지 않음(이미지: Coil) → 권한 추가 대신 체크 비활성화 (#102)
        disable += "NotificationPermission"
    }
}

dependencies {

    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material3)
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
    implementation(libs.firebase.common.ktx)
    implementation(libs.places)
    implementation(libs.material)
    implementation(libs.firebase.ml.modeldownloader)
    implementation(libs.tensorflow.lite)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.map.sdk)
    implementation(libs.naver.map.compose)

    // Firebase (BOM 으로 버전 관리)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    // firestore-ktx 는 BOM 관리 — 카탈로그 alias(25.1.4)로 옮기면 버전 변동 위험이라 좌표 유지
    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation(libs.play.services.location)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}