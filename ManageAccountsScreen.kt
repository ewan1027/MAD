package com.nibm.tmapp.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nibm.tmapp.model.Role
import com.nibm.tmapp.model.User
import com.nibm.tmapp.utils.Resource
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color

@Composable
fun ManageAccountsScreen(
    adminViewModel: AdminViewModel = viewModel(),
    navController: NavController? = null // Pass NavController if available
) {
    val usersState by adminViewModel.users.collectAsState()
    val updateState by adminViewModel.updateState.collectAsState()
    val deleteState by adminViewModel.deleteState.collectAsState()
    val context = LocalContext.current
    val addUserState by adminViewModel.addUserState.collectAsState()

    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        when (updateState) {
            is Resource.Success -> Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
            is Resource.Error -> Toast.makeText(context, "Error updating user: ${(updateState as Resource.Error<Unit>).message}", Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is Resource.Success -> Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
            is Resource.Error -> Toast.makeText(context, "Error deleting user: ${(deleteState as Resource.Error<Unit>).message}", Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    LaunchedEffect(addUserState) {
        when (addUserState) {
            is Resource.Success -> {
                Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show()
                adminViewModel.resetAddUserState()
            }
            is Resource.Error -> {
                Toast.makeText(context, "Error adding user: ${(addUserState as Resource.Error<Unit>).message}", Toast.LENGTH_LONG).show()
                adminViewModel.resetAddUserState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(text = "Manage Accounts", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showAddUserDialog = true }, modifier = Modifier.align(Alignment.End)) {
            Text("Add User")
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (usersState) {
            is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is Resource.Success -> {
                val users = (usersState as Resource.Success<List<User>>).data ?: emptyList()
                val teachers = users.filter { it.roles.contains(Role.TEACHER) }
                val students = users.filter { it.roles.contains(Role.STUDENT) }
                LazyColumn {
                    if (teachers.isNotEmpty()) {
                        item {
                            Text("Teachers", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(teachers) { teacherUser ->
    UserListItem(
        user = teacherUser,
        onEditClick = { userToEdit = teacherUser },
        onDeleteClick = { userToDelete = teacherUser },
        onLogoutClick = { teacherUser ->
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            if (teacherUser.uid == currentUid) {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                navController?.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
        showLogout = teacherUser.uid == FirebaseAuth.getInstance().currentUser?.uid
    )
    HorizontalDivider()
                        }
                    }
                    if (students.isNotEmpty()) {
                        item {
                            Text("Students", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(students) { studentUser ->
                            UserListItem(
                                user = studentUser,
                                onEditClick = { userToEdit = studentUser },
                                onDeleteClick = { userToDelete = studentUser },
                                onLogoutClick = { studentUser ->
                                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                                    if (studentUser.uid == currentUid) {
                                        FirebaseAuth.getInstance().signOut()
                                        Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                                        navController?.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                showLogout = studentUser.uid == FirebaseAuth.getInstance().currentUser?.uid
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
            is Resource.Error -> {
                Text(
                    text = "Error: ${(usersState as Resource.Error<List<User>>).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    userToEdit?.let {
        EditUserDialog(user = it, onDismiss = { userToEdit = null }, onConfirm = { newRole ->
            adminViewModel.updateUserRole(it, newRole)
            userToEdit = null
        })
    }

    userToDelete?.let {
        DeleteUserDialog(user = it, onDismiss = { userToDelete = null }, onConfirm = {
            adminViewModel.deleteUser(it)
            userToDelete = null
        })
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { email, password, role ->
                adminViewModel.addUser(email, password, role)
                showAddUserDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Role) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(Role.STUDENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    TextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(Role.TEACHER, Role.STUDENT).forEach { role ->
                            DropdownMenuItem(text = { Text(role.name) }, onClick = {
                                selectedRole = role
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(email, password, selectedRole) }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun UserListItem(
    user: User,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    onLogoutClick: (User) -> Unit,
    showLogout: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.email, style = MaterialTheme.typography.bodyLarge)
            Text(text = user.roles.joinToString(", ") { it.name }, style = MaterialTheme.typography.bodyMedium)
        }
        Row {
            Button(onClick = { onEditClick(user) }) {
                Text("Edit")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onDeleteClick(user) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
            if (showLogout) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onLogoutClick(user) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Logout")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(user: User, onDismiss: () -> Unit, onConfirm: (Role) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(user.roles.firstOrNull() ?: Role.STUDENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User Role") },
        text = { 
            Column {
                Text("Select a new role for ${user.email}")
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    TextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        Role.entries.forEach { role ->
                            DropdownMenuItem(text = { Text(role.name) }, onClick = { 
                                selectedRole = role
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedRole) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteUserDialog(user: User, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete User") },
        text = { Text("Are you sure you want to delete ${user.email}? This action cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
