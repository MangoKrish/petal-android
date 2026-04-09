package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.petal.app.data.model.*
import com.petal.app.data.repository.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class EducationUiState(
    val ageGroup: AgeGroup = AgeGroup.Age25Plus,
    val interests: List<EducationInterest> = emptyList(),
    val cards: List<EducationContent> = emptyList(),
    val intro: String = "",
    val phaseContent: List<PhaseContent> = emptyList(),
    val selectedCategory: QuestionCategory = QuestionCategory.Periods,
    val questionText: String = "",
    val lastAnswer: EducationQuestion? = null
)

@HiltViewModel
class EducationViewModel @Inject constructor(
    private val educationRepository: EducationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EducationUiState())
    val uiState: StateFlow<EducationUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        val state = _uiState.value
        val cards = educationRepository.getEducationCards(state.ageGroup, state.interests)
        val intro = educationRepository.getEducationIntro(state.ageGroup)
        val phases = educationRepository.getPhaseContent()

        _uiState.update {
            it.copy(cards = cards, intro = intro, phaseContent = phases)
        }
    }

    fun updateAgeGroup(ageGroup: AgeGroup) {
        _uiState.update { it.copy(ageGroup = ageGroup) }
        loadContent()
    }

    fun toggleInterest(interest: EducationInterest) {
        _uiState.update { state ->
            val current = state.interests.toMutableList()
            if (interest in current) current.remove(interest)
            else current.add(interest)
            state.copy(interests = current)
        }
        loadContent()
    }

    fun updateCategory(category: QuestionCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateQuestionText(text: String) {
        _uiState.update { it.copy(questionText = text) }
    }

    fun askQuestion() {
        val state = _uiState.value
        if (state.questionText.isBlank()) return

        val answer = educationRepository.answerHealthQuestion(
            state.questionText,
            state.selectedCategory
        )
        _uiState.update { it.copy(lastAnswer = answer, questionText = "") }
    }
}
