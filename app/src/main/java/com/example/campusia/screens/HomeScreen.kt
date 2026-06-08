package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.CourseCard
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.components.MetricCard
import com.example.campusia.entities.Course
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: change hardcoded values to real ones
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val role = SessionManager.userRole
    val db = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf(Firebase.auth.currentUser) }
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Firebase.auth.addAuthStateListener { auth ->
            user = auth.currentUser
        }
    }

    LaunchedEffect(user?.uid) {
        val currentUid = user?.uid
        if (currentUid != null) {
            db.collection("users").document(currentUid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val first = documentSnapshot.getString("firstName") ?: ""
                        val last = documentSnapshot.getString("lastName") ?: ""

                        if (first.isNotBlank() || last.isNotBlank()) {
                            username = "$first $last".trim()
                        }
                    }
                }
        }
    }

    DisposableEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        val query = when (role) {
            UserRole.LECTURER -> {
                db.collection("courses")
                    .whereArrayContains("lecturerIds", currentUserId ?: "")
            }

            UserRole.STUDENT -> {
                db.collection("courses")
                    .whereArrayContains("studentIds", currentUserId ?: "")
            }

            UserRole.ADMIN -> {
                db.collection("courses")
            }
        }

        val listenerRegistration = query
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(
                        context,
                        "Error fetching data",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    courses = snapshot.toObjects(Course::class.java)
                }
            }

        onDispose {
            listenerRegistration.remove()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "home"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryPurple,
                                    PrimaryPurpleDark
                                )
                            )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Welcome back, $username",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when (role) {
                                UserRole.LECTURER -> "Manage your courses and engage with students"
                                UserRole.STUDENT -> "Ready to continue your learning journey?"
                                UserRole.ADMIN -> "Manage your university platform analytics"
                            },
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            item{ Spacer(modifier = Modifier.height(15.dp)) }

            when (role) {
                UserRole.LECTURER -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Courses",
                                value = "${courses.size}",
                                icon = Icons.AutoMirrored.Filled.MenuBook
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Students",
                                value = "205",
                                icon = Icons.Default.People
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "To check",
                                value = "5",
                                icon = Icons.Default.AssignmentTurnedIn
                            )
                        }
                    }

                    item{ Spacer(modifier = Modifier.height(15.dp)) }

                    item {
                        Text(
                            text = "Quick Actions",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate("course_creation") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Create New Course")
                            }
                        }
                    }
                }

                UserRole.STUDENT -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "My Courses",
                                value = "${courses.size}",
                                icon = Icons.AutoMirrored.Filled.MenuBook
                            )

                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Pending Tasks",
                                value = "2",
                                icon = Icons.Default.AssignmentTurnedIn
                            )
                        }
                    }
                }


                UserRole.ADMIN -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Total Courses",
                                    value = "${courses.size}",
                                    icon = Icons.AutoMirrored.Filled.MenuBook
                                )

                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Total Students",
                                    value = "242",
                                    icon = Icons.Default.People)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Active Lecturers",
                                    value = "12",
                                    icon = Icons.Default.SupervisorAccount
                                    )

                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Departments",
                                    value = "4",
                                    icon = Icons.Default.HomeWork
                                    )
                            }
                        }
                    }

                    item{ Spacer(modifier = Modifier.height(15.dp)) }


                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Quick Actions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Button(
                                onClick = { navController.navigate("departments_screen") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Departments")
                            }
                        }
                    }
                }
            }

            item{ Spacer(modifier = Modifier.height(15.dp)) }

            if (courses.isNotEmpty()){
                item { Text (
                    text = "Your courses",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp)}
            }
            items(courses.take(3)) { course ->
                CourseCard(
                    course = course,
                    role = role,
                    onClick = {
                        navController.navigate(
                            "course_detail/${course.courseId}"
                        )
                    }
                )
            }
        }
    }
}
