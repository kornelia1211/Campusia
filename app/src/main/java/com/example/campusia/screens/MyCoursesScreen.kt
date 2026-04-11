package com.example.campusia.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.CourseCard
import com.example.campusia.entities.Course


@Composable
fun MyCoursesScreen(navController: NavHostController) {
    val role = SessionManager.userRole

    val courses = listOf(
        Course("1", "Intro to CS", "Basics of programming", 120, 150, "Mon"),
        Course("2", "Algorithms", "Data structures", 80, 100, "Tue"),
        Course("3", "Web Dev", "Frontend + backend", 95, 120, "Wed")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "My Courses",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        courses.forEach { course ->
            CourseCard(
                course = course,
                role = role,

                onClick = {
                    // TODO: open course details
                },

                onEnroll = {
                    // only student
                    println("Enroll to ${course.title}")
                },

                onEdit = {
                    // lecturer/admin
                    println("Edit ${course.title}")
                },

                onDelete = {
                    // lecturer/admin
                    println("Delete ${course.title}")
                }
            )
        }
    }
}