package com.example.kasku.data.remote

import android.util.Base64
import com.example.kasku.domain.model.AiFinancialSummary
import com.example.kasku.domain.model.AiProvider
import com.example.kasku.domain.model.ReceiptItem
import com.example.kasku.domain.model.ReceiptScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(
        provider: AiProvider,
        geminiApiKey: String,
        geminiModel: String,
        lmStudioUrl: String,
        lmStudioModel: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Balas satu kata: ONLINE"
            val response = when (provider) {
                AiProvider.GEMINI -> callGeminiText(prompt, geminiApiKey, geminiModel)
                AiProvider.LM_STUDIO -> callLmStudioText(prompt, lmStudioUrl, lmStudioModel)
            }
            Result.success("Koneksi berhasil! Respon: ${response.trim()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askFinancialAssistant(
        question: String,
        financialContext: String,
        provider: AiProvider,
        geminiApiKey: String,
        geminiModel: String,
        lmStudioUrl: String,
        lmStudioModel: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Kamu adalah asisten keuangan pribadi KasKu yang cerdas, bijak, ramah, dan profesional.
                
                $financialContext

                Pertanyaan Pengguna:
                $question

                Panduan Menjawab:
                1. Jika pengguna menanyakan tentang saldo, dompet (wallet), rekening bank, e-wallet, atau uang tunai, sebutkan secara jelas rincian nama dompet, saldonya, serta total saldo keseluruhan berdasarkan data di atas.
                2. Jika pengguna bertanya tentang pengeluaran, pemasukan, atau tips hemat/anggaran, berikan analisis yang relevan, praktis, dan akurat.
                3. Jawab secara ramah, ringkas, dan jelas dalam 2-4 kalimat berbahasa Indonesia.
            """.trimIndent()

            val response = when (provider) {
                AiProvider.GEMINI -> callGeminiText(prompt, geminiApiKey, geminiModel)
                AiProvider.LM_STUDIO -> callLmStudioText(prompt, lmStudioUrl, lmStudioModel)
            }
            Result.success(response.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanReceipt(
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
        provider: AiProvider,
        geminiApiKey: String,
        geminiModel: String,
        lmStudioUrl: String,
        lmStudioModel: String
    ): Result<ReceiptScanResult> = withContext(Dispatchers.IO) {
        try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val prompt = """
                Kamu adalah asisten keuangan KasKu untuk memindai struk belanja (receipt scanner).
                Analisis gambar struk belanja berikut dengan teliti.
                Kembalikan HANYA JSON murni (tanpa penjelasan tambahan, tanpa markdown formatting) dengan format persis seperti ini:
                {
                  "storeName": "Nama Toko atau Merchant",
                  "dateString": "YYYY-MM-DD atau null jika tidak jelas",
                  "totalAmount": 55000.0,
                  "suggestedCategory": "Makanan & Minuman",
                  "items": [
                    {"name": "Nama Produk", "quantity": 1, "price": 25000.0}
                  ],
                  "notes": "Catatan singkat"
                }
                Kategori yang disarankan harus salah satu dari:
                ["Makanan & Minuman", "Belanja & Supermarket", "Transportasi", "Tagihan & Utilitas", "Hiburan & Rekreasi", "Kesehatan", "Pendidikan & Buku"]

                Pedoman Mata Uang Asing & Kurs Valas:
                Jika struk bertransaksi dalam valuta asing (seperti USD $, SGD S$, EUR €, MYR RM, JPY ¥, GBP £):
                1. Kenali simbol mata uang aslinya.
                2. Konversikan totalAmount ke estimasi Rupiah (IDR) berdasarkan kurs referensi wajar (misal: 1 USD ≈ Rp 16.200, 1 SGD ≈ Rp 12.200, 1 EUR ≈ Rp 17.500, 1 MYR ≈ Rp 3.650, 1 JPY ≈ Rp 105, 1 GBP ≈ Rp 20.800).
                3. Cantumkan rincian nilai asli mata uang tersebut pada field "notes" (contoh: "Dikonversi dari ${'$'}14.50 USD dengan estimasi kurs 1 USD = Rp 16.200").
            """.trimIndent()

            val rawResponse = when (provider) {
                AiProvider.GEMINI -> callGeminiVision(prompt, base64Image, mimeType, geminiApiKey, geminiModel)
                AiProvider.LM_STUDIO -> callLmStudioVision(prompt, base64Image, mimeType, lmStudioUrl, lmStudioModel)
            }

            val cleanedJson = cleanJsonResponse(rawResponse)
            val parsedResult = parseReceiptJson(cleanedJson)
            Result.success(parsedResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun parseNaturalLanguageEntry(
        text: String,
        provider: AiProvider,
        geminiApiKey: String,
        geminiModel: String,
        lmStudioUrl: String,
        lmStudioModel: String
    ): Result<ReceiptScanResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Ekstrak transaksi keuangan dari kalimat berikut: "$text"
                Kembalikan HANYA JSON murni tanpa markdown dengan format:
                {
                  "storeName": "Keterangan/Tempat/Nama Transaksi",
                  "dateString": null,
                  "totalAmount": 25000.0,
                  "suggestedCategory": "Makanan & Minuman",
                  "items": [],
                  "notes": "Catatan transaksi"
                }
                Kategori yang disarankan harus salah satu dari:
                ["Makanan & Minuman", "Belanja & Supermarket", "Transportasi", "Tagihan & Utilitas", "Hiburan & Rekreasi", "Kesehatan", "Pendidikan & Buku", "Gaji & Pendapatan", "Investasi & Dividen", "Freelance & Usaha"]
            """.trimIndent()

            val rawResponse = when (provider) {
                AiProvider.GEMINI -> callGeminiText(prompt, geminiApiKey, geminiModel)
                AiProvider.LM_STUDIO -> callLmStudioText(prompt, lmStudioUrl, lmStudioModel)
            }

            val cleanedJson = cleanJsonResponse(rawResponse)
            val parsedResult = parseReceiptJson(cleanedJson)
            Result.success(parsedResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateMonthlySummary(
        transactionsSummaryText: String,
        monthYear: String,
        provider: AiProvider,
        geminiApiKey: String,
        geminiModel: String,
        lmStudioUrl: String,
        lmStudioModel: String
    ): Result<AiFinancialSummary> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Kamu adalah Penasihat Keuangan Cerdas KasKu (Apple-inspired smart financial advisor).
                Analisis data pengeluaran dan pemasukan bulan $monthYear berikut:
                $transactionsSummaryText

                Berikan evaluasi keuangan yang mendalam, ramah, dan profesional.
                Kembalikan HANYA JSON murni tanpa markdown:
                {
                  "overallHealthScore": 85,
                  "executiveSummary": "Ringkasan kondisi cashflow bulan ini secara jelas dan elegan dalam 2-3 kalimat.",
                  "burnRateWarning": "Peringatan laju pengeluaran atau konfirmasi bahwa ritme belanja sehat.",
                  "daysUntilBudgetRunsOut": 18,
                  "topSpendingCategories": ["Makanan & Minuman (45%)", "Belanja & Supermarket (25%)"],
                  "actionableTips": [
                    "Tips konkret 1 untuk menghemat",
                    "Tips konkret 2",
                    "Tips konkret 3"
                  ]
                }
            """.trimIndent()

            val rawResponse = when (provider) {
                AiProvider.GEMINI -> callGeminiText(prompt, geminiApiKey, geminiModel)
                AiProvider.LM_STUDIO -> callLmStudioText(prompt, lmStudioUrl, lmStudioModel)
            }

            val cleanedJson = cleanJsonResponse(rawResponse)
            val parsedJsonObj = json.parseToJsonElement(cleanedJson).jsonObject

            val summary = AiFinancialSummary(
                monthYear = monthYear,
                overallHealthScore = parsedJsonObj["overallHealthScore"]?.jsonPrimitive?.intOrNull ?: 75,
                executiveSummary = parsedJsonObj["executiveSummary"]?.jsonPrimitive?.content ?: "Kondisi keuangan bulan ini terpantau stabil.",
                burnRateWarning = parsedJsonObj["burnRateWarning"]?.jsonPrimitive?.content ?: "Laju pengeluaran masih dalam batas aman.",
                daysUntilBudgetRunsOut = parsedJsonObj["daysUntilBudgetRunsOut"]?.jsonPrimitive?.intOrNull,
                topSpendingCategories = parsedJsonObj["topSpendingCategories"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                actionableTips = parsedJsonObj["actionableTips"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAvailableGeminiModels(apiKey: String): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        try {
            val cleanKey = apiKey.trim()
            if (cleanKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("API Key Gemini tidak boleh kosong."))
            }
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw RuntimeException("Gagal menghubungi Google Gemini (${response.code}): $respBody")
                }
                val rootJson = json.parseToJsonElement(respBody).jsonObject
                val modelsArray = rootJson["models"]?.jsonArray ?: emptyList()
                val resultList = mutableListOf<Pair<String, String>>()

                for (item in modelsArray) {
                    val obj = item.jsonObject
                    val supportedMethods = obj["supportedGenerationMethods"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
                    if ("generateContent" in supportedMethods) {
                        val rawName = obj["name"]?.jsonPrimitive?.content ?: continue
                        val modelId = rawName.removePrefix("models/")
                        val displayName = obj["displayName"]?.jsonPrimitive?.content ?: modelId
                        resultList.add(modelId to displayName)
                    }
                }

                if (resultList.isEmpty()) {
                    throw RuntimeException("Tidak ada model generateContent yang ditemukan untuk API Key ini.")
                }

                // Prioritaskan model flash dan model terkini di urutan atas
                val sortedList = resultList.sortedWith(
                    compareByDescending<Pair<String, String>> { it.first.contains("3.8") }
                        .thenByDescending { it.first.contains("3.6") }
                        .thenByDescending { it.first.contains("3.5") }
                        .thenByDescending { it.first.contains("2.5-flash") }
                        .thenByDescending { it.first.contains("flash") }
                        .thenBy { it.first }
                )

                Result.success(sortedList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGeminiText(prompt: String, apiKey: String, model: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val reqBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )
        val bodyString = json.encodeToString(GeminiRequest.serializer(), reqBody)
        val request = Request.Builder()
            .url(url)
            .post(bodyString.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val respBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw RuntimeException("Gemini API Error (${response.code}): $respBody")
            }
            val geminiResponse = json.decodeFromString<GeminiResponse>(respBody)
            return geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw RuntimeException("Gemini response empty")
        }
    }

    private fun callGeminiVision(prompt: String, base64Image: String, mimeType: String, apiKey: String, model: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val reqBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Image))
                    )
                )
            )
        )
        val bodyString = json.encodeToString(GeminiRequest.serializer(), reqBody)
        val request = Request.Builder()
            .url(url)
            .post(bodyString.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val respBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw RuntimeException("Gemini API Error (${response.code}): $respBody")
            }
            val geminiResponse = json.decodeFromString<GeminiResponse>(respBody)
            return geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw RuntimeException("Gemini vision response empty")
        }
    }

    private fun callLmStudioText(prompt: String, baseUrl: String, model: String): String {
        val endpoint = "${baseUrl.removeSuffix("/")}/chat/completions"
        val reqBody = OpenAiChatRequest(
            model = model,
            messages = listOf(
                OpenAiMessage(
                    role = "user",
                    content = listOf(OpenAiContentPart(type = "text", text = prompt))
                )
            )
        )
        val bodyString = json.encodeToString(OpenAiChatRequest.serializer(), reqBody)
        val request = Request.Builder()
            .url(endpoint)
            .post(bodyString.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val respBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw RuntimeException("LM Studio Error (${response.code}): $respBody")
            }
            val openAiResponse = json.decodeFromString<OpenAiChatResponse>(respBody)
            return openAiResponse.choices?.firstOrNull()?.message?.content
                ?: throw RuntimeException("LM Studio response empty")
        }
    }

    private fun callLmStudioVision(prompt: String, base64Image: String, mimeType: String, baseUrl: String, model: String): String {
        val endpoint = "${baseUrl.removeSuffix("/")}/chat/completions"
        val dataUri = "data:$mimeType;base64,$base64Image"
        val reqBody = OpenAiChatRequest(
            model = model,
            messages = listOf(
                OpenAiMessage(
                    role = "user",
                    content = listOf(
                        OpenAiContentPart(type = "text", text = prompt),
                        OpenAiContentPart(type = "image_url", imageUrl = OpenAiImageUrl(url = dataUri))
                    )
                )
            )
        )
        val bodyString = json.encodeToString(OpenAiChatRequest.serializer(), reqBody)
        val request = Request.Builder()
            .url(endpoint)
            .post(bodyString.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val respBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw RuntimeException("LM Studio Vision Error (${response.code}): $respBody")
            }
            val openAiResponse = json.decodeFromString<OpenAiChatResponse>(respBody)
            return openAiResponse.choices?.firstOrNull()?.message?.content
                ?: throw RuntimeException("LM Studio vision response empty")
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        // Extract substring between first '{' and last '}'
        val startIdx = text.indexOf('{')
        val endIdx = text.lastIndexOf('}')
        return if (startIdx >= 0 && endIdx >= startIdx) {
            text.substring(startIdx, endIdx + 1)
        } else {
            text
        }
    }

    private fun parseReceiptJson(cleanedJson: String): ReceiptScanResult {
        val obj = json.parseToJsonElement(cleanedJson).jsonObject
        val storeName = obj["storeName"]?.jsonPrimitive?.content ?: "Toko / Merchant"
        val dateString = obj["dateString"]?.jsonPrimitive?.content
        val totalAmount = obj["totalAmount"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val suggestedCategory = obj["suggestedCategory"]?.jsonPrimitive?.content ?: "Belanja & Supermarket"
        val notes = obj["notes"]?.jsonPrimitive?.content ?: ""

        val items = obj["items"]?.jsonArray?.mapNotNull { itemElem ->
            try {
                val itemObj = itemElem.jsonObject
                ReceiptItem(
                    name = itemObj["name"]?.jsonPrimitive?.content ?: "Item",
                    quantity = itemObj["quantity"]?.jsonPrimitive?.intOrNull ?: 1,
                    price = itemObj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                )
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()

        return ReceiptScanResult(
            storeName = storeName,
            dateString = dateString,
            totalAmount = totalAmount,
            suggestedCategory = suggestedCategory,
            items = items,
            notes = notes
        )
    }
}
