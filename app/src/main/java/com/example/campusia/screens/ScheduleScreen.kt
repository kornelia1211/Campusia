package com.example.campusia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.entities.Course
import com.example.campusia.entities.CourseFrequency
import com.example.campusia.entities.UserRole
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

                courses = when(role) {

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        orderedDays.forEach { day ->

            val dayCourses = groupedCourses[day]

            if (!dayCourses.isNullOrEmpty()) {

                item {

                    Text(
                        text = day.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(dayCourses) { course ->

                    val frequencyText = when(course.schedule.frequency) {
                        CourseFrequency.EVERY_WEEK -> ""
                        CourseFrequency.EVEN_WEEKS -> " (even weeks)"
                        CourseFrequency.ODD_WEEKS -> " (odd weeks)"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${course.schedule.startTime} - ${course.schedule.endTime}$frequencyText"
                            )

                            Text(
                                text = "${course.schedule.building} ${course.schedule.room}"
                            )

                            Text(
                                text = course.schedule.type.name
                            )
                        }
                    }
                }
            }
        }
    }
}