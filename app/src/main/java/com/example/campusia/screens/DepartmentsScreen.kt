package com.example.campusia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.campusia.components.BottomNavBar
import com.example.campusia.entities.Department
import com.example.campusia.ui.theme.ScreenBackground
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusia.components.AlertDialogDelete
import com.example.campusia.components.RoundedButton
import com.example.campusia.components.StyledInputField
import com.example.campusia.ui.theme.DangerRed
import com.example.campusia.ui.theme.TextMuted
import com.example.campusia.ui.theme.TextPrimary
import androidx.compose.foundation.lazy.items

@Composable
fun DepartmentsScreen(navController: NavHostController){
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var departments by remember { mutableStateOf<List<Department>>(emptyList()) }
    var departmentToDelete by remember { mutableStateOf<Department?>(null) }
    var newDepartmentName by remember { mutableStateOf("") }


    DisposableEffect(Unit) {
        val listenerRegistration = db.collection("departments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    departments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Department::class.java)
                    }
                }
            }

        onDispose { listenerRegistration.remove() }
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BottomNavBar(
                navController = navController,
                selectedItem = "home"
            )
        }
    ){
        paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBackground)
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column {
                            Text(
                                text = "Departments",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create or remove departments",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                StyledInputField(
                    value = newDepartmentName,
                    onValueChange = { newDepartmentName = it },
                    placeholder = "Enter department name"
                )

                RoundedButton(
                    text = "Create",
                    icon = Icons.Outlined.Add,
                    onClick = {
                        if (newDepartmentName.trim().isNotBlank()) {
                            val data = mapOf("name" to newDepartmentName.trim())

                            db.collection("departments").document().set(data)
                                .addOnSuccessListener {
                                    newDepartmentName = ""
                                    Toast.makeText(context, "Department successfully added", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Department name cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    height = 50.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (departments.isEmpty()){
                    Text(
                        text = "No departments setup yet"
                    )
                }
                else{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(departments) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    )

                                    IconButton(onClick = { departmentToDelete = item }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete entry",
                                            tint = DangerRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    departmentToDelete?.let { dept ->
                        AlertDialogDelete(
                            message = "Are you sure you want to delete the department \"${dept.name}\"?",
                            onDismiss = { departmentToDelete = null },
                            onConfirm = {
                                db.collection("departments").document(dept.departmentId).delete()
                                departmentToDelete = null
                            }
                        )
                    }
            }

        }
    }
}