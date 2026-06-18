package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.RoundedButton
import com.example.campusia.components.StyledInputField
import com.example.campusia.entities.Course
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.IconButton

private data class ChatUserSuggestion(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

private data class RawChatRoom(
    val id: String,
    val storedTitle: String,
    val type: String,
    val participants: List<String>,
    val titlesByUser: Map<String, String>,
    val lastMessageAt: Timestamp?,
    val lastMessageSenderId: String,
    val lastReadAt: Timestamp?,
    val hiddenFor: List<String>
)


private data class UiChatRoom(
    val id: String,
    val title: String,
    val participants: List<String>,
    val isUnread: Boolean,
    val lastMessageMillis: Long
)

@Composable
fun ChatListScreen(
    navController: NavHostController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current
    val userRole = SessionManager.userRole

    var newChatTitle by remember { mutableStateOf("") }
    var rawChatRooms by remember {
        mutableStateOf<List<RawChatRoom>>(emptyList())
    }

    var availableCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var availableUsers by remember { mutableStateOf<List<ChatUserSuggestion>>(emptyList()) }

    var isCourseSelected by remember { mutableStateOf(true) }

    val searchText = newChatTitle.trim()

    val courseSuggestions =
        if (isCourseSelected && searchText.isNotBlank()) {
            availableCourses
                .filter { course ->
                    course.title.startsWith(searchText, ignoreCase = true)
                }
                .take(6)
        } else {
            emptyList()
        }

    val userSuggestions =
        if (!isCourseSelected && searchText.isNotBlank()) {
            availableUsers
                .filter { user ->
                    user.fullName.startsWith(searchText, ignoreCase = true) ||
                            user.firstName.startsWith(searchText, ignoreCase = true) ||
                            user.lastName.startsWith(searchText, ignoreCase = true)
                }
                .take(6)
        } else {
            emptyList()
        }

    var currentUserName by remember {
        mutableStateOf("Private Chat")
    }

    val chatRooms = remember(
        rawChatRooms,
        availableUsers,
        currentUserId
    ) {
        rawChatRooms
            .filter { room ->
                currentUserId !in room.hiddenFor
            }
            .map { room ->

                val displayedTitle =
                    if (room.type == "private") {
                        val otherUserId =
                            room.participants.firstOrNull { participantId ->
                                participantId != currentUserId
                            }

                        val otherUserName =
                            availableUsers
                                .firstOrNull { user ->
                                    user.userId == otherUserId
                                }
                                ?.fullName
                                ?.takeIf { it.isNotBlank() }

                        otherUserName
                            ?: room.titlesByUser[currentUserId]
                            ?: room.storedTitle.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Private Chat"
                    } else {
                        room.storedTitle.ifBlank {
                            "Unnamed Chat"
                        }
                    }

                val lastMessageMillis =
                    room.lastMessageAt
                        ?.toDate()
                        ?.time
                        ?: 0L

                val lastReadMillis =
                    room.lastReadAt
                        ?.toDate()
                        ?.time
                        ?: 0L

                val isUnread =
                    room.lastMessageSenderId.isNotBlank() &&
                            room.lastMessageSenderId != currentUserId &&
                            lastMessageMillis > lastReadMillis

                UiChatRoom(
                    id = room.id,
                    title = displayedTitle,
                    participants = room.participants,
                    isUnread = isUnread,
                    lastMessageMillis = lastMessageMillis
                )
            }
            .sortedWith(
                compareByDescending<UiChatRoom> {
                    it.isUnread
                }.thenByDescending {
                    it.lastMessageMillis
                }
            )
    }

    fun openCourseChat(course: Course) {
        val courseId = course.courseId

        if (courseId.isBlank()) {
            Toast.makeText(
                context,
                "Course id is missing",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val allParticipants =
            (course.studentIds + course.lecturerIds + currentUserId).distinct()

        val roomData = mapOf(
            "title" to course.title,
            "participants" to allParticipants
        )

        db.collection("chat_rooms")
            .document(courseId)
            .set(roomData)
        db.collection("chat_rooms")
            .document(courseId)
            .set(roomData, SetOptions.merge())
            .addOnSuccessListener {
                db.collection("chat_rooms")
                    .document(courseId)
                    .update(
                        "hiddenFor",
                        FieldValue.arrayRemove(currentUserId)
                    )

                newChatTitle = ""

                navController.navigate(
                    "chat/$courseId/${course.title}"
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Failed to open chat: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun hideChatFromList(chatRoomId: String) {
        if (currentUserId.isBlank()) {
            Toast.makeText(
                context,
                "User id is missing",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        db.collection("chat_rooms")
            .document(chatRoomId)
            .update(
                "hiddenFor",
                FieldValue.arrayUnion(currentUserId)
            )
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Could not remove chat: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun openPrivateChat(
        user: ChatUserSuggestion
    ) {
        val selectedUserId = user.userId

        val selectedUserName =
            user.fullName.ifBlank {
                "Private Chat"
            }

        if (
            selectedUserId.isBlank() ||
            currentUserId.isBlank()
        ) {
            Toast.makeText(
                context,
                "User id is missing",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val privateRoomId =
            listOf(
                currentUserId,
                selectedUserId
            )
                .sorted()
                .joinToString("_")

        val roomData = mapOf(
            "type" to "private",
            "participants" to listOf(
                currentUserId,
                selectedUserId
            ),
            "titlesByUser" to mapOf(
                currentUserId to selectedUserName,
                selectedUserId to currentUserName
            )
        )

        db.collection("chat_rooms")
            .document(privateRoomId)
            .set(roomData, SetOptions.merge())
            .addOnSuccessListener {
                db.collection("chat_rooms")
                    .document(privateRoomId)
                    .update(
                        "hiddenFor",
                        FieldValue.arrayRemove(currentUserId)
                    )

                newChatTitle = ""

                navController.navigate(
                    "chat/$privateRoomId/$selectedUserName"
                )
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Failed to open chat: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) {
            rawChatRooms = emptyList()
            onDispose { }
        } else {
            val query = db.collection("chat_rooms")
                .whereArrayContains(
                    "participants",
                    currentUserId
                )

            val liveChatsConnection =
                query.addSnapshotListener { snapshot, exception ->

                    if (exception != null) {
                        Toast.makeText(
                            context,
                            "Error loading chats: ${exception.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addSnapshotListener
                    }

                    rawChatRooms =
                        snapshot?.documents?.map { document ->

                            val participants =
                                (document.get("participants") as? List<*>)
                                    ?.mapNotNull { value ->
                                        value as? String
                                    }
                                    ?: emptyList()

                            val rawTitles =
                                document.get("titlesByUser")
                                        as? Map<*, *>

                            val titlesByUser =
                                rawTitles
                                    ?.mapNotNull { entry ->
                                        val key =
                                            entry.key as? String

                                        val value =
                                            entry.value as? String

                                        if (
                                            key != null &&
                                            value != null
                                        ) {
                                            key to value
                                        } else {
                                            null
                                        }
                                    }
                                    ?.toMap()
                                    ?: emptyMap()

                            val lastReadBy =
                                document.get("lastReadBy") as? Map<*, *>

                            val lastReadAt =
                                lastReadBy
                                    ?.get(currentUserId) as? Timestamp

                            val hiddenFor =
                                (document.get("hiddenFor") as? List<*>)
                                    ?.mapNotNull { it as? String }
                                    ?: emptyList()

                            RawChatRoom(
                                id = document.id,
                                storedTitle =
                                    document.getString("title")
                                        .orEmpty(),
                                type =
                                    document.getString("type")
                                        .orEmpty(),
                                participants = participants,
                                titlesByUser = titlesByUser,
                                lastMessageAt =
                                    document.getTimestamp("lastMessageAt"),
                                lastMessageSenderId =
                                    document.getString("lastMessageSenderId")
                                        .orEmpty(),
                                lastReadAt = lastReadAt,
                                hiddenFor = hiddenFor
                            )
                        }
                            ?: emptyList()
                }

            onDispose {
                liveChatsConnection.remove()
            }
        }
    }

    DisposableEffect(currentUserId, userRole) {
        if (currentUserId.isBlank()) {
            availableCourses = emptyList()
            onDispose { }
        } else {
            val query = when (userRole) {
                UserRole.STUDENT -> {
                    db.collection("courses")
                        .whereArrayContains("studentIds", currentUserId)
                }

                UserRole.LECTURER -> {
                    db.collection("courses")
                        .whereArrayContains("lecturerIds", currentUserId)
                }

                UserRole.ADMIN -> {
                    db.collection("courses")
                }
            }

            val coursesConnection = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        context,
                        "Error loading courses: ${exception.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    availableCourses = snapshot.documents.mapNotNull { document ->
                        val course = document.toObject(Course::class.java)

                        course?.copy(
                            courseId = course.courseId.ifBlank {
                                document.id
                            }
                        )
                    }
                }
            }

            onDispose {
                coursesConnection.remove()
            }
        }
    }

    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) {
            availableUsers = emptyList()
            onDispose { }
        } else {
            val usersConnection = db.collection("users")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(
                            context,
                            "Error loading users: ${exception.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        availableUsers = snapshot.documents.mapNotNull { document ->
                            val userId = document.id

                            if (userId == currentUserId) {
                                return@mapNotNull null
                            }

                            val firstName = document.getString("firstName") ?: ""
                            val lastName = document.getString("lastName") ?: ""
                            val email = document.getString("email") ?: ""
                            val role = document.getString("role") ?: ""

                            val fullName = "$firstName $lastName".trim()

                            if (fullName.isBlank()) {
                                null
                            } else {
                                ChatUserSuggestion(
                                    userId = userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    role = role
                                )
                            }
                        }
                    }
                }

            onDispose {
                usersConnection.remove()
            }
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryPurple,
                                PrimaryPurpleDark
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Your chats",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "chat"
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ScreenBackground)
                .padding(10.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(modifier = Modifier.weight(1f)) {
                    StyledInputField(
                        value = newChatTitle,
                        onValueChange = {
                            newChatTitle = it
                        },
                        placeholder =
                            if (isCourseSelected) {
                                "Start typing course name"
                            } else {
                                "Start typing user name"
                            }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                RoundedButton(
                    text = "Create",
                    icon = Icons.Filled.Add,
                    shape = RoundedCornerShape(16.dp),
                    height = 52.dp,
                    onClick = {
                        val cleanTitle = newChatTitle.trim()

                        if (cleanTitle.isBlank() || currentUserId.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please choose an option from the list",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@RoundedButton
                        }

                        if (isCourseSelected) {
                            val selectedCourse = availableCourses.firstOrNull { course ->
                                course.title.equals(cleanTitle, ignoreCase = true)
                            }

                            if (selectedCourse != null) {
                                openCourseChat(selectedCourse)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please choose a course from the list",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val selectedUser = availableUsers.firstOrNull { user ->
                                user.fullName.equals(cleanTitle, ignoreCase = true)
                            }

                            if (selectedUser != null) {
                                openPrivateChat(selectedUser)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please choose a user from the list",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isCourseSelected,
                    onClick = {
                        isCourseSelected = true
                        newChatTitle = ""
                    },
                    label = {
                        Text("Course Chat")
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = !isCourseSelected,
                    onClick = {
                        isCourseSelected = false
                        newChatTitle = ""
                    },
                    label = {
                        Text("Private Chat")
                    }
                )
            }

            if (searchText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                if (isCourseSelected) {
                    if (courseSuggestions.isEmpty()) {
                        Text(
                            text = "No matching courses",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            courseSuggestions.forEach { course ->
                                ChatSuggestionCard(
                                    title = course.title,
                                    subtitle = "Course chat",
                                    icon = Icons.Outlined.MenuBook,
                                    onClick = {
                                        newChatTitle = course.title
                                        openCourseChat(course)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    if (userSuggestions.isEmpty()) {
                        Text(
                            text = "No matching users",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userSuggestions.forEach { user ->
                                ChatSuggestionCard(
                                    title = user.fullName,
                                    subtitle =
                                        if (user.email.isNotBlank()) {
                                            user.email
                                        } else {
                                            user.role.ifBlank { "User" }
                                        },
                                    icon = Icons.Outlined.Person,
                                    onClick = {
                                        newChatTitle = user.fullName
                                        openPrivateChat(user)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chatRooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You don't have any conversations yet",
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(chatRooms) { room ->
                        ChatRoomCard(
                            chatRoom = room,
                            onClick = {
                                navController.navigate(
                                    "chat/${room.id}/${room.title}"
                                )
                            },
                            onDelete = {
                                hideChatFromList(room.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatSuggestionCard(
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
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
private fun ChatRoomCard(
    chatRoom: UiChatRoom,
    onClick: () -> Unit,
    onDelete: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = "Chat icon",
                tint = PrimaryPurple
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatRoom.title,
                    fontWeight =
                        if (chatRoom.isUnread) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.Bold
                        },
                    color = TextPrimary
                )

                Text(
                    text =
                        if (chatRoom.isUnread) {
                            "New message"
                        } else {
                            "Join conversation"
                        },
                    color =
                        if (chatRoom.isUnread) {
                            PrimaryPurpleDark
                        } else {
                            TextMuted
                        },
                    fontWeight =
                        if (chatRoom.isUnread) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                )

            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove chat from list",
                    tint = TextMuted
                )
            }

            if (chatRoom.isUnread) {
                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = PrimaryPurple,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}