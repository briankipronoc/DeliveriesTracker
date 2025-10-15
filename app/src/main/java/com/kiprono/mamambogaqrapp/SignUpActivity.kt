package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiprono.mamambogaqrapp.ui.theme.*
import androidx.compose.material3.OutlinedTextFieldDefaults

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MamaMbogaQRAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = SoftCream) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
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
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validation rules
    val isNameValid = fullName.trim().matches(Regex("^\\w+\\s+\\w+$"))
    val isUsernameValid = username.matches(Regex(".+@.+\\..+"))
    val isPhoneValid = phone.length == 9 && phone.matches(Regex("^(7|1)\\d{8}$"))
    val hasUppercase = password.contains(Regex("[A-Z]"))
    val hasLowercase = password.contains(Regex("[a-z]"))
    val hasSymbol = password.contains(Regex("[!@#\$%^&*]"))
    val isLengthValid = password.length >= 8
    val isPasswordStrong = hasUppercase && hasLowercase && hasSymbol && isLengthValid
    val isPasswordMatch = password == confirmPassword && confirmPassword.isNotEmpty()

    val isFormReady =
        isNameValid && isUsernameValid && isPhoneValid && isPasswordStrong && isPasswordMatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Full name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Email Address", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 9) phone = it },
                    label = { Text("Phone Number", color = Color.Gray) },
                    leadingIcon = { Text("🇰🇪 +254 ", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = MarketGreen
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.Gray) },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = MarketGreen
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                // Password Rules Card
                Spacer(modifier = Modifier.height(12.dp))
                PasswordRulesCard(
                    hasUppercase,
                    hasLowercase,
                    hasSymbol,
                    isLengthValid,
                    isPasswordMatch
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isFormReady) {
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
                    enabled = isFormReady,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormReady) MarketGreen else Color.LightGray
                    )
                ) {
                    Text("Sign Up", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = {
            context.startActivity(Intent(context, LoginActivity::class.java))
            if (context is SignUpActivity) context.finish()
        }) {
            Text("Already have an account? Log In", color = MamaOrange)
        }
    }
}

@Composable
fun PasswordRulesCard(
    hasUppercase: Boolean,
    hasLowercase: Boolean,
    hasSymbol: Boolean,
    isLengthValid: Boolean,
    isPasswordMatch: Boolean
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = SoftCream)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            PasswordRuleItem("At least 8 characters", isLengthValid)
            PasswordRuleItem("Contains uppercase letter", hasUppercase)
            PasswordRuleItem("Contains lowercase letter", hasLowercase)
            PasswordRuleItem("Contains special symbol", hasSymbol)
            PasswordRuleItem("Passwords match", isPasswordMatch)
        }
    }
}

@Composable
fun PasswordRuleItem(text: String, satisfied: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (satisfied) MarketGreen else Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = if (satisfied) MarketGreen else Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MarketGreen,
    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
    cursorColor = MarketGreen,
    focusedLabelColor = MarketGreen,
    unfocusedLabelColor = Color.Gray)