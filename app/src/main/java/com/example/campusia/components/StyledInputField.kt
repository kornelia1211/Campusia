package com.example.campusia.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusia.ui.theme.BorderColor
import com.example.campusia.ui.theme.PlaceholderColor
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.PrimaryPurpleDark
import com.example.campusia.ui.theme.TextPrimary

@Composable
fun StyledInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = PlaceholderColor
            )
        },
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = BorderColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryPurpleDark,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedPlaceholderColor = PlaceholderColor,
            unfocusedPlaceholderColor = PlaceholderColor
        )
    )
}