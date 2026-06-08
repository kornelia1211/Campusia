package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.campusia.SessionManager
import com.example.campusia.components.DropdownSelector
import com.example.campusia.entities.Course
import com.example.campusia.entities.CourseFrequency
import com.example.campusia.entities.CourseSchedule
import com.example.campusia.entities.CourseType
import com.example.campusia.entities.Department
import com.example.campusia.entities.User
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.CampusiaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DayOfWeek
import com.example.campusia.components.LabeledField
import com.example.campusia.components.StyledInputField
import com.example.campusia.components.TimePickerField
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.BorderColor
import com.example.campusia.ui.theme.TextPrimary

fun DayOfWeek.toDisplayName(): String = when (this) {
    DayOfWeek.MONDAY -> "Monday"
    DayOfWeek.TUESDAY -> "Tuesday"
    DayOfWeek.WEDNESDAY -> "Wednesday"
    DayOfWeek.THURSDAY -> "Thursday"
    DayOfWeek.FRIDAY -> "Friday"
    DayOfWeek.SATURDAY -> "Saturday"
    DayOfWeek.SUNDAY -> "Sunday"
    else -> "Unknown"
}

fun CourseFrequency.toDisplayName(): String = when (this) {
    CourseFrequency.EVERY_WEEK -> "Every week"
    CourseFrequency.EVEN_WEEKS -> "Even weeks"
    CourseFrequency.ODD_WEEKS -> "Odd weeks"
}

fun CourseType.toDisplayName(): String = when (this) {
    CourseType.LECTURE -> "Lecture"
    CourseType.LABORATORY -> "Laboratory"
    CourseType.SEMINAR -> "Seminar"
    CourseType.PROJECT -> "Project"
}

@Composable
fun CourseCreationScreen(
    navController: NavHostController,
    courseId: String? = null
) {
    var courseToEdit by remember {
        mutableStateOf<Course?>(null)
    }

    var title by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.title ?: "")
    }

    var description by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.description ?: "")
    }

    var maxStudents by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.maxStudents?.toString() ?: "")
    }

    var users by remember {
        mutableStateOf<List<User>>(emptyList())
    }

    var departments by remember {
        mutableStateOf<List<Department>>(emptyList())
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val currentUserRole = SessionManager.userRole

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener { result ->
                users = result.documents.mapNotNull {
                    it.toObject(User::class.java)
                }
            }
    }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("departments")
            .get()
            .addOnSuccessListener { result ->
                departments = result.documents.mapNotNull {
                    it.toObject(Department::class.java)
                }
            }
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedLecturers by remember {
        mutableStateOf<List<User>>(emptyList())
    }

    var lecturersInitialized by remember {
        mutableStateOf(false)
    }

    var selectedDepartment by remember(courseToEdit, departments) {
        mutableStateOf(
            departments.firstOrNull {
                it.name == courseToEdit?.department
            }
        )
    }

    var selectedDay by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.schedule?.dayOfWeek)
    }

    var selectedFrequency by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.schedule?.frequency)
    }

    var selectedType by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.schedule?.type)
    }

    var startHour by remember(courseToEdit) {
        mutableStateOf(
            courseToEdit?.schedule?.startTime
                ?.split(":")
                ?.getOrNull(0)
                ?: "08"
        )
    }

    var startMinute by remember(courseToEdit) {
        mutableStateOf(
            courseToEdit?.schedule?.startTime
                ?.split(":")
                ?.getOrNull(1)
                ?: "00"
        )
    }

    var endHour by remember(courseToEdit) {
        mutableStateOf(
            courseToEdit?.schedule?.endTime
                ?.split(":")
                ?.getOrNull(0)
                ?: "10"
        )
    }

    var endMinute by remember(courseToEdit) {
        mutableStateOf(
            courseToEdit?.schedule?.endTime
                ?.split(":")
                ?.getOrNull(1)
                ?: "00"
        )
    }

    var room by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.schedule?.room ?: "")
    }

    var building by remember(courseToEdit) {
        mutableStateOf(courseToEdit?.schedule?.building ?: "")
    }

    val context = LocalContext.current

    val days = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    LaunchedEffect(courseId) {
        if (courseId != null) {
            FirebaseFirestore.getInstance()
                .collection("courses")
                .document(courseId)
                .get()
                .addOnSuccessListener { document ->
                    courseToEdit = document.toObject(Course::class.java)
                }
        }
    }

    LaunchedEffect(courseToEdit, users) {
        if (
            !lecturersInitialized &&
            courseToEdit != null &&
            users.isNotEmpty()
        ) {
            val course = courseToEdit ?: return@LaunchedEffect

            selectedLecturers = users.filter {
                it.userId in course.lecturerIds && it.userId != currentUserId
            }

            lecturersInitialized = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopIntroCard(
            onBackClick = {
                navController.navigate("courses_screen") {
                    popUpTo("courses_screen") {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            isEditMode = courseId != null
        )

        FormSectionCard(title = "Basic Information") {
            LabeledField(label = "Title", icon = Icons.Outlined.MenuBook)

            StyledInputField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Introduction to Programming"
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Description", icon = Icons.Outlined.Subject)

            StyledInputField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Course about basics of programming languages"
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Department", icon = Icons.Outlined.Apartment)

            DropdownSelector(
                selectedValue = selectedDepartment?.name ?: "",
                placeholder = "Select department",
                items = departments.map { it.name },
                onItemSelected = { chosen ->
                    selectedDepartment =
                        departments.firstOrNull() { it.name == chosen }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Max Students", icon = Icons.Outlined.Groups)

            StyledInputField(
                value = maxStudents,
                onValueChange = { maxStudents = it.filter { char -> char.isDigit() } },
                placeholder = "30",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        FormSectionCard(title = "Schedule") {
            LabeledField(label = "Day of week", icon = Icons.Outlined.CalendarToday)

            DropdownSelector(
                selectedValue = selectedDay?.toDisplayName() ?: "",
                placeholder = "Select day of week",
                items = days.map { it.toDisplayName() },
                onItemSelected = { chosen ->
                    selectedDay = days.firstOrNull { it.toDisplayName() == chosen }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Frequency", icon = Icons.Outlined.EditCalendar)

            DropdownSelector(
                selectedValue = selectedFrequency?.toDisplayName() ?: "",
                placeholder = "Select frequency",
                items = CourseFrequency.entries.map { it.toDisplayName() },
                onItemSelected = { chosen ->
                    selectedFrequency =
                        CourseFrequency.entries.firstOrNull { it.toDisplayName() == chosen }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Course Type", icon = Icons.Outlined.Badge)

            DropdownSelector(
                selectedValue = selectedType?.toDisplayName() ?: "",
                placeholder = "Select type",
                items = CourseType.entries.map { it.toDisplayName() },
                onItemSelected = { chosen ->
                    selectedType =
                        CourseType.entries.firstOrNull { it.toDisplayName() == chosen }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Start time", icon = Icons.Outlined.Schedule)

            TimePickerField(
                selectedHour = startHour,
                selectedMinute = startMinute,
                onTimeSelected = { hour, minute ->
                    startHour = hour
                    startMinute = minute
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "End time", icon = Icons.Outlined.Schedule)

            TimePickerField(
                selectedHour = endHour,
                selectedMinute = endMinute,
                onTimeSelected = { hour, minute ->
                    endHour = hour
                    endMinute = minute
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Room", icon = Icons.Outlined.MeetingRoom)

            StyledInputField(
                value = room,
                onValueChange = { room = it },
                placeholder = "302"
            )

            Spacer(modifier = Modifier.height(14.dp))

            LabeledField(label = "Building", icon = Icons.Outlined.Apartment)

            StyledInputField(
                value = building,
                onValueChange = { building = it },
                placeholder = "Building B"
            )
        }

        val filteredLecturers = remember(users, searchQuery, currentUserRole) {
            users.filter { user ->
                user.role == "Lecturer" &&
                        (currentUserRole == UserRole.ADMIN || user.userId != currentUserId) &&
                        (user.firstName.contains(searchQuery, ignoreCase = true) ||
                                user.lastName.contains(searchQuery, ignoreCase = true))
            }
        }

        FormSectionCard(title = "Lecturers") {
            StyledInputField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search for other lecturers"
            )

            Spacer(modifier = Modifier.height(12.dp))

            filteredLecturers.take(5).forEach { lecturer ->
                val alreadySelected = selectedLecturers.any {
                    it.userId == lecturer.userId
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (alreadySelected) {
                                selectedLecturers =
                                    selectedLecturers.filter { it.userId != lecturer.userId }
                            } else if (selectedLecturers.size < 3) {
                                selectedLecturers = selectedLecturers + lecturer
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${lecturer.firstName} ${lecturer.lastName}",
                        modifier = Modifier.weight(1f),
                        color = if (alreadySelected) PrimaryPurpleDark else TextPrimary,
                        fontWeight = if (alreadySelected) FontWeight.Bold else FontWeight.Normal
                    )

                    if (alreadySelected) {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = null,
                            tint = PrimaryPurpleDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val maxStudentsInt = maxStudents.toIntOrNull()

                if (title.isBlank() || description.isBlank()) {
                    Toast.makeText(
                        context,
                        "Please fill in title and description",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (selectedDepartment == null) {
                    Toast.makeText(
                        context,
                        "Please select a department",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (maxStudentsInt == null || maxStudentsInt <= 0) {
                    Toast.makeText(
                        context,
                        "Please enter a valid number of students",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (selectedDay == null) {
                    Toast.makeText(
                        context,
                        "Please select a day",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (selectedFrequency == null) {
                    Toast.makeText(
                        context,
                        "Please select frequency",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (selectedType == null) {
                    Toast.makeText(
                        context,
                        "Please select course type",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val startHourInt = startHour.toIntOrNull()
                val startMinuteInt = startMinute.toIntOrNull()
                val endHourInt = endHour.toIntOrNull()
                val endMinuteInt = endMinute.toIntOrNull()

                if (
                    startHourInt == null ||
                    startMinuteInt == null ||
                    endHourInt == null ||
                    endMinuteInt == null
                ) {
                    Toast.makeText(
                        context,
                        "Please select valid start and end times",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val startTotalMinutes = startHourInt * 60 + startMinuteInt
                val endTotalMinutes = endHourInt * 60 + endMinuteInt

                if (endTotalMinutes <= startTotalMinutes) {
                    Toast.makeText(
                        context,
                        "End time must be later than start time",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (room.isBlank()) {
                    Toast.makeText(
                        context,
                        "Please enter room",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                if (building.isBlank()) {
                    Toast.makeText(
                        context,
                        "Please enter building",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val lecturersList = selectedLecturers.map { it.userId }

                val scheduleObject = CourseSchedule(
                    dayOfWeek = selectedDay!!,
                    frequency = selectedFrequency!!,
                    startTime = "$startHour:$startMinute",
                    endTime = "$endHour:$endMinute",
                    room = room,
                    building = building,
                    type = selectedType!!
                )

                if (courseToEdit == null) {
                    createCourse(
                        title = title,
                        description = description,
                        department = selectedDepartment!!.name,
                        schedule = scheduleObject,
                        maxStudents = maxStudentsInt,
                        providedLecturerIds = lecturersList,
                        context = context,
                        onSuccess = {
                            navController.navigate("courses_screen")
                        }
                    )
                } else {
                    val editingCourse = courseToEdit ?: return@Button

                    updateCourse(
                        courseId = editingCourse.courseId,
                        title = title,
                        description = description,
                        department = selectedDepartment!!.name,
                        schedule = scheduleObject,
                        maxStudents = maxStudentsInt,
                        lecturerIds = lecturersList,
                        studentIds = editingCourse.studentIds,
                        enrolledStudents = editingCourse.enrolledStudents,
                        context = context,
                        onSuccess = {
                            navController.navigate("courses_screen")
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = if (courseToEdit == null) {
                    "Create Course"
                } else {
                    "Save Changes"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TopIntroCard(
    onBackClick: () -> Unit,
    isEditMode: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFC4B5FD),
                        Color(0xFFA78BFA),
                        Color(0xFF9333EA)
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
                text = if (isEditMode) {
                    "Edit Course"
                } else {
                    "Create New Course"
                },
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEditMode) {
                    "Update the course details below."
                } else {
                    "Fill in the details below to add a new course."
                },
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.campusia.ui.theme.FieldBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}


fun createCourse(
    title: String,
    description: String,
    department: String,
    schedule: CourseSchedule,
    maxStudents: Int,
    providedLecturerIds: List<String> = emptyList(),
    context: Context,
    onSuccess: () -> Unit
) {
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
        Toast.makeText(
            context,
            "Please select at least one lecturer",
            Toast.LENGTH_SHORT
        ).show()
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

    courseRef.set(newCourse)
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Course was successfully created!",
                Toast.LENGTH_SHORT
            ).show()
            onSuccess()
        }
        .addOnFailureListener {
            Toast.makeText(
                context,
                "Failed to create course",
                Toast.LENGTH_SHORT
            ).show()
        }
}

fun updateCourse(
    courseId: String,
    title: String,
    description: String,
    department: String,
    schedule: CourseSchedule,
    maxStudents: Int,
    lecturerIds: List<String>,
    studentIds: List<String>,
    enrolledStudents: Int,
    context: Context,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val updatedCourse = Course(
        courseId = courseId,
        title = title,
        description = description,
        department = department,
        maxStudents = maxStudents,
        lecturerIds = lecturerIds,
        studentIds = studentIds,
        enrolledStudents = enrolledStudents,
        schedule = schedule
    )

    db.collection("courses")
        .document(courseId)
        .set(updatedCourse)
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Course updated successfully",
                Toast.LENGTH_SHORT
            ).show()

            onSuccess()
        }
        .addOnFailureListener {
            Toast.makeText(
                context,
                "Failed to update course",
                Toast.LENGTH_SHORT
            ).show()
        }
}

@Preview(showBackground = true)
@Composable
fun CourseCreationScreenPreview() {
    CampusiaTheme {
        CourseCreationScreen(
            navController = rememberNavController()
        )
    }
}