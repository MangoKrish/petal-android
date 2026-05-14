package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.repository.StoryBookmarksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** PHASE_6_7_PLAN.md §7.1 — exposes the bookmark set + toggle. */
@HiltViewModel
class StoryBookmarksViewModel @Inject constructor(
    private val repository: StoryBookmarksRepository
) : ViewModel() {

    val bookmarks: StateFlow<Set<String>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggle(storyId: String, next: Boolean) {
        viewModelScope.launch { repository.toggle(storyId, next) }
    }
}
