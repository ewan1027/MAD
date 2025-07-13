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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
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
import com.nibm.tmapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val signupState by authViewModel.signupState.collectAsState()

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
                text = "Join Us",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Create your new account",
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
            onClick = { authViewModel.signup(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = signupState !is Resource.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023), contentColor = Color.White)
        ) {
            if (signupState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = "Sign Up", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text(
                text = "Already have an account? Log in",
                style = MaterialTheme.typography.bodyMedium
            )
        }

            // Handle signup state changes
            LaunchedEffect(signupState) {
                when (val state = signupState) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Signup Successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") { // Navigate to login after successful signup
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    is Resource.Error -> {
                        val msg = (state as Resource.Error).message ?: "Unknown error"
                        if (msg.contains("Only the admin can sign up.")) {
                            Toast.makeText(context, "Only the admin can sign up.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error: $msg", Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }
}
