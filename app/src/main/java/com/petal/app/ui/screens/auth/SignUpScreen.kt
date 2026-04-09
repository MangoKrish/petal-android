package com.petal.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

private val securityQuestions = listOf(
    "What was the name of your first pet?",
    "What city were you born in?",
    "What is your mother's maiden name?",
    "What was your childhood nickname?",
    "What is the name of your favorite teacher?"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf(securityQuestions[0]) }
    var securityAnswer by remember { mutableStateOf("") }
    var questionDropdownExpanded by remember { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create account") },
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
            Spacer(modifier = Modifier.height(16.dp))

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

            PetalTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PetalTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PetalTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password (8+ characters)",
                isPassword = true,
                passwordVisible = passwordVisible,
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PetalTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                isPassword = true,
                passwordVisible = passwordVisible,
                isError = passwordMismatch,
                errorMessage = if (passwordMismatch) "Passwords don't match" else null,
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Security question (for password recovery)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = questionDropdownExpanded,
                onExpandedChange = { questionDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedQuestion,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionDropdownExpanded) },
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = questionDropdownExpanded,
                    onDismissRequest = { questionDropdownExpanded = false }
                ) {
                    securityQuestions.forEach { question ->
                        DropdownMenuItem(
                            text = { Text(question) },
                            onClick = {
                                selectedQuestion = question
                                questionDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PetalTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = "Your answer",
                leadingIcon = { Icon(Icons.Default.Security, null) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PetalButton(
                text = "Create account",
                onClick = {
                    viewModel.register(name, email, password, selectedQuestion, securityAnswer, onSignUpSuccess)
                },
                isLoading = uiState.isLoading,
                enabled = name.isNotBlank() && email.isNotBlank() &&
                        password.length >= 8 && password == confirmPassword &&
                        securityAnswer.isNotBlank()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateBack) {
                    Text("Sign in", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
