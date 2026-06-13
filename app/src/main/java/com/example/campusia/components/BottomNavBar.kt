package com.example.campusia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusia.SessionManager
import com.example.campusia.entities.UserRole

private val SelectedColor = Color(0xFF8B7CF6)
private val UnselectedColor = Color(0xFF7C7C86)
private val BarBackground = Color.White

@Composable
fun BottomNavBar(
    navController: NavHostController,
    selectedItem: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BarBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BarBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    label = "Home",
                    selected = selectedItem == "home",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = {
                        navController.navigate("home_screen") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    label = "Courses",
                    selected = selectedItem == "courses",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = "Courses",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = {
                        navController.navigate("courses_screen") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    label = "Chat",
                    selected = selectedItem == "chat",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Chat",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = {
                        navController.navigate("chatList_screen") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    label = "Notifications",
                    selected = selectedItem == "notifications",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = { }
                )

                if (SessionManager.userRole != UserRole.ADMIN) {
                    BottomNavItem(
                        modifier = Modifier.weight(1f),
                        label = "Schedule",
                        selected = selectedItem == "schedule",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Schedule",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            navController.navigate("schedule_screen") {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    label = "Profile",
                    selected = selectedItem == "profile",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = {
                        navController.navigate("profile_screen") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            Box(
                modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val contentColor = if (selected) SelectedColor else UnselectedColor

    Column(
        modifier = modifier
            .height(56.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }

        Text(
            text = label,
            color = contentColor,
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}