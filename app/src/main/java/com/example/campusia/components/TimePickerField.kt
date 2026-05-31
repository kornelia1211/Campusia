package com.example.campusia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusia.ui.theme.BorderColor
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.SoftPurple
import com.example.campusia.ui.theme.TextPrimary

@Composable
fun TimePickerField(
    selectedHour: String,
    selectedMinute: String,
    onTimeSelected: (String, String) -> Unit
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showDialog = true
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedHour:$selectedMinute",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = PrimaryPurpleDark
            )
        }
    }

    if (showDialog) {
        TimeSelectionDialog(
            initialHour = selectedHour.toIntOrNull() ?: 8,
            initialMinute = selectedMinute,
            onDismiss = {
                showDialog = false
            },
            onConfirm = { hour, minute ->
                onTimeSelected(
                    hour.toString().padStart(2, '0'),
                    minute
                )
                showDialog = false
            }
        )
    }
}

@Composable
private fun TimeSelectionDialog(
    initialHour: Int,
    initialMinute: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var selectedHour by remember {
        mutableIntStateOf(initialHour.coerceIn(0, 23))
    }

    var selectedMinute by remember {
        mutableStateOf(initialMinute)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedHour, selectedMinute)
                }
            ) {
                Text(
                    text = "Save",
                    color = PrimaryPurpleDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = TextPrimary
                )
            }
        },
        title = {
            Text(
                text = "Select time",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Hour",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                HourSelector(
                    selectedHour = selectedHour,
                    onHourSelected = {
                        selectedHour = it
                    }
                )

                Text(
                    text = "Minutes",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                MinuteSelector(
                    selectedMinute = selectedMinute,
                    onMinuteSelected = {
                        selectedMinute = it
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftPurple)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${selectedHour.toString().padStart(2, '0')}:$selectedMinute",
                        color = PrimaryPurpleDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun HourSelector(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    val hourRows = listOf(
        (0..5).toList(),
        (6..11).toList(),
        (12..17).toList(),
        (18..23).toList()
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hourRows.forEach { rowHours ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowHours.forEach { hour ->
                    TimeOptionChip(
                        text = hour.toString().padStart(2, '0'),
                        selected = selectedHour == hour,
                        onClick = {
                            onHourSelected(hour)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MinuteSelector(
    selectedMinute: String,
    onMinuteSelected: (String) -> Unit
) {
    val minuteOptions = listOf("00", "15", "30", "45")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        minuteOptions.forEach { minute ->
            TimeOptionChip(
                text = minute,
                selected = selectedMinute == minute,
                onClick = {
                    onMinuteSelected(minute)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun TimeOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SoftPurple else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) PrimaryPurple else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) PrimaryPurpleDark else TextPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}