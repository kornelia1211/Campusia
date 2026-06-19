package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.entities.Announcement
import com.example.campusia.ui.theme.FieldBackground
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AnnouncementDetailsScreen(
    navController: NavHostController,
    announcementId: String
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var announcement by remember {
        mutableStateOf<Announcement?>(null)
    }

    var currentUserRole by remember {
        mutableStateOf("")
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    currentUserRole = document.getString("role") ?: ""
                }
                .addOnFailureListener {
                    currentUserRole = ""
                }
        }
    }

    DisposableEffect(announcementId) {
        val listener = db.collection("announcements")
            .document(announcementId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        context,
                        "Announcement loading error: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val loadedAnnouncement =
                        snapshot.toObject(Announcement::class.java)

                    announcement = loadedAnnouncement?.copy(
                        announcementId = snapshot.id
                    )
                } else {
                    announcement = null
                }
            }

        onDispose {
            listener.remove()
        }
    }

    val normalizedRole = currentUserRole.trim().lowercase()

    val canManageAnnouncement =
        normalizedRole == "lecturer" &&
                announcement?.authorId == currentUserId

    val formattedSendTime = remember(announcement?.sendTime) {
        announcement?.sendTime?.toDate()?.let { date ->
            SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).format(date)
        } ?: "Sending..."
    }

    if (showDeleteDialog && announcement != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(
                    text = "Delete announcement?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this announcement? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val announcementToDelete = announcement ?: return@TextButton

                        db.collection("announcements")
                            .document(announcementToDelete.announcementId)
                            .delete()
                            .addOnSuccessListener {
                                showDeleteDialog = false

                                Toast.makeText(
                                    context,
                                    "Announcement deleted.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.popBackStack()
                            }
                            .addOnFailureListener { exception ->
                                showDeleteDialog = false

                                Toast.makeText(
                                    context,
                                    exception.message ?: "Failed to delete announcement.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "courses"
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
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = announcement?.title ?: "Announcement",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color.White
                            )

                            Spacer(Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Sent",
                                    color = Color.White.copy(alpha = .8f)
                                )

                                Text(
                                    text = formattedSendTime,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        if (!announcement?.authorName.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = "By ${announcement?.authorName}",
                                color = Color.White.copy(alpha = .88f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (canManageAnnouncement && announcement != null) {
                            Spacer(Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val selectedAnnouncement =
                                            announcement ?: return@Button

                                        navController.navigate(
                                            "edit_announcement/${selectedAnnouncement.announcementId}/${selectedAnnouncement.courseId}"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = PrimaryPurpleDark
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit announcement",
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text("Edit")
                                }

                                OutlinedButton(
                                    onClick = {
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete announcement",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        text = "Delete",
                                        color = Color.White
                                    )
                                }
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
                            text = "Announcement Message",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                    text = announcement?.message ?: "",
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}