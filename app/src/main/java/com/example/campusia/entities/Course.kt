package com.example.campusia.entities

import com.google.type.DayOfWeek

enum class CourseType {
    LECTURE, LABORATORY, SEMINAR, PROJECT
}
data class Course(
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val department: String = "",
    val enrolledStudents: Int = 0,
    val maxStudents: Int = 10,
    val lecturerIds: List<String> = emptyList(),
    val studentIds: List<String> = emptyList(),
    val schedule: CourseSchedule = CourseSchedule()
)

data class CourseSchedule(
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val startTime: String = "",
    val endTime: String = "",
    val room: String = "",
    val building: String = "",
    val type: CourseType = CourseType.LECTURE
)


