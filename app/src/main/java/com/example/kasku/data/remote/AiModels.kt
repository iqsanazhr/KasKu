package com.example.kasku.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Gemini Request / Response DTOs
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String // base64
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double = 0.2
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null
)

// OpenAI / LM Studio Request & Response DTOs
@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double = 0.2
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: List<OpenAiContentPart>
)

@Serializable
data class OpenAiContentPart(
    val type: String, // "text" or "image_url"
    val text: String? = null,
    val imageUrl: OpenAiImageUrl? = null
)

@Serializable
data class OpenAiImageUrl(
    val url: String // data:image/jpeg;base64,...
)

@Serializable
data class OpenAiChatResponse(
    val id: String? = null,
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@Serializable
data class OpenAiChoice(
    val index: Int? = null,
    val message: OpenAiResponseMessage? = null
)

@Serializable
data class OpenAiResponseMessage(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class OpenAiError(
    val message: String? = null,
    val type: String? = null
)
