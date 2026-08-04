package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    val themeIndex: StateFlow<Int> = settingsManager.themeIndexFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2
    )

    val fontFamilyIndex: StateFlow<Int> = settingsManager.fontFamilyIndexFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val fontSize: StateFlow<Float> = settingsManager.fontSizeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 18f
    )

    val lineSpacing: StateFlow<Float> = settingsManager.lineSpacingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.7f
    )

    val wordSpacing: StateFlow<Float> = settingsManager.wordSpacingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val margins: StateFlow<Float> = settingsManager.marginsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 35f
    )

    val scrollMode: StateFlow<Boolean> = settingsManager.scrollModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setTheme(index: Int) {
        viewModelScope.launch {
            settingsManager.setThemeIndex(index)
        }
    }

    fun setFontFamily(index: Int) {
        viewModelScope.launch {
            settingsManager.setFontFamilyIndex(index)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            settingsManager.setFontSize(size)
        }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch {
            settingsManager.setLineSpacing(spacing)
        }
    }

    fun setWordSpacing(spacing: Float) {
        viewModelScope.launch {
            settingsManager.setWordSpacing(spacing)
        }
    }

    fun setMargins(margin: Float) {
        viewModelScope.launch {
            settingsManager.setMargins(margin)
        }
    }

    fun setScrollMode(scroll: Boolean) {
        viewModelScope.launch {
            settingsManager.setScrollMode(scroll)
        }
    }
}

class SettingsViewModelFactory(private val settingsManager: SettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
