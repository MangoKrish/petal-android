package com.petal.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AgeGroup(val display: String) {
    Under13("Under 13"),
    Age13to15("13-15"),
    Age16to18("16-18"),
    Age19to24("19-24"),
    Age25Plus("25+");

    companion object {
        fun fromAge(age: Int): AgeGroup = when {
            age < 13 -> Under13
            age <= 15 -> Age13to15
            age <= 18 -> Age16to18
            age <= 24 -> Age19to24
            else -> Age25Plus
        }
    }
}

@Serializable
enum class EducationInterest(val display: String) {
    Periods("Periods"),
    Pregnancy("Pregnancy"),
    Sex("Sex"),
    Contraception("Contraception"),
    Relationships("Relationships"),
    BodyChanges("Body changes"),
    STIs("STIs"),
    Consent("Consent");
}

@Serializable
enum class QuestionCategory(val display: String) {
    Periods("Periods"),
    Pregnancy("Pregnancy"),
    Sex("Sex"),
    Relationships("Relationships");
}

@Serializable
data class EducationContent(
    val title: String,
    val summary: String,
    val interest: EducationInterest,
    val ageGroups: List<AgeGroup>,
    val sourceLabel: String,
    val sourceUrl: String
)

@Serializable
data class EducationQuestion(
    val id: String,
    val question: String,
    val category: QuestionCategory,
    val answerTitle: String,
    val answer: String,
    val guidance: String,
    val sourceLabel: String,
    val sourceUrl: String,
    val createdAt: String
)

@Serializable
data class PhaseContent(
    val id: String,
    val title: String,
    val body: String,
    val tips: List<String>
)

@Serializable
data class SymptomGuide(
    val id: String,
    val name: String,
    val causes: String,
    val levels: List<String>,
    val whenToSeeDoctor: String,
    val remedies: List<String>
)
