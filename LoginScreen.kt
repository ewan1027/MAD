package com.nibm.tmapp.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nibm.tmapp.model.Role

import com.nibm.tmapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val loginState by authViewModel.loginState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Login to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = loginState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023), contentColor = Color.White)
            ) {
                if (loginState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Login", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("signup") }) {
                Text(
                    text = "Don't have an account? Sign up",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Handle login state changes
            LaunchedEffect(loginState) {
                when (val state = loginState) {
                    is Resource.Success -> {
                        val user = state.data
                        if (user != null) {
                            val destination = when {
                                user.roles.contains(Role.ADMIN) -> "admin_dashboard"
                                user.roles.contains(Role.TEACHER) -> "teacher_dashboard"
                                user.roles.contains(Role.STUDENT) -> "student_dashboard"
                                else -> "login" // fallback if no known role
                            }
                            navController.navigate(destination) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        // Handle other states if necessary
                    }
                }
            }
        }
    }
}
