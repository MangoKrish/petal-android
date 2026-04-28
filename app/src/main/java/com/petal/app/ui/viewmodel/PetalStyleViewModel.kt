package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.local.PetalPreferences
import com.petal.app.ui.components.kawaii.PetalStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetalStyleViewModel @Inject constructor(
    private val prefs: PetalPreferences,
) : ViewModel() {

    val style: StateFlow<PetalStyle> = prefs.petalStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetalStyle.SAKURA)

    val enabled: StateFlow<Boolean> = prefs.petalBackgroundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setStyle(s: PetalStyle) {
        viewModelScope.launch { prefs.setPetalStyle(s) }
    }

    fun setEnabled(on: Boolean) {
        viewModelScope.launch { prefs.setPetalBackgroundEnabled(on) }
    }
}
