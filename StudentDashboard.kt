package com.nibm.tmapp.student

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun StudentDashboard(navController: NavController) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout, // Use a power or exit icon
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        bottomBar = {
    // Horizontal navigation bar
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { navController.navigate("student_attendance") }) {
            Icon(Icons.Filled.EventAvailable, contentDescription = "Attendance")
        }
        IconButton(onClick = { navController.navigate("student_assignments") }) {
            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Assignments")
        }
        IconButton(onClick = { navController.navigate("student_materials") }) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Course Materials")
        }
    }
},
floatingActionButton = {
    FloatingActionButton(
        onClick = { navController.navigate("qr_scanner") },
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Quick Action")
    }
},
        content = { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Welcome, Student!",
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(64.dp))
                // Cards/Sections

                Column(Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Upcoming Assignments", style = MaterialTheme.typography.titleMedium)
                            val assignmentsViewModel: ViewAssignmentsViewModel = viewModel()
                            val assignmentListState by assignmentsViewModel.assignmentListState.collectAsState()
                            when (assignmentListState) {
                                is AssignmentListState.Loading -> {
                                    CircularProgressIndicator(Modifier.size(16.dp))
                                }
                                is AssignmentListState.Success -> {
                                    val assignments = (assignmentListState as AssignmentListState.Success).assignments
                                    if (assignments.isNotEmpty()) {
                                        val nextAssignment = assignments.minByOrNull { it.dueDate?.time ?: Long.MAX_VALUE }
                                        Column {
                                            Text(nextAssignment?.title ?: "-", style = MaterialTheme.typography.bodyMedium)
                                            Text("Due: " + nextAssignment?.dueDate?.let { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it) } ?: "-", style = MaterialTheme.typography.bodySmall)
                                        }
                                    } else {
                                        Text("No upcoming assignments.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                is AssignmentListState.Error -> {
                                    Text("Failed to load assignments", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Attendance Summary", style = MaterialTheme.typography.titleMedium)
                            val attendanceViewModel: ViewAttendanceViewModel = viewModel()
                            val attendanceListState by attendanceViewModel.attendanceListState.collectAsState()
                            when (attendanceListState) {
                                is AttendanceListState.Loading -> {
                                    CircularProgressIndicator(Modifier.size(16.dp))
                                }
                                is AttendanceListState.Success -> {
                                    val records = (attendanceListState as AttendanceListState.Success).records
                                    if (records.isNotEmpty()) {
                                        Text("Total Present: ${records.size}", style = MaterialTheme.typography.bodyMedium)
                                        val last = records.maxByOrNull { it.timestamp }
                                        Text("Last: " + last?.timestamp?.let { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it) } ?: "-", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text("No attendance records.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                is AttendanceListState.Error -> {
                                    Text("Failed to load attendance", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Notifications", style = MaterialTheme.typography.titleMedium)
                        val notificationsViewModel: StudentNotificationsViewModel = viewModel()
                        val notificationListState by notificationsViewModel.notificationListState.collectAsState()
                        when (notificationListState) {
                            is NotificationListState.Loading -> {
                                CircularProgressIndicator(Modifier.size(16.dp))
                            }
                            is NotificationListState.Success -> {
                                val notifications = (notificationListState as NotificationListState.Success).notifications
                                if (notifications.isNotEmpty()) {
                                    val latest = notifications.maxByOrNull { it.timestamp }
                                    Column {
                                        Text(latest?.title ?: "-", style = MaterialTheme.typography.bodyMedium)
                                        Text(latest?.message ?: "-", style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Text("No notifications.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            is NotificationListState.Error -> {
                                Text("Failed to load notifications", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    )

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    try {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}