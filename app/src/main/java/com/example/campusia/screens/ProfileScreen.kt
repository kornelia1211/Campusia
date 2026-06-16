package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.ui.theme.DangerRed
import com.example.campusia.ui.theme.FieldBackground
import com.example.campusia.ui.theme.FieldBorder
import com.example.campusia.ui.theme.PlaceholderColor
import com.example.campusia.ui.theme.PrimaryPurple
import com.example.campusia.ui.theme.ScreenBackground
import com.example.campusia.ui.theme.TextDark
import com.example.campusia.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.campusia.notifications.NotificationPreferences

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUserId = auth.currentUser?.uid

    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var notificationsMuted by remember {
        mutableStateOf(
            NotificationPreferences.areNotificationsMuted(context)
        )
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId == null) {
            isLoading = false
            Toast.makeText(
                context,
                "User is not logged in.",
                Toast.LENGTH_LONG
            ).show()
            navController.navigate("login_screen") {
                popUpTo("home_screen") { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                firstName = document.getString("firstName") ?: ""
                lastName = document.getString("lastName") ?: ""
                email = document.getString("email") ?: auth.currentUser?.email.orEmpty()
                role = document.getString("role") ?: ""
                department = document.getString("department") ?: ""
                userId = document.getString("userId") ?: currentUserId
                phoneNumber = document.getString("phoneNumber") ?: ""
                address = document.getString("address") ?: ""

                val remoteNotificationsMuted =
                    document.getBoolean("notificationsMuted")

                if (remoteNotificationsMuted != null) {
                    notificationsMuted = remoteNotificationsMuted

                    NotificationPreferences.setNotificationsMuted(
                        context = context,
                        muted = remoteNotificationsMuted
                    )
                }

                isLoading = false
            }
            .addOnFailureListener { exception ->
                isLoading = false
                Toast.makeText(
                    context,
                    "Profile loading error: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "profile"
            )
        },
        containerColor = ScreenBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding)
        ) {
            ProfileTopBar(
                navController = navController,
                auth = auth
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryPurple
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Profile",
                        color = TextDark,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Manage your personal information",
                        color = TextMuted,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    ProfileHeaderCard(
                        firstName = firstName,
                        lastName = lastName,
                        role = role,
                        userId = userId
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    PersonalInformationCard(
                        firstName = firstName,
                        onFirstNameChange = { firstName = it },
                        lastName = lastName,
                        onLastNameChange = { lastName = it },
                        email = email,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it },
                        address = address,
                        onAddressChange = { address = it },
                        department = department,
                        isEditing = isEditing,
                        onEditClick = {
                            if (isEditing) {
                                if (currentUserId == null) {
                                    Toast.makeText(
                                        context,
                                        "User is not logged in.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@PersonalInformationCard
                                }

                                val updatedUser = hashMapOf(
                                    "firstName" to firstName.trim(),
                                    "lastName" to lastName.trim(),
                                    "phoneNumber" to phoneNumber.trim(),
                                    "address" to address.trim()
                                )

                                db.collection("users")
                                    .document(currentUserId)
                                    .set(updatedUser, SetOptions.merge())
                                    .addOnSuccessListener {
                                        isEditing = false
                                        Toast.makeText(
                                            context,
                                            "Profile updated successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(
                                            context,
                                            "Profile update error: ${exception.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                isEditing = true
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    AccountSettingsCard(
                        notificationsMuted = notificationsMuted,
                        onNotificationsMutedChange = { muted ->

                            notificationsMuted = muted

                            NotificationPreferences.setNotificationsMuted(
                                context = context,
                                muted = muted
                            )

                            if (currentUserId != null) {
                                db.collection("users")
                                    .document(currentUserId)
                                    .set(
                                        mapOf(
                                            "notificationsMuted" to muted
                                        ),
                                        SetOptions.merge()
                                    )
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(
                                            context,
                                            "Could not save notification settings: " +
                                                    exception.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }

                            Toast.makeText(
                                context,
                                if (muted) {
                                    "Notifications muted."
                                } else {
                                    "Notifications enabled."
                                },
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTopBar(
    navController: NavHostController,
    auth: FirebaseAuth
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Campusia",
            color = PrimaryPurple,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = {
                auth.signOut()
                navController.navigate("login_screen") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                tint = TextDark,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    firstName: String,
    lastName: String,
    role: String,
    userId: String
) {
    val fullName = buildFullName(firstName, lastName)
    val initials = buildInitials(firstName, lastName)
    val displayedRole = role.ifBlank { "User" }
    val displayedUserId = userId.ifBlank { "No user ID" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                .padding(vertical = 26.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = fullName,
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayedRole,
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayedUserId,
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PersonalInformationCard(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    email: String,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    department: String,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Information",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Outlined.Save else Icons.Outlined.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Text(
                    text = if (isEditing) "Save" else "Edit",
                    color = PrimaryPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInputField(
                label = "First Name",
                value = firstName,
                onValueChange = onFirstNameChange,
                enabled = isEditing,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "First Name"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInputField(
                label = "Last Name",
                value = lastName,
                onValueChange = onLastNameChange,
                enabled = isEditing,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Last Name"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInputField(
                label = "Email",
                value = email,
                onValueChange = { },
                enabled = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInputField(
                label = "Phone Number",
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                enabled = isEditing,
                placeholder = "Add phone number",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "Phone Number"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInputField(
                label = "Address",
                value = address,
                onValueChange = onAddressChange,
                enabled = isEditing,
                placeholder = "Add address",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Address"
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInputField(
                label = "Department",
                value = department,
                onValueChange = { },
                enabled = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Department"
                    )
                }
            )
        }
    }
}

@Composable
private fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String = "",
    icon: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = TextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = !enabled,
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = PlaceholderColor,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = TextDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = FieldBorder,
                disabledBorderColor = FieldBorder,
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                disabledContainerColor = FieldBackground,
                focusedLeadingIconColor = TextMuted,
                unfocusedLeadingIconColor = TextMuted,
                disabledLeadingIconColor = PlaceholderColor,
                disabledTextColor = TextMuted,
                disabledPlaceholderColor = PlaceholderColor
            )
        )
    }
}

@Composable
private fun AccountSettingsCard(
    notificationsMuted: Boolean,
    onNotificationsMutedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(18.dp),
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
                .padding(20.dp)
        ) {
            Text(
                text = "Account Settings",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            NotificationMuteRow(
                notificationsMuted = notificationsMuted,
                onNotificationsMutedChange =
                    onNotificationsMutedChange
            )

            Spacer(modifier = Modifier.height(18.dp))

            AccountSettingsButton(
                text = "Change Password",
                textColor = TextDark,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.VpnKey,
                        contentDescription = "Change Password"
                    )
                },
                onClick = { }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AccountSettingsButton(
                text = "Privacy Settings",
                textColor = TextDark,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Privacy Settings"
                    )
                },
                onClick = { }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AccountSettingsButton(
                text = "Delete Account",
                textColor = DangerRed,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Account"
                    )
                },
                onClick = { }
            )
        }
    }
}

@Composable
private fun AccountSettingsButton(
    text: String,
    textColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FieldBackground,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun buildFullName(
    firstName: String,
    lastName: String
): String {
    val fullName = "$firstName $lastName".trim()

    return if (fullName.isBlank()) {
        "User"
    } else {
        fullName
    }
}

private fun buildInitials(
    firstName: String,
    lastName: String
): String {
    val firstInitial = firstName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
    val lastInitial = lastName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""

    val initials = firstInitial + lastInitial

    return if (initials.isBlank()) {
        "U"
    } else {
        initials
    }
}

@Composable
private fun NotificationMuteRow(
    notificationsMuted: Boolean,
    onNotificationsMutedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FieldBackground)
            .border(
                width = 1.dp,
                color = FieldBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector =
                if (notificationsMuted) {
                    Icons.Outlined.NotificationsOff
                } else {
                    Icons.Outlined.NotificationsActive
                },
            contentDescription = null,
            tint =
                if (notificationsMuted) {
                    TextMuted
                } else {
                    PrimaryPurple
                },
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Mute Notifications",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text =
                    if (notificationsMuted) {
                        "Chat and assignment reminders are muted"
                    } else {
                        "Chat and assignment reminders are enabled"
                    },
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = notificationsMuted,
            onCheckedChange =
                onNotificationsMutedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = FieldBorder
            )
        )
    }
}