package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.MenuBook
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
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AnnouncementCreationScreen(
    navController: NavHostController,
    courseId: String,
    announcementId: String? = null
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val isEditMode = !announcementId.isNullOrBlank()

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf(currentUser?.email ?: "User") }
    var isLoading by remember { mutableStateOf(isEditMode) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val fullName = "$firstName $lastName".trim()

                if (fullName.isNotBlank()) {
                    currentUserName = fullName
                }
            }
    }

    LaunchedEffect(announcementId) {
        if (!announcementId.isNullOrBlank()) {
            db.collection("announcements")
                .document(announcementId)
                .get()
                .addOnSuccessListener { document ->
                    title = document.getString("title") ?: ""
                    message = document.getString("message") ?: ""
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    isLoading = false
                    Toast.makeText(
                        context,
                        exception.message ?: "Failed to load announcement.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    val isFormValid = title.isNotBlank() && message.isNotBlank() && !isLoading

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
            AnnouncementTopIntroCard(
                isEditMode = isEditMode,
                onBackClick = {
                    navController.popBackStack()
                }
            )

            LabeledField(
                label = "Title",
                icon = Icons.Outlined.MenuBook
            )

            StyledInputField(
                value = title,
                onValueChange = {
                    title = it
                },
                placeholder = "Important course announcement"
            )

            LabeledField(
                label = "Message",
                icon = Icons.Outlined.Subject
            )

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                },
                placeholder = {
                    Text("Write the announcement message...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(18.dp)
            )

            RoundedButton(
                text = if (isEditMode) "Save Changes" else "Create Announcement",
                enabled = isFormValid,
                onClick = {
                    if (isEditMode) {
                        updateAnnouncement(
                            announcementId = announcementId ?: "",
                            title = title,
                            message = message,
                            context = context,
                            onSuccess = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        createAnnouncement(
                            courseId = courseId,
                            title = title,
                            message = message,
                            authorName = currentUserName,
                            context = context,
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

@Composable
private fun AnnouncementTopIntroCard(
    isEditMode: Boolean,
    onBackClick: () -> Unit
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
                    contentDescription = "Back to course"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEditMode) "Edit Announcement" else "Create New Announcement",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEditMode) {
                    "Update the announcement details below."
                } else {
                    "Fill in the details below to add a new course announcement."
                },
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun createAnnouncement(
    courseId: String,
    title: String,
    message: String,
    authorName: String,
    context: Context,
    onSuccess: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        Toast.makeText(
            context,
            "You must be logged in to create an announcement.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val db = FirebaseFirestore.getInstance()
    val announcementRef = db.collection("announcements").document()

    val announcementData = mapOf(
        "announcementId" to announcementRef.id,
        "courseId" to courseId,
        "title" to title.trim(),
        "message" to message.trim(),
        "sendTime" to FieldValue.serverTimestamp(),
        "authorId" to currentUser.uid,
        "authorName" to authorName
    )

    announcementRef
        .set(announcementData)
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Announcement was successfully created!",
                Toast.LENGTH_SHORT
            ).show()

            onSuccess()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                context,
                exception.message ?: "Failed to create announcement.",
                Toast.LENGTH_LONG
            ).show()
        }
}

fun updateAnnouncement(
    announcementId: String,
    title: String,
    message: String,
    context: Context,
    onSuccess: () -> Unit
) {
    if (announcementId.isBlank()) {
        Toast.makeText(
            context,
            "Announcement id is missing.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    FirebaseFirestore.getInstance()
        .collection("announcements")
        .document(announcementId)
        .update(
            mapOf(
                "title" to title.trim(),
                "message" to message.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        .addOnSuccessListener {
            Toast.makeText(
                context,
                "Announcement was successfully updated!",
                Toast.LENGTH_SHORT
            ).show()

            onSuccess()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                context,
                exception.message ?: "Failed to update announcement.",
                Toast.LENGTH_LONG
            ).show()
        }
}