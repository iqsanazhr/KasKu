# 📘 KasKu: Brand Identity Guideline & Company Book

<div align="center">

  <img src="../app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="KasKu Logo" width="120" height="120" style="border-radius: 28px; box-shadow: 0 12px 32px rgba(226,91,69,0.25);"/>

  # **KASKU BRAND BOOK**
  ### *Smart Cash Flow & Expense Tracker*
  **Pedoman Resmi Identitas Merek, Sistem Desain UI/UX, dan Spesifikasi Produk Finansial Modern**

  *Versi Rilis 1.1.0 • Standar Desain Produk Finansial Kelas Enterprise*

</div>

---

## 📑 Daftar Isi
1. [Filosofi & Esensi Merek (Brand Essence)](#1-filosofi--esensi-merek-brand-essence)
2. [Sistem Identitas Visual (Visual Identity System)](#2-sistem-identitas-visual-visual-identity-system)
   - [Konstruksi Logo & Safe Area](#21-konstruksi-logo--safe-area)
   - [Palet Warna Resmi (Brand Colors)](#22-palet-warna-resmi-brand-colors)
   - [Sistem Tipografi & Skala Teks](#23-sistem-tipografi--skala-teks)
3. [Arsitektur Desain Antarmuka UI/UX (Design System)](#3-arsitektur-desain-antarmuka-uiux-design-system)
   - [Filosofi Sintesis: Fintech Modern + desain antarmuka humanis](#31-filosofi-sintesis-Fintech Modern-fintech--Desain Modern-hig)
   - [Komponen Khas KasKu (Signature Elements)](#32-komponen-khas-kasku-signature-elements)
4. [Showcase Antarmuka & Penjelasan Layar Produk](#4-showcase-antarmuka--penjelasan-layar-produk)
   - [Layar 1: Splash Screen & Inisialisasi Brand](#layar-1-splash-screen--inisialisasi-brand)
   - [Layar 2: Dashboard & Arus Kas Real-Time](#layar-2-dashboard--arus-kas-real-time)
   - [Layar 3: Riwayat & Pencarian Transaksi Cerdas](#layar-3-riwayat--pencarian-transaksi-cerdas)
   - [Layar 4: Smart Vision OCR CameraX Receipt Scanner](#layar-4-smart-vision-ocr-camerax-receipt-scanner)
   - [Layar 5: Tren & Analitik Arus Kas](#layar-5-tren--analitik-arus-kas)
   - [Layar 6: Multi-Wallet, Multi-Currency & Konfigurasi AI](#layar-6-multi-wallet-multi-currency--konfigurasi-ai)
   - [Layar 7: KasKu AI Assistant & Session Management](#layar-7-kasku-ai-assistant--session-management)
5. [Arsitektur Rekayasa & Standar Keamanan Data](#5-arsitektur-rekayasa--standar-keamanan-data)
6. [Tim Pengembang & Kredit Perusahaan](#6-tim-pengembang--kredit-perusahaan)

---

## 1. Filosofi & Esensi Merek (Brand Essence)

### 1.1 Latar Belakang & Pernyataan Masalah
Pencatatan keuangan pribadi seringkali ditinggalkan bukan karena pengguna tidak peduli pada masa depan mereka, melainkan karena pengalaman aplikasi finansial konvensional yang terasa seperti pekerjaan administrasi yang membosankan:
- Formulir input transaksi yang panjang dan manual.
- Tampilan antarmuka yang kaku, dipenuhi iklan popup, dan rumit dipahami.
- Kebocoran privasi akibat data keuangan pengguna dikirim dan dijual ke pihak ketiga.

**KasKu** hadir mendisrupsi kebiasaan tersebut dengan mendefinisikan ulang pencatatan kas: menjadikannya **cepat, intuitif, berestetika tinggi, dan sepenuhnya privat**.

### 1.2 Visi & Misi
- **Visi**: Menjadi standar emas aplikasi manajemen keuangan personal generasi baru di Indonesia yang menggabungkan kesederhanaan desain, kecerdasan buatan terapan, dan kedaulatan data privasi.
- **Misi**:
  1. *Frictionless Finance*: Memangkas friksi entri data dengan teknologi kamera multimodal AI Vision OCR untuk struk belanja.
  2. *Delightful Interface*: Menciptakan antarmuka yang anggun, berpadu antara perbankan fintech modern dan prinsip desain humanis ergonomis.
  3. *Actionable Intelligence*: Memberdayakan pengguna dengan asisten AI finansial yang memahami konteks belanja riil.
  4. *Absolute Privacy*: Menjamin 100% data keuangan tersimpan di perangkat lokal pengguna (Offline-First via Room SQLite).

### 1.3 Nilai Inti Merek (*Brand Core Values*)
| Nilai | Definisi & Implementasi |
|---|---|
| **Simplicity** | Menghilangkan kompleksitas teknis. Seluruh aksi penting dapat dilakukan dalam 1-2 ketukan. |
| **Trust & Privacy-First** | Tanpa tracking pihak ketiga. Dukungan mode AI offline lokal (LM Studio) tanpa koneksi internet luar. |
| **Applied Intelligence** | AI bukan gimik; AI mendeteksi struk fisik secara presisi dan memberi rekomendasi penghematan cerdas. |
| **Craftsmanship** | Perhatian mendalam pada lengkungan sudut 24dp, border 0.5dp, haptic feedback, dan micro-interactions. |

---

## 2. Sistem Identitas Visual (Visual Identity System)

### 2.1 Asal-Usul & Filosofi Logo KasKu (The Origin & Geometry of K²)

Monogram resmi **K²** tidak diciptakan secara acak, melainkan merangkum sintesis antara identitas pencipta, sains komputasi, dan filosofi kedaulatan finansial:

1. **Akar Penamaan "KasKu"**:
   - Berakar dari kata **"Kas"** (pencatatan dan pengelolaan arus kas harian secara transparan) dan **"Ku"** (penegasan kedaulatan data privasi pribadi, di mana data finansial 100% milik pengguna dan tersimpan di perangkat lokal tanpa campur tangan pihak ketiga).
2. **Monogram Kuadratik ($K^2$)**:
   - **Identitas Tim Pengembang**: Merupakan tribut kepada **Kelompok 2** (Kelas A Pemrograman Mobile S1 Informatika Universitas Jenderal Soedirman 2026).
   - **Formula Pertumbuhan Eksponensial**: Secara matematis, eksponen kuadrat ($K^2$) menyimbolkan pelipatgandaan modal dan ketahanan finansial yang tercipta dari sinergi dua kutub: *Pemasukan Aktif* dan *Pengendalian Pengeluaran Terukur*.
3. **Bentuk Geometri Squircle (Kurva Kontinyu kontur kurva kontinyu)**:
   - Logo dibungkus dalam bentuk squircle (*super-ellipse* dengan kurvatur kontinyu), menyingkirkan sudut tajam 90° yang kaku. Bentuk ini melambangkan perlindungan finansial yang menyeluruh, luwes, dan ramah bagi pengguna awam.
4. **Kubus Aksen AI (Superscript)**:
   - Elemen kotak/kubus melayang di sudut kanan atas huruf K melambangkan kecerdasan buatan (*applied multimodal intelligence*) yang senantiasa mendampingi setiap mutasi dan keputusan kas pengguna.
5. **Warna Terracotta Crimson (`#EB5B45`)**:
   - Mengombinasikan ketegasan warna merah finansial dengan kehangatan elemen tanah liat (*terracotta*), mencerminkan platform keuangan modern yang membumi, berintegritas, dan bersahabat.

- **Clear Space (Safe Area)**: Jarak bebas minimum di sekeliling logo setara dengan 50% tinggi monogram huruf K (0.5X).
- **Minimum Size**: 24dp x 24dp untuk ikon digital, 15mm x 15mm untuk media cetak.
- **Larangan Desain**: Dilarang memutar sudut logo, mengubah proporsi rasio 1:1, atau mengganti warna gradasi di luar palet resmi.

### 2.2 Palet Warna Resmi (Brand Colors)
KasKu mengombinasikan warna Terracotta yang hangat dan percaya diri dengan latar keramik gelap (*Ceramic Dark*) serta aksen kecerdasan cyber:

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  TERRACOTTA RED  │  │  CERAMIC DARK    │  │  CYBER AI VIOLET │
│     #E25B45      │  │     #121316      │  │     #7F52FF      │
│  Primary Action  │  │ Deep Background  │  │   AI Assistant   │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

| Nama Warna | Hex Code | Nilai RGB | Peran & Penggunaan Desain |
|---|---|---|---|
| **Terracotta Crimson** | `#E25B45` | `226, 91, 69` | Warna utama merek, tombol aksi primer, kartu debit virtual Fintech Modern |
| **Ceramic Dark** | `#121316` | `18, 19, 22` | Latar belakang dasar tema gelap, kontras tinggi |
| **Surface Midnight** | `#1E1F24` | `30, 31, 36` | Kontainer kartu, bottom sheet, komponen elevasi |
| **Soft Cloud Light** | `#F7F8FA` | `247, 248, 250` | Latar belakang tema terang, field input |
| **Emerald Surplus** | `#00C853` | `0, 200, 83` | Indikator pemasukan positif (+Rp), status cash flow surplus |
| **Rose Deficit** | `#FF3B30` | `255, 59, 48` | Indikator pengeluaran negatif (-Rp), status cash flow defisit |
| **Cyber AI Violet** | `#7F52FF` | `127, 82, 255` | Aksen KasKu AI Assistant, chips prompt rekomendasi |
| **Vibrant Coral** | `#FF6B4A` | `255, 107, 74` | Gradasi highlight kartu virtual dan active state |

### 2.3 Sistem Tipografi & Skala Teks
KasKu menggunakan tipografi neogrotesk modern (*Plus Jakarta Sans / Roboto*):
- **Display Saldo**: `SemiBold 32sp`, Tabular Numerals (Rp 12.450.000)
- **Headline Utama**: `Bold 20sp`, Pelacakan Huruf -0.5px
- **Title Subseksi**: `SemiBold 16sp`
- **Body Text**: `Regular 14sp`, Line Height 20sp
- **Micro Label / Badge**: `Medium 11sp`, Upper Case

---

## 3. Arsitektur Desain Antarmuka UI/UX (Design System)

### 3.1 Filosofi Sintesis: Fintech Modern + desain antarmuka humanis
1. **Pendekatan fintech modern**:
   - Kartu debit virtual interaktif berwarna Terracotta cerah sebagai focal point utama dompet.
   - Grafik batang arus kas 7 hari yang transparan dengan indikator surplus/defisit harian.
   - Pembedaan visual yang tegas antara rekening Bank, Kas Tunai, dan E-Wallet.
2. **Pendekatan prinsip desain humanis ergonomis**:
   - Dynamic Capsule Bottom Bar: bilah navigasi melayang (*floating capsule*) ber-radius 32dp dengan hairline border 0.5dp.
   - Modal Bottom Sheets dengan gestur geser ke bawah yang intuitif dan spring motion physics.
   - Hirarki tipografi proporsional dengan jarak bantalan (*padding*) 16dp dan 24dp yang konsisten.

---

## 4. Showcase Antarmuka & Penjelasan Layar Produk

### Layar 1: Splash Screen & Inisialisasi Brand
<p align="center">
  <img src="splash_loading.png" alt="Splash Screen" width="280" style="border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.3);"/>
</p>

* **Fungsionalitas**:
  - Menampilkan monogram logo resmi KasKu bernuansa Ceramic Slate.
  - Menjalankan sinkronisasi awal Room Database dan enkripsi DataStore di latar belakang secara asinkron.
  - Dilengkapi transisi fade-in 300ms dan fade-out 450ms untuk pengalaman pengguna yang lembut.

---

### Layar 2: Dashboard & Arus Kas Real-Time
<p align="center">
  <img src="01_dashboard.png" alt="Dashboard Screen" width="280" style="border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.3);"/>
</p>

* **Fungsionalitas Utama**:
  - **Sapaan Personal Dinamis**: Menampilkan waktu real-time (*"Selamat Pagi, iqsan"*), avatar profil, dan tombol cepat tambah transaksi (`+`).
  - **Kartu Virtual Dompet Interaktif**: Swipe gesture untuk berpindah rekening (Dompet Utama, Rekening Bank, Kas Tunai) dengan sakelar akumulatif saldo.
  - **Tombol Aksi Cepat**: Tombol *Catat Kas* dan *Kelola Dompet* untuk mutasi instan.
  - **Grafik Arus Kas 7 Hari**: Visualisasi perbandingan pemasukan vs pengeluaran harian dengan deteksi otomatis status *Surplus / Defisit*.
  - **Pintasan KasKu AI (✦)**: Akses sekali ketuk ke asisten finansial cerdas di sudut kanan atas.

---

### Layar 3: Riwayat & Pencarian Transaksi Cerdas
<p align="center">
  <img src="02_riwayat.png" alt="Riwayat Transaksi" width="280" style="border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.3);"/>
</p>

* **Fungsionalitas Utama**:
  - **Kartu Agregasi Saldo**: Total uang masuk (+Rp) dan uang keluar (-Rp) yang terhitung otomatis.
  - **Pencarian Cerdas Reaktif**: Pencarian real-time berdasarkan nama mutasi, nama toko/merchant, atau nama akun dompet.
  - **Filter Tab Instan**: Tiga kategori cepat: *Semua*, *Pemasukan*, dan *Pengeluaran*.
  - **Daftar Berkelompok**: Transaksi disusun rapi berdasarkan kelompok tanggal transaksi.
  - **Floating Action Button (FAB)**: Tombol melayang merah untuk pencatatan transaksi baru kapan saja.

---

### Layar 4: Smart Vision OCR CameraX Receipt Scanner
<p align="center">
  <img src="03_scan.png" alt="Receipt Scanner" width="280" style="border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.3);"/>
</p>

* **Fungsionalitas Utama**:
  - **Jendela Bidik Kamera Native (CameraX)**: Tampilan layar penuh dengan bingkai panduan struk fisik (*"Arahkan ke Struk"*).
  - **Kontrol Flash**: Sakelar lampu senter untuk pemindaian di tempat bercahaya redup.
  - **Import Galeri**: Pilihan mengambil foto nota dari penyimpanan lokal perangkat.
  - **AI Vision Parsing**: Mengirimkan citra ke Google Gemini Multimodal Vision untuk mendeteksi nama merchant, tanggal, rincian barang, dan total tagihan tanpa ketik manual.

---

### Layar 5: Tren & Analitik Arus Kas
<p align="center">
  <img src="04_tren.png" alt="Tren & Analitik" width="280" style="border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.3);"/>
</p>

* **Fungsionalitas Utama**:
  - **Pengalih Periode**: Beralih antara analisis *Mingguan (7 Hari)* dan *Bulanan*.
  - **Net Cash Flow Status**: Menampilkan selisih arus kas bersih dengan badge indikator *Surplus* hijau atau *Defisit* merah.
  - **Spending Leakage Detection**: Pemetaan kategori belanja paling boros untuk mengontrol pengeluaran impulsif.
  - **Rincian Harian**: Rekap transaksi per hari (Senin s.d. Minggu).

---

### Layar 6: Multi-Wallet, Multi-Currency & Konfigurasi AI
<p align="center">
  <img src="05_setelan_top.png" alt="Settings Top" width="260" style="border-radius: 20px; margin-right: 12px;"/>
  <img src="05_setelan_bottom.png" alt="Settings Bottom" width="260" style="border-radius: 20px;"/>
</p>

* **Fungsionalitas Utama**:
  - **Multi-Currency System**: Dukungan dinamis 9 mata uang dunia (IDR - Rp, USD - $, EUR - €, SGD - S$, MYR - RM, JPY - ¥, GBP - £, AUD - A$, SAR - SR).
  - **Live FX Currency Rates**: Kartu kurs valuta asing langsung dari REST API `open.er-api.com` dengan arsitektur Retrofit 2 UiState.
  - **Dual AI Engine**: Opsi mesin ganda antara **Google Gemini (Cloud)** dan **LM Studio (Local Offline)**.
  - **Dynamic Model Discovery**: Tombol *"Cek Izin & Sinkron Model"* yang secara otomatis mengambil seluruh model aktif dari Google AI API.
  - **Uji Koneksi AI (Ping)**: Diagnostik koneksi API key real-time.

---

### Layar 7: KasKu AI Assistant & Session Management
<p align="center">
  <img src="06_chat_ai.png" alt="Chat AI" width="260" style="border-radius: 20px; margin-right: 12px;"/>
  <img src="07_chat_ai_drawer.png" alt="Chat Drawer" width="260" style="border-radius: 20px;"/>
</p>

* **Fungsionalitas Utama**:
  - **Penasihat Finansial Generatif**: Berdialog interaktif seputar tips penghematan, evaluasi kas bulanan, dan alokasi bujet berdasarkan data mutasi nyata.
  - **Quick Prompt Chips**: Pintasan pertanyaan cepat: *"Evaluasi pengeluaranku bulan ini"*, *"Kategori apa yang paling boros?"*, *"Berapa sisa uang amanku saat ini?"*.
  - **Navigation Drawer Riwayat Sesi**:
    - Tombol **+ Chat Baru** untuk membuat sesi baru.
    - Riwayat sesi percakapan lampau yang dapat dikelola atau dihapus per topik.
    - Pintasan cepat kembali ke Beranda dan pembersihan riwayat obrolan.

---

## 5. Arsitektur Rekayasa & Standar Keamanan Data

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│        Jetpack Compose • Material 3 • Navigation       │
└───────────────────────────┬────────────────────────────┘
                            │ (StateFlow / UDF)
┌───────────────────────────▼────────────────────────────┐
│                    VIEWMODEL LAYER                     │
│         DashboardVM • TransactionsVM • ChatAiVM        │
└───────────────────────────┬────────────────────────────┘
                            │ (Coroutines Flow)
┌───────────────────────────▼────────────────────────────┐
│                    REPOSITORY LAYER                    │
│      KasKuRepository (Single Source of Truth)          │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
┌─────────────▼──────────────┐ ┌───────────▼─────────────┐
│      LOCAL DATA LAYER      │ │    REMOTE NETWORK LAYER │
│ • Room SQLite (5 Entitas)  │ │ • Gemini REST API       │
│ • Encrypted DataStore      │ │ • LM Studio OkHttp      │
│ • CameraX OCR Vision       │ │ • Retrofit 2 FX Rates   │
└────────────────────────────┘ └─────────────────────────┘
```

- **Clean Architecture & MVVM**: Pemisahan tegas antara UI, Domain Logika, dan Data Layer.
- **Offline-First Room SQLite**: 5 entitas relasional berstandar 3NF menjamin kecepatan kueri dan keandalan data tanpa membutuhkan koneksi internet.
- **Privacy Assurance**: Seluruh riwayat transaksi berada di dalam sandboxing lokal aplikasi Android (`/data/data/com.example.kasku/databases/`).

---

## 6. Tim Pengembang & Kredit Perusahaan

Aplikasi **KasKu** dikembangkan oleh **Kelompok 2 (Kelas A - Pemrograman Mobile)** Program Studi S1 Informatika, Fakultas Teknik, Universitas Jenderal Soedirman (Tahun Akademik 2026):

| No | Nama Mahasiswa | NIM | Peran Utama | Fokus Kontribusi Teknis |
|:---:|:---|:---:|:---|:---|
| 1 | **Iqsan Azhar Nuryadi** | `H1D024009` | **Lead Architect & UI/UX Specialist** | Konsep desain Fintech Modern + desain antarmuka humanis, arsitektur tema, `Desain ModernComponents.kt`, dan sistem multi-currency dinamis. |
| 2 | **Surung Nicholas Manalu** | `H1D024017` | **Core Feature & State Management** | `DashboardScreen`, stack kartu virtual swipeable, formulir mutasi transaksi (`AddTransactionSheet`), dan alur state UDF. |
| 3 | **Izaz Falih** | `H1D024034` | **Database & Repository Architect** | Skema relasional Room SQLite (5 entitas 3NF), DAO reaktif, enkripsi preferensi DataStore, dan repository pattern. |
| 4 | **Najmi Zahrian** | `H1D024038` | **AI Vision & Network Engineer** | Integrasi kamera CameraX, Gemini Multimodal Vision OCR struk belanja, Retrofit 2 REST API kurs live, dan discovery model dinamis. |

---

<div align="center">
  <sub>© 2026 KasKu Team • Hak Cipta Dilindungi Undang-Undang • All Rights Reserved • .</sub>
</div>
