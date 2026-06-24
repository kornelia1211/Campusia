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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

private data class AdminAppUser(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

@Composable
fun AdminStudentsListScreen(
    navController: NavHostController
) {
    AdminUsersListScreen(
        navController = navController,
        roleToShow = "student",
        title = "Students",
        subtitle = "Search and manage student accounts",
        emptyText = "No students found",
        icon = Icons.Outlined.Person
    )
}

@Composable
fun AdminLecturersListScreen(
    navController: NavHostController
) {
    AdminUsersListScreen(
        navController = navController,
        roleToShow = "lecturer",
        title = "Lecturers",
        subtitle = "Search and manage lecturer accounts",
        emptyText = "No lecturers found",
        icon = Icons.Outlined.SupervisorAccount
    )
}

@Composable
private fun AdminUsersListScreen(
    navController: NavHostController,
    roleToShow: String,
    title: String,
    subtitle: String,
    emptyText: String,
    icon: ImageVector
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var users by remember {
        mutableStateOf<List<AdminAppUser>>(emptyList())
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var userToDelete by remember {
        mutableStateOf<AdminAppUser?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    DisposableEffect(roleToShow) {
        val listener =
            db.collection("users")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        isLoading = false

                        Toast.makeText(
                            context,
                            "Error loading users: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addSnapshotListener
                    }

                    val allUsers =
                        snapshot
                            ?.documents
                            ?.map { document ->
                                val userIdFromField =
                                    document.getString("userId")
                                        .orEmpty()
                                        .trim()

                                AdminAppUser(
                                    uid = userIdFromField.ifBlank {
                                        document.id
                                    },
                                    firstName = document.getString("firstName") ?: "",
                                    lastName = document.getString("lastName") ?: "",
                                    email = document.getString("email") ?: "",
                                    role = document.getString("role") ?: ""
                                )
                            }
                            ?: emptyList()

                    users =
                        allUsers.filter { user ->
                            roleMatches(
                                savedRole = user.role,
                                expectedRole = roleToShow
                            )
                        }

                    isLoading = false
                }

        onDispose {
            listener.remove()
        }
    }

    val filteredUsers =
        users
            .filter { user ->
                val query = searchQuery.trim()

                if (query.isBlank()) {
                    true
                } else {
                    user.firstName.contains(query, ignoreCase = true) ||
                            user.lastName.contains(query, ignoreCase = true) ||
                            user.fullName.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true)
                }
            }
            .sortedWith(
                compareBy<AdminAppUser> {
                    it.lastName.lowercase()
                }.thenBy {
                    it.firstName.lowercase()
                }.thenBy {
                    it.email.lowercase()
                }
            )

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "home"
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
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
                    .statusBarsPadding()
                    .padding(18.dp)
            ) {
                Column {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = CircleShape
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.20f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.90f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = {
                    Text(
                        text = "Search by email, first name or last name",
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = PrimaryPurpleDark
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = FieldBorder,
                    cursorColor = PrimaryPurpleDark
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isLoading) {
                    "Loading users..."
                } else {
                    "${filteredUsers.size} users"
                },
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isLoading && filteredUsers.isEmpty()) {
                EmptyUsersCard(
                    text = if (searchQuery.isBlank()) {
                        emptyText
                    } else {
                        "No matching users found"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredUsers,
                        key = {
                            it.uid
                        }
                    ) { user ->
                        AdminUserCard(
                            user = user,
                            icon = icon,
                            onDeleteClick = {
                                userToDelete = user
                            }
                        )
                    }
                }
            }
        }
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = {
                userToDelete = null
            },
            title = {
                Text(
                    text = "Delete user?"
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${user.fullName.ifBlank { user.email }}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteUserFromApp(
                            db = db,
                            user = user,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "User deleted",
                                    Toast.LENGTH_SHORT
                                ).show()

                                userToDelete = null
                            },
                            onError = { message ->
                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_LONG
                                ).show()

                                userToDelete = null
                            }
                        )
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        userToDelete = null
                    }
                ) {
                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}

@Composable
private fun AdminUserCard(
    user: AdminAppUser,
    icon: ImageVector,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(22.dp)
            ),
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = PrimaryPurple.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurpleDark
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.fullName.ifBlank {
                        "No name"
                    },
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = user.email.ifBlank {
                            "No email"
                        },
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = Color(0xFFFFEBEE),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete user",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun EmptyUsersCard(
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = PrimaryPurpleDark,
                modifier = Modifier
                    .background(
                        color = PrimaryPurple.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .padding(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = text,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun roleMatches(
    savedRole: String,
    expectedRole: String
): Boolean {
    val normalizedSavedRole =
        savedRole
            .trim()
            .lowercase()

    val normalizedExpectedRole =
        expectedRole
            .trim()
            .lowercase()

    return normalizedSavedRole == normalizedExpectedRole ||
            normalizedSavedRole == "${normalizedExpectedRole}s"
}

private fun deleteUserFromApp(
    db: FirebaseFirestore,
    user: AdminAppUser,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val normalizedRole =
        user.role.trim().lowercase()

    val courseArrayField =
        if (normalizedRole == "lecturer") {
            "lecturerIds"
        } else {
            "studentIds"
        }

    db.collection("courses")
        .whereArrayContains(courseArrayField, user.uid)
        .get()
        .addOnSuccessListener { coursesSnapshot ->

            db.collection("chat_rooms")
                .whereArrayContains("participants", user.uid)
                .get()
                .addOnSuccessListener { chatRoomsSnapshot ->

                    commitDeleteUserBatch(
                        db = db,
                        user = user,
                        coursesSnapshot = coursesSnapshot,
                        chatRoomsSnapshot = chatRoomsSnapshot,
                        courseArrayField = courseArrayField,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
                .addOnFailureListener { exception ->
                    onError(
                        exception.message ?: "Could not update chat rooms."
                    )
                }
        }
        .addOnFailureListener { exception ->
            onError(
                exception.message ?: "Could not update courses."
            )
        }
}

private fun commitDeleteUserBatch(
    db: FirebaseFirestore,
    user: AdminAppUser,
    coursesSnapshot: QuerySnapshot,
    chatRoomsSnapshot: QuerySnapshot,
    courseArrayField: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val batch =
        db.batch()

    coursesSnapshot.documents.forEach { courseDocument ->

        val updates =
            mutableMapOf<String, Any>(
                courseArrayField to FieldValue.arrayRemove(user.uid)
            )

        if (roleMatches(user.role, "student")) {
            updates["enrolledStudents"] = FieldValue.increment(-1)
        }

        if (roleMatches(user.role, "lecturer")) {
            val fullName = user.fullName

            if (fullName.isNotBlank()) {
                updates["lecturerNames"] = FieldValue.arrayRemove(fullName)
            }
        }

        batch.update(
            courseDocument.reference,
            updates
        )
    }

    chatRoomsSnapshot.documents.forEach { chatRoomDocument ->
        batch.update(
            chatRoomDocument.reference,
            "participants",
            FieldValue.arrayRemove(user.uid)
        )
    }

    batch.delete(
        db.collection("users")
            .document(user.uid)
    )

    batch.commit()
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onError(
                exception.message ?: "Could not delete user."
            )
        }
}
