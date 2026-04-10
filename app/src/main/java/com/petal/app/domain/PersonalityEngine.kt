package com.petal.app.domain

import java.time.LocalTime
import kotlin.random.Random

object PersonalityEngine {

    fun getGreeting(name: String, phase: String, cycleDay: Int): String {
        val firstName = name.split(" ").first()
        val hour = LocalTime.now().hour
        val timeGreeting = when {
            hour < 5 -> "Late-night bloom"
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            hour < 21 -> "Good evening"
            else -> "Nighttime"
        }

        val phaseTouch = when (phase.lowercase()) {
            "menstrual" -> listOf(
                "Take it gently today, $firstName",
                "Your body is doing incredible work, $firstName",
                "Warmth and rest today, $firstName",
            )
            "follicular" -> listOf(
                "Fresh energy is building, $firstName",
                "Rising like a spring morning, $firstName",
                "New momentum today, $firstName",
            )
            "ovulation" -> listOf(
                "You're radiating, $firstName",
                "Peak energy today, $firstName",
                "Confidence is your superpower, $firstName",
            )
            "luteal" -> listOf(
                "Slow and steady, $firstName",
                "Cozy vibes today, $firstName",
                "Honor the wind-down, $firstName",
            )
            else -> listOf("$timeGreeting, $firstName")
        }

        return phaseTouch.random()
    }

    fun getPhaseVibe(phase: String): PhaseVibe {
        return when (phase.lowercase()) {
            "menstrual" -> PhaseVibe(
                emoji = "\uD83C\uDF39", // 🌹
                vibe = "Rest & Renewal",
                description = "Your body is releasing and resetting. Be gentle with yourself.",
                color = "#F43F5E",
            )
            "follicular" -> PhaseVibe(
                emoji = "\uD83C\uDF3F", // 🌿
                vibe = "Fresh Start",
                description = "Energy is rising. Great time for new ideas and adventures.",
                color = "#14B8A6",
            )
            "ovulation" -> PhaseVibe(
                emoji = "\u2728", // ✨
                vibe = "Radiant Peak",
                description = "You're at your most vibrant. Confidence and connection flow naturally.",
                color = "#F59E0B",
            )
            "luteal" -> PhaseVibe(
                emoji = "\uD83C\uDF19", // 🌙
                vibe = "Inner Calm",
                description = "Time to slow down, reflect, and nurture yourself.",
                color = "#A855F7",
            )
            else -> PhaseVibe(
                emoji = "\uD83C\uDF38", // 🌸
                vibe = "Your Cycle",
                description = "Track your cycle to unlock personalized insights.",
                color = "#F43F5E",
            )
        }
    }

    fun getEmptyStateMessage(context: String): EmptyState {
        return when (context) {
            "cycles" -> EmptyState(
                title = "Your story starts here",
                body = "Log your first cycle and watch Petal learn your unique rhythm.",
                cta = "Log your first cycle",
            )
            "insights" -> EmptyState(
                title = "Patterns take shape with time",
                body = "After a few cycles, you'll see trends, predictions, and personalized tips.",
                cta = "Start tracking",
            )
            "partner" -> EmptyState(
                title = "Share with someone who cares",
                body = "Invite a partner or caregiver to see phase-specific tips on supporting you.",
                cta = "Create invite",
            )
            "journal" -> EmptyState(
                title = "Your private space",
                body = "Write freely. Your journal entries are encrypted and only visible to you.",
                cta = "Write your first entry",
            )
            else -> EmptyState(
                title = "Welcome to Petal",
                body = "Your cycle intelligence companion. Private, personal, and always learning.",
                cta = "Get started",
            )
        }
    }

    fun getCelebrationMessage(event: String, count: Int = 0): CelebrationMessage {
        return when (event) {
            "first_cycle" -> CelebrationMessage(
                title = "First Bloom!",
                body = "You've logged your very first cycle. Petal is already learning your rhythm.",
                emoji = "\uD83C\uDF31",
            )
            "streak_3" -> CelebrationMessage(
                title = "Three-Peat!",
                body = "3 days in a row. You're building a beautiful habit.",
                emoji = "\uD83D\uDD25",
            )
            "streak_7" -> CelebrationMessage(
                title = "Week Warrior!",
                body = "A full week of daily logging. Your insights are getting sharper.",
                emoji = "\u26A1",
            )
            "streak_30" -> CelebrationMessage(
                title = "Month of Mindfulness!",
                body = "30 days of tracking. You know your body better than ever.",
                emoji = "\uD83C\uDFC6",
            )
            "partner_connected" -> CelebrationMessage(
                title = "Better Together!",
                body = "Your partner is now connected. They'll see tips on how to support you best.",
                emoji = "\uD83D\uDC95",
            )
            else -> CelebrationMessage(
                title = "Achievement Unlocked!",
                body = "You've reached a new milestone. Keep going!",
                emoji = "\uD83C\uDF89",
            )
        }
    }

    fun getLoadingMessage(): String {
        val messages = listOf(
            "Consulting the moon phases...",
            "Counting flower petals...",
            "Brewing some herbal tea...",
            "Reading the stars...",
            "Gathering your insights...",
            "Tending to your garden...",
            "Warming up your data...",
        )
        return messages[Random.nextInt(messages.size)]
    }

    fun getStreakMessage(days: Int): String {
        return when {
            days == 0 -> "Start your streak today!"
            days == 1 -> "First check-in today!"
            days < 3 -> "$days days in a row"
            days < 7 -> "$days-day streak \uD83D\uDD25"
            days < 14 -> "One week strong! \uD83D\uDCAA"
            days < 30 -> "$days days! Unstoppable \u26A1"
            days < 90 -> "$days days of mindfulness \uD83C\uDFC6"
            else -> "$days days — legendary! \uD83D\uDC51"
        }
    }
}

data class PhaseVibe(
    val emoji: String,
    val vibe: String,
    val description: String,
    val color: String,
)

data class EmptyState(
    val title: String,
    val body: String,
    val cta: String,
)

data class CelebrationMessage(
    val title: String,
    val body: String,
    val emoji: String,
)
