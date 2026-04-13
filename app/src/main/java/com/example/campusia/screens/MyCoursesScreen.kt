package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.CourseCard
import com.example.campusia.components.RoundedButton
import com.example.campusia.entities.Course
import com.example.campusia.entities.CourseSchedule
import com.example.campusia.entities.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun MyCoursesScreen(navController: NavHostController) {
    val role = SessionManager.userRole
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    courses = snapshot.toObjects(Course::class.java)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if(role != UserRole.STUDENT) {
            RoundedButton(
                text = "➕  New Course",
                onClick = { navController.navigate("course_creation") }
            )
        }

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
                    enrollToCourse(course, context)
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


fun enrollToCourse(
    course: Course,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    if (course.studentIds.contains(userId)) {
        Toast.makeText(context, "Already enrolled", Toast.LENGTH_SHORT).show()
        return
    }

    if (course.enrolledStudents >= course.maxStudents) {
        Toast.makeText(context, "Course is full", Toast.LENGTH_SHORT).show()
        return
    }

    val docRef = db.collection("courses").document(course.courseId)

    val updatedStudentIds = course.studentIds + userId
    val updatedCount = course.enrolledStudents + 1

    //update studentIds
    docRef.update("studentIds", updatedStudentIds)

    //update enrolledStudents
    docRef.update("enrolledStudents", updatedCount)
        .addOnSuccessListener {
            Toast.makeText(context, "Enrolled!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
}