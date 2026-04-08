package com.example.campusia.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordRequirementText(label: String, isProvided: Boolean){
    Row(
        modifier = Modifier.padding(3.dp)){
        val color = if (isProvided) Color(0xFF36A839) else Color(0xFFDC2619)
        val symbol = if (isProvided) "✔" else "✖"

        Text(
            text = "$symbol $label",
            color = color,
            fontSize = 14.sp
        )
    }
}