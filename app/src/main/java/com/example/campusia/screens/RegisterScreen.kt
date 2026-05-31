package com.example.campusia.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.components.PasswordRequirementText
import com.example.campusia.components.StudentHatIcon
import com.example.campusia.entities.Departments
import com.example.campusia.entities.PasswordRequirement
import com.example.campusia.entities.User
import com.example.campusia.entities.mapRole
import com.example.campusia.ui.theme.CampusiaTheme
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PlaceholderColor
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.TextDark
import com.example.campusia.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Student") }
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordConformationVisible by remember { mutableStateOf(false) }

    val roles = listOf("Student", "Lecturer")
    val departments =  remember { Departments.entries.map { it.displayName } }


    RegisterCardContainer {
        StudentHatIcon()

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

        RegisterFormLabel("First name")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = firstName,
            onValueChange = { newValue ->
                firstName = newValue.filter { it != '\n' }
            },
            placeholder = "John"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Last name")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = lastName,
            onValueChange = { newValue ->
                lastName = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "Smith"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Email address")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = email,
            onValueChange = { newValue ->
                email = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "student@university.edu",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Password")
        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()){
            val requirements = listOf(
                PasswordRequirement("At least 6 characters", password.length >= 6),
                PasswordRequirement("At least one letter", password.any { it.isLetter() }),
                PasswordRequirement("At least one number", password.any { it.isDigit() })
            )

            requirements.forEach { requirement ->
                PasswordRequirementText(label = requirement.label, isProvided = requirement.isProvided)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "••••••••",
            isPassword = !isPasswordVisible,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (isPasswordVisible) {
                            "Show password"
                        } else {
                            "Hide password"
                        },
                        tint = TextMuted
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Confirm your password")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = passwordConfirmation,
            onValueChange = { newValue ->
                passwordConfirmation = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "••••••••",
            isPassword = !isPasswordConformationVisible,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordConformationVisible = !isPasswordConformationVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordConformationVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (isPasswordConformationVisible) {
                            "Show password"
                        } else {
                            "Hide password"
                        },
                        tint = TextMuted
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Role")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterDropdownField(
            selectedValue = role,
            items = roles,
            placeholder = "Choose role",
            onItemSelected = { role = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Department")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterDropdownField(
            selectedValue = department,
            items = departments,
            placeholder = "Choose department",
            onItemSelected = { department = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                register(
                    auth = auth,
                    email = email,
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                    firstName = firstName,
                    lastName = lastName,
                    role = role,
                    department = department,
                    navController = navController,
                    context = context
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
                text = "Register",
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(
            onClick = { navController.navigate("login_screen") }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    color = PrimaryPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RegisterScreenContent() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Student") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordConformationVisible by remember { mutableStateOf(false) }

    val roles = listOf("Student", "Lecturer")
    val departments = listOf(
        "Engineering & Technology",
        "Business & Social Sciences",
        "Natural Sciences & Health",
        "Computer & IT"
    )

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

        RegisterFormLabel("First name")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = firstName,
            onValueChange = { newValue ->
                firstName = newValue.filter { it != '\n' }
            },
            placeholder = "John"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Last name")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = lastName,
            onValueChange = { newValue ->
                lastName = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "Smith"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Email address")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = email,
            onValueChange = { newValue ->
                email = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "student@university.edu",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Password")
        Spacer(modifier = Modifier.height(4.dp))

        Column(modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()){
            val requirements = listOf(
                    PasswordRequirement("At least 6 characters", password.length >= 6),
                    PasswordRequirement("At least one letter", password.any { it.isLetter() }),
                    PasswordRequirement("At least one number", password.any { it.isDigit() })
                )

            requirements.forEach { requirement ->
                PasswordRequirementText(label = requirement.label, isProvided = requirement.isProvided)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "••••••••",
            isPassword = !isPasswordVisible,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Show password" else "Hide password",
                        tint = TextMuted
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Confirm your password")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterTextField(
            value = passwordConfirmation,
            onValueChange = { newValue ->
                passwordConfirmation = newValue.filter { it != '\n' && it != ' ' }
            },
            placeholder = "••••••••",
            isPassword = !isPasswordConformationVisible,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordConformationVisible = !isPasswordConformationVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordConformationVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordConformationVisible) "Show password" else "Hide password",
                        tint = TextMuted
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Role")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterDropdownField(
            selectedValue = role,
            items = roles,
            placeholder = "Choose role",
            onItemSelected = { role = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegisterFormLabel("Department")
        Spacer(modifier = Modifier.height(8.dp))

        RegisterDropdownField(
            selectedValue = department,
            items = departments,
            placeholder = "Choose department",
            onItemSelected = { department = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Text(
                text = "Register",
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(onClick = { }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    color = PrimaryPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
            colors = CardDefaults.cardColors(containerColor = com.example.campusia.ui.theme.FieldBackground),
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
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
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
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
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
private fun RegisterDropdownField(
    selectedValue: String,
    items: List<String>,
    placeholder: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = FieldBorder,
                    shape = RoundedCornerShape(14.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures {
                        expanded = true
                    }
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedValue.isBlank()) placeholder else selectedValue,
                color = if (selectedValue.isBlank()) PlaceholderColor else Color(0xFF2F2F2F),
                fontSize = 16.sp
            )

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "Choose option",
                tint = TextMuted
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
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
        RegisterScreenContent()
    }
}

fun register(
    auth: FirebaseAuth,
    email: String,
    password: String,
    passwordConfirmation: String,
    role: String,
    firstName: String,
    lastName: String,
    department: String,
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
                context,
                "Password must contain at least 6 signs!",
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
                val userId = auth.currentUser?.uid
                val db = FirebaseFirestore.getInstance()

                val user = User(
                    userId = userId ?: "",
                    email = cleanEmail,
                    firstName = firstName,
                    lastName = lastName,
                    role = role,
                    department = department
                )

                if (!userId.isNullOrBlank()) {
                    db.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            SessionManager.userRole = mapRole(role) //update the role in session manager
                            Toast.makeText(
                                context,
                                "Successfully registered!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("courses_screen"){
                                popUpTo("register_screen") {inclusive = true} //so logged in user cannot go back to registration screen
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error occurred: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            } else {
                Toast.makeText(
                    context,
                    task.exception?.message ?: "Registration failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }