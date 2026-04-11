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

fun createCourse(
    title: String,
    description: String,
    department: String,
    schedule: CourseSchedule,
    maxStudents: Int,
    providedLecturerIds: List<String> = emptyList(), // if admin creates the course or lecturer wants to add a co-lecturer
    context: Context,
    onSuccess: () -> Unit
){
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return
    val currentUserRole = SessionManager.userRole

    val finalLecturerIds = if (currentUserRole == UserRole.LECTURER) {
        (providedLecturerIds + currentUserId).distinct()
    } else {
        providedLecturerIds.distinct()
    }

    if (finalLecturerIds.isEmpty()) {
        Toast.makeText(context, "Please select at least one lecturer", Toast.LENGTH_SHORT).show()
        return
    }

    val courseRef = db.collection("courses").document()

    val newCourse = Course(
        courseId = courseRef.id,
        title = title,
        description = description,
        department = department,
        maxStudents = maxStudents,
        lecturerIds = finalLecturerIds,
        schedule = schedule
    )

    courseRef.set(newCourse).addOnSuccessListener {
        Toast.makeText(context, "Course was successfully created!", Toast.LENGTH_SHORT).show()
        onSuccess()
    }
}

