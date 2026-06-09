package com.example.campusia

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusia.screens.AssignmentCreationScreen
import com.example.campusia.screens.AssignmentDetailsScreen
import com.example.campusia.screens.CourseCreationScreen
import com.example.campusia.screens.CourseDetailsScreen
import com.example.campusia.screens.DepartmentsScreen
import com.example.campusia.screens.HomeScreen
import com.example.campusia.screens.LoginScreen
import com.example.campusia.screens.MyCoursesScreen
import com.example.campusia.screens.RegisterScreen
import com.example.campusia.screens.ScheduleScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation(auth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login_screen"
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
    }
}