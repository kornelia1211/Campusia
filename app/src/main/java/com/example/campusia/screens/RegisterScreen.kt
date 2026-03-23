
package com.example.campusia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.ui.theme.CampusiaTheme
import com.example.campusia.ui.theme.CardBackground
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PlaceholderColor
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.TextDark
import com.example.campusia.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth


private val RegisterPageGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE9DDFD),
        Color(0xFFF7F5FB),
        Color(0xFFE9DDFD)
    )
)


@Composable
fun RegisterScreen(
    navController: NavHostController,
    auth: FirebaseAuth
) {
    RegisterScreenContent(
        onCreateAccountClick = { fullName, email, password, confirmPassword, role ->
            registerUser(
                auth = auth,
                fullName = fullName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                selectedRole = role,
                onSuccess = {
                    navController.navigate("home_screen")
                }
            )
        },
        onSignInClick = {
            navController.popBackStack()
        }
    )
}

@Composable
fun RegisterScreenContent(
    onCreateAccountClick: (String, String, String, String, String) -> Unit,
    onSignInClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Student") }

    val roles = listOf("Student", "Lecturer")

    RegisterCardContainer {
        RegisterTopIcon(
            icon = Icons.Outlined.School,
            backgroundColor = PrimaryPurple
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Join Campusia",
            fontSize = 15.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        RegisterFormLabel("Full Name")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "John Smith"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Email")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "student@university.edu"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Password")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "••••••••",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Confirm Password")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "••••••••",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Register as")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterRoleDropdown(
            selectedRole = selectedRole,
            roles = roles,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onRoleSelected = { selectedRole = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                onCreateAccountClick(
                    fullName,
                    email,
                    password,
                    confirmPassword,
                    selectedRole
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Text(
                text = "Create Account",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(onClick = onSignInClick) {
            Text(
                text = "Already have an account? ",
                color = TextMuted,
                fontSize = 14.sp
            )
            Text(
                text = "Sign in",
                color = PrimaryPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RegisterCardContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterPageGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 88.dp)
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
private fun RegisterFormLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF2F2F2F)
    )
}

@Composable
private fun RegisterTopIcon(
    icon: ImageVector,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                color = PlaceholderColor
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = FieldBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = PrimaryPurple
        )
    )
}

@Composable
private fun RegisterRoleDropdown(
    selectedRole: String,
    roles: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRoleSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedRole,
                    color = Color(0xFF2F2F2F),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Choose role",
                    tint = Color(0xFF555555)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role) },
                    onClick = {
                        onRoleSelected(role)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    CampusiaTheme {
        RegisterScreenContent(
            onCreateAccountClick = { _, _, _, _, _ -> },
            onSignInClick = {}
        )
    }
}

fun registerUser(
    auth: FirebaseAuth,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    selectedRole: String,
    onSuccess: () -> Unit
) {
    if (
        fullName.isBlank() ||
        email.isBlank() ||
        password.isBlank() ||
        confirmPassword.isBlank()
    ) return

    if (password != confirmPassword) return

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            }
        }
}

