# 📘 E-BOOK PANDUAN LENGKAP & BEDAH KODE PROYEK KASKU
> **Panduan Arsitektur, Penjelasan Kode Antar-Halaman, dan Simulasi Tanya-Jawab Sidang Presentasi UTS Pemrograman Mobile (Native Android Jetpack Compose)**

---

## 📑 DAFTAR ISI

1. [BAB 1: Pengenalan & Visi Proyek KasKu](#bab-1-pengenalan--visi-proyek-kasku)
2. [BAB 2: Matriks Pemenuhan 7 Materi UTS (Nilai Maksimal 100%)](#bab-2-matriks-pemenuhan-7-materi-uts-nilai-maksimal-100)
3. [BAB 3: Arsitektur Aplikasi (MVVM + UDF + Repository Pattern)](#bab-3-arsitektur-aplikasi-mvvm--udf--repository-pattern)
4. [BAB 4: Basis Data Lokal (Room Database & DataStore Preferences)](#bab-4-basis-data-lokal-room-database--datastore-preferences)
5. [BAB 5: Lapisan Jaringan & REST API (Retrofit 2 & Gemini AI)](#bab-5-lapisan-jaringan--rest-api-retrofit-2--gemini-ai)
6. [BAB 6: Bedah Layar & Penjelasan Kode (Screen-by-Screen Breakdown)](#bab-6-bedah-layar--penjelasan-kode-screen-by-screen-breakdown)
   * 6.1 [MainActivity & Navigasi Antar Layar (`Navigation.kt`)](#61-mainactivity--navigasi-antar-layar-navigationkt)
   * 6.2 [Dashboard / Beranda (`DashboardScreen.kt`)](#62-dashboard--beranda-dashboardscreenkt)
   * 6.3 [Riwayat Transaksi (`TransactionsScreen.kt`)](#63-riwayat-transaksi-transactionsscreenkt)
   * 6.4 [Detail Transaksi & Passing Argument (`TransactionDetailScreen.kt`)](#64-detail-transaksi--passing-argument-transactiondetailscreenkt)
   * 6.5 [Pemindai Struk Kamera AI (`ReceiptScannerScreen.kt`)](#65-pemindai-struk-kamera-ai-receiptscannerscreenkt)
   * 6.6 [Tren & Analisis Keuangan (`AiInsightsScreen.kt`)](#66-tren--analisis-keuangan-aiinsightsscreenkt)
   * 6.7 [KasKu AI Advisor Chatbot (`AiChatScreen.kt`)](#67-kasku-ai-advisor-chatbot-aichatscreenkt)
   * 6.8 [Pengaturan & Kurs Mata Uang Retrofit (`SettingsScreen.kt`)](#68-pengaturan--kurs-mata-uang-retrofit-settingsscreenkt)
7. [BAB 7: Bank Tanya-Jawab Khas Dosen saat Sidang/Presentasi](#bab-7-bank-tanya-jawab-khas-dosen-saat-sidangpresentasi)

---

# BAB 1: Pengenalan & Visi Proyek KasKu

### 1.1 Apa itu KasKu?
**KasKu** adalah aplikasi pencatatan keuangan pribadi (*personal expense & cash flow tracker*) cerdas berbasis Android Native yang dibangun menggunakan teknologi modern: **Jetpack Compose (Material 3)**, basis data reaktif **Room Database**, klien jaringan **Retrofit 2**, serta integrasi **Artificial Intelligence (Google Gemini Vision & LM Studio)**.

### 1.2 Masalah yang Diselesaikan
* Kebanyakan pengguna malas mencatat pengeluaran karena repot mengetik manual satu per satu.
* Struk belanja fisik sering hilang atau berserakan tanpa terekapitulasi.
* Tidak adanya evaluasi keuangan yang proaktif (apakah pengeluaran minggu ini sehat atau boros).

### 1.3 Solusi KasKu
* **Smart Receipt Scanner**: Cukup arahkan kamera ke struk belanja/nota makan, AI secara otomatis membaca nama merchant, rincian barang, total harga, dan kategori pengeluaran via Vision OCR.
* **KasKu AI Financial Advisor**: Asisten keuangan yang membaca konteks saldo dan pola belanja pengguna untuk memberikan tips penghematan dan rencana anggaran.
* **Live Currency Rates**: Memantau kurs valuta asing secara *real-time* via REST API resmi menggunakan Retrofit.

---

# BAB 2: Matriks Pemenuhan 7 Materi UTS (Nilai Maksimal 100%)

Dosen mewajibkan minimal **5 dari 7 materi**. KasKu mengimplementasikan **7 dari 7 materi secara lengkap (100% Terpenuhi)**:

| No | Materi Ujian | Implementasi di KasKu | Lokasi Kode Kunci |
|:---:|---|---|---|
| **1** | **UI & Layout Dasar** | `Column`, `Row`, `Box`, hierarki Modifiers (`padding`, `fillMaxSize`, `clip`, `shadow`, `navigationBarsPadding`). | [`AppleComponents.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/components/AppleComponents.kt), [`DashboardScreen.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/screens/dashboard/DashboardScreen.kt) |
| **2** | **Material Design 3 (M3)** | Tema Color scheme dinamis, Typography M3, komponen `Card`, `OutlinedTextField`, `Surface`, `IconButton`, `Scaffold`. | [`Theme.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/theme/Theme.kt), [`AddTransactionSheet.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/components/AddTransactionSheet.kt) |
| **3** | **State Management & UDF** | `remember`, `rememberSaveable` (tahan rotasi layar), State Hoisting, dan Unidirectional Data Flow via StateFlow. | [`AddTransactionSheet.kt:101`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/components/AddTransactionSheet.kt#L101), [`DashboardViewModel.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/screens/dashboard/DashboardViewModel.kt) |
| **4** | **Lazy Layouts** | `LazyColumn` & `LazyRow` dinamis dengan penanganan parameter `key = { it.id }` untuk rendering optimal. | [`TransactionsScreen.kt:438`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/screens/transactions/TransactionsScreen.kt#L438), [`AiChatScreen.kt:641`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/screens/chat/AiChatScreen.kt#L641) |
| **5** | **Networking & API** | REST API asinkron menggunakan **Retrofit 2** + Coroutines (`getExchangeRates`) dan OkHttp AI Client. | [`CurrencyApiService.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/data/remote/currency/CurrencyApiService.kt), [`AiService.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/data/remote/AiService.kt) |
| **6** | **Arsitektur Aplikasi** | Penerapan **MVVM** dengan ViewModel dan pola **UiState sealed interface** (`Idle`, `Loading`, `Success`, `Error`). | [`UiState.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/common/UiState.kt), [`CurrencyViewModel.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/screens/currency/CurrencyViewModel.kt) |
| **7** | **Navigation Compose** | Navigasi 7 layar terintegrasi `Scaffold` & Bottom Bar, serta **Transfer Data Antar Layar** via `NavArgument`. | [`MainActivity.kt:225`](file:///d:/KasKu/app/src/main/java/com/example/kasku/MainActivity.kt#L225), [`TransactionDetailScreen.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/screens/detail/TransactionDetailScreen.kt) |

---

# BAB 3: Arsitektur Aplikasi (MVVM + UDF + Repository Pattern)

Aplikasi KasKu dirancang mengikuti kaidah **Modern Android Architecture**:

```text
 ┌─────────────────────────────────────────────────────────────┐
 │                      UI LAYER (Compose)                     │
 │   DashboardScreen / TransactionsScreen / ReceiptScanner     │
 └──────────────────────────────▲──────────────────────────────┘
                                │  StateFlow (UiState / Data)
             Events (User Action)│
 ┌──────────────────────────────▼──────────────────────────────┐
 │                      VIEWMODEL LAYER                        │
 │    DashboardViewModel / TransactionsViewModel / CurrencyVM   │
 └──────────────────────────────▲──────────────────────────────┘
                                │  suspend fun / Flow<T>
 ┌──────────────────────────────▼──────────────────────────────┐
 │                     REPOSITORY LAYER                        │
 │           KasKuRepository / DataStore Preferences           │
 └───────────────▲─────────────────────────────▲───────────────┘
                 │                             │
 ┌───────────────▼──────────────┐   ┌──────────▼───────────────┐
 │     LOCAL DATA SOURCE        │   │    REMOTE DATA SOURCE    │
 │   Room SQLite Database       │   │  Retrofit 2 REST API     │
 │  (Transactions, Wallets)     │   │  Google Gemini Vision AI │
 └──────────────────────────────┘   └──────────────────────────┘
```

### 3.1 Pola Unidirectional Data Flow (UDF)
1. **Data mengalir ke bawah**: State dari ViewModel dialirkan ke Composable melalui `StateFlow` yang di-`collectAsState()`.
2. **Event mengalir ke atas**: Interaksi pengguna (klik tombol, input teks) diteruskan ke ViewModel via callback lambda (*State Hoisting*).

### 3.2 Standardisasi `UiState` Terpadu
Kode pada [`UiState.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/ui/common/UiState.kt):
```kotlin
package com.example.kasku.ui.common

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```
* **Kelebihan**: Layar Compose cukup mengevaluasi `when (uiState)` untuk menampilkan spinner saat `Loading`, kartu data saat `Success`, atau banner peringatan saat `Error`.

---

# BAB 4: Basis Data Lokal (Room Database & DataStore Preferences)

### 4.1 Entitas & Tabel Database (`Entities.kt`)
* `TransactionEntity`: Menyimpan judul transaksi, nominal uang, tipe (EXPENSE / INCOME / TRANSFER), id akun dompet, id kategori, tanggal, catatan, dan JSON barang struk.
* `AccountEntity`: Menyimpan sumber dana (Tunai, Rekening BCA, GoPay, Mandiri) beserta saldo real-time.
* `CategoryEntity`: Menyimpan nama kategori (Makanan, Transportasi, Gaji, dll.), warna hex, dan nama ikon vektor.

### 4.2 Reaktivitas dengan Kotlin Flow
Pada [`Daos.kt`](file:///d:/KasKu/app/src/main/java/com/example/kasku/data/local/dao/Daos.kt):
```kotlin
@Query("SELECT * FROM transactions ORDER BY date DESC")
fun getAllTransactions(): Flow<List<TransactionEntity>>
```
* **Mengapa menggunakan `Flow`?** Setiap kali ada penambahan, pengeditan, atau penghapusan transaksi baru di database, Room secara otomatis memicu emisi data baru ke UI secara instan tanpa perlu memanggil `refresh()` manual!

---

# BAB 5: Lapisan Jaringan & REST API (Retrofit 2 & Gemini AI)

### 5.1 Implementasi Retrofit 2 (`CurrencyApiService.kt`)
Retrofit digunakan untuk mengambil data kurs valuta asing dari REST API publik `https://open.er-api.com/`:

```kotlin
@Serializable
data class CurrencyResponse(
    @SerialName("result") val result: String = "",
    @SerialName("base_code") val baseCode: String = "",
    @SerialName("rates") val rates: Map<String, Double> = emptyMap()
)

interface CurrencyApiService {
    @GET("v6/latest/{base}")
    suspend fun getExchangeRates(
        @Path("base") base: String = "USD"
    ): CurrencyResponse

    companion object {
        fun create(): CurrencyApiService {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            return Retrofit.Builder()
                .baseUrl("https://open.er-api.com/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CurrencyApiService::class.java)
        }
    }
}
```
* **Poin Penting untuk Dosen**: Fungsi diberi penanda `suspend` sehingga pemanggilan HTTP berjalan asinkron di latar belakang (*background thread*) via Coroutines dan tidak memblokir antarmuka aplikasi (*UI Thread*).

---

# BAB 6: Bedah Layar & Penjelasan Kode

### 6.1 `MainActivity.kt` & Navigasi Antar Layar (`Navigation.kt`)
* **Peran**: Titik masuk utama aplikasi (*Single Activity Architecture*) dengan `NavHost` yang mengelola 7 layar.
* **Integrasi Scaffold**:
  ```kotlin
  Scaffold(
      bottomBar = {
          if (!WindowInsets.isImeVisible && currentRoute != Screen.Scanner.route && !currentRoute.startsWith("transaction_detail")) {
              TelegramLiquidGlassBottomBar(currentRoute = currentRoute, onNavigate = { ... })
          }
      }
  ) { innerPadding ->
      NavHost(...) { ... }
  }
  ```

### 6.2 Beranda (`DashboardScreen.kt`)
* **Peran**: Menampilkan kartu ringkasan saldo total akumulasi semua dompet, perbandingan pemasukan vs pengeluaran bulanan, dan daftar 5 transaksi terbaru.
* **Logika Reactive Flow**:
  Menggabungkan aliran data nama pengguna, daftar dompet, dan daftar transaksi menggunakan operator `combine`:
  ```kotlin
  val uiState: StateFlow<DashboardUiState> = combine(
      userPreferences.userNameFlow,
      repository.getAllAccounts(),
      repository.getAllTransactions()
  ) { name, accounts, transactions ->
      val balance = accounts.sumOf { it.balance }
      val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
      val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
      DashboardUiState(userName = name, totalBalance = balance, ...)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
  ```

### 6.3 Riwayat Transaksi (`TransactionsScreen.kt`)
* **Peran**: Menampilkan seluruh riwayat pengeluaran & pemasukan dengan fitur pencarian real-time dan penyaringan (*Filter Chips: Semua, Pengeluaran, Pemasukan*).
* **Penggunaan `LazyColumn` dengan `key`**:
  ```kotlin
  items(filteredTransactions, key = { it.id }) { tx ->
      MonzoTransactionCard(
          transaction = tx,
          onClick = { onTransactionClick?.invoke(tx.id) },
          onDeleteClick = { transactionToDelete = tx }
      )
  }
  ```

### 6.4 Detail Transaksi & Passing Argument (`TransactionDetailScreen.kt`)
* **Peran**: Menampilkan rincian transaksi lengkap dan opsi hapus saat salah satu kartu transaksi di klik.
* **Koneksi Navigasi di `MainActivity.kt`**:
  ```kotlin
  composable(
      route = Screen.TransactionDetail.route, // "transaction_detail/{transactionId}"
      arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
  ) { backStackEntry ->
      val id = backStackEntry.arguments?.getLong("transactionId") ?: 0L
      TransactionDetailScreen(transactionId = id, repository = repository, onNavigateBack = { navController.popBackStack() })
  }
  ```

### 6.5 Pemindai Struk Kamera AI (`ReceiptScannerScreen.kt`)
* **Peran**: Mengambil gambar nota/struk belanja menggunakan **CameraX** (`ImageCapture`), lalu mengirimkan byte gambar ke Google Gemini Vision API untuk mengekstrak teks nota menjadi objek terstruktur.

### 6.6 Tren & Analisis Keuangan (`AiInsightsScreen.kt`)
* **Peran**: Menghitung **Financial Health Score** (skor kesehatan keuangan dari skala 0-100) dan menampilkan grafik batang arus kas mingguan vs bulanan.

### 6.7 KasKu AI Advisor Chatbot (`AiChatScreen.kt`)
* **Peran**: Percakapan asisten keuangan interaktif dengan teknik *Context Injection*. Sistem mengirimkan riwayat saldo dan belanja pengguna ke prompt sistem AI sehingga jawaban yang diberikan sangat relevan dan personal.

### 6.8 Pengaturan & Kartu Kurs Retrofit (`SettingsScreen.kt`)
* **Peran**: Tempat konfigurasi profil pengguna, API Key Gemini, pemilihan engine AI, dan monitoring kurs mata uang dunia via Retrofit REST API.

---

# BAB 7: Bank Tanya-Jawab Khas Dosen saat Sidang/Presentasi

Berikut adalah 5 pertanyaan favorit dosen saat menguji project Mobile Jetpack Compose beserta jawaban terbaiknya:

#### ❓ Pertanyaan 1: "Mengapa kalian memilih Jetpack Compose daripada XML Layout konvensional?"
> **Jawaban Ideal**:  
> *"Jetpack Compose menggunakan paradigma **Declarative UI**, di mana antarmuka didefinisikan secara langsung di dalam kode Kotlin sesuai dengan state yang dimilikinya. Berbeda dengan XML yang bersifat imperatif (di mana kita harus memanggil `findViewById` atau ViewBinding dan memanipulasi view secara manual), di Compose ketika state berubah, antarmuka otomatis melakukan **Recomposition** hanya pada elemen yang terdampak, sehingga kode menjadi jauh lebih ringkas, minim bug inkonsistensi state, dan mudah di-maintain."*

#### ❓ Pertanyaan 2: "Apa fungsi parameter `key` di dalam `items(...)` pada LazyColumn?"
> **Jawaban Ideal**:  
> *"Parameter `key` berfungsi sebagai identitas unik bagi setiap item data (seperti Primary Key). Tanpa parameter `key`, saat ada data yang dihapus, disisipkan, atau diurutkan ulang, Compose harus me-render ulang seluruh daftar dari awal. Dengan memberikan `key = { it.id }`, Compose hanya akan memindahkan atau menghapus node composable yang spesifik, sehingga proses animasi halus dan performa memori menjadi sangat efisien."*

#### ❓ Pertanyaan 3: "Apa perbedaan `remember` dan `rememberSaveable`?"
> **Jawaban Ideal**:  
> *"Keduanya digunakan untuk menyimpan state di memori. Namun `remember` hanya mempertahankan state selama siklus Recomposition, dan nilainya akan hilang saat terjadi **Configuration Change** (seperti rotasi layar HP atau perubahan tema gelap/terang). Sedangkan `rememberSaveable` otomatis menyimpan state ke dalam `Bundle`, sehingga nilai input form (seperti nominal dan catatan) tetap terjaga meskipun layar dirotasi atau proses aplikasi sempat di-kill oleh sistem operasi."*

#### ❓ Pertanyaan 4: "Bagaimana cara kerja Retrofit dan Coroutines agar aplikasi tidak macet (ANR)?"
> **Jawaban Ideal**:  
> *"Retrofit 2 mendukung fungsi bertanda `suspend`. Saat kita memanggil `apiService.getExchangeRates()`, pemanggilan jaringan dijalankan menggunakan Kotlin Coroutines di dalam `viewModelScope` dengan dispatcher latar belakang (`Dispatchers.IO`). Coroutines bersifat **non-blocking asynchronous**, sehingga saat proses download data HTTP berlangsung, thread utama (Main/UI Thread) tetap bebas menggambar antarmuka tanpa terjadi kendala Application Not Responding (ANR)."*

#### ❓ Pertanyaan 5: "Bagaimana kalian membagi tugas dalam kelompok?"
> **Jawaban Ideal**:  
> *"Tim kami membagi peran secara spesifik menjadi 4 ranah tanggung jawab:  
> 1. **UI/UX & Design System**: Merancang palet warna Monzo, tipografi Material 3, dan komponen kartu reusable di `AppleComponents.kt`.  
> 2. **Mobile Frontend Developer**: Mengembangkan screen utama (`DashboardScreen.kt`, `TransactionsScreen.kt`, dan `TransactionDetailScreen.kt`).  
> 3. **Database & Architecture Engineer**: Membangun lapisan data lokal Room SQLite (`Daos.kt`, `Entities.kt`, `KasKuRepository.kt`) dan DataStore Preferences.  
> 4. **Networking & AI Integration Engineer**: Mengonfigurasi Retrofit REST API (`CurrencyApiService.kt`) dan integrasi AI Vision Scanner."*

---

*Disiapkan secara khusus untuk Kelompok Proyek KasKu dalam rangka Ujian Tengah Semester (UTS) Pemrograman Mobile.*
