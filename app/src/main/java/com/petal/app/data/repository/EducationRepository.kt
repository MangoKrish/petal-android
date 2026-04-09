package com.petal.app.data.repository

import com.petal.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationRepository @Inject constructor() {

    private val educationCards = listOf(
        EducationContent(
            title = "What a typical first period can look like",
            summary = "Periods can be irregular at first, and the first few years may not follow a perfect monthly pattern.",
            interest = EducationInterest.Periods,
            ageGroups = listOf(AgeGroup.Under13, AgeGroup.Age13to15),
            sourceLabel = "Based on ACOG patient guidance",
            sourceUrl = "https://www.acog.org/womens-health/faqs/your-first-period"
        ),
        EducationContent(
            title = "Painful cramps: when home care is common and when to ask for help",
            summary = "Mild to moderate cramps are common, but pain that stops school, sleep, or daily life deserves medical attention.",
            interest = EducationInterest.Periods,
            ageGroups = listOf(AgeGroup.Age13to15, AgeGroup.Age16to18, AgeGroup.Age19to24, AgeGroup.Age25Plus),
            sourceLabel = "Based on ACOG medical guidance",
            sourceUrl = "https://www.acog.org/womens-health/faqs/dysmenorrhea-painful-periods"
        ),
        EducationContent(
            title = "Pregnancy basics, simply explained",
            summary = "Pregnancy can happen any time sperm enters the vagina, even if it is a first sexual experience or timing feels uncertain.",
            interest = EducationInterest.Pregnancy,
            ageGroups = listOf(AgeGroup.Age13to15, AgeGroup.Age16to18, AgeGroup.Age19to24, AgeGroup.Age25Plus),
            sourceLabel = "Based on CDC reproductive health guidance",
            sourceUrl = "https://www.cdc.gov/teen-pregnancy/about/index.html"
        ),
        EducationContent(
            title = "Contraception options are not one-size-fits-all",
            summary = "Different birth control methods have different levels of effectiveness, ease, and side effects, so matching the method matters.",
            interest = EducationInterest.Contraception,
            ageGroups = listOf(AgeGroup.Age16to18, AgeGroup.Age19to24, AgeGroup.Age25Plus),
            sourceLabel = "Based on CDC contraception guidance",
            sourceUrl = "https://www.cdc.gov/contraception/about/index.html"
        ),
        EducationContent(
            title = "Condoms help with pregnancy prevention and STI risk",
            summary = "Condoms are one of the only methods that lower STI risk while also helping prevent pregnancy.",
            interest = EducationInterest.Sex,
            ageGroups = listOf(AgeGroup.Age13to15, AgeGroup.Age16to18, AgeGroup.Age19to24, AgeGroup.Age25Plus),
            sourceLabel = "Based on CDC sexual health guidance",
            sourceUrl = "https://www.cdc.gov/sti/prevention/index.html"
        ),
        EducationContent(
            title = "Consent should be clear, voluntary, and ongoing",
            summary = "A healthy sexual experience requires agreement that is active and can be changed at any time.",
            interest = EducationInterest.Consent,
            ageGroups = AgeGroup.entries,
            sourceLabel = "Based on public health safety guidance",
            sourceUrl = "https://www.cdc.gov/violenceprevention/sexualviolence/fastfact.html"
        ),
        EducationContent(
            title = "Healthy relationships should feel safe and respectful",
            summary = "Feeling pressured, frightened, or controlled is not a healthy relationship pattern, even when emotions are strong.",
            interest = EducationInterest.Relationships,
            ageGroups = listOf(AgeGroup.Age13to15, AgeGroup.Age16to18, AgeGroup.Age19to24, AgeGroup.Age25Plus),
            sourceLabel = "Based on public health relationship safety guidance",
            sourceUrl = "https://www.cdc.gov/violenceprevention/intimatepartnerviolence/teendatingviolence/fastfact.html"
        ),
        EducationContent(
            title = "Body changes during puberty vary a lot",
            summary = "Breast development, skin changes, vaginal discharge, and growth can all happen at different times and still be normal.",
            interest = EducationInterest.BodyChanges,
            ageGroups = listOf(AgeGroup.Under13, AgeGroup.Age13to15, AgeGroup.Age16to18),
            sourceLabel = "Based on ACOG puberty guidance",
            sourceUrl = "https://www.acog.org/womens-health/faqs/your-first-period"
        ),
        EducationContent(
            title = "STI prevention works best as a routine, not a panic step",
            summary = "Condom use, testing when needed, and honest conversations all help lower risk.",
            interest = EducationInterest.STIs,
            ageGroups = listOf(AgeGroup.Age16to18, AgeGroup.Age19to24, AgeGroup.Age25Plus),
            sourceLabel = "Based on CDC STI prevention guidance",
            sourceUrl = "https://www.cdc.gov/sti/prevention/index.html"
        )
    )

    private data class AnswerTemplate(
        val title: String,
        val answer: String,
        val guidance: String,
        val sourceLabel: String,
        val sourceUrl: String,
        val keywords: List<String>,
        val category: QuestionCategory
    )

    private val answerTemplates = listOf(
        AnswerTemplate(
            title = "Period pain can be common, but severe pain is not something you should just push through",
            answer = "Mild or moderate cramps are common. If the pain is severe, suddenly much worse, or keeps you from school, work, sleep, or daily activities, it is a good idea to get medical advice.",
            guidance = "If pain is intense, heavy bleeding is unusual for you, or you faint, seek medical care promptly.",
            sourceLabel = "Based on ACOG medical guidance",
            sourceUrl = "https://www.acog.org/womens-health/faqs/dysmenorrhea-painful-periods",
            keywords = listOf("cramp", "pain", "painful", "period hurt", "heavy bleeding", "faint"),
            category = QuestionCategory.Periods
        ),
        AnswerTemplate(
            title = "Pregnancy is possible whenever sperm reaches the vagina",
            answer = "Pregnancy can happen even if sex happens during a period, the timing feels early, or it is a first sexual experience. Timing can change from cycle to cycle, so it is safer not to rely on guesses alone.",
            guidance = "If pregnancy is a concern, consider a pregnancy test based on timing and speak with a clinician or trusted health service for next steps.",
            sourceLabel = "Based on CDC reproductive health guidance",
            sourceUrl = "https://www.cdc.gov/teen-pregnancy/about/index.html",
            keywords = listOf("pregnant", "pregnancy", "can i get pregnant", "first time", "during period", "missed period"),
            category = QuestionCategory.Pregnancy
        ),
        AnswerTemplate(
            title = "Condoms help reduce STI risk and can also help prevent pregnancy",
            answer = "Condoms are one of the only methods that help with both STI prevention and pregnancy prevention. Using them correctly every time matters.",
            guidance = "If you want pregnancy prevention plus extra reliability, a clinician can help compare contraception options.",
            sourceLabel = "Based on CDC sexual health guidance",
            sourceUrl = "https://www.cdc.gov/sti/prevention/index.html",
            keywords = listOf("condom", "safe sex", "protection", "birth control", "prevent sti"),
            category = QuestionCategory.Sex
        ),
        AnswerTemplate(
            title = "Consent should be clear, willing, and something that can change",
            answer = "A healthy sexual situation depends on both people agreeing freely. Pressure, fear, being very impaired, or not feeling able to say no are not signs of real consent.",
            guidance = "If you feel unsafe or pressured, reach out to a trusted adult, clinician, or local support service.",
            sourceLabel = "Based on public health safety guidance",
            sourceUrl = "https://www.cdc.gov/violenceprevention/sexualviolence/fastfact.html",
            keywords = listOf("consent", "pressure", "forced", "safe relationship", "healthy relationship"),
            category = QuestionCategory.Relationships
        ),
        AnswerTemplate(
            title = "Birth control choices depend on what matters most to you",
            answer = "Different contraception methods vary in effectiveness, convenience, side effects, and whether they protect against STIs. There is not one best option for everyone.",
            guidance = "A clinician or sexual health service can help compare methods based on your goals and comfort.",
            sourceLabel = "Based on CDC contraception guidance",
            sourceUrl = "https://www.cdc.gov/contraception/about/index.html",
            keywords = listOf("contraception", "birth control", "pill", "iud", "implant", "which method"),
            category = QuestionCategory.Sex
        ),
        AnswerTemplate(
            title = "Testing and prevention are both part of STI care",
            answer = "Many STIs can have few or no symptoms, so prevention and testing both matter. Condoms lower risk, and testing can be important after exposure or when starting new sexual relationships.",
            guidance = "If you think you were exposed or have symptoms, contact a clinician or local sexual health service.",
            sourceLabel = "Based on CDC STI prevention guidance",
            sourceUrl = "https://www.cdc.gov/sti/prevention/index.html",
            keywords = listOf("sti", "std", "infection", "testing", "exposed", "symptoms"),
            category = QuestionCategory.Sex
        )
    )

    fun getEducationCards(ageGroup: AgeGroup, interests: List<EducationInterest>): List<EducationContent> {
        val selectedInterests = interests.ifEmpty {
            listOf(EducationInterest.Periods, EducationInterest.BodyChanges, EducationInterest.Relationships)
        }

        val primaryCards = educationCards.filter { card ->
            card.interest in selectedInterests && ageGroup in card.ageGroups
        }

        val fallbackCards = educationCards.filter { card ->
            ageGroup in card.ageGroups
        }.take(2)

        return (if (primaryCards.isNotEmpty()) primaryCards else fallbackCards).take(6)
    }

    fun getEducationIntro(ageGroup: AgeGroup): String = when (ageGroup) {
        AgeGroup.Under13 -> "This guide stays gentle and age-appropriate, with basics on body changes, periods, boundaries, and when to ask a trusted adult for help."
        AgeGroup.Age13to15 -> "This guide focuses on clear basics about periods, body changes, boundaries, pregnancy, and respectful relationships without making things feel overwhelming."
        AgeGroup.Age16to18 -> "This guide balances practical sexual health basics with consent, contraception, STI prevention, and how to notice when something needs medical support."
        AgeGroup.Age19to24 -> "This guide focuses on practical, everyday sexual health decisions like contraception, safer sex, symptom awareness, and respectful communication."
        AgeGroup.Age25Plus -> "This guide offers a calm refresher on sexual health topics with practical reminders around periods, pregnancy, contraception, STI prevention, and care."
    }

    fun answerHealthQuestion(question: String, category: QuestionCategory): EducationQuestion {
        val normalized = question.trim().lowercase()

        val matched = answerTemplates.firstOrNull { template ->
            template.category == category && template.keywords.any { normalized.contains(it) }
        } ?: answerTemplates.firstOrNull { template ->
            template.keywords.any { normalized.contains(it) }
        } ?: AnswerTemplate(
            title = "A calm starting point",
            answer = "This app can offer general education, but it cannot safely diagnose personal medical situations. For questions about symptoms, pregnancy concerns, pain, bleeding changes, or sexual health exposure, a clinician or trusted sexual health service is the safest next step.",
            guidance = "Use the category cards for basics, and seek urgent care right away if there is severe pain, fainting, heavy bleeding, trouble breathing, or safety concerns.",
            sourceLabel = "Based on CDC and ACOG patient guidance",
            sourceUrl = "https://www.cdc.gov/contraception/about/index.html",
            keywords = emptyList(),
            category = category
        )

        return EducationQuestion(
            id = java.util.UUID.randomUUID().toString(),
            question = question,
            category = category,
            answerTitle = matched.title,
            answer = matched.answer,
            guidance = matched.guidance,
            sourceLabel = matched.sourceLabel,
            sourceUrl = matched.sourceUrl,
            createdAt = java.time.LocalDateTime.now().toString()
        )
    }

    fun getPhaseContent(): List<PhaseContent> = listOf(
        PhaseContent(
            id = "menstrual",
            title = "Menstrual Phase (Days 1-5)",
            body = "Your cycle begins on the first day of your period. Estrogen and progesterone drop to their lowest levels, triggering the uterine lining to shed. You may experience cramping, fatigue, and lower back pain. Periods typically last 3-7 days, with heavier flow in the first 1-2 days.",
            tips = listOf(
                "Apply a heating pad or hot water bottle to your lower abdomen to ease cramps.",
                "Stay hydrated and choose iron-rich foods like spinach, lentils, and red meat.",
                "Gentle movement such as walking or restorative yoga can reduce cramping.",
                "Give yourself permission to rest. Sleep needs may increase by 30-60 minutes.",
                "Track your flow (light, medium, heavy) each day for better predictions."
            )
        ),
        PhaseContent(
            id = "follicular",
            title = "Follicular Phase (Days 1-13)",
            body = "Estrogen steadily rises, rebuilding the uterine lining and boosting serotonin and dopamine. Many people notice a natural lift in energy, mood, creativity, and confidence as this phase progresses.",
            tips = listOf(
                "Channel rising energy into challenging workouts like HIIT and strength training.",
                "Great window for tackling big projects and trying new things.",
                "Eat lean protein, fermented foods, and fresh vegetables.",
                "Start tracking cervical mucus to understand your fertility window."
            )
        ),
        PhaseContent(
            id = "ovulation",
            title = "Ovulation Phase (Around Day 14)",
            body = "A surge in LH causes the dominant follicle to release a mature egg. Estrogen peaks, and you may notice heightened libido, peak energy, and increased confidence. Some feel mild pelvic twinges called mittelschmerz.",
            tips = listOf(
                "The 5 days before ovulation and the day itself are your most fertile window.",
                "Take advantage of peak energy for intense exercise and demanding tasks.",
                "Stay well-hydrated; estrogen peaks can trigger mild headaches.",
                "Use this for presentations, interviews, and important conversations."
            )
        ),
        PhaseContent(
            id = "luteal",
            title = "Luteal Phase (Days 15-28)",
            body = "Progesterone rises sharply then drops if the egg is not fertilized. This hormonal shift triggers PMS -- bloating, breast tenderness, mood changes, food cravings, fatigue. The luteal phase is relatively consistent at 12-14 days.",
            tips = listOf(
                "Swap high-intensity workouts for moderate activities like swimming or pilates.",
                "Complex carbs (oats, brown rice, sweet potatoes) boost serotonin.",
                "Magnesium-rich foods (dark chocolate, almonds) ease bloating and cramps.",
                "Prioritize sleep hygiene -- progesterone raises core body temperature.",
                "Practice self-compassion. PMS symptoms are physiological, not personal."
            )
        )
    )
}
