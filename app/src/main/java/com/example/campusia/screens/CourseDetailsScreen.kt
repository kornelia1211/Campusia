package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.AlertDialogDelete
import com.example.campusia.entities.Course
import com.example.campusia.entities.User
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CourseDetailsScreen(
    navController: NavHostController,
    courseId: String
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var course by remember {
        mutableStateOf<Course?>(null)
    }

    var students by remember {
        mutableStateOf<List<User>>(emptyList())
    }

    var studentToRemove by remember{mutableStateOf<User?>(null)}

    LaunchedEffect(Unit) {
        db.collection("courses")
            .document(courseId)
            .addSnapshotListener {snapshot, _ ->
                val fetchedCourse = snapshot?.toObject(Course::class.java)
                course = fetchedCourse

                fetchedCourse?.studentIds?.let { ids ->
                    if (ids.isNotEmpty()) {
                        db.collection("users")
                            .whereIn("userId", ids)
                            .get()
                            .addOnSuccessListener { user ->
                                students = user.toObjects(User::class.java)
                            }
                    }
                    else {
                        students = emptyList()
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(5.dp)){
        Text(text = "COURSE DETAILS")

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Title: ${course?.title}")
        Text(text = "Description: ${course?.description}")
        Text(text = "Department: ${course?.department}")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Schedule")

        Text(text = "Day: ${course?.schedule?.dayOfWeek}")
        Text(text = "Time: ${course?.schedule?.startTime} - ${course?.schedule?.endTime}")
        Text(text = "Room: ${course?.schedule?.room}")
        Text(text = "Building: ${course?.schedule?.building}")

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "ENROLLED STUDENTS")
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn{
            items(students) { student ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(){
                        Text(text = student.firstName)
                        Text(text = student.lastName)
                    }

                    Text(text = student.email)

                    IconButton(onClick = { studentToRemove = student }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color.Red)
                    }
                }
            }
        }
    }


    studentToRemove?.let { student ->
        AlertDialogDelete(
            message = "Are you sure you want to remove the student\"${student.firstName} ${student.lastName}\" from this course?",
            onDismiss = { studentToRemove = null },
            onConfirm = {
                val docRef = db.collection("courses").document(courseId)

                docRef.update(
                    "studentIds", FieldValue.arrayRemove(student.userId),
                    "enrolledStudents", FieldValue.increment(-1) // to also change the number of enrolled students
                ).addOnSuccessListener {
                    Toast.makeText(context, "Student removed", Toast.LENGTH_SHORT).show()
                    studentToRemove = null
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to remove student", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}


