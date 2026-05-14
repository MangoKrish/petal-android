package com.petal.app.data.model

/**
 * Static card library for Android. PHASE_6_7_PLAN.md §6A.3.
 * Mirrors the web adapter (new-project/src/lib/education-cards.ts) with a
 * tighter selection while the API seed migration is pending. Replace with
 * remote fetch from /education/cards when the seed lands.
 */
object EducationCards {
    val ALL: List<EducationCard> = listOf(
        EducationCard(
            id = "phase-menstrual",
            category = EducationCategory.Period,
            title = "Menstrual phase (days 1–5)",
            bodyShort = "Estrogen and progesterone hit their lowest point and the uterine lining sheds. Cramps, fatigue, and lower-back pain are common — your body's doing real work. Many people feel quieter or lower-energy, and that's fine. Periods usually last 3–7 days, heavier in the first two.",
            source = "ACOG · Mayo Clinic",
            tags = listOf("cycle", "menstrual"),
            audience = EducationAudience.All,
            readingTimeSeconds = 22,
        ),
        EducationCard(
            id = "phase-follicular",
            category = EducationCategory.Body,
            title = "Follicular phase (days 1–13)",
            bodyShort = "Estrogen rises, the lining rebuilds, and serotonin/dopamine lift. Many feel a natural climb in energy and focus. Good window for harder workouts and tackling big projects.",
            source = "ACOG · Cleveland Clinic",
            tags = listOf("cycle", "follicular"),
            audience = EducationAudience.All,
            readingTimeSeconds = 18,
        ),
        EducationCard(
            id = "phase-ovulation",
            category = EducationCategory.Fertility,
            title = "Ovulation (around day 14)",
            bodyShort = "An LH surge releases the egg. Cervical mucus turns clear and stretchy (egg-white) and basal body temperature ticks up by 0.2–0.5°C after. Trying to conceive: the 5 days before plus the day of ovulation are your fertile window.",
            source = "Mayo Clinic · ACOG",
            tags = listOf("cycle", "ovulation", "ttc"),
            audience = EducationAudience.All,
            readingTimeSeconds = 22,
        ),
        EducationCard(
            id = "phase-luteal",
            category = EducationCategory.Body,
            title = "Luteal phase (days 15–28)",
            bodyShort = "Progesterone rises, then drops sharply if there's no pregnancy — that drop drives PMS. Bloating, breast tenderness, and mood shifts are physiological, not personal failings. Magnesium-rich foods and complex carbs help.",
            source = "ACOG · NHS",
            tags = listOf("cycle", "luteal", "pms"),
            audience = EducationAudience.All,
            readingTimeSeconds = 22,
        ),
        EducationCard(
            id = "symptom-cramps",
            category = EducationCategory.Body,
            title = "Cramps (dysmenorrhea)",
            bodyShort = "Prostaglandins drive the uterine contractions that shed the lining; higher prostaglandin levels mean stronger cramps. Heat is as effective as ibuprofen in some studies. NSAIDs taken at the first twinge work better than waiting.",
            source = "ACOG · Mayo Clinic",
            tags = listOf("symptom", "cramps"),
            audience = EducationAudience.All,
            readingTimeSeconds = 22,
        ),
        EducationCard(
            id = "myth-exercise",
            category = EducationCategory.Myth,
            title = "Is it safe to exercise during my period?",
            bodyShort = "Yes — and it can actually reduce cramping, lift mood, and ease fatigue. Match the intensity to how you feel. Some athletes report personal bests during menstruation. Tampons, cups, and discs all let you swim.",
            source = "ACOG · NHS",
            tags = listOf("faq", "exercise"),
            audience = EducationAudience.All,
            readingTimeSeconds = 18,
        ),
        EducationCard(
            id = "pcos-overview",
            category = EducationCategory.Pcos,
            title = "PCOS — what it actually is",
            bodyShort = "PCOS is a hormonal pattern, not a single disease. Signs include irregular cycles, elevated androgens (acne, hirsutism), and small ovarian cysts. It's diagnosed by symptoms + bloodwork + ultrasound — none alone is enough. Treatment is symptom-led, not one-size-fits-all.",
            source = "ACOG · NHS",
            tags = listOf("pcos"),
            audience = EducationAudience.All,
            readingTimeSeconds = 22,
        ),
        EducationCard(
            id = "warning-heavy-bleeding",
            category = EducationCategory.Body,
            title = "When heavy bleeding warrants a check",
            bodyShort = "Soaking through a pad an hour for several hours, or passing clots larger than a quarter, can cause iron deficiency and may signal fibroids, polyps, adenomyosis, or a clotting disorder. Effective treatments exist — you don't have to just 'deal with it'.",
            source = "ACOG",
            tags = listOf("warning", "see-doctor"),
            audience = EducationAudience.All,
            readingTimeSeconds = 18,
        ),
        EducationCard(
            id = "warning-severe-pain",
            category = EducationCategory.Body,
            title = "Severe pain that NSAIDs don't touch",
            bodyShort = "Cramps that ibuprofen or naproxen can't manage, or pain that stops you from working or studying, deserves evaluation. It can indicate endometriosis, adenomyosis, ovarian cysts, or pelvic inflammatory disease.",
            source = "ACOG · Cleveland Clinic",
            tags = listOf("warning", "see-doctor"),
            audience = EducationAudience.All,
            readingTimeSeconds = 18,
        ),
        EducationCard(
            id = "partner-pms-feels",
            category = EducationCategory.Partner,
            title = "What 'PMS' actually feels like",
            bodyShort = "PMS isn't just irritability — it can mean breast tenderness, bloating, exhaustion, sudden tearfulness, or trouble sleeping for a week or more. None of it is a choice. Showing up with patience, warmth, and small acts of care matters more than fixing.",
            tags = listOf("pms", "support"),
            audience = EducationAudience.Supporter,
            readingTimeSeconds = 22,
        ),
        EducationCard(
            id = "partner-rest-is-okay",
            category = EducationCategory.Partner,
            title = "Rest is a valid response",
            bodyShort = "Period ads often show people 'conquering' the day — that's marketing, not biology. The kindest thing you can do is make rest easy: warm drinks, a quiet evening, no asking 'why are you so tired'.",
            tags = listOf("rest", "support"),
            audience = EducationAudience.Supporter,
            readingTimeSeconds = 18,
        ),
        EducationCard(
            id = "partner-when-to-worry",
            category = EducationCategory.Partner,
            title = "When something's worth a doctor visit",
            bodyShort = "Heavy bleeding, severe pain that doesn't ease with rest or NSAIDs, periods that disappear for months, or new symptoms after age 40 are worth a check-in with a clinician — not panic, just attention.",
            source = "ACOG · NHS",
            tags = listOf("health", "support"),
            audience = EducationAudience.Supporter,
            readingTimeSeconds = 22,
        ),
    )

    fun filter(
        category: EducationCategory? = null,
        audience: EducationAudience = EducationAudience.All,
        tag: String? = null,
    ): List<EducationCard> = ALL.filter { c ->
        (category == null || c.category == category) &&
            (audience == EducationAudience.All || c.audience == EducationAudience.All || c.audience == audience) &&
            (tag == null || c.tags.contains(tag))
    }

    fun loadingTips(audience: EducationAudience = EducationAudience.All): List<EducationCard> =
        ALL.filter {
            it.readingTimeSeconds <= 22 &&
                (audience == EducationAudience.All || it.audience == EducationAudience.All || it.audience == audience)
        }

    fun byId(id: String): EducationCard? = ALL.firstOrNull { it.id == id }
}
