package com.example.kasku.data.local

import android.content.Context
import com.example.kasku.ui.screens.insights.ChatMessage
import com.example.kasku.ui.screens.insights.ChatSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class ChatSessionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Obrolan Baru",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessageSerializable> = emptyList()
)

@Serializable
data class ChatMessageSerializable(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            sender = if (sender == "USER") ChatSender.USER else ChatSender.AI,
            text = text,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageSerializable {
            return ChatMessageSerializable(
                id = msg.id,
                sender = msg.sender.name,
                text = msg.text,
                timestamp = msg.timestamp
            )
        }
    }
}

class ChatHistoryManager(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
    }

    private val file: File
        get() = File(context.filesDir, "kasku_chat_history.json")

    suspend fun loadSessions(): List<ChatSessionItem> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext emptyList()
            val content = file.readText()
            if (content.isBlank()) return@withContext emptyList()
            json.decodeFromString<List<ChatSessionItem>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveSessions(sessions: List<ChatSessionItem>) = withContext(Dispatchers.IO) {
        try {
            val content = json.encodeToString(sessions)
            file.writeText(content)
        } catch (_: Exception) {}
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }
}
