package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.campusia.SessionManager
import com.example.campusia.entities.Course
import com.example.campusia.entities.CourseFrequency
import com.example.campusia.entities.CourseSchedule
import com.example.campusia.entities.CourseType
import com.example.campusia.entities.Departments
import com.example.campusia.entities.User
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.CampusiaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DayOfWeek

private val ScreenBackground = Color(0xFFF8F7FC)
private val CardBackground = Color.White
private val PrimaryPurple = Color(0xFFA78BFA)
private val PrimaryPurpleDark = Color(0xFF9333EA)
private val SoftPurple = Color(0xFFF3E8FF)
private val BorderColor = Color(0xFFE7E0F4)
private val TextPrimary = Color(0xFF1F2937)
private val PlaceholderColor = Color(0xFF9CA3AF)

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

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedLecturers by remember {
        mutableStateOf<List<User>>(emptyList())
    }

    var selectedDepartment by remember(courseToEdit) {
        mutableStateOf(
            Departments.entries.firstOrNull {
                it.displayName == courseToEdit?.department
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
                selectedValue = selectedDepartment?.displayName ?: "",
                placeholder = "Select department",
                items = Departments.entries.map { it.displayName },
                onItemSelected = { chosen ->
                    selectedDepartment =
                        Departments.entries.firstOrNull { it.displayName == chosen }
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

        val filteredLecturers = remember(users, searchQuery) {
            users.filter { user ->
                user.role == "Lecturer" && (
                        user.firstName.contains(searchQuery, ignoreCase = true) ||
                                user.lastName.contains(searchQuery, ignoreCase = true)
                        )
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
                        department = selectedDepartment!!.displayName,
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
                        department = selectedDepartment!!.displayName,
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
        colors = CardDefaults.cardColors(containerColor = CardBackground),
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

@Composable
private fun LabeledField(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(SoftPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurpleDark,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = label,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StyledInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = PlaceholderColor
            )
        },
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = BorderColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryPurpleDark,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedPlaceholderColor = PlaceholderColor,
            unfocusedPlaceholderColor = PlaceholderColor
        )
    )
}

@Composable
fun DropdownSelector(
    selectedValue: String,
    placeholder: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures {
                        expanded = true
                    }
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedValue.isBlank()) placeholder else selectedValue,
                color = if (selectedValue.isBlank()) PlaceholderColor else TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = PrimaryPurpleDark
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            color = TextPrimary
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SoftPurple else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) PrimaryPurple else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryPurpleDark else TextPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TimePickerField(
    selectedHour: String,
    selectedMinute: String,
    onTimeSelected: (String, String) -> Unit
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showDialog = true
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedHour:$selectedMinute",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = PrimaryPurpleDark
            )
        }
    }

    if (showDialog) {
        TimeSelectionDialog(
            initialHour = selectedHour.toIntOrNull() ?: 8,
            initialMinute = selectedMinute,
            onDismiss = {
                showDialog = false
            },
            onConfirm = { hour, minute ->
                onTimeSelected(
                    hour.toString().padStart(2, '0'),
                    minute
                )
                showDialog = false
            }
        )
    }
}

@Composable
private fun TimeSelectionDialog(
    initialHour: Int,
    initialMinute: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var selectedHour by remember {
        mutableIntStateOf(initialHour.coerceIn(0, 23))
    }

    var selectedMinute by remember {
        mutableStateOf(initialMinute)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedHour, selectedMinute)
                }
            ) {
                Text(
                    text = "Save",
                    color = PrimaryPurpleDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = TextPrimary
                )
            }
        },
        title = {
            Text(
                text = "Select time",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Hour",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                HourSelector(
                    selectedHour = selectedHour,
                    onHourSelected = {
                        selectedHour = it
                    }
                )

                Text(
                    text = "Minutes",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                MinuteSelector(
                    selectedMinute = selectedMinute,
                    onMinuteSelected = {
                        selectedMinute = it
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftPurple)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${selectedHour.toString().padStart(2, '0')}:$selectedMinute",
                        color = PrimaryPurpleDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun HourSelector(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    val hourRows = listOf(
        (0..5).toList(),
        (6..11).toList(),
        (12..17).toList(),
        (18..23).toList()
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hourRows.forEach { rowHours ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowHours.forEach { hour ->
                    TimeOptionChip(
                        text = hour.toString().padStart(2, '0'),
                        selected = selectedHour == hour,
                        onClick = {
                            onHourSelected(hour)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MinuteSelector(
    selectedMinute: String,
    onMinuteSelected: (String) -> Unit
) {
    val minuteOptions = listOf("00", "15", "30", "45")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        minuteOptions.forEach { minute ->
            TimeOptionChip(
                text = minute,
                selected = selectedMinute == minute,
                onClick = {
                    onMinuteSelected(minute)
                },
                modifier = Modifier.weight(1f)
            )
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