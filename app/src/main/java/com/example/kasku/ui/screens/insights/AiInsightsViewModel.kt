package com.example.kasku.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kasku.data.local.ChatHistoryManager
import com.example.kasku.data.local.ChatMessageSerializable
import com.example.kasku.data.local.ChatSessionItem
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import com.example.kasku.ui.components.CurrencyConfig
import com.example.kasku.ui.components.TopNotif
import com.example.kasku.ui.components.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatSender {
    USER,
    AI
}

enum class ChartPeriod(val label: String) {
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan")
}

data class CashFlowBarData(
    val label: String,
    val income: Double,
    val expense: Double,
    val timestamp: Long
)

data class CategorySpending(
    val categoryName: String,
    val categoryColor: Long,
    val totalAmount: Double,
    val percentage: Float
)

class AiInsightsViewModel(
    private val repository: KasKuRepository,
    private val aiService: AiService,
    private val aiPreferences: AiPreferences
) : ViewModel() {

    val chatHistoryManager = ChatHistoryManager(aiPreferences.context)

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val accounts: StateFlow<List<Account>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val initialGreeting = ChatMessage(
        sender = ChatSender.AI,
        text = "Halo! Saya KasKu AI, asisten keuangan pribadimu 👋\n\nKamu bisa bertanya seputar analisis pengeluaran mingguan/bulanan, evaluasi belanja, atau rekomendasi anggaran. Ada yang ingin kamu diskusikan?"
    )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(initialGreeting))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSessionItem>>(emptyList())
    val chatSessions: StateFlow<List<ChatSessionItem>> = _chatSessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String>("")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _isAnsweringQuestion = MutableStateFlow(false)
    val isAnsweringQuestion: StateFlow<Boolean> = _isAnsweringQuestion.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(ChartPeriod.WEEKLY)
    val selectedPeriod: StateFlow<ChartPeriod> = _selectedPeriod.asStateFlow()

    init {
        viewModelScope.launch {
            val savedSessions = chatHistoryManager.loadSessions()
            if (savedSessions.isNotEmpty()) {
                _chatSessions.value = savedSessions
                val firstSession = savedSessions.first()
                _currentSessionId.value = firstSession.id
                _chatMessages.value = if (firstSession.messages.isNotEmpty()) {
                    firstSession.messages.map { it.toDomain() }
                } else {
                    listOf(initialGreeting)
                }
            } else {
                createNewSession()
            }
        }
    }

    fun setChartPeriod(period: ChartPeriod) {
        _selectedPeriod.value = period
    }

    fun getWeeklyData(txList: List<Transaction>): List<CashFlowBarData> {
        val list = mutableListOf<CashFlowBarData>()
        val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endMs = cal.timeInMillis

            val dayTx = txList.filter { it.date in startMs..endMs }
            val inc = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val label = dayFormat.format(cal.time)

            list.add(CashFlowBarData(label = label, income = inc, expense = exp, timestamp = startMs))
        }
        return list
    }

    fun getMonthlyData(txList: List<Transaction>): List<CashFlowBarData> {
        val list = mutableListOf<CashFlowBarData>()
        // 4 Pekan terakhir (masing-masing 7 hari)
        for (i in 3 downTo 0) {
            val calEnd = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -(i * 7))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val calStart = Calendar.getInstance().apply {
                timeInMillis = calEnd.timeInMillis
                add(Calendar.DAY_OF_YEAR, -6)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = calStart.timeInMillis
            val endMs = calEnd.timeInMillis

            val weekTx = txList.filter { it.date in startMs..endMs }
            val inc = weekTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = weekTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val label = "Mgg ${4 - i}"

            list.add(CashFlowBarData(label = label, income = inc, expense = exp, timestamp = startMs))
        }
        return list
    }

    fun getCategorySpending(): List<CategorySpending> {
        val txList = transactions.value
        val period = _selectedPeriod.value

        val calStart = Calendar.getInstance().apply {
            val daysBack = if (period == ChartPeriod.WEEKLY) 6 else 27
            add(Calendar.DAY_OF_YEAR, -daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMs = calStart.timeInMillis

        val filteredExpenses = txList.filter { it.type == TransactionType.EXPENSE && it.date >= startMs }
        val totalExpense = filteredExpenses.sumOf { it.amount }

        if (totalExpense <= 0.0) return emptyList()

        return filteredExpenses.groupBy { it.categoryName }
            .map { (catName, items) ->
                val sum = items.sumOf { it.amount }
                val color = items.firstOrNull()?.categoryColor ?: 0xFF00A389
                val percent = (sum / totalExpense).toFloat()
                CategorySpending(
                    categoryName = catName,
                    categoryColor = color,
                    totalAmount = sum,
                    percentage = percent
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    fun createNewSession() {
        val newSessionId = UUID.randomUUID().toString()
        val newSession = ChatSessionItem(
            id = newSessionId,
            title = "Obrolan Baru",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messages = listOf(ChatMessageSerializable.fromDomain(initialGreeting))
        )
        val updated = listOf(newSession) + _chatSessions.value
        _chatSessions.value = updated
        _currentSessionId.value = newSessionId
        _chatMessages.value = listOf(initialGreeting)
        viewModelScope.launch {
            chatHistoryManager.saveSessions(updated)
        }
    }

    fun selectSession(sessionId: String) {
        val session = _chatSessions.value.firstOrNull { it.id == sessionId } ?: return
        _currentSessionId.value = sessionId
        _chatMessages.value = if (session.messages.isNotEmpty()) {
            session.messages.map { it.toDomain() }
        } else {
            listOf(initialGreeting)
        }
    }

    fun deleteSession(sessionId: String) {
        val updated = _chatSessions.value.filter { it.id != sessionId }
        _chatSessions.value = updated
        viewModelScope.launch {
            chatHistoryManager.saveSessions(updated)
        }
        if (_currentSessionId.value == sessionId) {
            if (updated.isNotEmpty()) {
                selectSession(updated.first().id)
            } else {
                createNewSession()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatHistoryManager.clearAll()
            _chatSessions.value = emptyList()
            createNewSession()
        }
    }

    private fun persistCurrentSession(customTitle: String? = null) {
        val currId = _currentSessionId.value
        val serializableMsgs = _chatMessages.value.map { ChatMessageSerializable.fromDomain(it) }
        val updated = _chatSessions.value.map { session ->
            if (session.id == currId) {
                session.copy(
                    title = customTitle ?: session.title,
                    updatedAt = System.currentTimeMillis(),
                    messages = serializableMsgs
                )
            } else {
                session
            }
        }
        _chatSessions.value = updated
        viewModelScope.launch {
            chatHistoryManager.saveSessions(updated)
        }
    }

    fun sendMessage(question: String) {
        if (question.isBlank() || _isAnsweringQuestion.value) return
        val userMsg = ChatMessage(sender = ChatSender.USER, text = question.trim())
        _chatMessages.value = _chatMessages.value + userMsg

        val currSession = _chatSessions.value.firstOrNull { it.id == _currentSessionId.value }
        val sessionTitle = if (currSession == null || currSession.title == "Obrolan Baru") {
            question.trim().take(30)
        } else {
            currSession.title
        }
        persistCurrentSession(sessionTitle)

        viewModelScope.launch {
            _isAnsweringQuestion.value = true
            try {
                val provider = aiPreferences.aiProviderFlow.first()
                val key = aiPreferences.geminiApiKeyFlow.first()
                val gModel = aiPreferences.geminiModelFlow.first()
                val lmUrl = aiPreferences.lmStudioUrlFlow.first()
                val lmModel = aiPreferences.lmStudioModelFlow.first()

                // Ambil data dompet dan riwayat transaksi langsung dari database Room secara sinkron & fresh
                val accList = repository.getAllAccounts().first()
                val totalWalletBalance = accList.sumOf { it.balance }
                val walletDetails = if (accList.isNotEmpty()) {
                    accList.joinToString("; ") { acc ->
                        val typeLabel = when (acc.type) {
                            "BANK" -> "Rekening Bank"
                            "E_WALLET" -> "E-Wallet"
                            "CASH" -> "Uang Tunai"
                            else -> "Dompet"
                        }
                        "${acc.name} ($typeLabel): ${formatRupiah(acc.balance)}"
                    }
                } else {
                    "Belum ada akun dompet terdaftar"
                }

                val txList = repository.getAllTransactions().first()
                val totalExp = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val totalInc = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

                val topCategories = txList.filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryName }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(4)
                    .joinToString(", ") { "${it.first}: ${formatRupiah(it.second)}" }

                val recentTxSummary = if (txList.isNotEmpty()) {
                    txList.take(15).joinToString("\n  * ") { tx ->
                        val typeSign = if (tx.type == TransactionType.EXPENSE) "Pengeluaran (-)" else "Pemasukan (+)"
                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(tx.date))
                        "${tx.title}: $typeSign ${formatRupiah(tx.amount)} [Kategori: ${tx.categoryName}, Dompet: ${tx.accountName}, Tanggal: $dateStr]"
                    }
                } else {
                    "Belum ada transaksi tercatat"
                }

                val currencyExchangeContext = try {
                    withTimeoutOrNull(2500) {
                        val rates = com.example.kasku.data.remote.currency.CurrencyApiService.create().getExchangeRates("USD").rates
                        val idr = rates["IDR"] ?: 16200.0
                        val eur = rates["EUR"] ?: 0.92
                        val sgd = rates["SGD"] ?: 1.34
                        val myr = rates["MYR"] ?: 4.45
                        val jpy = rates["JPY"] ?: 152.0
                        "1 USD = Rp ${formatRupiah(idr, false)} | 1 EUR = €$eur | 1 SGD = S$$sgd | 1 MYR = RM$myr | 1 JPY = ¥$jpy"
                    } ?: "1 USD ≈ Rp 16.200 | 1 SGD ≈ Rp 12.200 | 1 EUR ≈ Rp 17.500 | 1 MYR ≈ Rp 3.650"
                } catch (e: Exception) {
                    "1 USD ≈ Rp 16.200 | 1 SGD ≈ Rp 12.200 | 1 EUR ≈ Rp 17.500 | 1 MYR ≈ Rp 3.650"
                }

                val activeCurr = CurrencyConfig.currentCurrency
                val allCategories = repository.getAllCategories().first()
                val categoriesSummary = allCategories.joinToString(", ") { "${it.name} (${it.type})" }

                val financialContext = buildString {
                    appendLine("DATA KEUANGAN & DOMPET PENGGUNA TERKINI (KASKU):")
                    appendLine("- Mata Uang Aktif Aplikasi: $activeCurr (${CurrencyConfig.getSymbol().trim()})")
                    appendLine("- Total Saldo Seluruh Dompet: ${formatRupiah(totalWalletBalance)}")
                    appendLine("- Daftar Dompet/Akun & Saldo: $walletDetails")
                    appendLine("- Total Pemasukan: ${formatRupiah(totalInc)}")
                    appendLine("- Total Pengeluaran: ${formatRupiah(totalExp)}")
                    appendLine("- Arus Kas Bersih (Pemasukan - Pengeluaran): ${formatRupiah(totalInc - totalExp)}")
                    appendLine("- Jumlah Transaksi Tercatat: ${txList.size}")
                    appendLine("- Daftar Kategori Tersedia: $categoriesSummary")
                    if (topCategories.isNotBlank()) {
                        appendLine("- Kategori Pengeluaran Terbanyak: $topCategories")
                    }
                    if (txList.isNotEmpty()) {
                        appendLine("- Rincian Riwayat Transaksi Pengguna:")
                        appendLine("  * $recentTxSummary")
                    } else {
                        appendLine("- Rincian Riwayat Transaksi Pengguna: Belum ada transaksi yang disimpan.")
                    }
                    appendLine("- Kurs Valuta Asing Terkini: $currencyExchangeContext | Acuan: ${CurrencyConfig.getRatesTableSummary()}")
                    appendLine("  (Gunakan data kurs ini jika pengguna menyebut mata uang yang berbeda dari mata uang aktif $activeCurr atau meminta konversi).")
                }

                val res = aiService.askFinancialAssistant(
                    question = question,
                    financialContext = financialContext,
                    provider = provider,
                    geminiApiKey = key,
                    geminiModel = gModel,
                    lmStudioUrl = lmUrl,
                    lmStudioModel = lmModel
                )

                val rawAnswer = res.getOrElse { err ->
                    "Maaf, gagal memproses pertanyaan (${err.localizedMessage ?: "Koneksi bermasalah"}). Pastikan API Key Gemini atau LM Studio sudah diatur di menu Pengaturan."
                }

                // Ekstrak blok JSON aksi mandiri AI jika ada
                val actionRegex = Regex("""<<<ACTION_JSON>>>\s*([\s\S]*?)\s*<<<END_ACTION>>>""")
                val actionMatch = actionRegex.find(rawAnswer)
                val cleanAnswerText = if (actionMatch != null) {
                    rawAnswer.replace(actionRegex, "").trim()
                } else {
                    rawAnswer.trim()
                }

                _chatMessages.value = _chatMessages.value + ChatMessage(sender = ChatSender.AI, text = cleanAnswerText)
                persistCurrentSession(sessionTitle)

                // Jalankan aksi database secara mandiri di latar belakang jika AI memberikan payload instruksi aksi
                if (actionMatch != null) {
                    val jsonContent = actionMatch.groupValues[1]
                    executeAutonomousAction(jsonContent)
                }
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = ChatSender.AI,
                    text = "Terjadi kesalahan: ${e.localizedMessage}. Silakan coba lagi nanti."
                )
                persistCurrentSession(sessionTitle)
            } finally {
                _isAnsweringQuestion.value = false
            }
        }
    }

    private suspend fun executeAutonomousAction(jsonContent: String) {
        try {
            val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
            val jsonObj = jsonParser.parseToJsonElement(jsonContent).jsonObject
            val action = jsonObj["action"]?.jsonPrimitive?.content ?: return

            when (action) {
                "CREATE_TRANSACTION" -> {
                    val typeStr = jsonObj["type"]?.jsonPrimitive?.content ?: "EXPENSE"
                    val txType = if (typeStr.equals("INCOME", true)) TransactionType.INCOME else TransactionType.EXPENSE
                    val title = jsonObj["title"]?.jsonPrimitive?.content?.ifBlank { "Transaksi AI" } ?: "Transaksi AI"
                    val rawAmount = jsonObj["amount"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val currency = jsonObj["currency"]?.jsonPrimitive?.content ?: CurrencyConfig.currentCurrency
                    val amountInIdr = if (currency.equals("IDR", true)) {
                        rawAmount
                    } else {
                        CurrencyConfig.convertToIdr(rawAmount, currency)
                    }

                    if (amountInIdr > 0.0) {
                        val catName = jsonObj["categoryName"]?.jsonPrimitive?.content ?: ""
                        val accName = jsonObj["accountName"]?.jsonPrimitive?.content ?: ""

                        val allCats = repository.getAllCategories().first()
                        val matchedCat = allCats.firstOrNull {
                            it.type == txType && (it.name.contains(catName, true) || catName.contains(it.name, true))
                        } ?: allCats.firstOrNull { it.type == txType } ?: allCats.firstOrNull()

                        val allAccs = repository.getAllAccounts().first()
                        val matchedAcc = allAccs.firstOrNull {
                            it.name.contains(accName, true) || accName.contains(it.name, true)
                        } ?: allAccs.firstOrNull()

                        if (matchedCat != null && matchedAcc != null) {
                            repository.addTransaction(
                                Transaction(
                                    title = title,
                                    amount = amountInIdr,
                                    type = txType,
                                    categoryId = matchedCat.id,
                                    categoryName = matchedCat.name,
                                    categoryIcon = matchedCat.iconName,
                                    categoryColor = matchedCat.colorHex,
                                    accountId = matchedAcc.id,
                                    accountName = matchedAcc.name,
                                    date = System.currentTimeMillis()
                                )
                            )
                            val typeLabel = if (txType == TransactionType.INCOME) "Pemasukan" else "Pengeluaran"
                            TopNotif.showSuccess(
                                title = "Transaksi Dicatat!",
                                message = "$typeLabel '$title' senilai ${formatRupiah(amountInIdr)} berhasil disimpan ke dompet ${matchedAcc.name}."
                            )
                        }
                    }
                }

                "REDUCE_TRANSACTION" -> {
                    val searchTitle = jsonObj["searchTitle"]?.jsonPrimitive?.content ?: "last"
                    val reduceAmount = jsonObj["reduceAmount"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val currency = jsonObj["currency"]?.jsonPrimitive?.content ?: CurrencyConfig.currentCurrency
                    val reduceInIdr = if (currency.equals("IDR", true)) {
                        reduceAmount
                    } else {
                        CurrencyConfig.convertToIdr(reduceAmount, currency)
                    }

                    val allTx = repository.getAllTransactions().first()
                    val targetTx = if (searchTitle.equals("last", true)) {
                        allTx.firstOrNull { it.type == TransactionType.EXPENSE }
                    } else {
                        allTx.firstOrNull { it.type == TransactionType.EXPENSE && it.title.contains(searchTitle, true) }
                            ?: allTx.firstOrNull { it.title.contains(searchTitle, true) }
                    }

                    if (targetTx != null && reduceInIdr > 0.0) {
                        val newAmount = targetTx.amount - reduceInIdr
                        if (newAmount <= 0.0) {
                            repository.deleteTransaction(targetTx)
                            TopNotif.showSuccess(
                                title = "Pengeluaran Dihapus",
                                message = "Transaksi '${targetTx.title}' dihapus karena nominal telah berkurang habis."
                            )
                        } else {
                            repository.deleteTransaction(targetTx)
                            repository.addTransaction(
                                targetTx.copy(
                                    id = 0,
                                    amount = newAmount,
                                    date = targetTx.date
                                )
                            )
                            TopNotif.showSuccess(
                                title = "Pengeluaran Dikurangi",
                                message = "Pengeluaran '${targetTx.title}' dikurangi ${formatRupiah(reduceInIdr)}. Sisa nominal: ${formatRupiah(newAmount)}."
                            )
                        }
                    } else if (targetTx == null) {
                        TopNotif.showWarning(
                            title = "Tidak Ditemukan",
                            message = "Tidak menemukan transaksi yang cocok untuk dikurangi nominalnya."
                        )
                    }
                }

                "DELETE_TRANSACTION" -> {
                    val searchTitle = jsonObj["searchTitle"]?.jsonPrimitive?.content ?: "last"
                    val allTx = repository.getAllTransactions().first()
                    val targetTx = if (searchTitle.equals("last", true)) {
                        allTx.firstOrNull()
                    } else {
                        allTx.firstOrNull { it.title.contains(searchTitle, true) }
                    }

                    if (targetTx != null) {
                        repository.deleteTransaction(targetTx)
                        TopNotif.showSuccess(
                            title = "Transaksi Dihapus",
                            message = "Transaksi '${targetTx.title}' senilai ${formatRupiah(targetTx.amount)} berhasil dihapus dari dompet ${targetTx.accountName}."
                        )
                    } else {
                        TopNotif.showWarning(
                            title = "Tidak Ditemukan",
                            message = "Tidak menemukan transaksi yang cocok untuk dihapus."
                        )
                    }
                }

                "CREATE_ACCOUNT" -> {
                    val name = jsonObj["name"]?.jsonPrimitive?.content?.ifBlank { "Dompet Baru" } ?: "Dompet Baru"
                    val typeStr = jsonObj["type"]?.jsonPrimitive?.content ?: "BANK"
                    val accType = when (typeStr.uppercase()) {
                        "E_WALLET", "EWALLET" -> "E_WALLET"
                        "CASH", "TUNAI" -> "CASH"
                        else -> "BANK"
                    }
                    val rawBal = jsonObj["initialBalance"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val currency = jsonObj["currency"]?.jsonPrimitive?.content ?: CurrencyConfig.currentCurrency
                    val balInIdr = if (currency.equals("IDR", true)) {
                        rawBal
                    } else {
                        CurrencyConfig.convertToIdr(rawBal, currency)
                    }

                    val iconName = when (accType) {
                        "BANK" -> "account_balance"
                        "E_WALLET" -> "account_balance_wallet"
                        else -> "payments"
                    }

                    repository.addAccount(
                        Account(
                            name = name,
                            type = accType,
                            balance = balInIdr,
                            iconName = iconName
                        )
                    )
                    TopNotif.showSuccess(
                        title = "Dompet Baru Dibuat!",
                        message = "Dompet '$name' ($accType) dengan saldo awal ${formatRupiah(balInIdr)} berhasil ditambahkan."
                    )
                }
            }
        } catch (e: Exception) {
            TopNotif.showError("Gagal Eksekusi Aksi AI", e.localizedMessage)
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(initialGreeting)
        persistCurrentSession()
    }

    class Factory(
        private val repository: KasKuRepository,
        private val aiService: AiService,
        private val aiPreferences: AiPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AiInsightsViewModel(repository, aiService, aiPreferences) as T
        }
    }
}
