package com.example.campusia.screens

import com.google.firebase.auth.FirebaseAuth

fun signIn(
    auth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavController
) {
    if (email.isEmpty() || password.isEmpty()) return

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate(Routes.homeScreen)
            }
        }
}

fun register(
    auth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavController
) {
    if (email.isEmpty() || password.isEmpty()) return

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate(Routes.homeScreen)
            }
        }
}