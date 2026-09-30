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
import com.example.kasku.ui.components.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

                val accList = accounts.value
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

                val txList = transactions.value
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
                    txList.take(5).joinToString("; ") { tx ->
                        val typeSign = if (tx.type == TransactionType.EXPENSE) "-" else "+"
                        "${tx.title} ($typeSign${formatRupiah(tx.amount)} via ${tx.accountName})"
                    }
                } else {
                    "Belum ada transaksi tercatat"
                }

                val financialContext = buildString {
                    appendLine("DATA KEUANGAN & DOMPET PENGGUNA TERKINI:")
                    appendLine("- Total Saldo Seluruh Dompet: ${formatRupiah(totalWalletBalance)}")
                    appendLine("- Daftar Dompet/Akun & Saldo: $walletDetails")
                    appendLine("- Total Pemasukan: ${formatRupiah(totalInc)}")
                    appendLine("- Total Pengeluaran: ${formatRupiah(totalExp)}")
                    appendLine("- Arus Kas Bersih (Pemasukan - Pengeluaran): ${formatRupiah(totalInc - totalExp)}")
                    appendLine("- Jumlah Transaksi: ${txList.size}")
                    if (topCategories.isNotBlank()) {
                        appendLine("- Kategori Pengeluaran Terbanyak: $topCategories")
                    }
                    appendLine("- Transaksi Terkini: $recentTxSummary")
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

                val answerText = res.getOrElse { err ->
                    "Maaf, gagal memproses pertanyaan (${err.localizedMessage ?: "Koneksi bermasalah"}). Pastikan API Key Gemini atau LM Studio sudah diatur di menu Pengaturan."
                }
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = ChatSender.AI, text = answerText)
                persistCurrentSession(sessionTitle)
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
