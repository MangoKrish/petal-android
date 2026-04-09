package com.petal.app.ui.screens.education

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.QuestionCategory
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.components.PetalTextField
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.EducationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthQAScreen(
    onNavigateBack: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Q&A") },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Ask a health question",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Get safe, evidence-based answers. Not a substitute for medical advice.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category selector
            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestionCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category == uiState.selectedCategory,
                        onClick = { viewModel.updateCategory(category) },
                        label = { Text(category.display) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question input
            PetalTextField(
                value = uiState.questionText,
                onValueChange = { viewModel.updateQuestionText(it) },
                label = "Your question",
                singleLine = false,
                maxLines = 4,
                placeholder = "e.g., Are my cramps normal?"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.askQuestion() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.questionText.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get answer")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Answer
            uiState.lastAnswer?.let { answer ->
                PetalCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            answer.answerTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            answer.answer,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Gold100
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Gold700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "When to seek help",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Gold700
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    answer.guidance,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gold700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            answer.sourceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
