package com.example.campusia.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect

data class NotificationHistoryItem(
    @DocumentId val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val targetRoute: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val assignmentId: String = "",
    val announcementId: String = "",
    val createdAt: Timestamp? = null
)

@Composable
fun NotificationHistoryScreen(
    navController: NavHostController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var notifications by remember {
        mutableStateOf<List<NotificationHistoryItem>>(emptyList())
    }

    DisposableEffect(currentUserId) {
        if (currentUserId.isNullOrBlank()) {
            onDispose { }
        } else {
            val listener = db.collection("users")
                .document(currentUserId)
                .collection("notificationHistory")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(
                            context,
                            "Notifications loading error: ${exception.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addSnapshotListener
                    }

                    notifications = snapshot
                        ?.documents
                        ?.mapNotNull { document ->
                            document.toObject(NotificationHistoryItem::class.java)
                                ?.copy(id = document.id)
                        }
                        ?: emptyList()
                }

            onDispose {
                listener.remove()
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "notifications"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                NotificationsHeaderCard()
            }

            if (notifications.isEmpty()) {
                item {
                    Text(
                        text = "No notifications yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                itemsIndexed(
                    items = notifications,
                    key = { _, notification -> notification.id }
                ) { _, notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            if (notification.targetRoute.isNotBlank()) {
                                navController.navigate(notification.targetRoute) {
                                    launchSingleTop = true
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "This notification cannot be opened.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PrimaryPurple,
                        PrimaryPurpleDark
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notifications",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "History of important course and assignment notifications.",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationHistoryItem,
    onClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var resolvedCourseName by remember(
        notification.id,
        notification.courseName,
        notification.courseId,
        notification.assignmentId,
        notification.announcementId,
        notification.targetRoute
    ) {
        mutableStateOf(notification.courseName)
    }

    LaunchedEffect(
        notification.id,
        notification.courseName,
        notification.courseId,
        notification.assignmentId,
        notification.announcementId,
        notification.targetRoute
    ) {
        if (resolvedCourseName.isBlank()) {
            resolveCourseNameForNotification(
                db = db,
                notification = notification,
                onResolved = { courseName ->
                    resolvedCourseName = courseName
                }
            )
        }
    }

    val displayTitle = buildNotificationDisplayTitle(
        notification = notification,
        courseName = resolvedCourseName
    )

    val formattedDate = remember(notification.createdAt) {
        notification.createdAt?.toDate()?.let { date ->
            SimpleDateFormat(
                "MM/dd/yyyy HH:mm",
                Locale.getDefault()
            ).format(date)
        } ?: "Just now"
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
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        PrimaryPurple.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = PrimaryPurpleDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

private fun buildNotificationDisplayTitle(
    notification: NotificationHistoryItem,
    courseName: String
): String {
    val notificationName = when (notification.type) {
        "course_announcement" -> "New announcement"
        "assignment_deadline" -> "Assignment deadline"
        "assignment_grade" -> "Assignment graded"
        else -> notification.title.ifBlank { "New notification" }
    }

    return if (courseName.isNotBlank()) {
        "$courseName • $notificationName"
    } else {
        notification.title.ifBlank { notificationName }
    }
}

private fun resolveCourseNameForNotification(
    db: FirebaseFirestore,
    notification: NotificationHistoryItem,
    onResolved: (String) -> Unit
) {
    val directCourseId = notification.courseId

    if (directCourseId.isNotBlank()) {
        loadCourseName(
            db = db,
            courseId = directCourseId,
            onResolved = onResolved
        )
        return
    }

    val assignmentId = notification.assignmentId.ifBlank {
        extractRouteId(
            route = notification.targetRoute,
            prefix = "assignment_details"
        )
    }

    if (assignmentId.isNotBlank()) {
        db.collection("assignments")
            .document(assignmentId)
            .get()
            .addOnSuccessListener { assignmentDocument ->
                val courseId = assignmentDocument.getString("courseId") ?: ""

                if (courseId.isNotBlank()) {
                    loadCourseName(
                        db = db,
                        courseId = courseId,
                        onResolved = onResolved
                    )
                }
            }

        return
    }

    val announcementId = notification.announcementId.ifBlank {
        extractRouteId(
            route = notification.targetRoute,
            prefix = "announcement_details_screen"
        )
    }

    if (announcementId.isNotBlank()) {
        db.collection("announcements")
            .document(announcementId)
            .get()
            .addOnSuccessListener { announcementDocument ->
                val courseId = announcementDocument.getString("courseId") ?: ""

                if (courseId.isNotBlank()) {
                    loadCourseName(
                        db = db,
                        courseId = courseId,
                        onResolved = onResolved
                    )
                }
            }
    }
}

private fun loadCourseName(
    db: FirebaseFirestore,
    courseId: String,
    onResolved: (String) -> Unit
) {
    db.collection("courses")
        .document(courseId)
        .get()
        .addOnSuccessListener { courseDocument ->
            val courseName = courseDocument.getString("title") ?: ""

            if (courseName.isNotBlank()) {
                onResolved(courseName)
            }
        }
}

private fun extractRouteId(
    route: String,
    prefix: String
): String {
    val expectedPrefix = "$prefix/"

    return if (route.startsWith(expectedPrefix)) {
        route.removePrefix(expectedPrefix)
    } else {
        ""
    }
}