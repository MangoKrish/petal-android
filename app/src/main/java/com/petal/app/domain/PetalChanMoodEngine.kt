package com.petal.app.domain

import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.components.kawaii.PetalChanMood

/**
 * Picks a Petal-chan expression + a soft kawaii line based on what the user
 * just logged and where they are in the cycle. Quiet, never alarmist.
 */
object PetalChanMoodEngine {

    fun moodFor(
        phase: CyclePhase,
        isOnPeriod: Boolean,
        recentSymptoms: List<String> = emptyList(),
        streakDays: Int = 0,
        flowIntensity: String? = null,
    ): PetalChanMood {
        val sym = recentSymptoms.map { it.lowercase() }
        if (streakDays >= 7) return PetalChanMood.BLOOM
        if (sym.any { it.contains("cramp") || it.contains("headache") }) return PetalChanMood.CRAMPS
        if (sym.any { it.contains("fatigue") || it.contains("sleep") }) return PetalChanMood.SLEEPY
        if (flowIntensity?.lowercase() == "heavy") return PetalChanMood.HEAVY
        if (isOnPeriod) return PetalChanMood.CRAMPS
        return when (phase) {
            CyclePhase.Ovulation -> PetalChanMood.BLOOM
            CyclePhase.Follicular -> PetalChanMood.HAPPY
            CyclePhase.Luteal -> PetalChanMood.SLEEPY
            CyclePhase.Menstrual -> PetalChanMood.CRAMPS
        }
    }

    fun quoteFor(mood: PetalChanMood, cycleDay: Int): String {
        val pool: List<String> = when (mood) {
            PetalChanMood.HAPPY -> listOf(
                "you're glowing today! drink some water, okay?",
                "soft breezes, soft beginnings ⊹",
                "let's start something tiny and lovely ✿",
            )
            PetalChanMood.LOVED -> listOf(
                "you are so loved today ♡",
                "a little kindness goes a long way ⌒",
            )
            PetalChanMood.SLEEPY -> listOf(
                "be tender with yourself today ⌒",
                "small comforts count — wear soft socks?",
                "rest gently — i'll keep watch ♡",
            )
            PetalChanMood.CRAMPS -> listOf(
                "warm tea and slow movements, okay?",
                "you're doing the brave work of just being today ⌒",
                "i packed your favourite snack in spirit ♡",
            )
            PetalChanMood.HEAVY -> listOf(
                "soft layers, soft pace ❀",
                "i brought you a tissue and a hug ⌒",
            )
            PetalChanMood.BLOOM -> listOf(
                "you're in full bloom today ✿",
                "look at you, glowing ♡",
                "a whole streak of you taking care — i'm so proud ⌒",
            )
            PetalChanMood.WAVE -> listOf("hi friend ♡", "hello, lovely ⌒")
        }
        return pool[cycleDay.coerceAtLeast(0) % pool.size]
    }
}
