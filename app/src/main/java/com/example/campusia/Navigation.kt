package com.example.campusia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusia.entities.mapRole
import com.example.campusia.notifications.FcmTokenManager
import com.example.campusia.notifications.NotificationPermissionEffect
import com.example.campusia.screens.AnnouncementCreationScreen
import com.example.campusia.screens.AnnouncementDetailsScreen
import com.example.campusia.screens.AssignmentCreationScreen
import com.example.campusia.screens.AssignmentDetailsScreen
import com.example.campusia.screens.ChatListScreen
import com.example.campusia.screens.ChatScreen
import com.example.campusia.screens.CourseAnnouncementsListScreen
import com.example.campusia.screens.CourseAssignmentsListScreen
import com.example.campusia.screens.CourseCreationScreen
import com.example.campusia.screens.CourseDetailsScreen
import com.example.campusia.screens.DepartmentsScreen
import com.example.campusia.screens.HomeScreen
import com.example.campusia.screens.LoginScreen
import com.example.campusia.screens.MyCoursesScreen
import com.example.campusia.screens.ProfileScreen
import com.example.campusia.screens.RegisterScreen
import com.example.campusia.screens.ScheduleScreen
import com.example.campusia.ui.theme.PrimaryPurple
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.campusia.screens.NotificationHistoryScreen

@Composable
fun Navigation(auth: FirebaseAuth,
               initialRoute: String? = null,
               onInitialRouteConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    var isCheckingSession by remember {
        mutableStateOf(true)
    }

    var startDestination by remember {
        mutableStateOf("login_screen")
    }

    NotificationPermissionEffect()

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startDestination = "login_screen"
            isCheckingSession = false
        } else {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val roleString = document.getString("role") ?: "Student"
                    SessionManager.userRole = mapRole(roleString)

                    FcmTokenManager.saveCurrentTokenForLoggedUser()

                    startDestination = "home_screen"
                    isCheckingSession = false
                }
                .addOnFailureListener {
                    FcmTokenManager.saveCurrentTokenForLoggedUser()

                    startDestination = "home_screen"
                    isCheckingSession = false
                }
        }
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryPurple
            )
        }
        return
    }

    LaunchedEffect(isCheckingSession, initialRoute, startDestination) {
        if (
            !isCheckingSession &&
            !initialRoute.isNullOrBlank() &&
            startDestination != "login_screen"
        ) {
            navController.navigate(initialRoute) {
                launchSingleTop = true
            }

            onInitialRouteConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login_screen") {
            LoginScreen(navController, auth)
        }

        composable("register_screen") {
            RegisterScreen(navController, auth)
        }

        composable("home_screen") {
            HomeScreen(navController)
        }

        composable("courses_screen") {
            MyCoursesScreen(navController)
        }

        composable("course_creation") {
            CourseCreationScreen(navController)
        }

        composable(
            route = "edit_course/{courseId}"
        ) { backStackEntry ->

            val courseId =
                backStackEntry.arguments?.getString("courseId")

            CourseCreationScreen(
                navController = navController,
                courseId = courseId
            )
        }

        composable(
            route = "course_detail/{courseId}"
        ) { backStackEntry ->

            val courseId =
                backStackEntry.arguments?.getString("courseId")

            if (courseId != null) {
                CourseDetailsScreen(
                    navController = navController,
                    courseId = courseId
                )
            }
        }

        composable("schedule_screen") {
            ScheduleScreen(navController)
        }

        composable("profile_screen") {
            ProfileScreen(navController)
        }

        composable(
            route = "assignment_creation_screen/{courseId}"
        ) { backStackEntry ->

            val courseId =
                backStackEntry.arguments?.getString("courseId")

            if (courseId != null) {
                AssignmentCreationScreen(
                    navController = navController,
                    courseId = courseId
                )
            }
        }

        composable(
            route = "edit_assignment/{assignmentId}/{courseId}"
        ) { backStackEntry ->

            val assignmentId =
                backStackEntry.arguments?.getString("assignmentId")

            val courseId =
                backStackEntry.arguments?.getString("courseId") ?: ""

            AssignmentCreationScreen(
                navController = navController,
                courseId = courseId,
                assignmentId = assignmentId
            )
        }

        composable(
            route = "assignment_details/{assignmentId}"
        ) { backStackEntry ->

            val assignmentId =
                backStackEntry.arguments?.getString("assignmentId")

            if (assignmentId != null) {
                AssignmentDetailsScreen(
                    navController = navController,
                    assignmentId = assignmentId
                )
            }
        }

        composable("departments_screen") {
            DepartmentsScreen(navController = navController)
        }

        composable("chatList_screen") {
            ChatListScreen(navController = navController)
        }

        composable("chat/{chatRoomId}/{chatTitle}") { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId")
            val chatTitle = backStackEntry.arguments?.getString("chatTitle")

            if (chatRoomId != null && chatTitle != null) {
                ChatScreen(
                    navController = navController,
                    chatRoomId,
                    chatTitle
                )
            }
        }

        composable("announcement_creation_screen/{courseId}") { backStackEntry ->
            val courseId =
                backStackEntry.arguments?.getString("courseId")
                    ?: return@composable

            AnnouncementCreationScreen(
                navController = navController,
                courseId = courseId
            )
        }

        composable("announcement_details_screen/{announcementId}") { backStackEntry ->
            val announcementId =
                backStackEntry.arguments?.getString("announcementId")
                    ?: return@composable

            AnnouncementDetailsScreen(
                navController = navController,
                announcementId = announcementId
            )
        }

        composable("course_assignments_screen/{courseId}") { backStackEntry ->
            val courseId =
                backStackEntry.arguments?.getString("courseId")
                    ?: return@composable

            CourseAssignmentsListScreen(
                navController = navController,
                courseId = courseId
            )
        }

        composable("course_announcements_screen/{courseId}") { backStackEntry ->
            val courseId =
                backStackEntry.arguments?.getString("courseId")
                    ?: return@composable

            CourseAnnouncementsListScreen(
                navController = navController,
                courseId = courseId
            )
        }

        composable("edit_announcement/{announcementId}/{courseId}") { backStackEntry ->
            val announcementId =
                backStackEntry.arguments?.getString("announcementId")
                    ?: return@composable

            val courseId =
                backStackEntry.arguments?.getString("courseId")
                    ?: return@composable

            AnnouncementCreationScreen(
                navController = navController,
                courseId = courseId,
                announcementId = announcementId
            )
        }

        composable("notifications_screen") {
            NotificationHistoryScreen(navController = navController)
        }
    }
}