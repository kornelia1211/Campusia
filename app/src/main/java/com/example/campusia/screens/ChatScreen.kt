package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.components.RoundedButton
import com.example.campusia.components.StyledInputField
import com.example.campusia.entities.Message
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FieldValue

@Composable
fun ChatScreen(
    navController: NavHostController,
    chatRoomId: String,
    chatTitle: String
){
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var currentUserName by remember { mutableStateOf("User") }
    var currentFcmToken by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val first = document.getString("firstName") ?: ""
                        val last = document.getString("lastName") ?: ""
                        if (first.isNotBlank() || last.isNotBlank()) {
                            currentUserName = "$first $last".trim()
                        }
                    }
                }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    currentFcmToken = token
                }
        }
    }


    DisposableEffect(chatRoomId) {
        val query = db.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val liveConnection = query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(context, "Error fetching chat: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null){
                    messages = snapshot.documents.mapNotNull { document ->
                        document.toObject(Message::class.java)
                    }

                    if (messages.isNotEmpty()) {
                        scope.launch { listState.animateScrollToItem(messages.size - 1) }
                    }
                }
            }

        onDispose { liveConnection.remove() }
    }


    Scaffold(
        containerColor = ScreenBackground,
        topBar = {
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
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column {
                    IconButton(
                        onClick = { navController.popBackStack() },
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

                    Text(
                        text = chatTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ScreenBackground)
        ){
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                items(messages) { message ->
                    MessageItem(message, message.senderId == currentUser?.uid)
                }}


            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StyledInputField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = "Type a message"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    RoundedButton(
                        text = "Send",
                        icon = Icons.Filled.Send,
                        shape = RoundedCornerShape(16.dp),
                        height = 52.dp,
                        onClick = {
                            if (messageText.trim().isNotBlank() && currentUser != null) {
                                val messageData = mapOf(
                                    "senderId" to currentUser.uid,
                                    "senderName" to currentUserName,
                                    "senderFcmToken" to currentFcmToken,
                                    "text" to messageText.trim(),
                                    "timestamp" to FieldValue.serverTimestamp()
                                )

                                db.collection("chat_rooms")
                                    .document(chatRoomId)
                                    .collection("messages")
                                    .add(messageData)
                                    .addOnSuccessListener {
                                        messageText = ""
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to send the message", Toast.LENGTH_SHORT).show()
                                    }
                            }

                        }
                    )
                }
            }


            }

        }
    }



@Composable
fun MessageItem(message: Message, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isCurrentUser) PrimaryPurple else Color.LightGray
    val textColor = if (isCurrentUser) Color.White else Color.Black
    val timeTextColor = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else TextMuted
    val timeString = remember(message.timestamp) {
        message.timestamp?.toDate()?.let { date ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = alignment
    ) {
        if (!isCurrentUser) {
            Text(
                text = message.senderName,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 16.dp
            ),
            shadowElevation = 1.dp,
            color = backgroundColor
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = textColor
                )

                if (timeString.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeString,
                        fontSize = 10.sp,
                        color = timeTextColor,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}