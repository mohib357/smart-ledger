package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onSkip: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App name header
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(if (isSignUpMode) R.string.signup_title else R.string.login_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Name field — signup mode only
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(R.string.signup_name_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(stringResource(R.string.login_email_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.login_password_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        // Error message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Primary action button
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = {
                                    if (isSignUpMode) {
                                        onSignUp(name.trim(), email.trim(), password)
                                    } else {
                                        onSignIn(email.trim(), password)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = email.isNotBlank() && password.isNotBlank() &&
                                        (!isSignUpMode || name.isNotBlank())
                            ) {
                                Text(
                                    text = stringResource(if (isSignUpMode) R.string.login_signup_button else R.string.login_button),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Forgot password — sign-in mode only
                        if (!isSignUpMode) {
                            TextButton(
                                onClick = { onForgotPassword(email.trim()) },
                                enabled = !isLoading
                            ) {
                                Text(stringResource(R.string.login_forgot_password))
                            }
                        }

                        // Toggle sign-up / sign-in mode
                        TextButton(
                            onClick = {
                                isSignUpMode = !isSignUpMode
                                name = ""
                                password = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = if (isSignUpMode)
                                    stringResource(R.string.login_button)
                                else
                                    stringResource(R.string.login_signup_button)
                            )
                        }

                        // Skip button
                        TextButton(
                            onClick = onSkip,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "এখন না, পরে",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
