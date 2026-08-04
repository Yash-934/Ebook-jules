package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val THEME_INDEX = intPreferencesKey("theme_index") // 0: White, 1: Dark Blue, 2: Sepia, 3: Black
        val FONT_FAMILY_INDEX = intPreferencesKey("font_family_index")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val WORD_SPACING = floatPreferencesKey("word_spacing")
        val MARGINS = floatPreferencesKey("margins")
        val SCROLL_MODE = booleanPreferencesKey("scroll_mode")
    }

    val themeIndexFlow: Flow<Int> = context.dataStore.data.map { it[THEME_INDEX] ?: 2 } // default Sepia
    val fontFamilyIndexFlow: Flow<Int> = context.dataStore.data.map { it[FONT_FAMILY_INDEX] ?: 0 }
    val fontSizeFlow: Flow<Float> = context.dataStore.data.map { it[FONT_SIZE] ?: 16f }
    val lineSpacingFlow: Flow<Float> = context.dataStore.data.map { it[LINE_SPACING] ?: 1.5f }
    val wordSpacingFlow: Flow<Float> = context.dataStore.data.map { it[WORD_SPACING] ?: 0f }
    val marginsFlow: Flow<Float> = context.dataStore.data.map { it[MARGINS] ?: 24f }
    val scrollModeFlow: Flow<Boolean> = context.dataStore.data.map { it[SCROLL_MODE] ?: true }

    suspend fun setThemeIndex(index: Int) { context.dataStore.edit { it[THEME_INDEX] = index } }
    suspend fun setFontFamilyIndex(index: Int) { context.dataStore.edit { it[FONT_FAMILY_INDEX] = index } }
    suspend fun setFontSize(size: Float) { context.dataStore.edit { it[FONT_SIZE] = size } }
    suspend fun setLineSpacing(spacing: Float) { context.dataStore.edit { it[LINE_SPACING] = spacing } }
    suspend fun setWordSpacing(spacing: Float) { context.dataStore.edit { it[WORD_SPACING] = spacing } }
    suspend fun setMargins(margin: Float) { context.dataStore.edit { it[MARGINS] = margin } }
    suspend fun setScrollMode(scroll: Boolean) { context.dataStore.edit { it[SCROLL_MODE] = scroll } }
}
