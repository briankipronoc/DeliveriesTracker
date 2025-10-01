@file:OptIn(ExperimentalMaterial3Api::class)

package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(context, LoginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isNameValid = remember { derivedStateOf { fullName.trim().matches(Regex("^\\w+\\s+\\w+$")) } }
    val isPasswordStrong = remember {
        derivedStateOf {
            password.length >= 8 &&
                    password.contains(Regex(".*[a-z].*")) &&
                    password.contains(Regex(".*[A-Z].*")) &&
                    password.contains(Regex(".*[!@#\$%^&*].*"))
        }
    }
    val isPhoneValid = remember {
        derivedStateOf { phone.length == 9 && phone.matches(Regex("^(7|1)\\d{8}$")) }
    }
    val isUsernameValid = remember { derivedStateOf { username.matches(Regex(".+@.+\\..+")) } }
    val isPasswordMatch = remember { derivedStateOf { password == confirmPassword && confirmPassword.isNotEmpty() } }

    val isFormReady = remember {
        derivedStateOf {
            isNameValid.value &&
                    isPasswordStrong.value &&
                    isPhoneValid.value &&
                    isUsernameValid.value &&
                    isPasswordMatch.value
        }
    }

    val buttonColors = if (isFormReady.value) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name (Two Names)") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (isNameValid.value) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Name Valid", tint = Color.Green.copy(alpha = 0.8f))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username (Email)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            trailingIcon = {
                if (isUsernameValid.value) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Email Valid", tint = Color.Green.copy(alpha = 0.8f))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 9) phone = it },
            label = { Text("Phone Number") },
            leadingIcon = {
                Text("🇰🇪 +254 ", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            trailingIcon = {
                if (isPhoneValid.value) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Phone Valid", tint = Color.Green.copy(alpha = 0.8f))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (Strong)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                if (isPasswordStrong.value) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Password Strong", tint = Color.Green.copy(alpha = 0.8f))
                } else {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                if (isPasswordMatch.value) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Password Match", tint = Color.Green.copy(alpha = 0.8f))
                } else {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (confirmPasswordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isFormReady.value) {
                    val newUser = UserStore.User(
                        username = username.trim(),
                        password = password,
                        name = fullName.trim(),
                        phone = "254${phone.trim()}",
                        email = username.trim(),
                        bikeBrand = "",
                        bikeCC = "",
                        plate = ""
                    )
                    UserStore.addUser(newUser)
                    onSignUpSuccess()
                } else {
                    Toast.makeText(context, "Please complete all fields correctly.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isFormReady.value,
            colors = buttonColors,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?")
            TextButton(onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
                if (context is SignUpActivity) {
                    context.finish()
                }
            }) {
                Text("Log In")
            }
        }
    }
}