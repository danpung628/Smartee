import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.example.smartee.buildlogic"

// build-logic 자체는 Gradle JVM(AGP 8.10 구동 = JDK 17+)에서 컴파일/실행된다.
// (앱 모듈의 bytecode 타깃 JVM 11 과는 별개 — 컨벤션 플러그인이 앱 모듈을 11 로 설정.)
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "smartee.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
