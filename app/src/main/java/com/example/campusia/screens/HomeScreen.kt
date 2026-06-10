package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.CourseCard
import com.example.campusia.components.MetricCard
import com.example.campusia.entities.Course
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextDark
import com.example.campusia.ui.theme.TextMuted
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

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
            db.collection("users")
                .document(currentUid)
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
        containerColor = ScreenBackground,
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = if (username.isBlank()) {
                                "Welcome back"
                            } else {
                                "Welcome back, $username"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

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

            item {
                when (role) {
                    UserRole.LECTURER -> {
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

                    UserRole.STUDENT -> {
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

                    UserRole.ADMIN -> {
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
                                    icon = Icons.Default.People
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                }
            }

            if (role != UserRole.STUDENT) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(text = "Quick Actions")

                        when (role) {
                            UserRole.LECTURER -> {
                                QuickActionCard(
                                    title = "Create New Course",
                                    subtitle = "Add a new course to your teaching list",
                                    icon = Icons.Outlined.Add,
                                    onClick = { navController.navigate("course_creation") }
                                )
                            }

                            UserRole.ADMIN -> {
                                QuickActionCard(
                                    title = "Manage Departments",
                                    subtitle = "Create and organize university departments",
                                    icon = Icons.Outlined.Apartment,
                                    onClick = { navController.navigate("departments_screen") }
                                )
                            }

                            UserRole.STUDENT -> Unit
                        }
                    }
                }
            }

            if (courses.isNotEmpty()) {
                item {
                    SectionTitle(text = "Your courses")
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
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        color = TextDark,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = ScreenBackground,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = FieldBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}