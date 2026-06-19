package com.example.campusia.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.BottomNavBar
import com.example.campusia.entities.Course
import com.example.campusia.entities.CourseFrequency
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DayOfWeek

@Composable
fun ScheduleScreen(
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val role = SessionManager.userRole

    var courses by remember {
        mutableStateOf<List<Course>>(emptyList())
    }

    LaunchedEffect(Unit) {
        db.collection("courses")
            .get()
            .addOnSuccessListener { snapshot ->

                val allCourses = snapshot.toObjects(Course::class.java)

                courses = when (role) {
                    UserRole.LECTURER -> {
                        allCourses.filter {
                            it.lecturerIds.contains(currentUserId)
                        }
                    }

                    UserRole.STUDENT -> {
                        allCourses.filter {
                            it.studentIds.contains(currentUserId)
                        }
                    }

                    UserRole.ADMIN -> {
                        allCourses
                    }
                }
            }
    }

    val groupedCourses = courses
        .sortedWith(
            compareBy<Course>(
                { it.schedule.dayOfWeek.number },
                { it.schedule.startTime }
            )
        )
        .groupBy { it.schedule.dayOfWeek }

    val orderedDays = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    val daysWithCourses = orderedDays.count { day ->
        !groupedCourses[day].isNullOrEmpty()
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "schedule"
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ScheduleHeaderCard(
                    courseCount = courses.size,
                    daysCount = daysWithCourses
                )
            }

            if (courses.isEmpty()) {
                item {
                    EmptyScheduleCard()
                }
            } else {
                orderedDays.forEach { day ->
                    val dayCourses = groupedCourses[day]

                    if (!dayCourses.isNullOrEmpty()) {
                        item {
                            DaySectionHeader(
                                day = day,
                                count = dayCourses.size
                            )
                        }

                        items(dayCourses) { course ->
                            ScheduleCourseCard(
                                course = course,
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
    }
}

@Composable
private fun ScheduleHeaderCard(
    courseCount: Int,
    daysCount: Int
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
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your schedule",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Check your weekly course timetable.",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeaderStatPill(
                    modifier = Modifier.weight(1f),
                    value = courseCount.toString(),
                    label = "Courses"
                )

                HeaderStatPill(
                    modifier = Modifier.weight(1f),
                    value = daysCount.toString(),
                    label = "Active days"
                )
            }
        }
    }
}

@Composable
private fun HeaderStatPill(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.24f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DaySectionHeader(
    day: DayOfWeek,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDayName(day),
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PrimaryPurple.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (count == 1) "1 class" else "$count classes",
                color = PrimaryPurpleDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ScheduleCourseCard(
    course: Course,
    onClick: () -> Unit
) {
    val frequencyText = remember(course.schedule.frequency) {
        when (course.schedule.frequency) {
            CourseFrequency.EVERY_WEEK -> "Every week"
            CourseFrequency.EVEN_WEEKS -> "Even weeks"
            CourseFrequency.ODD_WEEKS -> "Odd weeks"
        }
    }

    val typeText = remember(course.schedule.type) {
        formatEnumName(course.schedule.type.name)
    }

    val locationText = remember(
        course.schedule.building,
        course.schedule.room
    ) {
        listOf(
            course.schedule.building,
            course.schedule.room
        )
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { "No room assigned" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
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
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFF2ECFF),
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
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = PrimaryPurpleDark,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = course.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ScheduleInfoRow(
                    icon = Icons.Outlined.Schedule,
                    text = "${course.schedule.startTime} - ${course.schedule.endTime}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                ScheduleInfoRow(
                    icon = Icons.Outlined.LocationOn,
                    text = locationText
                )

                Spacer(modifier = Modifier.height(8.dp))

                ScheduleInfoRow(
                    icon = Icons.Outlined.Info,
                    text = "$typeText • $frequencyText"
                )
            }
        }
    }
}

@Composable
private fun ScheduleInfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryPurpleDark,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun EmptyScheduleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        color = Color(0xFFF2ECFF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryPurpleDark,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No classes scheduled yet",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your weekly timetable will appear here when courses are added.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatDayName(day: DayOfWeek): String {
    return day.name
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}

private fun formatEnumName(value: String): String {
    return value
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}
