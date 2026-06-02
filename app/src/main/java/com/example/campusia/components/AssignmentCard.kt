package com.example.campusia.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusia.entities.Assignment
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentCard(
    assignment: Assignment,
    onClick: () -> Unit
) {

    val formattedDate = remember(assignment.dueDate) {
        assignment.dueDate?.toDate()?.let { date ->
            SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(date)
        } ?: "No due date"
    }

    Card(
        onClick = { onClick() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Due: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )

        }
    }
}