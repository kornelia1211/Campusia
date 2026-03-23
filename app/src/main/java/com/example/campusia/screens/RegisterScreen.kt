package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.campusia.ui.theme.PurpleDark
import com.example.campusia.ui.theme.PurpleLight
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun RegisterScreen (
    navController: NavHostController,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember {mutableStateOf("")}
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordConformationVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                    newValue -> email = newValue.filter {  it != '\n' && it != ' ' }
            },
            label = {
                Text(text = "Email address")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue.filter { it != '\n' && it != ' ' }
            },
            label = {
                Text(text = "Password")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (isPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Show password" else "Hide password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = passwordConfirmation,
            onValueChange = { newValue ->
                passwordConfirmation = newValue.filter { it != '\n' && it != ' ' }
            },
            label = {
                Text(text = "Confirm your password")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (isPasswordConformationVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordConformationVisible = !isPasswordConformationVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordConformationVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordConformationVisible) "Show password" else "Hide password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                register(
                    auth = auth,
                    email = email,
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                    navController = navController,
                    context = context
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PurpleDark, PurpleLight)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                text = "Register",
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {navController.navigate("login_screen")},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Sign In",
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

fun register(
    auth: FirebaseAuth,
    email: String,
    password: String,
    passwordConfirmation: String,
    navController: NavController,
    context: Context
) {
    val cleanEmail = email.trim()
    val cleanPassword = password.trim()
    val cleanPasswordConfirmation = passwordConfirmation.trim()

    when {
        !cleanEmail.contains("@") -> {
            Toast.makeText(context, "Email has to contain '@'!", Toast.LENGTH_SHORT).show()
            return
        }

        cleanPassword.length < 6 -> {
            Toast.makeText(
                context, "Password must contain at least 6 signs!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        !cleanPassword.any { it.isLetter() } -> {
            Toast.makeText(
                context,
                "Password must contain at least one letter!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        !cleanPassword.any { it.isDigit() } -> {
            Toast.makeText(
                context,
                "Password must contain at least one number!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        cleanPassword != cleanPasswordConfirmation -> {
            Toast.makeText(context, "Provided passwords are different!", Toast.LENGTH_SHORT).show()
            return
        }
    }

    auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    context,
                    "Successfully registered!",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("home_screen")
            }
            else {
                Toast.makeText(
                    context,
                    task.exception?.message ?: "Registration failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}