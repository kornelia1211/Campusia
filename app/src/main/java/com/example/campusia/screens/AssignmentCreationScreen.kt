package com.example.campusia.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.LabeledField
import com.example.campusia.components.RoundedButton
import com.example.campusia.components.StyledInputField
import com.example.campusia.components.TimePickerField
import com.example.campusia.components.UploadMaterialsCard
import com.example.campusia.entities.Assignment
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.campusia.ui.theme.ScreenBackground
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID
import com.example.campusia.entities.AssignmentMaterial

@Composable
fun AssignmentCreationScreen(
    navController: NavHostController,
    courseId: String,
    assignmentId: String? = null
){

    var title by remember{
        mutableStateOf("")
    }

    var description by remember{
        mutableStateOf("")
    }

    val calendar = Calendar.getInstance()

    var selectedDate by remember {
        mutableStateOf("")
    }

    var dueHour by remember {
        mutableStateOf("00")
    }

    var dueMinute by remember{
        mutableStateOf("00")
    }

    val context = LocalContext.current

    var selectedFiles by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    var existingMaterials by remember {
        mutableStateOf(
            emptyList<AssignmentMaterial>()
        )
    }

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                selectedFiles =
                    selectedFiles + uri

                Toast.makeText(
                    context,
                    "File added",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    val isFormValid =
        title.isNotBlank() &&
                description.isNotBlank() &&
                selectedDate.isNotBlank()

    // Edit assignment
    var assignmentToEdit by remember {
        mutableStateOf<Assignment?>(null)
    }

    LaunchedEffect(assignmentId) {

        if (assignmentId != null) {

            FirebaseFirestore.getInstance()
                .collection("assignments")
                .document(assignmentId)
                .get()
                .addOnSuccessListener { document ->

                    assignmentToEdit =
                        document.toObject(
                            Assignment::class.java
                        )

                    existingMaterials =
                        assignmentToEdit
                            ?.materials
                            ?: emptyList()

                    assignmentToEdit?.let {

                        title = it.title
                        description = it.description

                        it.dueDate
                            ?.toDate()
                            ?.let { date ->

                                val formatter =
                                    SimpleDateFormat(
                                        "MM/dd/yyyy",
                                        Locale.getDefault()
                                    )

                                selectedDate =
                                    formatter.format(date)

                                dueHour =
                                    SimpleDateFormat(
                                        "HH",
                                        Locale.getDefault()
                                    ).format(date)

                                dueMinute =
                                    SimpleDateFormat(
                                        "mm",
                                        Locale.getDefault()
                                    ).format(date)
                            }
                    }
                }
        }
    }

    Scaffold(

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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                TopIntroCard(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    isEditMode =
                        assignmentId != null
                )

            LabeledField("Title", icon = Icons.Outlined.MenuBook)

            StyledInputField(
                value = title,
                onValueChange = { title = it },
                placeholder = "List 1 - Functions and classes"
            )

            LabeledField(label = "Description", icon = Icons.Outlined.Subject)

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                placeholder = {
                    Text("Task 1. Create a function which...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(18.dp)
            )

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, day ->

                    selectedDate =
                        String.format(
                            "%02d/%02d/%04d",
                            month + 1,
                            day,
                            year
                        )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            LabeledField(
                label = "Due date",
                icon = Icons.Outlined.DateRange
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                StyledInputField(
                    value = selectedDate,
                    onValueChange = {},
                    placeholder = "Select date"
                )

                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = PrimaryPurpleDark
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            datePickerDialog.show()
                        }
                )
            }

            LabeledField(label = "Due time", icon = Icons.Outlined.Schedule)

            TimePickerField(
                selectedHour = dueHour,
                selectedMinute = dueMinute,
                onTimeSelected = { hour, minute ->
                    dueHour = hour
                    dueMinute = minute
                }
            )

            UploadMaterialsCard(
                onClick = {
                    filePickerLauncher.launch("*/*")
                }
            )

            if (existingMaterials.isNotEmpty()) {

                Text(
                    text = "Current Materials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                existingMaterials.forEach { material ->

                    UploadMaterialsCard(
                        title = material.fileName,
                        subtitle = "Already uploaded",
                        onClick = {

                            val intent =
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    Uri.parse(
                                        material.downloadUrl
                                    )
                                )

                            context.startActivity(
                                intent
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            if (selectedFiles.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Uploaded Files",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                selectedFiles.forEach { file ->

                    UploadMaterialsCard(
                        title =
                            file.lastPathSegment
                                ?: "Selected file",

                        subtitle =
                            "Ready to upload",

                        onClick = {

                            // no-op
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            RoundedButton(
                text =
                    if (assignmentId == null)
                        "Create Assignment"
                    else
                        "Save Changes",
                enabled = isFormValid,
                onClick = {
                    if (assignmentId == null) {
                        createAssignment(
                            courseId = courseId,
                            title = title,
                            description = description,
                            selectedDate = selectedDate,
                            dueHour = dueHour,
                            dueMinute = dueMinute,
                            context = context,
                            onSuccess = {
                                navController.popBackStack()
                            },
                            selectedFiles = selectedFiles
                        )
                    } else {

                        updateAssignment(
                            assignmentId = assignmentId,
                            title = title,
                            description = description,
                            selectedDate = selectedDate,
                            dueHour = dueHour,
                            dueMinute = dueMinute,
                            context = context,
                            selectedFiles = selectedFiles,
                            onSuccess = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
fun createAssignment(
    courseId: String,
    title: String,
    description: String,
    selectedDate: String,
    dueHour: String,
    dueMinute: String,
    context: Context,
    selectedFiles: List<Uri>,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val dueDateString =
        "$selectedDate $dueHour:$dueMinute"

    val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val parsedDate = formatter.parse(dueDateString) ?: throw Exception("Parsing failed")
    val firebaseTimestamp = Timestamp(parsedDate)


    val assignmentRef =
        db.collection("assignments")
            .document()

    val assignmentId =
        assignmentRef.id

    val newAssignment = Assignment(
        assignmentId = assignmentId,
        courseId = courseId,
        title = title,
        description = description,
        dueDate = firebaseTimestamp
    )

    assignmentRef
        .set(newAssignment)
        .addOnSuccessListener {

            uploadFilesForAssignment(
                assignmentId = assignmentId,
                files = selectedFiles,
                context = context
            ) {

                Toast.makeText(
                    context,
                    "Assignment was successfully created!",
                    Toast.LENGTH_SHORT
                ).show()

                onSuccess()
            }

        }
        .addOnFailureListener { exception ->

            Toast.makeText(
                context,
                exception.message,
                Toast.LENGTH_LONG
            ).show()
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
                    "Edit Assignment"
                } else {
                    "Create New Assignment"
                },
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEditMode) {
                    "Update the assignment details below."
                } else {
                    "Fill in the details below to add a new assignment."
                },
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


fun uploadFile(
    uri: Uri,
    courseId: String,
    context: Context
) {

    val storage =
        FirebaseStorage.getInstance()

    val storageRef =
        storage.reference

    val fileName =
        UUID.randomUUID().toString()

    val fileRef =
        storageRef.child(
            "assignments/$courseId/materials/$fileName"
        )

    fileRef
        .putFile(uri)

        .addOnSuccessListener {

            Toast.makeText(
                context,
                "File uploaded successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }

        .addOnFailureListener { e ->

            Toast.makeText(
                context,
                "Upload failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
}

fun uploadFilesForAssignment(
    assignmentId: String,
    files: List<Uri>,
    context: Context,
    onComplete: () -> Unit
) {

    if (files.isEmpty()) {
        onComplete()
        return
    }

    val storage =
        FirebaseStorage.getInstance()

    val db =
        FirebaseFirestore.getInstance()

    val uploadedMaterials =
        mutableListOf<Map<String,String>>()

    var completed = 0

    files.forEach { uri ->

        val fileName =
            UUID.randomUUID().toString()

        val ref =
            storage.reference.child(
                "assignments/$assignmentId/materials/$fileName"
            )

        ref.putFile(uri)

            .addOnSuccessListener {

                ref.downloadUrl
                    .addOnSuccessListener { downloadUri ->

                        uploadedMaterials.add(
                            mapOf(
                                "fileName" to fileName,
                                "downloadUrl" to downloadUri.toString()
                            )
                        )

                        completed++

                        if (completed == files.size) {

                            db.collection("assignments")
                                .document(assignmentId)
                                .update(
                                    "materials",
                                    uploadedMaterials
                                )

                            onComplete()
                        }
                    }
            }
    }
}

fun updateAssignment(
    assignmentId: String,
    title: String,
    description: String,
    selectedDate: String,
    dueHour: String,
    dueMinute: String,
    context: Context,
    selectedFiles: List<Uri>,
    onSuccess: () -> Unit
) {

    val formatter =
        SimpleDateFormat(
            "MM/dd/yyyy HH:mm",
            Locale.getDefault()
        )

    val parsedDate =
        formatter.parse(
            "$selectedDate $dueHour:$dueMinute"
        )

    FirebaseFirestore.getInstance()
        .collection("assignments")
        .document(assignmentId)
        .update(
            mapOf(
                "title" to title,
                "description" to description,
                "dueDate" to Timestamp(parsedDate!!)
            )
        )
        .addOnSuccessListener {

            uploadFilesForAssignment(
                assignmentId = assignmentId,
                files = selectedFiles,
                context = context
            ) {

                Toast.makeText(
                    context,
                    "Assignment updated!",
                    Toast.LENGTH_SHORT
                ).show()

                onSuccess()
            }
        }
}