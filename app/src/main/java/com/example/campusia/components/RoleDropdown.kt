package com.example.campusia.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(){
    val roles = listOf("Student", "Lecturer")

    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("") }

    Column {

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {

            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select role") },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)}
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false}
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = {Text(role)},
                        onClick = {
                            selectedRole = role
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}