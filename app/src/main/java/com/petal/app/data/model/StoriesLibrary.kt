package com.petal.app.data.model

/**
 * PHASE_6_7_PLAN.md §7.1 — Android-side launch corpus. Mirrors
 * new-project/src/lib/stories.ts byte-for-byte; both clients read from
 * /stories API once migration 009's seed lands.
 *
 * Every entry is documented public-domain history or pulled from widely-
 * published interviews / autobiographies. Don't add stories here without
 * a citation and a clear consent_status — defamation and right-of-
 * publicity risk is real.
 */
object StoriesLibrary {
    val ALL: List<Story> = listOf(
        Story(
            id = "kiran-gandhi",
            stream = StoryStream.DidItAnyway,
            subjectName = "Kiran Gandhi",
            subjectRole = "Drummer · Activist",
            bodyShort = "When her period started the morning of the 2015 London Marathon, Kiran Gandhi made a deliberate choice — she ran the full 26.2 miles without a pad, blood visible. She wanted to call attention to the millions of people worldwide who lack access to menstrual products and to the silence around periods in sport. She finished in 4:49:11. The point wasn't endurance for its own sake; it was refusing to hide.",
            pullQuote = "I ran with blood dripping down my legs for sisters who don't have access to tampons.",
            tags = listOf("sport", "activism", "stigma"),
            source = "Cosmopolitan / The Independent / Gandhi's own essays, 2015",
        ),
        Story(
            id = "fu-yuanhui",
            stream = StoryStream.DidItAnyway,
            subjectName = "Fu Yuanhui",
            subjectRole = "Olympic swimmer",
            bodyShort = "After her 4×100m relay at the 2016 Rio Olympics, Fu Yuanhui mentioned on live TV that her period had started the night before. Chinese media gasped — talking about menstruation in elite sport simply wasn't done. She apologized for letting her teammates down, then explained matter-of-factly that she'd had cramps and felt weak. The conversation that followed cracked open a long-held silence about periods in competition.",
            pullQuote = "It's because my period came yesterday, so I felt particularly tired — but this isn't an excuse.",
            tags = listOf("sport", "olympics", "stigma"),
            source = "BBC / The Guardian, 2016",
        ),
        Story(
            id = "wilma-rudolph",
            stream = StoryStream.DidItAnyway,
            subjectName = "Wilma Rudolph",
            subjectRole = "Olympic sprinter",
            bodyShort = "Wilma Rudolph contracted polio at age four and was told she'd never walk again. By twenty she'd won three gold medals at the 1960 Rome Olympics — the first American woman to do so in a single Games. The story usually stops there. The fuller version: she trained while raising a child, navigated chronic pain in the leg polio had weakened, and refused segregated victory parades when Tennessee tried to throw her one. Doing it anyway didn't mean doing it without cost.",
            pullQuote = "Never underestimate the power of dreams and the influence of the human spirit. We are all the same in this notion.",
            tags = listOf("sport", "history", "disability"),
            source = "Smithsonian Magazine / Rudolph's autobiography, 1977",
        ),
        Story(
            id = "audre-lorde",
            stream = StoryStream.OkayToRest,
            subjectName = "Audre Lorde",
            subjectRole = "Poet · Essayist",
            bodyShort = "Audre Lorde wrote one of the most-quoted lines in modern self-care discourse — but the line is usually stripped of its context. She wrote it during her cancer treatment, between exhausting rounds of chemotherapy, while continuing to work as an organizer and mother. Rest, for her, wasn't a spa weekend. It was the protection of her body from a world that would gladly use her up. Naming that as political — not selfish — gave generations after her permission to do the same.",
            pullQuote = "Caring for myself is not self-indulgence — it is self-preservation, and that is an act of political warfare.",
            tags = listOf("rest", "writing", "self-care"),
            source = "A Burst of Light, 1988",
        ),
        Story(
            id = "frida-kahlo",
            stream = StoryStream.OkayToRest,
            subjectName = "Frida Kahlo",
            subjectRole = "Painter",
            bodyShort = "After a streetcar accident at eighteen left her body shattered, Frida Kahlo spent long stretches of her life bedridden — sometimes in body casts that immobilized her from chest to hip. She painted from bed using a custom easel, mirror over her head. The story isn't that she 'overcame' chronic pain. It's that she kept making work in the rhythms her body allowed: bursts when she could, stillness when she couldn't, never apologizing for the difference between the two.",
            pullQuote = "I tried to drown my sorrows, but the bastards learned how to swim.",
            tags = listOf("art", "chronic-pain", "rest"),
            source = "Hayden Herrera, Frida: A Biography of Frida Kahlo, 1983",
        ),
        Story(
            id = "florence-nightingale",
            stream = StoryStream.OkayToRest,
            subjectName = "Florence Nightingale",
            subjectRole = "Nurse · Statistician",
            bodyShort = "Most people remember Florence Nightingale as the Crimean War nurse with the lamp. Fewer know that for the last fifty years of her life, she rarely left her bedroom — likely a chronic illness she contracted in Crimea. From that bed she co-founded modern nursing, pioneered statistical infographics that changed public health, and corresponded with prime ministers. Resting wasn't the end of her work. It was the room her work came from.",
            pullQuote = "I attribute my success to this — I never gave or took any excuse.",
            tags = listOf("history", "rest", "chronic-illness"),
            source = "Mark Bostridge, Florence Nightingale: The Making of an Icon, 2008",
        ),
    )

    fun byStream(stream: StoryStream?): List<Story> =
        if (stream == null) ALL else ALL.filter { it.stream == stream }

    fun byId(id: String): Story? = ALL.firstOrNull { it.id == id }
}
