package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.repository.EducationBookmarksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PHASE_6_7_PLAN.md §6A.3 — exposes the bookmark set + a toggle action.
 * Mirrors new-project/src/hooks/use-education-bookmarks.ts.
 */
@HiltViewModel
class EducationBookmarksViewModel @Inject constructor(
    private val repository: EducationBookmarksRepository
) : ViewModel() {

    val bookmarks: StateFlow<Set<String>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggle(cardId: String, next: Boolean) {
        viewModelScope.launch { repository.toggle(cardId, next) }
    }
}
