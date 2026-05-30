# Smartee Project Conventions

## Project Context
- 모듈 구조: 단일 `:app` 모듈 (`com.example.smartee`). `settings.gradle.kts` 에 `:studycreationmodule` include 가 있으나 실제 디렉터리·빌드스크립트가 없는 **dangling include** — 정리 대상.
- 패키지 구조 (`app/src/main/java/com/example/smartee/`): `model`(+factory), `repository`, `viewmodel`, `service`, `bluetooth`, `navigation`(NavGraph/Screen), `ui/{study, profile, signup, attendance, request, Map, admin, badge, login, meeting, report, splash, common, theme}`. 앱 클래스 `SmarteeApplication`, 단일 `MainActivity`. 패턴: MVVM (Compose UI + ViewModel + Firestore Repository).
- 백엔드: **Firebase** (Firestore / Auth / Functions / Analytics). Cloud Functions 는 `functions/` (Node 22 + TypeScript). 야간 스케줄러(`processStudySettlement`)가 종료된 스터디를 정산해 출석률 기반으로 잉크·만년필·뱃지를 지급.
- SDK / JVM: minSdk 32 / targetSdk 35 / compileSdk 35, **JVM 11**. 정의 위치: `app/build.gradle.kts`.
- 빌드: AGP 8.10.1 + Kotlin 2.0.21 + Compose Compiler 플러그인, Version Catalog(`gradle/libs.versions.toml`). Compose BOM 2024.09.00, Material3 1.3.2, Navigation-Compose 2.9.0-rc01. `secrets-gradle-plugin`(Maps/OAuth 키), `google-services` 플러그인 — 빌드에 `app/google-services.json` 필요(.gitignore 대상).
- 핵심 도메인: **스터디 그룹 탐색·생성·관리 앱**. 가입/프로필, 카테고리·지도 기반 검색(Naver/Google Maps + Places), 좋아요·댓글, 가입 신청, BLE 근접 출석(host/participant), 잉크(ink)·만년필(pen) 가상화폐 + 뱃지 게이미피케이션, 관리자 신고/모더레이션.

## 작업 시작/마무리 규약 (위반 금지)
- **이슈 우선 + 브랜치 링크**: 새 브랜치는 메타데이터 완비된 GitHub 이슈에 정식 링크된 상태여야 한다. 절차 자동화는 `android-issue-branch` skill 에 위임 — 사용자가 "브랜치 파"라고만 해도 skill 진입 후 이슈부터 작성. 브랜치 규약은 `feat/<이슈번호>`. `git checkout -b` 직행은 `git-branch-guard.sh` 가 차단.
- **자율 커밋 금지**: 변경사항을 임의로 `git commit` 하지 말 것. 빌드/lint/test 검증 결과만 보고하고 멈춤. 사용자가 Android Studio 커밋 탭에서 직접 검토·커밋한다.
- **git state 변경은 명시 지시 시에만**: `git push`, `git reset --hard`, `git rebase`, force-push, 원격 브랜치 삭제(`git push --delete`) 등 모든 상태 변경 동작은 사용자의 명시 지시가 있을 때만 수행.
- 위 규약은 hook으로 강제(`.claude/settings.local.json` + `.claude/hooks/`). 우회·예외 처리 시도 금지.

## 작업 원칙
- 기억으로 답하지 말 것. 라이브러리 좌표·버전·API 시그니처는 매번 검증.
- 검증 우선순위:
    - **Android**: 1. developer.android.com (Architecture Guide, 공식 문서) → 2. AndroidX 릴리즈 노트(`developer.android.com/jetpack/androidx/releases/*`) → 3. mvnrepository.com / androidx.tech (최신 Stable Maven 좌표) → 4. 필요 시 android.googlesource.com / AndroidX GitHub (시그니처·소스).
    - **Firebase / 지도**: firebase.google.com (Firestore / Auth / Functions), Naver Maps SDK 문서, Google Maps Platform 문서.
    - **백엔드(`functions/`)**: npmjs.com + 각 패키지 공식 문서(`firebase-functions`, `firebase-admin`, `@google-cloud/vertexai`).
- 핵심 문서는 `web_fetch`로 원문 확인. 답변·커밋 메시지에 출처 URL 명시.
- 공식 문서와 다른 판단을 내릴 땐 근거와 트레이드오프를 명시한 뒤 진행.

## Architecture (Google 'Guide to app architecture'만)
- Layer: **UI → Domain(선택) → Data**
- SSOT: 각 데이터 타입은 단일 소스에서만 흐름.
- UDF: 상태는 위→아래, 이벤트는 아래→위.
- Data Layer 진입점은 **Repository**로 한정. ViewModel/UseCase는 DataSource(Firestore/네트워크/센서)에 직접 의존 금지.
- **금지 용어/패턴**: Hexagonal Architecture, Ports & Adapters, Port, Interactor 등 안드로이드 비표준 Clean Architecture 용어.

### ViewModel ↔ Repository ↔ UseCase
- ViewModel은 Repository를 직접 주입받아 호출. (현재 Smartee 는 ViewModel→Repository 직접 호출 구조로 본 규칙과 일치 — 프록시 UseCase 없음.)
- Repository를 1:1로 감싸는 프록시 UseCase는 **만들지 말 것**.
- UseCase는 다음 중 하나일 때만 도입:
    1. 여러 Repository를 조합하는 비즈니스 로직
    2. 여러 ViewModel에서 재사용되는 로직
    3. ViewModel 복잡도가 임계치를 넘었을 때
- UseCase 네이밍: `동사(현재형) + 명사 + UseCase`
  예: `GetStudyWithMeetingsUseCase`, `SettleAttendanceUseCase`, `FormatDateUseCase`
- **Stateless** — UseCase 클래스 멤버 필드로 mutable 데이터(`var`, `MutableStateFlow`, mutable collection 등) 보유 금지. mutable 상태는 UI / Data 레이어가 보유. 공식 가이드: *"각 사용 사례에서는 기능 하나만 담당해야 하고, 변경 가능한 데이터를 포함해서는 안 됩니다"*.
- **Main-safe** — UseCase 는 main thread 에서 호출돼도 안전해야 함. 차단 작업(파일 I/O, 무거운 계산 등)은 `withContext(defaultDispatcher) { ... }` 로 background 이동. Repository 호출이 이미 suspend 면 그대로 위임 가능.
- **UseCase → UseCase 호출 허용** — 재사용 단위라 다른 UseCase 를 종속 항목으로 받아 호출 가능. 다층 도메인 정상.
- **데이터 레이어 캐싱 우선** — 복잡한 계산이라도 도메인으로 무조건 빼지 말 것. *재사용·캐싱이 더 자연스러우면 Repository / DataSource 에 두는 게 우선*. 공식 가이드: *"복잡한 계산은 재사용이나 캐싱을 유도하기 위해 데이터 레이어에서 이루어집니다"*.
- 출처: https://developer.android.com/topic/architecture/domain-layer?hl=ko

## UI Layer
- **한 화면당 단일 UI State 객체** (data class) 지향. loading/error/data 독립 스트림 분리 지양.
- 상태 노출은 `StateFlow` 권장, `MutableStateFlow`는 반드시 `private` 캡슐화.
- 상태 수집은 `collectAsStateWithLifecycle()` 권장. 신규 코드에서 `collectAsState()` 지양.
- 신규 화면은 `@Composable` destination + `navigation/Screen` 라우트. Fragment 신규 생성은 원칙적 지양.
- 일회성 이벤트도 UI state 에 흡수 (Google 공식 권고 — "ViewModel events should always result in a UI state update"). `UiState` 의 nullable 필드(`userMessage`·`navigateTo` 등) + 화면이 소비 후 VM `onConsumed()` 콜백으로 null 처리.
- **현황 메모**: 코드베이스에 `StateFlow`·`LiveData`·`mutableStateOf`·`collectAsState` 가 혼재. 위 규칙은 going-forward 방향이며, 기존 혼재 사용처는 해당 화면 작업 시 점진적으로 `StateFlow` + `collectAsStateWithLifecycle()` 로 수렴한다.

## 필수 라이브러리
- UI: **Jetpack Compose** + **Material3** (Compose BOM 2024.09.00).
- DI: **별도 DI 프레임워크 없음** — ViewModel/Repository 수동 인스턴스화. (Hilt/Dagger 미사용. 도입 시 팀 합의 후 전면 적용.)
- Navigation: **Compose Navigation** (`navigation-compose`), `navigation/Screen` sealed 라우트.
- 비동기: **Coroutines + Flow**.
- 백엔드/DB: **Firebase** — Firestore(`firebase-firestore-ktx`), Auth(`firebase-auth-ktx` + Google Sign-In: `play-services-auth`·`googleid`), Functions(`firebase-functions-ktx`), Analytics. (Retrofit / Room 미사용.)
- 직렬화/JSON: **Gson** (`com.google.code.gson`).
- 이미지: **Coil 3** (`coil-compose`).
- 지도: **Naver Maps**(`map-sdk` + `naver-map-compose`) + **Google Maps**(`play-services-maps` + `maps-compose`) + **Places**.
- ML: `firebase-ml-modeldownloader` + `tensorflow-lite`.
- 어노테이션 처리: 현재 KSP/kapt 미사용(애노테이션 프로세서 없음). 도입 시 **KSP 우선**.

## 신규 도입 금지(구버전·비표준 차단)
- findViewById / XML UI 신규 → **Jetpack Compose**
- AsyncTask, RxJava → **Coroutines + Flow**
- `collectAsState()` 신규 → `collectAsStateWithLifecycle()`
- 신규 ViewModel 상태 노출에 `LiveData` 신규 도입 지양 → `StateFlow` (기존 LiveData 는 점진 이전)
- kapt 신규 → **KSP**
- Deprecated Fragment 인자 전달 → `by navArgs()` 또는 `SavedStateHandle`
- (위는 going-forward 방향. 기존 혼재 코드는 점진 정리.)

## 의존성 작성 규칙
- **(Android)** 모든 의존성은 `gradle/libs.versions.toml`에 등록 후 모듈 `build.gradle.kts`에서 alias로 참조. BOM이 존재하는 라이브러리(Compose 등)는 BOM 우선 사용.
    - 현재 `app/build.gradle.kts` 에 카탈로그 alias 와 하드코딩 좌표(`material`/`material3`/`navigation` 등)가 중복 — 신규 추가 시 카탈로그로 일원화하고, 중복은 발견 시 정리.
- **(백엔드)** `functions/` 의존성은 `functions/package.json`에 등록(npm).
- 라이브러리 추가/업데이트 시 릴리즈 노트 URL을 PR 설명에 첨부.

## 출력 형식
- 코드: Android 는 **Kotlin**, `functions/` 백엔드는 **TypeScript**. 불필요한 주석·서론·맺음말 배제.
- 코드 외 설명은 핵심 의도와 트레이드오프만 간결히.
- 라이브러리 버전 언급 시 Android 는 Maven 좌표, 백엔드는 npm 패키지명 + 출처 URL 동봉.
- 정당한 예외(레거시 통합 등)는 완화 사유를 먼저 설명한 뒤 진행.

## 빌드 / 테스트 명령
```bash
# Android (:app) — 빌드에 app/google-services.json 필요(.gitignore 대상)
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew :app:connectedDebugAndroidTest   # 인스트루멘티드 테스트(에뮬레이터/기기)

# Firebase functions (functions/, Node 22 + TypeScript)
cd functions && npm ci
npm run build      # tsc
npm run lint       # eslint (google config)
npm run serve      # firebase emulators:start --only functions
npm run deploy     # firebase deploy --only functions
```

## 코드 변경 시 체크리스트
- [ ] 새 라이브러리 좌표·버전을 검색으로 검증했는가
- [ ] (Android) `libs.versions.toml`에 등록하고 alias로 참조했는가 / (백엔드) `functions/package.json`에 등록했는가
- [ ] UI 상태가 단일 객체 + `StateFlow` + `collectAsStateWithLifecycle()` 방향에 부합하는가
- [ ] Repository를 우회해 Firestore/DataSource에 직접 의존하지 않는가
- [ ] 새로 만든 UseCase가 도입 3가지 조건 중 하나를 충족하는가
- [ ] `functions/`를 건드렸다면 `npm run lint` / `npm run build`가 통과하는가
