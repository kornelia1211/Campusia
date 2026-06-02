package com.example.campusia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.components.UploadMaterialsCard
import com.example.campusia.entities.Assignment
import com.example.campusia.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AssignmentDetailsScreen(
    navController: NavHostController,
    assignmentId: String
) {

    val db = FirebaseFirestore.getInstance()

    var assignment by remember {
        mutableStateOf<Assignment?>(null)
    }

    LaunchedEffect(Unit) {

        db.collection("assignments")
            .document(assignmentId)
            .get()
            .addOnSuccessListener {
                assignment =
                    it.toObject(Assignment::class.java)
            }
    }

    val formattedDate = remember(assignment?.dueDate) {
        assignment?.dueDate?.toDate()?.let { date ->
            SimpleDateFormat("MM/dd/yyyy HH:mm", java.util.Locale.getDefault()).format(date)
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
                                Icons.Outlined.ArrowBack,
                                null,
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
                                Icons.Outlined.CalendarMonth,
                                null,
                                tint = Color.White
                            )

                            Spacer(Modifier.width(8.dp))

                            Column {

                                Text(
                                    "Due date",
                                    color = Color.White.copy(alpha = .8f)
                                )

                                Text(
                                    formattedDate,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
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
                            "Task Description",
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
                                    Icons.Outlined.Description,
                                    null,
                                    tint = PrimaryPurpleDark
                                )

                                Spacer(Modifier.width(10.dp))

                                Text(
                                    assignment?.description ?: "",
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            item {
                UploadMaterialsCard(
                    onClick = {

                        // TODO upload file
                    }
                )
            }
        }
    }
}