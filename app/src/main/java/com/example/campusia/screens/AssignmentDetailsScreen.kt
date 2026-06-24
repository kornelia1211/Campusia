package com.example.campusia.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.UploadMaterialsCard
import com.example.campusia.entities.Assignment
import com.example.campusia.entities.Course
import com.example.campusia.ui.theme.FieldBackground
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Locale

data class SubmittedAssignmentFile(
    val fileName: String = "",
    val downloadUrl: String = "",
    val storagePath: String = "",
    val uploadedAt: Timestamp? = null
)

data class AssignmentSubmission(
    val studentId: String = "",
    val studentEmail: String = "",
    val submittedAt: Timestamp? = null,
    val dueDate: Timestamp? = null,
    val isLate: Boolean = false,
    val gradePercent: Long? = null,
    val gradedAt: Timestamp? = null,
    val gradedBy: String = "",
    val files: List<SubmittedAssignmentFile> = emptyList()
)

@Composable
fun AssignmentDetailsScreen(
    navController: NavHostController,
    assignmentId: String
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current

    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid
    val currentUserEmail = currentUser?.email ?: ""

    var assignment by remember { mutableStateOf<Assignment?>(null) }
    var userRole by remember { mutableStateOf("student") }
    var studentSubmission by remember { mutableStateOf<AssignmentSubmission?>(null) }
    var allSubmissions by remember { mutableStateOf<List<AssignmentSubmission>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var course by remember { mutableStateOf<Course?>(null) }

    val normalizedUserRole = userRole.trim().lowercase()
    val isStaff = normalizedUserRole == "lecturer" || normalizedUserRole == "admin"
    val isStudent = normalizedUserRole == "student"

    val deadlinePassed = remember(assignment?.dueDate) {
        assignment?.dueDate?.toDate()?.time?.let { dueTime ->
            System.currentTimeMillis() > dueTime
        } ?: false
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->

        if (uris.isEmpty()) return@rememberLauncherForActivityResult

        if (currentUserId == null) {
            Toast.makeText(
                context,
                "You must be logged in to submit files.",
                Toast.LENGTH_SHORT
            ).show()
            return@rememberLauncherForActivityResult
        }

        if (!isStudent) {
            Toast.makeText(
                context,
                "Only students can submit assignment files.",
                Toast.LENGTH_SHORT
            ).show()
            return@rememberLauncherForActivityResult
        }

        uploadStudentSubmissionFiles(
            context = context,
            db = db,
            storage = storage,
            assignmentId = assignmentId,
            studentId = currentUserId,
            studentEmail = currentUserEmail,
            dueDate = assignment?.dueDate,
            fileUris = uris,
            onUploadingChange = {
                isUploading = it
            },
            onSuccess = { wasLate ->
                val message = if (wasLate) {
                    "Files submitted after deadline. Submission marked as late."
                } else {
                    "Files submitted successfully."
                }

                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            },
            onError = { message ->
                Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    DisposableEffect(assignmentId) {
        val assignmentListener =
            db.collection("assignments")
                .document(assignmentId)
                .addSnapshotListener { snapshot, exception ->

                    if (exception != null) {
                        Toast.makeText(
                            context,
                            "Assignment loading error: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    assignment =
                        snapshot?.toObject(Assignment::class.java)

                    val courseId = assignment?.courseId
                    if (!courseId.isNullOrBlank() && course == null) {
                        db.collection("courses")
                            .document(courseId)
                            .get()
                            .addOnSuccessListener { courseSnapshot ->
                                course = courseSnapshot.toObject(Course::class.java)
                            }
                    }
                }

        onDispose {
            assignmentListener.remove()
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    userRole = document.getString("role") ?: "student"
                }
                .addOnFailureListener {
                    userRole = "student"
                }
        }
    }

    DisposableEffect(assignmentId, currentUserId, normalizedUserRole) {
        if (currentUserId == null) {
            onDispose { }
        } else {
            val listener: ListenerRegistration = if (isStaff) {
                db.collection("assignments")
                    .document(assignmentId)
                    .collection("submissions")
                    .addSnapshotListener { snapshot, _ ->
                        allSubmissions = snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.toObject(AssignmentSubmission::class.java)
                            }
                            ?: emptyList()
                    }
            } else {
                db.collection("assignments")
                    .document(assignmentId)
                    .collection("submissions")
                    .document(currentUserId)
                    .addSnapshotListener { snapshot, _ ->
                        studentSubmission =
                            snapshot?.toObject(AssignmentSubmission::class.java)
                    }
            }

            onDispose {
                listener.remove()
            }
        }
    }

    val formattedDate = remember(assignment?.dueDate) {
        assignment?.dueDate?.toDate()?.let { date ->
            SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).format(date)
        } ?: "No due date"
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "assignments"
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                        .padding(20.dp)
                ) {
                    Column {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.20f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = assignment?.title ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White
                            )

                            Spacer(Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Due date",
                                    color = Color.White.copy(alpha = .8f)
                                )

                                Text(
                                    text = formattedDate,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        if (deadlinePassed) {
                            Spacer(Modifier.height(16.dp))

                            LateBadge(
                                text = "Deadline has passed. Submissions are still allowed, but will be marked as late."
                            )
                        }
                    }
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
                            text = "Task Description",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(FieldBackground)
                                .border(
                                    1.dp,
                                    FieldBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(18.dp)
                        ) {
                            Row {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = PrimaryPurpleDark
                                )

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    text = assignment?.description ?: "",
                                    color = TextMuted
                                )
                            }
                        }
                    }
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
                            text = "Assignment Materials",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(
                            modifier = Modifier.height(18.dp)
                        )

                        if (assignment?.materials.isNullOrEmpty()) {
                            Text(
                                text = "No files uploaded by lecturer yet.",
                                color = TextMuted
                            )
                        } else {
                            assignment?.materials?.forEach { material ->

                                UploadMaterialsCard(
                                    title = material.fileName,
                                    subtitle = "Tap to open file",
                                    onClick = {
                                        openFileUrl(
                                            context = context,
                                            url = material.downloadUrl
                                        )
                                    }
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isStudent) {
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
                                text = "Submit Your Assignment",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(14.dp))

                            if (deadlinePassed) {
                                LateBadge(
                                    text = "Deadline has passed. You can still submit files, but lecturer and admin will see that this submission is late."
                                )
                            } else {
                                Text(
                                    text = "Attach files for this assignment. Only you, lecturer and admin can see your submission.",
                                    color = TextMuted
                                )
                            }

                            Spacer(Modifier.height(18.dp))

                            UploadMaterialsCard(
                                title = if (isUploading) {
                                    "Uploading files..."
                                } else {
                                    "Upload Submission Files"
                                },
                                subtitle = if (isUploading) {
                                    "Please wait until upload is finished"
                                } else {
                                    "Tap to choose files from your device"
                                },
                                onClick = {
                                    if (!isUploading) {
                                        filePickerLauncher.launch("*/*")
                                    }
                                }
                            )

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = "Your Submitted Files",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(12.dp))

                            val isStudentSubmissionLate = studentSubmission?.let { submission ->
                                isSubmissionLate(
                                    submission = submission,
                                    assignmentDueDate = assignment?.dueDate
                                )
                            } == true

                            if (isStudentSubmissionLate) {
                                LateBadge(
                                    text = "Your submitted assignment is marked as late."
                                )

                                Spacer(Modifier.height(12.dp))
                            }

                            val studentGrade = studentSubmission?.gradePercent

                            if (studentGrade != null) {
                                GradeBadge(
                                    text = "Graded: $studentGrade%"
                                )

                                Spacer(Modifier.height(12.dp))
                            } else if (studentSubmission != null) {
                                Text(
                                    text = "Not graded yet.",
                                    color = TextMuted
                                )

                                Spacer(Modifier.height(12.dp))
                            }

                            val files = studentSubmission?.files ?: emptyList()

                            if (files.isEmpty()) {
                                Text(
                                    text = "You have not submitted any files yet.",
                                    color = TextMuted
                                )
                            } else {
                                files.forEach { file ->

                                    SubmittedFileItem(
                                        fileName = file.fileName,
                                        subtitle = "Tap to open submitted file",
                                        onClick = {
                                            openFileUrl(
                                                context = context,
                                                url = file.downloadUrl
                                            )
                                        }
                                    )

                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (isStaff) {
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
                                text = "Student Submissions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(18.dp))

                            val submissionsCount = assignment?.submittedBy?.size ?: 0
                            val totalStudents = course?.studentIds?.size ?: 0

                            Text(
                                text = "Total submissions: $submissionsCount / $totalStudents",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryPurpleDark,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(18.dp))

                            if (allSubmissions.isEmpty()) {
                                Text(
                                    text = "No student submissions yet.",
                                    color = TextMuted
                                )
                            } else {
                                allSubmissions.forEach { submission ->

                                    StudentSubmissionCard(
                                        submission = submission,
                                        assignmentDueDate = assignment?.dueDate,
                                        onFileClick = { url ->
                                            openFileUrl(
                                                context = context,
                                                url = url
                                            )
                                        },
                                        onGradeSave = { gradedSubmission, grade ->
                                            saveSubmissionGrade(
                                                context = context,
                                                db = db,
                                                assignmentId = assignmentId,
                                                studentId = gradedSubmission.studentId,
                                                gradePercent = grade,
                                                graderId = currentUserId.orEmpty()
                                            )
                                        }
                                    )

                                    Spacer(Modifier.height(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isSubmissionLate(
    submission: AssignmentSubmission,
    assignmentDueDate: Timestamp?
): Boolean {
    if (submission.isLate) {
        return true
    }

    val submittedTime = submission.submittedAt
        ?.toDate()
        ?.time
        ?: return false

    val dueTime = submission.dueDate
        ?.toDate()
        ?.time
        ?: assignmentDueDate
            ?.toDate()
            ?.time
        ?: return false

    return submittedTime > dueTime
}
@Composable
private fun LateBadge(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFE8E8))
            .border(
                width = 1.dp,
                color = Color(0xFFD9534F),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFB00020),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GradeBadge(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryPurple.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = PrimaryPurple.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = text,
            color = PrimaryPurpleDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SubmittedFileItem(
    fileName: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryPurple.copy(alpha = .08f)
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
                        Color.White,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = PrimaryPurpleDark
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StudentSubmissionCard(
    submission: AssignmentSubmission,
    assignmentDueDate: Timestamp?,
    onFileClick: (String) -> Unit,
    onGradeSave: (AssignmentSubmission, Int) -> Unit
) {
    val formattedSubmittedDate = remember(submission.submittedAt) {
        submission.submittedAt?.toDate()?.let { date ->
            SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).format(date)
        } ?: "No submission date"
    }

    val submissionIsLate = remember(
        submission.isLate,
        submission.submittedAt,
        submission.dueDate,
        assignmentDueDate
    ) {
        isSubmissionLate(
            submission = submission,
            assignmentDueDate = assignmentDueDate
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = .75f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = submission.studentEmail.ifBlank {
                    submission.studentId
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Submitted: $formattedSubmittedDate",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )

            if (submissionIsLate) {
                Spacer(Modifier.height(12.dp))

                LateBadge(
                    text = "Submitted after deadline"
                )
            }

            Spacer(Modifier.height(14.dp))

            GradeEditor(
                submission = submission,
                onGradeSave = onGradeSave
            )

            Spacer(Modifier.height(14.dp))

            if (submission.files.isEmpty()) {
                Text(
                    text = "No files in this submission.",
                    color = TextMuted
                )
            } else {
                submission.files.forEach { file ->

                    SubmittedFileItem(
                        fileName = file.fileName,
                        subtitle = "Tap to open file",
                        onClick = {
                            onFileClick(file.downloadUrl)
                        }
                    )

                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun GradeEditor(
    submission: AssignmentSubmission,
    onGradeSave: (AssignmentSubmission, Int) -> Unit
) {
    var gradeText by remember(submission.gradePercent) {
        mutableStateOf(
            submission.gradePercent?.toString() ?: ""
        )
    }

    var errorText by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = if (submission.gradePercent != null) {
                "Grade: ${submission.gradePercent}%"
            } else {
                "Grade: not added yet"
            },
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = gradeText,
                onValueChange = { value ->
                    gradeText = value
                        .filter { it.isDigit() }
                        .take(3)
                    errorText = null
                },
                label = {
                    Text("Grade %")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    val grade = gradeText.toIntOrNull()

                    if (grade == null || grade !in 0..100) {
                        errorText = "Enter 0-100"
                    } else {
                        errorText = null
                        onGradeSave(submission, grade)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurpleDark,
                    contentColor = Color.White
                )
            ) {
                Text("Save")
            }
        }

        errorText?.let { message ->
            Spacer(Modifier.height(6.dp))

            Text(
                text = message,
                color = Color(0xFFB00020),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun uploadStudentSubmissionFiles(
    context: Context,
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    assignmentId: String,
    studentId: String,
    studentEmail: String,
    dueDate: Timestamp?,
    fileUris: List<Uri>,
    onUploadingChange: (Boolean) -> Unit,
    onSuccess: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    onUploadingChange(true)

    val isLate = dueDate?.toDate()?.time?.let { dueTime ->
        System.currentTimeMillis() > dueTime
    } ?: false

    val uploadedFiles = mutableListOf<Map<String, Any?>>()
    var completedUploads = 0
    var hasError = false

    fileUris.forEach { uri ->

        val originalFileName = getFileNameFromUri(
            context = context,
            uri = uri
        )

        val safeFileName = originalFileName
            .replace(" ", "_")
            .replace("/", "_")
            .replace("\\", "_")

        val storagePath =
            "assignment_submissions/$assignmentId/$studentId/${System.currentTimeMillis()}_$safeFileName"

        val fileRef = storage.reference.child(storagePath)

        fileRef.putFile(uri)
            .addOnSuccessListener {

                fileRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->

                        uploadedFiles.add(
                            mapOf(
                                "fileName" to originalFileName,
                                "downloadUrl" to downloadUri.toString(),
                                "storagePath" to storagePath,
                                "uploadedAt" to Timestamp.now()
                            )
                        )

                        completedUploads++

                        if (completedUploads == fileUris.size && !hasError) {
                            saveSubmissionToFirestore(
                                db = db,
                                assignmentId = assignmentId,
                                studentId = studentId,
                                studentEmail = studentEmail,
                                dueDate = dueDate,
                                isLate = isLate,
                                uploadedFiles = uploadedFiles,
                                onUploadingChange = onUploadingChange,
                                onSuccess = onSuccess,
                                onError = onError
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (!hasError) {
                            hasError = true
                            onUploadingChange(false)
                            onError(
                                exception.message
                                    ?: "Could not get file download URL."
                            )
                        }
                    }
            }
            .addOnFailureListener { exception ->
                if (!hasError) {
                    hasError = true
                    onUploadingChange(false)
                    onError(
                        exception.message
                            ?: "File upload failed."
                    )
                }
            }
    }
}

private fun saveSubmissionToFirestore(
    db: FirebaseFirestore,
    assignmentId: String,
    studentId: String,
    studentEmail: String,
    dueDate: Timestamp?,
    isLate: Boolean,
    uploadedFiles: List<Map<String, Any?>>,
    onUploadingChange: (Boolean) -> Unit,
    onSuccess: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    val submittedAt = Timestamp.now()

    val finalIsLate = dueDate?.toDate()?.time?.let { dueTime ->
        submittedAt.toDate().time > dueTime
    } ?: isLate

    db.collection("assignments")
        .document(assignmentId)
        .collection("submissions")
        .document(studentId)
        .set(
            mapOf(
                "studentId" to studentId,
                "studentEmail" to studentEmail,
                "submittedAt" to submittedAt,
                "dueDate" to dueDate,
                "isLate" to finalIsLate,
                "files" to FieldValue.arrayUnion(
                    *uploadedFiles.toTypedArray()
                )
            ),
            SetOptions.merge()
        )
        .addOnSuccessListener {
            db.collection("assignments")
                .document(assignmentId)
                .update("submittedBy", FieldValue.arrayUnion(studentId))
                .addOnSuccessListener {
                    onUploadingChange(false)
                    onSuccess(finalIsLate)
                }
                .addOnFailureListener { exception ->
                    onUploadingChange(false)
                    onSuccess(finalIsLate)
                }
        }
        .addOnFailureListener { exception ->
            onUploadingChange(false)
            onError(
                exception.message
                    ?: "Could not save submission."
            )
        }
}

private fun saveSubmissionGrade(
    context: Context,
    db: FirebaseFirestore,
    assignmentId: String,
    studentId: String,
    gradePercent: Int,
    graderId: String
) {
    if (studentId.isBlank()) {
        Toast.makeText(
            context,
            "Student id is missing.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    db.collection("assignments")
        .document(assignmentId)
        .collection("submissions")
        .document(studentId)
        .set(
            mapOf(
                "gradePercent" to gradePercent,
                "gradedAt" to Timestamp.now(),
                "gradedBy" to graderId
            ),
            SetOptions.merge()
        )
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Grade saved.",
                Toast.LENGTH_SHORT
            ).show()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                context,
                exception.message ?: "Could not save grade.",
                Toast.LENGTH_LONG
            ).show()
        }
}

private fun getFileNameFromUri(
    context: Context,
    uri: Uri
): String {
    var fileName = "file"

    val cursor = context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(
            OpenableColumns.DISPLAY_NAME
        )

        if (nameIndex >= 0 && it.moveToFirst()) {
            fileName = it.getString(nameIndex) ?: "file"
        }
    }

    return fileName
}

private fun openFileUrl(
    context: Context,
    url: String
) {
    if (url.isBlank()) {
        Toast.makeText(
            context,
            "File URL is empty.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url)
    )

    context.startActivity(intent)
}