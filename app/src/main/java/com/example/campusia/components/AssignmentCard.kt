package com.example.campusia.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusia.entities.Assignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentCard(
    assignment: Assignment,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUserId = auth.currentUser?.uid ?: ""

    var userRole by remember {
        mutableStateOf("")
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    userRole = document.getString("role") ?: ""
                }
                .addOnFailureListener {
                    userRole = ""
                }
        }
    }

    val normalizedRole = userRole.trim().lowercase()

    val canEditOrDelete =
        normalizedRole == "lecturer" ||
                normalizedRole == "admin"

    val formattedDate = remember(
        assignment.dueDate
    ) {

        assignment.dueDate
            ?.toDate()
            ?.let { date ->

                SimpleDateFormat(
                    "MM/dd/yyyy HH:mm",
                    Locale.getDefault()
                ).format(date)

            } ?: "No due date"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Due: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )

            if (canEditOrDelete) {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.End,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onEdit
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Edit,
                            contentDescription =
                                "Edit assignment"
                        )
                    }

                    IconButton(
                        onClick = onDelete
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Delete,
                            contentDescription =
                                "Delete assignment"
                        )
                    }
                }
            }
        }
    }
}