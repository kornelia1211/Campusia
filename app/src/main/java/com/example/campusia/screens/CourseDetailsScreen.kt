package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.AlertDialogDelete
import com.example.campusia.entities.Course
import com.example.campusia.entities.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private val ScreenBackground = Color(0xFFF5F3F8)
private val CardBackground = Color(0xFFFDFDFF)
private val CardBorder = Color(0xFFE1DAF0)
private val FieldBackground = Color(0xFFF8F6FC)
private val IconCircle = Color(0xFFE9DDFB)
private val AccentPurple = Color(0xFF8E49E8)
private val AccentPurpleLight = Color(0xFFB8ACEF)
private val TextPrimary = Color(0xFF2E3240)
private val TextSecondary = Color(0xFF8B92A1)
private val DangerRed = Color(0xFFE25555)

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

    var studentToRemove by remember {
        mutableStateOf<User?>(null)
    }

    LaunchedEffect(Unit) {
        db.collection("courses")
            .document(courseId)
            .addSnapshotListener { snapshot, _ ->
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
                    } else {
                        students = emptyList()
                    }
                }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            HeaderCard(
                onBackClick = {
                    navController.navigate("courses_screen") {
                        popUpTo("courses_screen") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        item {
            SectionCard(
                title = "Basic Information"
            ) {
                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = "Title",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Title",
                    value = course?.title ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Description",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Description",
                    value = course?.description ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Business,
                            contentDescription = "Department",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Department",
                    value = course?.department ?: ""
                )
            }
        }

        item {
            SectionCard(
                title = "Schedule"
            ) {
                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Day",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Day of week",
                    value = course?.schedule?.dayOfWeek?.name ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Time",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Time",
                    value = "${course?.schedule?.startTime ?: ""} - ${course?.schedule?.endTime ?: ""}"
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Room",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Room",
                    value = course?.schedule?.room ?: ""
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Business,
                            contentDescription = "Building",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Building",
                    value = course?.schedule?.building ?: ""
                )
            }
        }

        item {
            SectionCard(
                title = "Enrolled Students"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconCircleBox(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Groups,
                                contentDescription = "Students",
                                tint = AccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Students",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                students.forEachIndexed { index, student ->

                    StudentCard(
                        student = student,
                        onDeleteClick = {
                            studentToRemove = student
                        }
                    )

                    if (index != students.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    studentToRemove?.let { student ->
        AlertDialogDelete(
            message = "Are you sure you want to remove the student \"${student.firstName} ${student.lastName}\" from this course?",
            onDismiss = { studentToRemove = null },
            onConfirm = {
                val docRef = db.collection("courses").document(courseId)

                docRef.update(
                    "studentIds", FieldValue.arrayRemove(student.userId),
                    "enrolledStudents", FieldValue.increment(-1)
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

@Composable
private fun HeaderCard(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AccentPurpleLight,
                        AccentPurple
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Column {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back to courses"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Course Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Review the course details and enrolled students.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CardBorder,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            content()
        }
    }
}

@Composable
private fun DetailRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircleBox(icon = icon)

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FieldBackground)
                .border(
                    width = 1.dp,
                    color = CardBorder,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StudentCard(
    student: User,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = FieldBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircleBox(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Student",
                        tint = AccentPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = student.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = DangerRed.copy(alpha = 0.12f),
                    contentColor = DangerRed
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove student"
                )
            }
        }
    }
}

@Composable
private fun IconCircleBox(
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(IconCircle),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}