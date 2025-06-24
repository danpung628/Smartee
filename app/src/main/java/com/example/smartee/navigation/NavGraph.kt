// smartee/navigation/NavGraph.kt

package com.example.smartee.navigation

// HostScreen import는 이제 필요 없습니다.
//import com.example.smartee.ui.splash.SplashScreen
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartee.ui.LocalAuthViewModel
import com.example.smartee.ui.LocalNavGraphViewModelStoreOwner
import com.example.smartee.ui.Map.NaverMapScreen
import com.example.smartee.ui.admin.AdminReportScreen
import com.example.smartee.ui.attendance.AttendanceScreen
import com.example.smartee.ui.attendance.ParticipantScreen
import com.example.smartee.ui.badge.BadgeScreen
import com.example.smartee.ui.comment.CommentScreen
import com.example.smartee.ui.login.LoginScreen
import com.example.smartee.ui.meeting.MeetingEditScreen
import com.example.smartee.ui.profile.ProfileEditScreen
import com.example.smartee.ui.profile.ProfileScreen
import com.example.smartee.ui.report.ReportScreen
import com.example.smartee.ui.request.MeetingRequestListScreen
import com.example.smartee.ui.request.RequestListScreen
import com.example.smartee.ui.screen.MyStudyScreen
import com.example.smartee.ui.signup.FillProfileScreen
import com.example.smartee.ui.signup.SignUpScreen
import com.example.smartee.ui.splash.SplashScreen
import com.example.smartee.ui.study.creatstudy.ui.screen.StudyCreationScreen
import com.example.smartee.ui.study.editstudy.ui.StudyEditScreen
import com.example.smartee.ui.study.studyList.main.StudyListScreen
import com.example.smartee.ui.study.studyList.search.StudySearchScreen
import com.example.smartee.ui.study.studyList.studydetail.StudyDetailScreen
import com.example.smartee.viewmodel.UserViewModel
import com.example.smartee.viewmodel.UserViewModelFactory


@Composable
fun SmarteeNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val randomCode = remember { mutableIntStateOf((100..999).random()) }

    // 다른 화면들처럼 제대로 된 방법으로 사용자 정보 가져오기
    val authViewModel = LocalAuthViewModel.current
    val userViewModel: UserViewModel = viewModel(
        viewModelStoreOwner = LocalNavGraphViewModelStoreOwner.current,
        factory = UserViewModelFactory(LocalContext.current.applicationContext as Application)
    )

    // by delegate 제거하고 .value로 직접 접근
    val currentUser = authViewModel.currentUser.collectAsState().value
    val userData = userViewModel.userData.observeAsState().value

    val currentUserId = currentUser?.uid ?: ""  // currentUser에서 uid 가져오기
    val currentUserNickname = userData?.nickname ?: ""

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(navController = navController)
        }
        composable(Screen.Badge.route) {
            BadgeScreen()
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.FillProfile.route) {
            FillProfileScreen(navController)
        }
        composable(Screen.StudyCreate.route) {
            StudyCreationScreen(navController)
        }
        composable(
            route = Screen.StudyList.route
        ) {
            StudyListScreen(
                onStudyDetailNavigate = { studyId ->
                    navController.navigate(Screen.Detail.route + "?studyID=$studyId")
                },
                onSearchNavigate = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }
        composable(route = Screen.Search.route) {
            StudySearchScreen { keyword ->
                navController.navigate(Screen.StudyList.route + "?keyword=$keyword")
            }
        }
        composable(
            route = Screen.Detail.route + "?studyID={ID}",
            arguments = listOf(
                navArgument("ID") {
                    type = NavType.StringType
                }
            )
        ) {

            // [수정] StudyDetailScreen에 파라미터 전달
            StudyDetailScreen(
                studyId = it.arguments!!.getString("ID")!!,
                navController = navController,
                randomCode = randomCode.intValue,
                onCodeGenerated = { newCode -> randomCode.intValue = newCode }
            )
        }
        composable(
            route = Screen.StudyEdit.route + "?studyID={ID}",
            arguments = listOf(
                navArgument("ID") {
                    type = NavType.StringType
                }
            )
        ) {
            StudyEditScreen(
                studyId = it.arguments!!.getString("ID")!!,
                navController = navController // [추가] navController를 전달합니다.
            )
        }
        composable(Screen.Attendance.route) {
            AttendanceScreen(navController)
        }

        // [삭제] HostScreen 경로 삭제
        // composable(Screen.Host.route) { ... }

        composable(Screen.Participant.route) {
            ParticipantScreen(navController = navController)
        }
        composable("my_study") {
            MyStudyScreen(
                onStudyClick = { studyId ->
                    navController.navigate(Screen.Detail.route + "?studyID=$studyId")
                }
            )
        }
        composable("map") {
            NaverMapScreen()
        }
        composable(
            route = "request_list/{studyId}",
            arguments = listOf(navArgument("studyId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequestListScreen(studyId = backStackEntry.arguments?.getString("studyId") ?: "")
        }
        composable(
            route = "meeting_edit/{parentStudyId}?meetingId={meetingId}",
            arguments = listOf(
                navArgument("parentStudyId") { type = NavType.StringType },
                navArgument("meetingId") { nullable = true }
            )
        ) { backStackEntry ->
            MeetingEditScreen(
                parentStudyId = backStackEntry.arguments?.getString("parentStudyId") ?: "",
                meetingId = backStackEntry.arguments?.getString("meetingId"),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "meeting_request_list/{meetingId}",
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType })
        ) { backStackEntry ->
            MeetingRequestListScreen(
                meetingId = backStackEntry.arguments?.getString("meetingId") ?: "",
                navController = navController
            )
        }

        //신고 화면
        composable(
            route = Screen.Report.route + "?studyID={studyID}",
            arguments = listOf(
                navArgument("studyID") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studyId = backStackEntry.arguments?.getString("studyID") ?: ""
            ReportScreen(studyId = studyId, navController = navController)
        }

        //관리자 화면
        composable(Screen.AdminReport.route) {
            AdminReportScreen(navController = navController)
        }

        composable(
            route = "comment/{studyId}",
            arguments = listOf(navArgument("studyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studyId = backStackEntry.arguments?.getString("studyId") ?: ""
            CommentScreen(
                studyId = studyId,
                currentUserId = currentUserId,
                currentUserNickname = currentUserNickname,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}