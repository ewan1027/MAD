package com.nibm.tmapp.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(navController: NavController) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) {
        Surface(
            Modifier.fillMaxSize().padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(24.dp)
                        .semantics { contentDescription = "Admin dashboard card" },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Welcome, Admin!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        val btnMod = Modifier.fillMaxWidth().height(48.dp)
                        val btnShape = RoundedCornerShape(16.dp)
                        Button(
                            onClick = { navController.navigate("manage_accounts") },
                            modifier = btnMod,
                            shape = btnShape
                        ) { Text("Manage Accounts") }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate("notifications") },
                            modifier = btnMod,
                            shape = btnShape
                        ) { Text("Notifications") }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate("reports") },
                            modifier = btnMod,
                            shape = btnShape
                        ) { Text("Reports") }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        try {
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}