package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.InputTextField
import com.example.campusia.components.RoundedButton
import com.example.campusia.entities.Course
import com.example.campusia.entities.CourseSchedule
import com.example.campusia.entities.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CourseCreationScreen(
    navController: NavHostController
){
    var title by remember { mutableStateOf( "") }
    var description by remember { mutableStateOf( "") }
    var department by remember { mutableStateOf( "") }
    var schedule by remember { mutableStateOf( "") }
    var maxStudents by remember { mutableStateOf( "") }
    var providedLecturerIds by remember { mutableStateOf( "") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()), // Allows scrolling if keyboard overlaps
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Title", fontWeight = FontWeight.SemiBold)
        InputTextField(
            title,
            onValueChange = { title = it },
            placeholder = "Introduction to Programming"
        )

        Text("Description", fontWeight = FontWeight.SemiBold)
        InputTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = "Course about basics of programming languages"
        )

        //Change it to Dropdown using Departments enum
        Text("Department", fontWeight = FontWeight.SemiBold)
        InputTextField(
            value = department,
            onValueChange = { department = it },
            placeholder = "Computer Science"
        )

        //Scrolling to the right number instead of numeric keyboard?
        Text("Max Students", fontWeight = FontWeight.SemiBold)
        InputTextField(
            value = maxStudents,
            onValueChange = { maxStudents = it },
            placeholder = "30",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        //"Just so it works for now, should use CourseScedule data class
        Text("Schedule", fontWeight = FontWeight.SemiBold)
        InputTextField(
            value = schedule,
            onValueChange = { schedule = it },
            placeholder = "Room 302, Building B"
        )

        //Should be a dropdown
        Text("Other Lecturers", fontWeight = FontWeight.SemiBold)
        InputTextField(
            value = providedLecturerIds,
            onValueChange = { providedLecturerIds = it },
            placeholder = "Mariola Watson, Gabriel Doe"
        )

        RoundedButton("Create Course", onClick = {
            val scheduleObject = CourseSchedule(room = schedule)
            val maxStudentsInt = maxStudents.toIntOrNull() ?: 0
            val lecturersList = if (providedLecturerIds.isBlank()) {
                emptyList()
            } else {
                providedLecturerIds.split(",").map { it.trim() }
            }
            createCourse(
                title = title,
                description = description,
                department = department,
                schedule = scheduleObject,
                maxStudents = maxStudentsInt,
                providedLecturerIds = lecturersList,
                context = context,
                onSuccess = { navController.navigate("courses_screen") }
            )
        })
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
