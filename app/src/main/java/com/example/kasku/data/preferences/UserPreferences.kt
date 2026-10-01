package com.example.kasku.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val KEY_TUTORIAL_TRANSACTIONS = booleanPreferencesKey("tutorial_transactions_completed")
        val KEY_TUTORIAL_INSIGHTS = booleanPreferencesKey("tutorial_insights_completed")
        val KEY_TUTORIAL_SETTINGS = booleanPreferencesKey("tutorial_settings_completed")
        val KEY_TUTORIAL_AI_CHAT = booleanPreferencesKey("tutorial_ai_chat_completed")
        val KEY_SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
    }

    val selectedCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CURRENCY] ?: "IDR"
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: ""
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isTutorialCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TUTORIAL_COMPLETED] ?: false
    }

    val isTutorialTransactionsCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TUTORIAL_TRANSACTIONS] ?: false
    }

    val isTutorialInsightsCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TUTORIAL_INSIGHTS] ?: false
    }

    val isTutorialSettingsCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TUTORIAL_SETTINGS] ?: false
    }

    val isTutorialAiChatCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TUTORIAL_AI_CHAT] ?: false
    }

    suspend fun saveSelectedCurrency(currencyCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_CURRENCY] = currencyCode.uppercase().trim()
        }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name.trim()
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = false
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_COMPLETED] = completed
        }
    }

    suspend fun setTutorialTransactionsCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_TRANSACTIONS] = completed
        }
    }

    suspend fun setTutorialInsightsCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_INSIGHTS] = completed
        }
    }

    suspend fun setTutorialSettingsCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_SETTINGS] = completed
        }
    }

    suspend fun setTutorialAiChatCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_AI_CHAT] = completed
        }
    }

    suspend fun resetTutorial() {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_COMPLETED] = false
            preferences[KEY_TUTORIAL_TRANSACTIONS] = false
            preferences[KEY_TUTORIAL_INSIGHTS] = false
            preferences[KEY_TUTORIAL_SETTINGS] = false
            preferences[KEY_TUTORIAL_AI_CHAT] = false
        }
    }
}
