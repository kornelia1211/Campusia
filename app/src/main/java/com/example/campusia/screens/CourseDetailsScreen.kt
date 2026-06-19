package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.AlertDialogDelete
import com.example.campusia.components.AssignmentCard
import com.example.campusia.components.RoundedButton
import com.example.campusia.entities.Assignment
import com.example.campusia.entities.Course
import com.example.campusia.entities.User
import com.example.campusia.entities.UserRole
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextPrimary
import com.example.campusia.ui.theme.FieldBackground
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.IconCircle
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.DangerRed
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import com.example.campusia.entities.Announcement
import java.text.SimpleDateFormat
import java.util.Locale


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

    var assignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }

    var announcements by remember {
        mutableStateOf<List<Announcement>>(emptyList())
    }

    var studentToRemove by remember {
        mutableStateOf<User?>(null)
    }

    val role = SessionManager.userRole

    val previewAssignments = assignments.take(3)
    val previewAnnouncements = announcements.take(3)

    DisposableEffect(courseId, role) {

        val courseListener =
            db.collection("courses")
                .document(courseId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Toast.makeText(
                            context,
                            "Course loading error: ${error.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    val fetchedCourse =
                        snapshot?.toObject(Course::class.java)

                    course = fetchedCourse

                    val studentIds =
                        fetchedCourse?.studentIds.orEmpty()

                    if (studentIds.isEmpty()) {
                        students = emptyList()
                    } else {
                        db.collection("users")
                            .whereIn("userId", studentIds)
                            .get()
                            .addOnSuccessListener { result ->
                                students =
                                    result.toObjects(User::class.java)
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    context,
                                    "Students loading error: ${exception.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }

        val assignmentsListener =
            db.collection("assignments")
                .whereEqualTo("courseId", courseId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Toast.makeText(
                            context,
                            "Assignments loading error: ${error.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    val loadedAssignments =
                        snapshot
                            ?.documents
                            ?.mapNotNull { document ->
                                document
                                    .toObject(Assignment::class.java)
                                    ?.copy(
                                        assignmentId =
                                            document.id
                                    )
                            }
                            ?.sortedBy { assignment ->
                                assignment.dueDate
                                    ?.toDate()
                                    ?.time
                                    ?: Long.MAX_VALUE
                            }
                            ?: emptyList()

                    assignments = loadedAssignments
                }

        onDispose {
            courseListener.remove()
            assignmentsListener.remove()
        }
    }

    DisposableEffect(courseId) {
        val listener = FirebaseFirestore.getInstance()
            .collection("announcements")
            .whereEqualTo("courseId", courseId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    announcements = emptyList()
                    return@addSnapshotListener
                }

                announcements = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document
                            .toObject(Announcement::class.java)
                            ?.copy(
                                announcementId = document.id
                            )
                    }
                    ?.sortedByDescending { announcement ->
                        announcement.sendTime?.toDate()?.time ?: 0L
                    }
                    ?: emptyList()
            }

        onDispose {
            listener.remove()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (role == UserRole.LECTURER || role == UserRole.ADMIN){
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        text = "Assignment",
                        height = 40.dp,
                        fontSize = 13.sp,
                        iconSize = 17.dp,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        onClick = {
                            navController.navigate("assignment_creation_screen/$courseId")
                        }
                    )
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Campaign,
                        text = "Announce",
                        height = 40.dp,
                        fontSize = 13.sp,
                        iconSize = 17.dp,
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        onClick = {
                            navController.navigate("announcement_creation_screen/$courseId")
                        }
                    )
                }
            }
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
                            tint = PrimaryPurpleDark,
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
                            tint = PrimaryPurpleDark,
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
                            tint = PrimaryPurpleDark,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Department",
                    value = course?.department ?: ""
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        FieldBorder,
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FieldBackground
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Course Announcements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (announcements.isEmpty()) {
                        Text(
                            text = "No announcements yet.",
                            color = TextMuted
                        )
                    } else {
                        previewAnnouncements.forEachIndexed { index, announcement ->
                            CourseAnnouncementItem(
                                announcement = announcement,
                                onClick = {
                                    navController.navigate(
                                        "announcement_details_screen/${announcement.announcementId}"
                                    )
                                }
                            )

                            if (index != previewAnnouncements.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        if (announcements.size > previewAnnouncements.size) {
                            Spacer(modifier = Modifier.height(14.dp))

                            RoundedButton(
                                text = "View all announcements",
                                height = 44.dp,
                                fontSize = 14.sp,
                                onClick = {
                                    navController.navigate(
                                        "course_announcements_screen/$courseId"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "Course Assignments") {
                if (assignments.isEmpty()) {
                    Text(
                        text = "No assignments assigned yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                } else {
                    previewAssignments.forEachIndexed { index, assignment ->
                        CourseStyleAssignmentCard(
                            assignment = assignment,
                            role = role,
                            onClick = {
                                navController.navigate(
                                    "assignment_details/${assignment.assignmentId}"
                                )
                            },
                            onEdit = {
                                navController.navigate(
                                    "edit_assignment/${assignment.assignmentId}/$courseId"
                                )
                            },
                            onDelete = {
                                FirebaseFirestore
                                    .getInstance()
                                    .collection("assignments")
                                    .document(assignment.assignmentId)
                                    .delete()
                                    .addOnSuccessListener {
                                        assignments =
                                            assignments.filterNot {
                                                it.assignmentId == assignment.assignmentId
                                            }
                                    }
                            }
                        )

                        if (index != previewAssignments.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (assignments.size > previewAssignments.size) {
                        Spacer(modifier = Modifier.height(14.dp))

                        RoundedButton(
                            text = "View all assignments",
                            height = 44.dp,
                            fontSize = 14.sp,
                            onClick = {
                                navController.navigate(
                                    "course_assignments_screen/$courseId"
                                )
                            }
                        )
                    }
                }
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
                            tint = PrimaryPurpleDark,
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
                            tint = PrimaryPurpleDark,
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
                            tint = PrimaryPurpleDark,
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
                            tint = PrimaryPurpleDark,
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
                                tint = PrimaryPurpleDark,
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
                        role = role,
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

        //toDo: Add lecturers in this course
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
                    db.collection("chat_rooms").document(courseId)
                        .update("participants", FieldValue.arrayRemove(student.userId))
                        .addOnSuccessListener {
                            Toast.makeText(context, "Student removed from course and chat", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Student removed from course list", Toast.LENGTH_SHORT).show()
                        }
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
                        PrimaryPurple,
                        PrimaryPurpleDark
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
private fun CourseStyleAssignmentCard(
    assignment: Assignment,
    role: UserRole,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDueDate = remember(assignment.dueDate) {
        assignment.dueDate?.toDate()?.let { date ->
            SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).format(date)
        } ?: "No due date"
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = Color(0xFFF2ECFF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
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
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tap for more details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                if (role != UserRole.STUDENT) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = PrimaryPurple.copy(alpha = 0.10f),
                                contentColor = PrimaryPurpleDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit assignment",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = DangerRed.copy(alpha = 0.12f),
                                contentColor = DangerRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete assignment",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryPurpleDark,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Due: $formattedDueDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
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
                color = FieldBorder,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FieldBackground
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
                    color = FieldBorder,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun CourseAnnouncementItem(
    announcement: Announcement,
    onClick: () -> Unit
) {
    val formattedSendTime = remember(announcement.sendTime) {
        announcement.sendTime?.toDate()?.let { date ->
            SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).format(date)
        } ?: "Sending..."
    }

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
                    .size(42.dp)
                    .background(
                        PrimaryPurple.copy(alpha = .12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = PrimaryPurpleDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = announcement.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Tap to view announcement",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = PrimaryPurpleDark,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Sent: $formattedSendTime",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: User,
    role: UserRole,
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
                        tint = PrimaryPurpleDark,
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
                    color = TextMuted
                )
            }

            if (role != UserRole.STUDENT){
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