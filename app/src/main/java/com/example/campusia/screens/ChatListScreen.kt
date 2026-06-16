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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.components.RoundedButton
import com.example.campusia.components.StyledInputField
import com.example.campusia.entities.ChatRoom
import com.example.campusia.entities.Course
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.campusia.components.BottomNavBar

@Composable
fun ChatListScreen(
    navController: NavHostController
){
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    var newChatTitle by remember { mutableStateOf("") }
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }

    var isCourseSelected by remember { mutableStateOf(true) }

    DisposableEffect(currentUserId) {
        val query = db.collection("chat_rooms")
            .whereArrayContains("participants", currentUserId)

        val liveChatsConnection = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Toast.makeText(context, "Error loading chats: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                chatRooms = snapshot.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: "Unnamed Chat"
                    val participantsList = document.get("participants") as? List<String> ?: emptyList()
                    ChatRoom(id = document.id, title = title, participants = participantsList)
                }
            }
        }

        onDispose { liveChatsConnection.remove() }
    }

    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryPurple, PrimaryPurpleDark)
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
    ){
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ScreenBackground)
                .padding(10.dp)
        ){
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Todo: change text input field to searching lecturer/course that user is in from database
                Box(modifier = Modifier.weight(1f)) {
                    StyledInputField(
                        value = newChatTitle,
                        onValueChange = { newChatTitle = it },
                        placeholder = if (isCourseSelected) "Enter exact Course Title" else "Enter Lecturer Name"
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
                            Toast.makeText(context, "Please enter a valid title", Toast.LENGTH_SHORT).show()
                            return@RoundedButton
                        }

                        if (isCourseSelected) {
                            db.collection("courses")
                                .whereEqualTo("title", cleanTitle)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val courseDocument = querySnapshot.documents.firstOrNull()
                                    if (courseDocument != null) {
                                        val students = courseDocument.get("studentIds") as? List<String> ?: emptyList()
                                        val lecturers = courseDocument.get("lecturerIds") as? List<String> ?: emptyList()
                                        val allParticipants = (students + lecturers + currentUserId).distinct()

                                        val roomData = mapOf(
                                            "title" to cleanTitle,
                                            "participants" to allParticipants
                                        )

                                        db.collection("chat_rooms").document(courseDocument.id).set(roomData)
                                            .addOnSuccessListener {
                                                newChatTitle = ""
                                                navController.navigate("chat/${courseDocument.id}/$cleanTitle")
                                            }
                                    } else {
                                        Toast.makeText(context, "Course title was not found in database", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            //toDo: change the title of private room to both participants?
                            val nameParts = cleanTitle.split(" ")
                            val searchFirstName = nameParts[0]
                            val searchLastName = nameParts[1]

                            db.collection("users")
                                .whereEqualTo("firstName", searchFirstName)
                                .whereEqualTo("lastName", searchLastName)
                                .get()
                                .addOnSuccessListener { userQuery ->
                                    val lecturerDoc = userQuery.documents.firstOrNull()
                                    if (lecturerDoc != null) {
                                        val lecturerId = lecturerDoc.id
                                        val privateRoomId = if (currentUserId < lecturerId) "${currentUserId}_$lecturerId" else "${lecturerId}_$currentUserId"

                                        db.collection("chat_rooms").document(privateRoomId).get()
                                            .addOnSuccessListener {
                                                    roomDocument ->
                                                if (roomDocument.exists()) {
                                                    val existingTitle = roomDocument.getString("title") ?: cleanTitle
                                                    newChatTitle = ""
                                                    navController.navigate("chat/$privateRoomId/$existingTitle")
                                                } else {
                                                    val fullName = "$searchFirstName $searchLastName"
                                                    val roomData = mapOf(
                                                        "title" to fullName,
                                                        "participants" to listOf(currentUserId, lecturerId)
                                                    )

                                                    db.collection("chat_rooms").document(privateRoomId).set(roomData)
                                                        .addOnSuccessListener {
                                                            newChatTitle = ""
                                                            navController.navigate("chat/$privateRoomId/$fullName")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(context, "Failed to create chat: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Database error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "User with that name was not found in database", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                )
            }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = isCourseSelected,
                onClick = { isCourseSelected = true },
                label = { Text("Course Chat") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = !isCourseSelected,
                onClick = { isCourseSelected = false },
                label = { Text("Private Chat") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

            if (chatRooms.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You don't have any conversations yet",
                        color = TextMuted
                    )
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(chatRooms) { room ->
                        ChatRoomCard(
                            chatRoom = room,
                            onClick = {
                                navController.navigate("chat/${room.id}/${room.title}")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ChatRoomCard(chatRoom: ChatRoom, onClick: () -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ){

            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = "Chat icon",
                tint = PrimaryPurple
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = chatRoom.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary

                )
                Text(
                    text = "Join conversation",
                    color = TextMuted
                )
            }
        }
    }
}