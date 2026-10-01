<p align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp" alt="KasKu Logo" width="100" height="100" style="border-radius: 20%;"/>
</p>

<h1 align="center">KasKu — Smart Cash Flow & Expense Tracker</h1>

<p align="center">
  <strong>Aplikasi Manajemen Keuangan Cerdas & Pelacak Pengeluaran Pribadi berbasis Android dengan Jetpack Compose & AI Integration.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/Database-Room_Local_DB-00599C?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room"/>
  <img src="https://img.shields.io/badge/AI-Gemini_&_LM_Studio-EA4335?style=for-the-badge&logo=google&logoColor=white" alt="AI"/>
  <img src="https://img.shields.io/badge/License-MIT-brightgreen?style=for-the-badge" alt="License"/>
</p>

---

## 📖 Tentang KasKu

**KasKu** adalah aplikasi pencatatan keuangan pribadi modern untuk perangkat Android yang dirancang dengan perpaduan estetika perbankan digital ala *Monzo* dan kehalusan antarmuka *Apple Design*. KasKu tidak hanya mencatat uang masuk dan keluar secara konvensional, melainkan menyematkan kecerdasan buatan (*Artificial Intelligence*) untuk mengenali struk belanja secara otomatis dan menjadi asisten penasihat keuangan pribadi (*Personal Financial Advisor*).

---

## ✨ Fitur Unggulan

### 1. 📊 Dashboard & Arus Kas Real-time
* **Ringkasan Saldo Global**: Pemantauan total aset, akumulasi pendapatan, dan pengeluaran bulanan dalam satu kartu ringkas.
* **Multi-Wallet Management**: Kelola banyak sumber dana sekaligus (Dompet Tunai, Rekening Bank BCA/Mandiri/BRI, E-Wallet GoPay/OVO/Dana) lengkap dengan riwayat transfer antar dompet.
* **Quick Transaction Entry**: Bottom sheet cepat untuk menambah transaksi dengan pilihan kategori intuitif dan kalkulator bawaan.

### 2. 🧾 Smart Receipt Scanner (AI Vision OCR)
* **Scan Struk Belanja Instan**: Ambil foto nota atau struk belanja menggunakan kamera bawaan CameraX.
* **Ekstraksi Otomatis**: AI otomatis membaca total belanja, nama toko/merchant, tanggal, rincian barang, serta merekomendasikan kategori pengeluaran tanpa perlu mengetik manual.

### 3. 🤖 KasKu AI — Asisten Penasihat Keuangan
* **Chatbot Interaktif**: Bertanya langsung mengenai kondisi keuangan, tips hemat, hingga strategi alokasi anggaran bulanan.
* **Context-Aware Analytics**: AI membaca riwayat cash flow secara lokal untuk memberikan saran yang relevan dan terpersonalisasi.
* **Dukungan Multi-Engine**: Pilihan menggunakan cloud API (*Google Gemini 1.5 Flash/Pro*) atau *Local Offline LLM* melalui LM Studio.

### 4. 📈 Trends & Analisis Finansial Cerdas
* **Financial Health Score**: Skor kesehatan finansial otomatis berdasarkan rasio tabungan, kestabilan pengeluaran, dan batas anggaran.
* **Visualisasi Pengeluaran**: Grafik proporsi belanja per kategori untuk mendeteksi pos pengeluaran yang paling boros (*anomalous spending*).

### 5. 🛡️ Privasi & Offline-First
* Seluruh data transaksi tersimpan aman di database lokal **Room SQLite**.
* API key AI dan preferensi pengguna disimpan terenkripsi pada **DataStore Preferences**.

---

## 🏗️ Arsitektur & Teknologi

KasKu dibangun mengikuti panduan **Modern Android Architecture (MVVM + Clean Architecture principles)**:

```text
KasKu/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/kasku/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room DB, DAOs, & Entities
│   │   │   │   │   ├── preferences/    # DataStore Preferences (AI & User settings)
│   │   │   │   │   ├── remote/         # AI Service (Gemini API & LM Studio OkHttp)
│   │   │   │   │   └── repository/     # Central Repository Implementation
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/          # Domain Models & Enums
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     # Reusable Compose Widgets & Apple Sheets
│   │   │   │   │   ├── navigation/     # NavHost & Authentic Monzo Bottom Bar
│   │   │   │   │   ├── screens/        # Dashboard, Transactions, Scanner, Chat, Insights
│   │   │   │   │   └── theme/          # Typography, Color Palette, & Glassmorphic styling
│   │   │   │   └── MainActivity.kt     # Single Activity Entry Point
│   │   │   └── res/                    # Drawables, Mipmaps, Values, Manifest
│   │   └── test/                       # Unit & Instrumentation Tests
├── gradle/                             # Gradle Wrapper
├── build.gradle.kts                    # Root build configuration
└── settings.gradle.kts                 # Project settings & dependency resolution
```

### Tech Stack:
* **Bahasa**: Kotlin (Coroutines, StateFlow, Serialization)
* **UI Toolkit**: Jetpack Compose, Material 3, Material Icons Extended
* **Basis Data**: Room Database (SQLite) + KSP (Kotlin Symbol Processing)
* **Kamera & Media**: CameraX, Coil Image Loader
* **Jaringan**: OkHttp 3, Kotlinx Serialization JSON
* **Kecerdasan Buatan**: Google Gemini API, LM Studio (OpenAI-compatible REST API)

---

## 🚀 Memulai (Quick Start)

### Prasyarat
1. **Android Studio**: Android Studio Ladybug / Meerkat (versi terbaru).
2. **JDK**: Java Development Kit 17 atau yang lebih baru.
3. **Android SDK**: Min SDK 29 (Android 10), Target SDK 36 (Android 16).

### Langkah Instalasi

1. **Clone Repositori**:
   ```bash
   git clone https://github.com/iqsanazhr/KasKu.git
   cd KasKu
   ```

2. **Buka di Android Studio**:
   * Buka Android Studio, pilih menu **File** > **Open**, lalu arahkan ke folder repositori `KasKu`.
   * Tunggu proses Gradle Sync hingga selesai.

3. **Jalankan Aplikasi**:
   * Hubungkan perangkat fisik Android melalui USB Debugging atau gunakan Android Emulator.
   * Tekan tombol **Run** (`Shift + F10`) di Android Studio.

---

## ⚙️ Konfigurasi KasKu AI

Untuk mengaktifkan fitur scan struk AI dan chatbot penasihat keuangan:

1. Buka tab **Pengaturan** di dalam aplikasi KasKu.
2. Pilih penyedia AI yang diinginkan:
   * **Google Gemini API**: Masukkan API Key gratis yang diperoleh dari [Google AI Studio](https://aistudio.google.com/). Pilih model (misal: `gemini-1.5-flash`).
   * **LM Studio (Local / Offline)**: Masukkan endpoint lokal (misal: `http://192.168.1.x:1234/v1`) untuk menjalankan LLM secara privat tanpa koneksi internet luar.
3. Tekan **Test Connection** untuk memverifikasi kesiapan AI.

---

## 📄 Lisensi

Proyek ini didistribusikan di bawah lisensi **MIT License**. Silakan gunakan, pelajari, dan kembangkan lebih lanjut.

---
