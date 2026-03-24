package com.example.campusia

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campusia.screens.AdminHomeScreen
import com.example.campusia.screens.HomeScreen
import com.example.campusia.screens.LecturerHomeScreen
import com.example.campusia.screens.LoginScreen
import com.example.campusia.screens.RegisterScreen
import com.example.campusia.screens.StudentHomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Navigation(auth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login_screen"
    ){
        composable("login_screen") {
            LoginScreen(navController, auth)
        }
        composable("register_screen") {
            RegisterScreen(navController, auth)
        }
        composable("home_screen") {
            HomeScreen(navController)
        }
        composable("student_home") {
            StudentHomeScreen(navController)
        }
        composable("lecturer_home") {
            LecturerHomeScreen(navController)
        }
        composable("admin_home") {
            AdminHomeScreen(navController)
        }


    }

}