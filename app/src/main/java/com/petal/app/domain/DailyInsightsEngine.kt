package com.petal.app.domain

import com.petal.app.data.model.DailyInsight
import com.petal.app.data.model.DayInsights
import com.petal.app.data.model.InsightCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Complete port of the web app's daily insights engine (lib/daily-insights.ts).
 * Generates phase-specific daily cards with nutrition, exercise, and self-care advice,
 * plus partner-facing notes for the partner dashboard.
 */
@Singleton
class DailyInsightsEngine @Inject constructor() {

    fun getDailyInsights(cycleDay: Int, cycleLength: Int, userName: String): DayInsights {
        val menstrualEnd = 5
        val follicularEnd = (cycleLength * 0.46).roundToInt()
        val ovulationEnd = follicularEnd + 2

        return when {
            cycleDay <= menstrualEnd -> getMenstrualInsights(cycleDay, userName)
            cycleDay <= follicularEnd -> getFollicularInsights(cycleDay, userName)
            cycleDay <= ovulationEnd -> getOvulationInsights(cycleDay, userName)
            else -> getLutealInsights(cycleDay, cycleLength, userName)
        }
    }

    private fun getMenstrualInsights(cycleDay: Int, userName: String): DayInsights {
        val dayInPhase = cycleDay
        return DayInsights(
            greeting = if (dayInPhase == 1) {
                "Hey $userName, day 1 -- take it easy"
            } else {
                "Day $cycleDay -- you've got this"
            },
            phase = "Menstrual",
            hormoneNote = "Estrogen and progesterone are at their lowest. Your body is doing important work shedding the uterine lining.",
            cards = listOf(
                DailyInsight(
                    headline = "Warm comfort foods",
                    body = if (dayInPhase <= 2) {
                        "Heavy flow days call for iron-rich meals. Try lentil soup, spinach salad with citrus dressing, or beef stew."
                    } else {
                        "As flow eases, keep up iron intake. Dark chocolate (70%+) is your friend -- magnesium helps with lingering cramps."
                    },
                    tip = if (dayInPhase <= 2) {
                        "Pair iron-rich foods with vitamin C for better absorption"
                    } else {
                        "A warm cup of ginger tea after meals aids digestion"
                    },
                    emoji = "pot_of_food",
                    category = InsightCategory.Nutrition
                ),
                DailyInsight(
                    headline = if (dayInPhase <= 2) "Gentle movement only" else "Light activity",
                    body = if (dayInPhase <= 2) {
                        "Stick to walking, gentle stretching, or restorative yoga. Your body needs recovery, not performance."
                    } else {
                        "You can start adding light cardio -- a 20-minute walk or easy swim. Listen to your energy levels."
                    },
                    tip = if (dayInPhase <= 2) {
                        "Child's pose and cat-cow stretches ease lower back tension"
                    } else {
                        "Even 10 minutes of movement boosts endorphins"
                    },
                    emoji = "yoga",
                    category = InsightCategory.Exercise
                ),
                DailyInsight(
                    headline = "Rest is productive",
                    body = "Your sleep need increases during menstruation. Give yourself permission to go to bed 30-60 minutes earlier than usual.",
                    tip = "A heating pad on your lower abdomen before bed helps with sleep quality",
                    emoji = "bathtub",
                    category = InsightCategory.SelfCare
                )
            ),
            partnerNote = "$userName is on day $cycleDay of their period. They may be experiencing cramps, fatigue, and lower energy. Ways to help: bring them a warm drink, offer a heating pad, be patient with lower energy levels, and don't take quietness personally."
        )
    }

    private fun getFollicularInsights(cycleDay: Int, userName: String): DayInsights {
        return DayInsights(
            greeting = "Good morning $userName! Energy rising",
            phase = "Follicular",
            hormoneNote = "Estrogen is climbing, boosting serotonin and dopamine. You'll feel sharper, more creative, and more social.",
            cards = listOf(
                DailyInsight(
                    headline = "Fuel your momentum",
                    body = "Your metabolism is efficient right now. Focus on lean proteins (chicken, fish, eggs), complex carbs (sweet potato, quinoa), and fermented foods for gut health.",
                    tip = "Pre-workout: banana + peanut butter 30 minutes before exercise",
                    emoji = "green_salad",
                    category = InsightCategory.Nutrition
                ),
                DailyInsight(
                    headline = "Push yourself today",
                    body = "This is your athletic peak window. HIIT, strength training, running -- your body recovers faster now thanks to rising estrogen. Set new goals.",
                    tip = "Great day to try a new workout class or increase your weights",
                    emoji = "flexed_biceps",
                    category = InsightCategory.Exercise
                ),
                DailyInsight(
                    headline = "Start something new",
                    body = "Your brain craves novelty during the follicular phase. Start that project, have the hard conversation, make the plan.",
                    tip = "Schedule important decisions and creative work this week",
                    emoji = "rocket",
                    category = InsightCategory.SelfCare
                )
            ),
            partnerNote = "$userName is in their follicular phase (rising energy). They're likely feeling social, creative, and energized. Great time for date nights, trying new activities together, and having deeper conversations."
        )
    }

    private fun getOvulationInsights(cycleDay: Int, userName: String): DayInsights {
        return DayInsights(
            greeting = "Hey $userName, you're glowing",
            phase = "Ovulation",
            hormoneNote = "Estrogen peaks, LH surges. This is your most fertile window. Energy, confidence, and social drive are at their highest.",
            cards = listOf(
                DailyInsight(
                    headline = "Anti-inflammatory focus",
                    body = "Eat colorful: berries, leafy greens, fatty fish. Fiber-rich foods (broccoli, flaxseeds) help metabolize the estrogen surge. Stay hydrated.",
                    tip = "Zinc-rich foods like pumpkin seeds support reproductive health",
                    emoji = "fish",
                    category = InsightCategory.Nutrition
                ),
                DailyInsight(
                    headline = "Peak performance",
                    body = "Your athletic performance peaks around ovulation. Go for personal bests, competitive activities, or high-energy group workouts.",
                    tip = "Recovery is fastest now -- push harder than usual",
                    emoji = "woman_running",
                    category = InsightCategory.Exercise
                ),
                DailyInsight(
                    headline = "Your communication superpower",
                    body = "Verbal fluency and social confidence peak at ovulation. Use this for presentations, interviews, important conversations, or networking.",
                    tip = "Schedule your most important meetings this week",
                    emoji = "speech_balloon",
                    category = InsightCategory.SelfCare
                )
            ),
            partnerNote = "$userName is ovulating -- peak energy, confidence, and social drive. They may want more connection, activity, and conversation. This is their most outgoing phase. Match their energy!"
        )
    }

    private fun getLutealInsights(cycleDay: Int, cycleLength: Int, userName: String): DayInsights {
        val daysUntilPeriod = cycleLength - cycleDay
        val isLateLuteal = daysUntilPeriod <= 5

        return DayInsights(
            greeting = if (isLateLuteal) {
                "Hang in there $userName"
            } else {
                "Winding down, $userName"
            },
            phase = "Luteal",
            hormoneNote = if (isLateLuteal) {
                "Progesterone is dropping sharply. PMS symptoms peak. This is temporary -- your body is preparing for the next cycle."
            } else {
                "Progesterone is elevated, which can cause bloating, cravings, and lower mood. This is your body's preparation phase."
            },
            cards = listOf(
                DailyInsight(
                    headline = if (isLateLuteal) "Craving-smart eating" else "Stabilize your mood with food",
                    body = if (isLateLuteal) {
                        "Cravings are physiological, not weakness. Satisfy them mindfully: dark chocolate, sweet potato, trail mix. Reduce salt and alcohol to minimize bloating."
                    } else {
                        "Complex carbs (oats, brown rice, sweet potatoes) boost serotonin. Magnesium-rich foods (almonds, dark chocolate) ease PMS. Calcium reduces symptoms by 30-50%."
                    },
                    tip = if (isLateLuteal) {
                        "Pre-make healthy snacks so you don't reach for junk"
                    } else {
                        "A banana before bed provides B6 and tryptophan for better sleep"
                    },
                    emoji = "chocolate_bar",
                    category = InsightCategory.Nutrition
                ),
                DailyInsight(
                    headline = if (isLateLuteal) "Be gentle with yourself" else "Moderate and mindful",
                    body = if (isLateLuteal) {
                        "Swap HIIT for walking, yoga, or swimming. Your cortisol is already elevated -- intense exercise adds more stress."
                    } else {
                        "Pilates, nature walks, moderate strength training. Your body benefits from movement but doesn't need to be pushed to the max."
                    },
                    tip = if (isLateLuteal) {
                        "A 20-minute walk in nature reduces cortisol more than a gym session"
                    } else {
                        "Hip-opening yoga poses help with bloating and tension"
                    },
                    emoji = "woman_walking",
                    category = InsightCategory.Exercise
                ),
                DailyInsight(
                    headline = if (isLateLuteal) {
                        val dayWord = if (daysUntilPeriod != 1) "days" else "day"
                        "Period prep: $daysUntilPeriod $dayWord away"
                    } else {
                        "Protect your peace"
                    },
                    body = if (isLateLuteal) {
                        "Stock up: heating pad, comfort snacks, dark chocolate, tea. Clear your schedule where possible. Your period is coming -- preparation reduces stress."
                    } else {
                        "Reduce social commitments if you feel drained. Journal, take warm baths, practice self-compassion. PMS emotions are valid but temporary."
                    },
                    tip = if (isLateLuteal) {
                        "Pack your period kit tonight: pads/tampons, pain relief, cozy clothes"
                    } else {
                        "Weighted blankets can reduce anxiety and improve deep sleep"
                    },
                    emoji = if (isLateLuteal) "package" else "bed",
                    category = InsightCategory.SelfCare
                )
            ),
            partnerNote = if (isLateLuteal) {
                val dayWord = if (daysUntilPeriod != 1) "days" else "day"
                "$userName is $daysUntilPeriod $dayWord from their period. PMS symptoms are likely peaking -- mood swings, bloating, fatigue, cravings. How to help: don't take irritability personally, bring comfort food, suggest a cozy night in, be extra patient and supportive."
            } else {
                "$userName is in the luteal phase (winding down). They may be more tired, moody, or withdrawn than usual. This is hormonal and temporary. Ways to help: respect their need for quiet time, be flexible with plans, and don't pressure them into high-energy activities."
            }
        )
    }
}
