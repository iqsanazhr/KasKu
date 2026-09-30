package com.example.kasku.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kasku.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kasku_settings")

class AiPreferences(val context: Context) {
    companion object {
        val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val KEY_LM_STUDIO_URL = stringPreferencesKey("lm_studio_url")
        val KEY_LM_STUDIO_MODEL = stringPreferencesKey("lm_studio_model")
    }

    val aiProviderFlow: Flow<AiProvider> = context.dataStore.data.map { preferences ->
        val providerStr = preferences[KEY_AI_PROVIDER] ?: AiProvider.GEMINI.name
        try {
            AiProvider.valueOf(providerStr)
        } catch (e: Exception) {
            AiProvider.GEMINI
        }
    }

    val geminiApiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY] ?: ""
    }

    val geminiModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_MODEL] ?: "gemini-2.5-flash"
    }

    val lmStudioUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LM_STUDIO_URL] ?: "http://10.0.2.2:1234/v1"
    }

    val lmStudioModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LM_STUDIO_MODEL] ?: "gemma-3-4b-it"
    }

    suspend fun saveAiProvider(provider: AiProvider) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AI_PROVIDER] = provider.name
        }
    }

    suspend fun saveGeminiConfig(apiKey: String, model: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GEMINI_API_KEY] = apiKey.trim()
            preferences[KEY_GEMINI_MODEL] = model.trim()
        }
    }

    suspend fun saveLmStudioConfig(url: String, model: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LM_STUDIO_URL] = url.trim().removeSuffix("/")
            preferences[KEY_LM_STUDIO_MODEL] = model.trim()
        }
    }
}
