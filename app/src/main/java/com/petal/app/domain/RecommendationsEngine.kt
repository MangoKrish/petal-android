package com.petal.app.domain

import javax.inject.Inject
import javax.inject.Singleton

data class Recommendation(
    val title: String,
    val items: List<String>
)

data class PhaseRecommendations(
    val phase: String,
    val summary: String,
    val diet: Recommendation,
    val exercise: Recommendation,
    val sleep: Recommendation,
    val selfCare: Recommendation
)

/**
 * Complete port of the web app's recommendations engine (lib/recommendations.ts).
 * Provides phase-specific recommendations for diet, exercise, sleep, and self-care.
 */
@Singleton
class RecommendationsEngine @Inject constructor() {

    private val data = mapOf(
        "Menstrual" to PhaseRecommendations(
            phase = "Menstrual",
            summary = "Your body is shedding its lining. Focus on replenishing iron, staying warm, and gentle movement. Rest is productive right now.",
            diet = Recommendation(
                title = "Nutrition -- Replenish & Comfort",
                items = listOf(
                    "Iron-rich: spinach, lentils, lean red meat, tofu, fortified cereals",
                    "Pair with vitamin C (citrus, bell peppers) to boost iron absorption",
                    "Anti-inflammatory: salmon, walnuts, turmeric, ginger tea",
                    "Dark chocolate (70%+) -- magnesium helps cramps",
                    "Warm soups, stews, and broths for comfort and hydration",
                    "Avoid excess caffeine and salt -- they worsen bloating"
                )
            ),
            exercise = Recommendation(
                title = "Movement -- Gentle & Restorative",
                items = listOf(
                    "Walking -- 20-30 min, low impact, boosts circulation",
                    "Restorative yoga -- child's pose, reclined butterfly, gentle twists",
                    "Light stretching -- hips, lower back, hamstrings",
                    "Skip HIIT and heavy lifting on heavy-flow days",
                    "Listen to your body -- rest days are okay"
                )
            ),
            sleep = Recommendation(
                title = "Sleep -- Prioritize Recovery",
                items = listOf(
                    "Aim for 8-9 hours -- your body needs extra rest during menstruation",
                    "Sleep on your side with a pillow between your knees for cramp relief",
                    "Keep bedroom cool (18-20C / 65-68F) -- body temp drops during this phase",
                    "Use a heating pad on lower abdomen before bed if cramps disrupt sleep",
                    "Avoid screens 30 min before bed -- blue light disrupts melatonin",
                    "REM cycles matter -- try to sleep in 90-min blocks (7.5h or 9h)"
                )
            ),
            selfCare = Recommendation(
                title = "Self-Care -- Be Gentle",
                items = listOf(
                    "Warm bath with epsom salts for muscle relaxation",
                    "Journal -- processing emotions helps during low-energy phases",
                    "Cancel plans that drain you -- it's okay to say no",
                    "Heating pad or hot water bottle for cramp relief"
                )
            )
        ),
        "Follicular" to PhaseRecommendations(
            phase = "Follicular",
            summary = "Estrogen is rising -- energy, mood, and creativity peak. This is your power phase. Go big on workouts and new projects.",
            diet = Recommendation(
                title = "Nutrition -- Fuel Performance",
                items = listOf(
                    "Lean protein: chicken, fish, eggs, Greek yogurt, legumes",
                    "Complex carbs: oats, sweet potatoes, quinoa, brown rice",
                    "Fermented foods: kimchi, yogurt, sauerkraut -- gut health peaks now",
                    "Fresh fruits and vegetables -- your body absorbs nutrients efficiently",
                    "Pre-workout: banana + peanut butter 30 min before exercise",
                    "Stay hydrated -- 2-3L water daily"
                )
            ),
            exercise = Recommendation(
                title = "Movement -- Push Yourself",
                items = listOf(
                    "HIIT -- your body handles high intensity best right now",
                    "Strength training -- estrogen supports muscle recovery",
                    "Running, cycling, or swimming at higher intensity",
                    "Try a new sport or class -- your brain craves novelty",
                    "Best time for personal records and challenging goals"
                )
            ),
            sleep = Recommendation(
                title = "Sleep -- Maintain Routine",
                items = listOf(
                    "7-8 hours is usually sufficient -- energy is naturally high",
                    "Consistent wake time matters more than total hours",
                    "Morning sunlight within 30 min of waking sets your circadian clock",
                    "You may feel alert later -- avoid compensating with caffeine after 2pm",
                    "Use this energy wisely but don't build sleep debt"
                )
            ),
            selfCare = Recommendation(
                title = "Self-Care -- Social & Creative",
                items = listOf(
                    "Schedule challenging conversations and creative projects",
                    "Try new experiences -- your brain is primed for learning",
                    "Social time feels energizing now -- lean into connections",
                    "Start new habits -- motivation is highest in this phase"
                )
            )
        ),
        "Ovulation" to PhaseRecommendations(
            phase = "Ovulation",
            summary = "Estrogen peaks, LH surges. Peak energy, confidence, and social drive. Maintain balanced nutrition -- your metabolism speeds up slightly.",
            diet = Recommendation(
                title = "Nutrition -- Anti-Inflammatory Focus",
                items = listOf(
                    "Anti-inflammatory: berries, fatty fish (salmon, sardines), leafy greens",
                    "Fiber-rich: broccoli, Brussels sprouts, flaxseeds -- supports estrogen metabolism",
                    "Hydrating foods: cucumber, watermelon, celery, coconut water",
                    "Light, fresh meals -- heavy foods can cause sluggishness",
                    "Zinc-rich: pumpkin seeds, chickpeas -- supports reproductive health",
                    "Moderate caffeine is fine -- don't overdo it"
                )
            ),
            exercise = Recommendation(
                title = "Movement -- Peak Performance",
                items = listOf(
                    "This is your athletic peak -- go for personal bests",
                    "High-intensity interval training (HIIT)",
                    "Heavy strength training -- muscles recover fastest now",
                    "Group sports, dance, or competitive activities",
                    "Long runs, cycling, or swimming at race pace"
                )
            ),
            sleep = Recommendation(
                title = "Sleep -- Stay Consistent",
                items = listOf(
                    "7-8 hours is ideal -- you may naturally sleep a bit less",
                    "Body temperature rises slightly -- keep bedroom extra cool",
                    "Elevated energy can make it harder to wind down -- try a calming routine",
                    "Magnesium before bed can help with restlessness",
                    "Don't sacrifice sleep for socializing -- protect your routine"
                )
            ),
            selfCare = Recommendation(
                title = "Self-Care -- Connect & Celebrate",
                items = listOf(
                    "Peak confidence -- great time for presentations, dates, interviews",
                    "Your communication skills are sharpest now",
                    "Celebrate your body and what it can do",
                    "Take photos -- many people feel their best around ovulation"
                )
            )
        ),
        "Luteal" to PhaseRecommendations(
            phase = "Luteal",
            summary = "Progesterone rises, then drops. PMS symptoms may appear. Focus on calming foods, moderate exercise, and extra sleep. Be patient with yourself.",
            diet = Recommendation(
                title = "Nutrition -- Stabilize & Soothe",
                items = listOf(
                    "Magnesium-rich: dark chocolate, almonds, cashews, pumpkin seeds",
                    "Complex carbs: sweet potatoes, oats, brown rice -- stabilize serotonin",
                    "B6-rich foods: chickpeas, bananas, potatoes -- helps with PMS",
                    "Calcium-rich: yogurt, leafy greens -- reduces PMS symptoms by 30-50%",
                    "Reduce salt, alcohol, and excess sugar -- they worsen bloating and mood",
                    "Cravings are physiological -- satisfy them mindfully, not guiltily"
                )
            ),
            exercise = Recommendation(
                title = "Movement -- Moderate & Mindful",
                items = listOf(
                    "Moderate-intensity: Pilates, swimming, brisk walking",
                    "Yoga -- especially hip openers and twists for bloating",
                    "Strength training at lighter weights, higher reps",
                    "Nature walks -- fresh air and sunlight boost serotonin",
                    "Reduce HIIT frequency -- cortisol is already elevated"
                )
            ),
            sleep = Recommendation(
                title = "Sleep -- Extra Rest Needed",
                items = listOf(
                    "Aim for 8-9 hours -- progesterone makes you sleepier",
                    "Your body temperature is highest -- keep room very cool (17-19C / 63-66F)",
                    "Melatonin production may be disrupted -- dim lights 1 hour before bed",
                    "REM sleep quality often drops pre-period -- extend total sleep time to compensate",
                    "Magnesium glycinate before bed can improve sleep quality",
                    "Weighted blankets can reduce anxiety and improve deep sleep"
                )
            ),
            selfCare = Recommendation(
                title = "Self-Care -- Protect Your Peace",
                items = listOf(
                    "Reduce social commitments -- alone time recharges during this phase",
                    "Warm baths, cozy blankets, comfort routines",
                    "Journal about feelings -- PMS emotions are valid but temporary",
                    "Prep for your period: stock up on comfort items, clear your schedule",
                    "Practice self-compassion -- this phase is the hardest and that's okay"
                )
            )
        )
    )

    fun getRecommendations(phase: String): PhaseRecommendations =
        data[phase] ?: data["Follicular"]!!
}
