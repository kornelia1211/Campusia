package com.example.campusia.screens

import android.content.Context
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.AlertDialogDelete
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.CourseCard
import com.example.campusia.entities.Course
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


private val ScreenBackground = Color(0xFFF8F7FB)
val HeaderTextColor = Color(0xFF1F1F29)
private val SubtitleTextColor = Color(0xFF8A8A98)
val AccentPurple = Color(0xFFA78BFA)
val AccentPurpleDark = Color(0xFF8B5CF6)
private val SearchBackground = Color.White
val SearchBorder = Color(0xFFE7E2F3)
private val EmptyCardBackground = Color.White

@Composable
fun MyCoursesScreen(navController: NavHostController) {
    val role = SessionManager.userRole
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyEnrolled by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }
    var courseToDelete by remember {
        mutableStateOf<Course?>(null)
    }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isBlank()) return@LaunchedEffect

        val query = when (role) {

            UserRole.LECTURER -> {
                db.collection("courses")
                    .whereArrayContains("lecturerIds", currentUserId ?: "")
            }

            else -> {
                db.collection("courses")
            }
        }

        listenerRegistration = query
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
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    val filteredCourses = courses.filter { course ->
        val query = searchQuery.trim()
        val matchesTitle = course.title.contains(searchQuery.trim(), ignoreCase = true)
        val matchesDepartment = course.department.contains(query, ignoreCase = true)
        val matchesLecturer = course.lecturerNames.any { name ->
            name.contains(query, ignoreCase = true)
        }

        matchesTitle || matchesDepartment || matchesLecturer
    }.filter { course ->
        if (showOnlyEnrolled) {
            course.studentIds.contains(currentUserId)
        } else {
            true
        }
    }.sortedBy { it.title.lowercase() }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "courses"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding()
                ),
        ) {
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
                        text = "My Courses",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (role == UserRole.STUDENT) {
                            "Browse and manage your enrolled courses"
                        } else {
                            "Manage your teaching courses"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )
                }
            }

            if (role != UserRole.STUDENT) {
                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = { navController.navigate("course_creation") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .background(
                            color = AccentPurple,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add course",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "New Course",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = {
                    Text(
                        text = "Search by course name, lecturer or department",
                        color = SubtitleTextColor
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = AccentPurpleDark
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SearchBackground,
                    unfocusedContainerColor = SearchBackground,
                    disabledContainerColor = SearchBackground,
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = SearchBorder,
                    cursorColor = AccentPurpleDark
                )
            )

            if (role == UserRole.STUDENT){
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showOnlyEnrolled) AccentPurple.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (showOnlyEnrolled) AccentPurple else SearchBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showOnlyEnrolled = !showOnlyEnrolled }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enrolled Only",
                        color = if (showOnlyEnrolled) AccentPurpleDark else HeaderTextColor,
                        fontWeight = if (showOnlyEnrolled) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (filteredCourses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmptyCardBackground
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFF2ECFF),
                                    shape = CircleShape
                                )
                                .padding(14.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "No courses available"
                            } else {
                                "No matching courses found"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = HeaderTextColor
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Courses will appear here when they are added."
                            } else {
                                "Try a different course name."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SubtitleTextColor
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCourses) { course ->
                        CourseCard(
                            course = course,
                            role = role,
                            onClick = {
                                navController.navigate(
                                    "course_detail/${course.courseId}"
                                )
                            },
                            onEnroll = {
                                enrollToCourse(course, context)
                            },
                            onEdit = {
                                navController.navigate(
                                    "edit_course/${course.courseId}"
                                )
                            },
                            onDelete = {
                                courseToDelete = course
                            }
                        )
                    }

                }
            }
        }
    }

    courseToDelete?.let { course ->
        AlertDialogDelete(
            message = "Are you sure you want to delete the course \"${course.title}\"?",
            onDismiss = { courseToDelete = null },
            onConfirm = {
                db.collection("courses").document(course.courseId).delete()
                courseToDelete = null
            }
        )
    }
}

fun enrollToCourse(
    course: Course,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    if (course.enrolledStudents >= course.maxStudents) {
        Toast.makeText(context, "Course is full", Toast.LENGTH_SHORT).show()
        return
    }

    val docRef = db.collection("courses").document(course.courseId)

    val updatedStudentIds = course.studentIds + userId
    val updatedCount = course.enrolledStudents + 1

    docRef.update(
        mapOf(
            "studentIds" to updatedStudentIds,
            "enrolledStudents" to updatedCount
        )
    )
        .addOnSuccessListener {
            db.collection("chat_rooms").document(course.courseId)
                .update("participants", FieldValue.arrayUnion(userId))
            Toast.makeText(context, "Enrolled!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
}