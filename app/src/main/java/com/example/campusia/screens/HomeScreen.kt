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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.CourseCard
import com.example.campusia.components.MetricCard
import com.example.campusia.entities.Assignment
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
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import com.example.campusia.ui.theme.TextPrimary

private data class HomeUpcomingAssignment(
    val assignment: Assignment,
    val courseTitle: String,
    val dueDateText: String
)

private data class HomeSubmissionToCheck(
    val assignmentId: String,
    val assignmentTitle: String,
    val courseTitle: String,
    val studentEmail: String,
    val submittedAtText: String,
    val isLate: Boolean
)

@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val role = SessionManager.userRole
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var user by remember {
        mutableStateOf(Firebase.auth.currentUser)
    }

    var courses by remember {
        mutableStateOf<List<Course>>(emptyList())
    }

    var username by remember {
        mutableStateOf("")
    }

    var totalStudents by remember {
        mutableStateOf(0)
    }

    var totalLecturers by remember {
        mutableStateOf(0)
    }

    var totalDepartments by remember {
        mutableStateOf(0)
    }

    var upcomingAssignments by remember {
        mutableStateOf<List<HomeUpcomingAssignment>>(emptyList())
    }

    var submissionsToCheck by remember {
        mutableStateOf<List<HomeSubmissionToCheck>>(emptyList())
    }

    var showUpcomingAssignmentsDialog by remember {
        mutableStateOf(false)
    }

    var showSubmissionsDialog by remember {
        mutableStateOf(false)
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
                        val first =
                            documentSnapshot.getString("firstName") ?: ""

                        val last =
                            documentSnapshot.getString("lastName") ?: ""

                        if (first.isNotBlank() || last.isNotBlank()) {
                            username = "$first $last".trim()
                        }
                    }
                }
        }
    }

    DisposableEffect(Unit) {
        val currentUserId =
            FirebaseAuth.getInstance().currentUser?.uid

        val query = when (role) {
            UserRole.LECTURER -> {
                db.collection("courses")
                    .whereArrayContains(
                        "lecturerIds",
                        currentUserId ?: ""
                    )
            }

            UserRole.STUDENT -> {
                db.collection("courses")
                    .whereArrayContains(
                        "studentIds",
                        currentUserId ?: ""
                    )
            }

            UserRole.ADMIN -> {
                db.collection("courses")
            }
        }

        val listenerRegistration =
            query.addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(
                        context,
                        "Error fetching data",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    courses =
                        snapshot.toObjects(Course::class.java)
                }
            }

        onDispose {
            listenerRegistration.remove()
        }
    }

    DisposableEffect(Unit) {
        val usersListener =
            db.collection("users")
                .addSnapshotListener { snapshot, _ ->

                    val users =
                        snapshot?.documents ?: emptyList()

                    totalStudents =
                        users.count { document ->
                            document
                                .getString("role")
                                .orEmpty()
                                .trim()
                                .lowercase() == "student"
                        }

                    totalLecturers =
                        users.count { document ->
                            document
                                .getString("role")
                                .orEmpty()
                                .trim()
                                .lowercase() == "lecturer"
                        }
                }

        val departmentsListener =
            db.collection("departments")
                .addSnapshotListener { snapshot, _ ->
                    totalDepartments =
                        snapshot?.documents?.size ?: 0
                }

        onDispose {
            usersListener.remove()
            departmentsListener.remove()
        }
    }

    LaunchedEffect(courses, role) {
        loadHomeAssignmentsData(
            db = db,
            role = role,
            courses = courses,
            onUpcomingAssignmentsLoaded = {
                upcomingAssignments = it
            },
            onSubmissionsToCheckLoaded = {
                submissionsToCheck = it
            }
        )
    }

    if (showUpcomingAssignmentsDialog) {
        UpcomingAssignmentsDialog(
            assignments = upcomingAssignments,
            onDismiss = {
                showUpcomingAssignmentsDialog = false
            },
            onAssignmentClick = { assignmentId ->
                showUpcomingAssignmentsDialog = false

                navController.navigate(
                    "assignment_details/$assignmentId"
                )
            }
        )
    }

    if (showSubmissionsDialog) {
        SubmissionsToCheckDialog(
            submissions = submissionsToCheck,
            onDismiss = {
                showSubmissionsDialog = false
            },
            onSubmissionClick = { assignmentId ->
                showSubmissionsDialog = false

                navController.navigate(
                    "assignment_details/$assignmentId"
                )
            }
        )
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

        val visibleUpcomingAssignments =
            upcomingAssignments.take(5)

        val coursesSectionIndex =
            when (role) {
                UserRole.STUDENT -> {
                    3 + visibleUpcomingAssignments.size
                }

                UserRole.LECTURER,
                UserRole.ADMIN -> {
                    3
                }
            }

        LazyColumn(
            state = listState,
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

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = when (role) {
                                UserRole.LECTURER ->
                                    "Manage your courses and engage with students"

                                UserRole.STUDENT ->
                                    "Ready to continue your learning journey?"

                                UserRole.ADMIN ->
                                    "Manage your university platform analytics"
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
                                label = "Total Courses",
                                value = "${courses.size}",
                                icon = Icons.Filled.MenuBook
                            )

                            MetricCard(
                                modifier = Modifier.weight(1f),
                                label = "Students",
                                value = countStudentsInCourses(courses).toString(),
                                icon = Icons.Default.People
                            )

                            MetricCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        showSubmissionsDialog = true
                                    },
                                label = "To check",
                                value = "${submissionsToCheck.size}",
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
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (courses.isNotEmpty()) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(
                                                    coursesSectionIndex
                                                )
                                            }
                                        }
                                    },
                                label = "My Courses",
                                value = "${courses.size}",
                                icon = Icons.Filled.MenuBook
                            )

                            MetricCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        showUpcomingAssignmentsDialog = true
                                    },
                                label = "Pending Tasks",
                                value = "${upcomingAssignments.size}",
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            navController.navigate("courses_screen") {
                                                launchSingleTop = true
                                            }
                                        },
                                    label = "Total Courses",
                                    value = "${courses.size}",
                                    icon = Icons.Filled.MenuBook
                                )
                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Total Students",
                                    value = "$totalStudents",
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
                                    value = "$totalLecturers",
                                    icon = Icons.Default.SupervisorAccount
                                )

                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = "Departments",
                                    value = "$totalDepartments",
                                    icon = Icons.Default.HomeWork
                                )
                            }
                        }
                    }
                }
            }

            if (role == UserRole.STUDENT) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(
                            text = "Upcoming deadlines"
                        )

                        if (upcomingAssignments.isEmpty()) {
                            Text(
                                text = "No upcoming assignment deadlines.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                items(
                    items = visibleUpcomingAssignments,
                    key = {
                        it.assignment.assignmentId
                    }
                ) { upcoming ->
                    UpcomingDeadlineCard(
                        upcomingAssignment = upcoming,
                        onClick = {
                            navController.navigate(
                                "assignment_details/${upcoming.assignment.assignmentId}"
                            )
                        }
                    )
                }
            }

            if (role != UserRole.STUDENT) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(
                            text = "Quick Actions"
                        )

                        when (role) {
                            UserRole.LECTURER -> {
                                QuickActionCard(
                                    title = "Create New Course",
                                    subtitle = "Add a new course to your teaching list",
                                    icon = Icons.Outlined.Add,
                                    onClick = {
                                        navController.navigate(
                                            "course_creation"
                                        )
                                    }
                                )
                            }

                            UserRole.ADMIN -> {
                                QuickActionCard(
                                    title = "Manage Departments",
                                    subtitle = "Create and organize university departments",
                                    icon = Icons.Outlined.Apartment,
                                    onClick = {
                                        navController.navigate(
                                            "departments_screen"
                                        )
                                    }
                                )
                                AdminDateRow()
                            }

                            UserRole.STUDENT -> Unit
                        }
                    }
                }
            }

            if (role != UserRole.ADMIN && courses.isNotEmpty()) {
                item {
                    SectionTitle(
                        text = "Your courses"
                    )
                }

                items(
                    items = courses,
                    key = {
                        it.courseId
                    }
                ) { course ->
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
            .clickable {
                onClick()
            },
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

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun UpcomingDeadlineCard(
    upcomingAssignment: HomeUpcomingAssignment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
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
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = upcomingAssignment.assignment.title.ifBlank {
                        "Untitled assignment"
                    },
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = upcomingAssignment.courseTitle,
                    color = TextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = "Due: ${upcomingAssignment.dueDateText}",
                    color = PrimaryPurpleDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UpcomingAssignmentsDialog(
    assignments: List<HomeUpcomingAssignment>,
    onDismiss: () -> Unit,
    onAssignmentClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Upcoming deadlines",
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (assignments.isEmpty()) {
                Text(
                    text = "No upcoming assignment deadlines.",
                    color = TextMuted
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = assignments,
                        key = {
                            it.assignment.assignmentId
                        }
                    ) { item ->
                        DialogListItem(
                            title = item.assignment.title.ifBlank {
                                "Untitled assignment"
                            },
                            subtitle =
                                "${item.courseTitle}\nDue: ${item.dueDateText}",
                            onClick = {
                                onAssignmentClick(
                                    item.assignment.assignmentId
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = PrimaryPurple
                )
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White
    )
}

@Composable
private fun SubmissionsToCheckDialog(
    submissions: List<HomeSubmissionToCheck>,
    onDismiss: () -> Unit,
    onSubmissionClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Assignments to check",
                color = TextDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (submissions.isEmpty()) {
                Text(
                    text = "No submissions waiting for grading.",
                    color = TextMuted
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = submissions,
                        key = {
                            "${it.assignmentId}_${it.studentEmail}"
                        }
                    ) { submission ->

                        val lateText =
                            if (submission.isLate) {
                                "\nLate submission"
                            } else {
                                ""
                            }

                        DialogListItem(
                            title = submission.assignmentTitle,
                            subtitle =
                                "${submission.courseTitle}\n${submission.studentEmail}\nSubmitted: ${submission.submittedAtText}$lateText",
                            onClick = {
                                onSubmissionClick(
                                    submission.assignmentId
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = PrimaryPurple
                )
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White
    )
}

@Composable
private fun DialogListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScreenBackground
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(24.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AdminDateRow() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var dateInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("academicYearStart").document("current").get()
            .addOnSuccessListener { document ->
                val dateFromDb = document.getString("date")
                if (!dateFromDb.isNullOrBlank()) {
                    dateInput = dateFromDb
                }
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Change Term Start Date (YYYYMMDD)",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { dateInput },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = FieldBorder,
                        focusedLabelColor = PrimaryPurple
                    )
                )

                Button(
                    onClick = {
                        if (dateInput.length == 8 && dateInput.all { it.isDigit() }) {
                            db.collection("academicYearStart").document("current")
                                .set(mapOf("date" to dateInput))
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Use YYYYMMDD!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun loadHomeAssignmentsData(
    db: FirebaseFirestore,
    role: UserRole,
    courses: List<Course>,
    onUpcomingAssignmentsLoaded: (List<HomeUpcomingAssignment>) -> Unit,
    onSubmissionsToCheckLoaded: (List<HomeSubmissionToCheck>) -> Unit
) {
    val courseIds =
        courses.map {
            it.courseId
        }.filter {
            it.isNotBlank()
        }

    if (courseIds.isEmpty() && role != UserRole.ADMIN) {
        onUpcomingAssignmentsLoaded(emptyList())
        onSubmissionsToCheckLoaded(emptyList())
        return
    }

    val courseTitleById =
        courses.associate {
            it.courseId to it.title.ifBlank {
                "Untitled course"
            }
        }

    db.collection("assignments")
        .get()
        .addOnSuccessListener { snapshot ->

            val allAssignments =
                snapshot.toObjects(Assignment::class.java)

            val visibleAssignments =
                if (role == UserRole.ADMIN) {
                    allAssignments
                } else {
                    allAssignments.filter {
                        it.courseId in courseIds
                    }
                }

            if (role == UserRole.STUDENT) {
                val now =
                    System.currentTimeMillis()

                val sevenDaysFromNow =
                    now + 7L * 24L * 60L * 60L * 1000L

                val dateFormatter =
                    SimpleDateFormat(
                        "MM/dd/yyyy HH:mm",
                        Locale.getDefault()
                    )

                val upcoming =
                    visibleAssignments
                        .filter { assignment ->
                            val dueMillis =
                                assignment
                                    .dueDate
                                    ?.toDate()
                                    ?.time

                            dueMillis != null &&
                                    dueMillis >= now &&
                                    dueMillis <= sevenDaysFromNow
                        }
                        .sortedBy {
                            it.dueDate?.toDate()?.time
                                ?: Long.MAX_VALUE
                        }
                        .map { assignment ->
                            HomeUpcomingAssignment(
                                assignment = assignment,
                                courseTitle =
                                    courseTitleById[assignment.courseId]
                                        ?: "Course",
                                dueDateText =
                                    assignment
                                        .dueDate
                                        ?.toDate()
                                        ?.let {
                                            dateFormatter.format(it)
                                        }
                                        ?: "No due date"
                            )
                        }

                onUpcomingAssignmentsLoaded(upcoming)
                onSubmissionsToCheckLoaded(emptyList())
            } else {
                onUpcomingAssignmentsLoaded(emptyList())

                val dateFormatter =
                    SimpleDateFormat(
                        "MM/dd/yyyy HH:mm",
                        Locale.getDefault()
                    )

                loadSubmissionsToCheck(
                    db = db,
                    assignments = visibleAssignments,
                    courseTitleById = courseTitleById,
                    dateFormatter = dateFormatter,
                    onLoaded = onSubmissionsToCheckLoaded
                )
            }
        }
        .addOnFailureListener {
            onUpcomingAssignmentsLoaded(emptyList())
            onSubmissionsToCheckLoaded(emptyList())
        }
}

private fun loadSubmissionsToCheck(
    db: FirebaseFirestore,
    assignments: List<Assignment>,
    courseTitleById: Map<String, String>,
    dateFormatter: SimpleDateFormat,
    onLoaded: (List<HomeSubmissionToCheck>) -> Unit
) {
    val assignmentsWithIds =
        assignments.filter {
            it.assignmentId.isNotBlank()
        }

    if (assignmentsWithIds.isEmpty()) {
        onLoaded(emptyList())
        return
    }

    val result =
        mutableListOf<HomeSubmissionToCheck>()

    var completedRequests = 0

    assignmentsWithIds.forEach { assignment ->

        db.collection("assignments")
            .document(assignment.assignmentId)
            .collection("submissions")
            .get()
            .addOnSuccessListener { submissionsSnapshot ->

                submissionsSnapshot.documents.forEach { document ->

                    val gradePercent =
                        document.getLong("gradePercent")

                    val files =
                        document.get("files") as? List<*>

                    val hasFiles =
                        files?.isNotEmpty() == true

                    if (gradePercent == null && hasFiles) {
                        val submittedAt =
                            document
                                .getTimestamp("submittedAt")
                                ?.toDate()

                        result.add(
                            HomeSubmissionToCheck(
                                assignmentId = assignment.assignmentId,
                                assignmentTitle =
                                    assignment.title.ifBlank {
                                        "Untitled assignment"
                                    },
                                courseTitle =
                                    courseTitleById[assignment.courseId]
                                        ?: "Course",
                                studentEmail =
                                    document.getString("studentEmail")
                                        ?: document.id,
                                submittedAtText =
                                    submittedAt?.let {
                                        dateFormatter.format(it)
                                    } ?: "No date",
                                isLate =
                                    document.getBoolean("isLate")
                                        ?: false
                            )
                        )
                    }
                }

                completedRequests++

                if (completedRequests == assignmentsWithIds.size) {
                    onLoaded(
                        result.sortedBy {
                            it.submittedAtText
                        }
                    )
                }
            }
            .addOnFailureListener {

                completedRequests++

                if (completedRequests == assignmentsWithIds.size) {
                    onLoaded(
                        result.sortedBy {
                            it.submittedAtText
                        }
                    )
                }
            }
    }
}

private fun countStudentsInCourses(
    courses: List<Course>
): Int {
    return courses
        .flatMap {
            it.studentIds
        }
        .distinct()
        .size
}