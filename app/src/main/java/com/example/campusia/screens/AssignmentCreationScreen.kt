package com.example.campusia.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.campusia.components.LabeledField
import com.example.campusia.components.RoundedButton
import com.example.campusia.components.StyledInputField
import com.example.campusia.components.TimePickerField
import com.example.campusia.entities.Assignment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.campusia.ui.theme.ScreenBackground
@Composable
fun AssignmentCreationScreen(
    navController: NavHostController,
    courseId: String){

    var title by remember{
        mutableStateOf("")
    }

    var description by remember{
        mutableStateOf("")
    }

    var dueDay by remember{
        mutableStateOf("")
    }

    var dueMonth by remember{
        mutableStateOf("")
    }

    var dueYear by remember{
        mutableStateOf("")
    }

    var dueHour by remember {
        mutableStateOf("00")
    }

    var dueMinute by remember{
        mutableStateOf("00")
    }

    val context = LocalContext.current

    val isFormValid = title.isNotBlank() &&
            description.isNotBlank() &&
            dueDay.isNotBlank() &&
            dueMonth.isNotBlank() &&
            dueYear.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){

        LabeledField("Title", icon = Icons.Outlined.MenuBook)

        StyledInputField(
            value = title,
            onValueChange = { title = it },
            placeholder = "List 1 - Functions and classes"
        )

        Spacer(modifier = Modifier.height(14.dp))

        LabeledField(label = "Description", icon = Icons.Outlined.Subject)

        StyledInputField(
            value = description,
            onValueChange = { description = it },
            placeholder = "Task 1. Create a function which..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // toDo: Change styledInputFields for month, day and year to DatePickerField like for hour and minute

        LabeledField(label = "Due Day", icon = Icons.Outlined.Subject)

        StyledInputField(
            value = dueDay,
            onValueChange = { dueDay = it },
            placeholder = "1"
        )

        Spacer(modifier = Modifier.height(14.dp))

        LabeledField(label = "Due Month", icon = Icons.Outlined.Subject)

        StyledInputField(
            value = dueMonth,
            onValueChange = { dueMonth = it },
            placeholder = "1"
        )

        Spacer(modifier = Modifier.height(14.dp))

        LabeledField(label = "Due Year", icon = Icons.Outlined.Subject)

        StyledInputField(
            value = dueYear,
            onValueChange = { dueYear = it },
            placeholder = "2026"
        )

        Spacer(modifier = Modifier.height(14.dp))

        LabeledField(label = "Due time", icon = Icons.Outlined.Schedule)

        TimePickerField(
            selectedHour = dueHour,
            selectedMinute = dueMinute,
            onTimeSelected = { hour, minute ->
                dueHour = hour
                dueMinute = minute
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoundedButton(
            text = "Create Assignment",
            enabled = isFormValid,
            onClick = {
                createAssignment(
                courseId,
                title,
                description,
                dueDay,
                dueMonth,
                dueYear,
                dueHour,
                dueMinute,
                context,
                onSuccess = {
                    navController.popBackStack()
                })
            }
        )
    }
}

@SuppressLint("DefaultLocale")
fun createAssignment(
    courseId: String,
    title: String,
    description: String,
    dueDay: String,
    dueMonth: String,
    dueYear: String,
    dueHour: String,
    dueMinute: String,
    context: Context,
    onSuccess: () -> Unit
){
    val db = FirebaseFirestore.getInstance()

    val dayInt = dueDay.toIntOrNull() ?: 1
    val monthInt = dueMonth.toIntOrNull() ?: 1
    val yearInt = dueYear.toIntOrNull() ?: 2026
    val formattedDay = String.format("%02d", dayInt)
    val formattedMonth = String.format("%02d", monthInt)
    val formattedYear = String.format("%04d", yearInt)

    val dueDateString = "$formattedMonth/$formattedDay/$formattedYear $dueHour:$dueMinute"

    val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val parsedDate = formatter.parse(dueDateString) ?: throw Exception("Parsing failed")
    val firebaseTimestamp = Timestamp(parsedDate)

    val newAssignment = Assignment(
        courseId = courseId,
        title = title,
        description = description,
        dueDate = firebaseTimestamp
    )

    db.collection("assignments")
        .add(newAssignment)
        .addOnSuccessListener {
            Toast.makeText(context, "Assignment was successfully created!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }.addOnFailureListener { exception ->
            Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
        }
}