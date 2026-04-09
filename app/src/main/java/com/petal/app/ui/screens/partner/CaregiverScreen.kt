package com.petal.app.ui.screens.partner

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
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver Guide") },
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

            PetalCard(containerColor = Teal50) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = Teal600,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Supporting a young person",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Teal800
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This guide helps parents and caregivers support teens through their menstrual health journey with confidence and sensitivity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Teal800
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CaregiverSection(
                title = "Starting the conversation",
                items = listOf(
                    "Use calm, matter-of-fact language about periods and body changes",
                    "Let them know periods are normal and nothing to be embarrassed about",
                    "Ask open-ended questions: 'How are you feeling about it?'",
                    "Share your own experiences if comfortable and appropriate",
                    "Make sure they know they can always come to you with questions"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CaregiverSection(
                title = "Practical preparation",
                items = listOf(
                    "Help them build a 'period kit' for school (pads, spare underwear, pain relief)",
                    "Show them how to use different menstrual products",
                    "Keep supplies stocked at home without them having to ask",
                    "Help them track their cycle so they can anticipate their period",
                    "Discuss when to see a doctor (very heavy bleeding, severe pain, irregular cycles after 2 years)"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CaregiverSection(
                title = "Emotional support",
                items = listOf(
                    "Validate their feelings -- PMS and period emotions are real and physiological",
                    "Don't dismiss complaints of pain or discomfort",
                    "Offer comfort: heating pad, warm drink, quiet time",
                    "Be patient with mood changes and don't take irritability personally",
                    "Respect their privacy and growing need for independence"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CaregiverSection(
                title = "When to seek medical help",
                items = listOf(
                    "Periods haven't started by age 15",
                    "Periods are consistently extremely painful (interfering with school/activities)",
                    "Bleeding is very heavy (soaking through a pad every hour)",
                    "Cycles are still very irregular after the first 2 years",
                    "They show signs of an eating disorder or extreme weight changes",
                    "They express concerns about their body or development"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            PetalCard(containerColor = Lavender50) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Age-appropriate information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Lavender700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Petal's education section automatically adjusts content based on age group. " +
                                "You can review what your teen sees in the Education tab. All content is based on " +
                                "guidelines from ACOG, CDC, and other medical authorities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Lavender700
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CaregiverSection(
    title: String,
    items: List<String>
) {
    PetalCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = Teal500,
                        modifier = Modifier
                            .size(8.dp)
                            .padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        item,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
