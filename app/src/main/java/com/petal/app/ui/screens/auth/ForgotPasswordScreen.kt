package com.petal.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalButton
import com.petal.app.ui.components.PetalTextField
import com.petal.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1 = email, 2 = answer, 3 = success

    LaunchedEffect(uiState.securityQuestion) {
        if (uiState.securityQuestion != null && step == 1) step = 2
    }

    LaunchedEffect(uiState.passwordResetSuccess) {
        if (uiState.passwordResetSuccess) step = 3
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (step) {
                1 -> {
                    Text(
                        "Enter your email address to look up your security question.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PetalTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = { Icon(Icons.Default.Email, null) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PetalButton(
                        text = "Find account",
                        onClick = { viewModel.getSecurityQuestion(email) },
                        isLoading = uiState.isLoading,
                        enabled = email.isNotBlank()
                    )
                }

                2 -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Security Question",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                uiState.securityQuestion ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    PetalTextField(
                        value = securityAnswer,
                        onValueChange = { securityAnswer = it },
                        label = "Your answer",
                        leadingIcon = { Icon(Icons.Default.Security, null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PetalTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New password (8+ characters)",
                        isPassword = true,
                        leadingIcon = { Icon(Icons.Default.Lock, null) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PetalButton(
                        text = "Reset password",
                        onClick = { viewModel.resetPassword(email, securityAnswer, newPassword) },
                        isLoading = uiState.isLoading,
                        enabled = securityAnswer.isNotBlank() && newPassword.length >= 8
                    )
                }

                3 -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Password reset successful!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can now sign in with your new password.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    PetalButton(
                        text = "Back to sign in",
                        onClick = onNavigateBack
                    )
                }
            }
        }
    }
}
