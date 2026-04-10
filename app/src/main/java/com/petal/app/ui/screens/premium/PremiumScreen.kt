package com.petal.app.ui.screens.premium

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petal.app.ui.components.GlassCard
import com.petal.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    isPremium: Boolean = false,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Petal Premium") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium badge
            val infiniteTransition = rememberInfiniteTransition(label = "premium")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "glow"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DarkRoseSoft.copy(alpha = glowAlpha),
                                DarkLavenderSoft.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isPremium) "\uD83D\uDC8E" else "\uD83C\uDF38",
                    fontSize = 40.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPremium) "You're Premium!" else "Unlock Your Full Potential",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = if (isPremium)
                    "Thank you for supporting Petal. All premium features are unlocked."
                else
                    "Get unlimited history, advanced analytics, and more for just \$3.99/mo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            // Feature list
            val features = listOf(
                FeatureItem(Icons.Default.AllInclusive, "Unlimited History", "Access all your past cycle data"),
                FeatureItem(Icons.Default.Analytics, "Mood Heatmap", "Visualize emotional patterns over time"),
                FeatureItem(Icons.Default.Compare, "Cycle Comparison", "Compare cycles side by side"),
                FeatureItem(Icons.Default.BubbleChart, "Symptom Correlation", "Find patterns between symptoms"),
                FeatureItem(Icons.Default.People, "Partner Dashboard", "Full partner insights and sharing"),
                FeatureItem(Icons.Default.FileDownload, "Data Export", "Export your data as CSV"),
                FeatureItem(Icons.Default.AutoGraph, "Priority Predictions", "Enhanced Bayesian predictions"),
                FeatureItem(Icons.Default.Book, "Wellness Journal", "Private reflection space"),
            )

            features.forEach { feature ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    cornerRadius = 16.dp,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = feature.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (isPremium) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Included",
                                tint = SuccessColor,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isPremium) {
                Button(
                    onClick = { openStripeCheckout(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        "Subscribe — \$3.99/month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = "Cancel anytime. No commitment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun openStripeCheckout(context: Context) {
    val baseUrl = "https://petal-web-mangokrishs-projects.vercel.app"
    val checkoutUrl = "$baseUrl/?view=settings&premium=checkout"

    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(checkoutUrl))
    } catch (e: Exception) {
        // Fallback to regular browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
        context.startActivity(intent)
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)
