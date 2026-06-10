package com.example.campusia.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusia.entities.Course
import com.example.campusia.entities.UserRole
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextDark
import com.example.campusia.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(
    course: Course,
    role: UserRole,
    onClick: () -> Unit,
    onEnroll: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val isEnrolled = userId != null && course.studentIds.contains(userId)
    val canEditOrDelete = role != UserRole.STUDENT && (onEdit != null || onDelete != null)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
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
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = ScreenBackground,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = FieldBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Course",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = course.title.ifBlank { "Untitled course" },
                        color = TextDark,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = course.description.ifBlank { "No description added yet." },
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CourseInfoRow(
                text = course.department.ifBlank { "No department" },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Apartment,
                        contentDescription = "Department",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CourseInfoRow(
                text = buildScheduleText(course),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Schedule",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            if (course.schedule.room.isNotBlank() || course.schedule.building.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                CourseInfoRow(
                    text = buildLocationText(course),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            when {
                role == UserRole.STUDENT && onEnroll != null -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { if (!isEnrolled) onEnroll.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        enabled = !isEnrolled,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            contentColor = Color.White,
                            disabledContainerColor = ScreenBackground,
                            disabledContentColor = TextMuted
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = if (isEnrolled) "Enrolled" else "Enroll",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                canEditOrDelete -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (onEdit != null) {
                            Button(
                                onClick = onEdit,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Text("Edit")
                            }
                        }

                        if (onDelete != null) {
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, FieldBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = TextDark
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseInfoRow(
    text: String,
    icon: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildScheduleText(course: Course): String {
    val start = course.schedule.startTime
    val end = course.schedule.endTime

    val time = if (start.isNotBlank() && end.isNotBlank()) {
        "$start - $end"
    } else {
        "No time set"
    }

    return "${course.schedule.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} • $time"
}

private fun buildLocationText(course: Course): String {
    val room = course.schedule.room
    val building = course.schedule.building

    return when {
        room.isNotBlank() && building.isNotBlank() -> "$building, room $room"
        building.isNotBlank() -> building
        room.isNotBlank() -> "Room $room"
        else -> "No location"
    }
}