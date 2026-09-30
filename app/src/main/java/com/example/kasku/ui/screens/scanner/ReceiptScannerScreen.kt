package com.example.kasku.ui.screens.scanner

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun ReceiptScannerScreen(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    onTransactionSaved: () -> Unit,
    modifier: Modifier = Modifier,
    onCloseScanner: () -> Unit = onTransactionSaved,
    viewModel: ReceiptScannerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ReceiptScannerViewModel.Factory(repository, aiService, aiPreferences)
    )
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveSuccessMessage by viewModel.saveSuccessMessage.collectAsState()

    // 1. Permission States
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasGalleryPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        val readPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        hasGalleryPermission = perms[readPerm] == true ||
                ContextCompat.checkSelfPermission(context, readPerm) == PackageManager.PERMISSION_GRANTED
    }

    // Auto-request permission on screen open
    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasGalleryPermission) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    // 2. CameraX States
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isCapturing by remember { mutableStateOf(false) }

    // 3. Mini Gallery Strip States
    var recentPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }

    fun refreshRecentPhotos() {
        if (hasGalleryPermission) {
            scope.launch(Dispatchers.IO) {
                val photos = queryRecentGalleryImages(context)
                withContext(Dispatchers.Main) {
                    recentPhotos = photos
                }
            }
        }
    }

    LaunchedEffect(hasGalleryPermission) {
        if (hasGalleryPermission) {
            refreshRecentPhotos()
        }
    }

    // Image Picker Launcher (buka file picker sistem)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            previewImageUri = uri
            scope.launch {
                val bytes = processUriToBytes(context, uri)
                if (bytes != null) {
                    viewModel.clearScan()
                    viewModel.scanReceiptImage(bytes)
                }
            }
        }
    }

    // Form inputs for editing scanned receipt
    var storeNameInput by remember { mutableStateOf("") }
    var totalAmountInput by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(scanResult) {
        scanResult?.let { extracted ->
            storeNameInput = extracted.storeName
            totalAmountInput = if (extracted.totalAmount > 0) extracted.totalAmount.toLong().toString() else ""
            val matchedCat = categories.firstOrNull {
                it.name.contains(extracted.suggestedCategory, ignoreCase = true)
            } ?: categories.firstOrNull()
            selectedCategoryId = matchedCat?.id
            if (selectedAccountId == null) {
                selectedAccountId = accounts.firstOrNull()?.id
            }
        }
    }

    // ====================================================================
    // LAYOUT UTAMA: FULLSCREEN CAMERA VIEWPORT
    // ====================================================================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Live Camera Viewfinder via AndroidView CameraX
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                            // Set initial flash state
                            camera?.cameraControl?.enableTorch(isFlashOn)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    // Update flash / torch when state changes
                    camera?.cameraControl?.enableTorch(isFlashOn)
                }
            )
        } else {
            // Fallback jika izin kamera belum diberikan
            PermissionNoticeOverlay(
                onRequestPermissions = { permissionLauncher.launch(requiredPermissions) }
            )
        }

        // ====================================================================
        // TOP CONTROLS (Gaya WhatsApp: Close [X] kiri, Flash kanan)
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol [X] Batal / Kembali ke Home
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable { onCloseScanner() },
                shape = CircleShape,
                color = Color(0x66000000)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Status Text Minimalis di Tengah
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x55000000),
                border = BorderStroke(0.5.dp, Color(0x33FFFFFF))
            ) {
                Text(
                    text = "Arahkan ke Struk",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )
                )
            }

            // Tombol Flash / Senter HP (Kanan Atas)
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable {
                        isFlashOn = !isFlashOn
                        camera?.cameraControl?.enableTorch(isFlashOn)
                    },
                shape = CircleShape,
                color = if (isFlashOn) Color(0x99FFD600) else Color(0x66000000),
                border = if (isFlashOn) BorderStroke(1.dp, Color(0xFFFFD600)) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = if (isFlashOn) "Matikan Flash" else "Nyalakan Flash",
                        tint = if (isFlashOn) Color.Black else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ====================================================================
        // BOTTOM CONTAINER: MINI GALLERY STRIP + WHATSAPP CONTROLS
        // ====================================================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x99000000),
                            Color(0xEE000000)
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Handle Pill Putih (Seperti di WhatsApp)
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(width = 38.dp, height = 4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xCCFFFFFF))
            )

            // 2. Strip Horizontal Galeri Mini (Foto-Foto Terbaru)
            if (recentPhotos.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentPhotos) { uri ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                                .clickable {
                                    previewImageUri = uri
                                    scope.launch {
                                        val bytes = processUriToBytes(context, uri)
                                        if (bytes != null) {
                                            viewModel.clearScan()
                                            viewModel.scanReceiptImage(bytes)
                                        }
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Foto Galeri",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Tombol Shutter Capture & Aksi Kamera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Galeri (Kiri Bawah)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = CircleShape,
                    color = Color(0x66000000),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Pilih dari Galeri",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Tombol Capture / Shutter Utama (Tengah - Gaya WhatsApp)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable(enabled = !isCapturing && !isScanning && hasCameraPermission) {
                            val capture = imageCapture
                            if (capture != null) {
                                isCapturing = true
                                val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            isCapturing = false
                                            val uri = Uri.fromFile(photoFile)
                                            previewImageUri = uri
                                            scope.launch {
                                                val bytes = processFileToBytes(photoFile)
                                                if (bytes != null) {
                                                    viewModel.clearScan()
                                                    viewModel.scanReceiptImage(bytes)
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isCapturing = false
                                            exception.printStackTrace()
                                        }
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Lingkaran Putih Dalam
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = if (isCapturing || isScanning) Color(0x88FFFFFF) else Color.White
                    ) {
                        if (isCapturing || isScanning) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = MonzoTeal,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }
                }

                // Tombol Flip / Switch Kamera (Kanan Bawah)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                    shape = CircleShape,
                    color = Color(0x66000000),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.FlipCameraAndroid,
                            contentDescription = "Putar Kamera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Label Mode di Bawah Shutter
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFFFFD600), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PINDAI STRUK AI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD600),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // ====================================================================
        // OVERLAY SAAT AI SEDANG MEMINDAI (SCANNING LASER EFFECT)
        // ====================================================================
        if (isScanning) {
            val infiniteTransition = rememberInfiniteTransition(label = "laser")
            val laserOffset by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laserOffset"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
            ) {
                // Bingkai Area Scan
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.65f)
                        .border(2.dp, MonzoTeal.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                ) {
                    // Garis Laser Scan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.015f)
                            .align(Alignment.TopCenter)
                            .offset(y = (laserOffset * 350).dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MonzoTeal,
                                        Color(0xFF64FFDA),
                                        MonzoTeal,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Status Banner AI
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC000000),
                    border = BorderStroke(1.dp, MonzoTeal.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MonzoTeal,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "KasKu AI sedang mengekstrak rincian struk...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // ====================================================================
        // MODAL BOTTOM SHEET: HASIL EKSTRAKSI STRUK & SIMPAN TRANSAKSI
        // ====================================================================
        AnimatedVisibility(
            visible = scanResult != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            scanResult?.let { result ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.82f)
                        .shadow(16.dp, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    color = MonzoSurface,
                    border = BorderStroke(1.dp, MonzoBorder)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Handle & Title
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 40.dp, height = 5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFD1D8D4))
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MonzoIncomeGreen.copy(alpha = 0.15f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                tint = MonzoIncomeGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Struk Berhasil Diekstrak",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MonzoTextPrimary,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Text(
                                            text = "Silakan periksa & sesuaikan sebelum disimpan",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MonzoTextSecondary,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.clearScan() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Tutup",
                                        tint = MonzoTextSecondary
                                    )
                                }
                            }
                        }

                        // Form Toko
                        item {
                            OutlinedTextField(
                                value = storeNameInput,
                                onValueChange = { storeNameInput = it },
                                label = { Text("Nama Toko / Merchant") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonzoTeal,
                                    unfocusedBorderColor = MonzoBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFCFA)
                                )
                            )
                        }

                        // Form Total Nominal
                        item {
                            OutlinedTextField(
                                value = totalAmountInput,
                                onValueChange = { totalAmountInput = it },
                                label = { Text("Total Pengeluaran (Rp)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MonzoTeal,
                                    unfocusedBorderColor = MonzoBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFCFA)
                                )
                            )
                        }

                        // Pilihan Akun / Dompet Pembayar
                        item {
                            Text(
                                text = "Dibayar Dari Dompet:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val isSelected = acc.id == selectedAccountId
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedAccountId = acc.id },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MonzoTeal else Color(0xFFF1F5F2),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MonzoTeal else Color(0xFFE2E9E4)
                                        )
                                    ) {
                                        Text(
                                            text = acc.name.split(" ").firstOrNull() ?: acc.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else MonzoTextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Pilihan Kategori
                        item {
                            Text(
                                text = "Kategori Pengeluaran:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { cat ->
                                    val isSelected = cat.id == selectedCategoryId
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedCategoryId = cat.id },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MonzoCoral else Color(0xFFF1F5F2),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MonzoCoral else Color(0xFFE2E9E4)
                                        )
                                    ) {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else MonzoTextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Rincian Item Struk
                        if (result.items.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF7FAF8),
                                    border = BorderStroke(1.dp, Color(0xFFE4ECE6))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Daftar Barang (${result.items.size} item):",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MonzoTextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        result.items.forEach { itm ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${itm.quantity}x ${itm.name}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = MonzoTextSecondary),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = formatRupiah(itm.price),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MonzoTextPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Tombol Aksi: Simpan Transaksi & Foto Ulang
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Foto Ulang
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.clearScan() },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFF1F5F2),
                                    border = BorderStroke(1.dp, Color(0xFFD6DFD8))
                                ) {
                                    Text(
                                        text = "Foto Ulang",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MonzoTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(vertical = 14.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Simpan
                                Surface(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            val amount = totalAmountInput.toDoubleOrNull() ?: 0.0
                                            val catId = selectedCategoryId ?: categories.firstOrNull()?.id ?: 0L
                                            val accId = selectedAccountId ?: accounts.firstOrNull()?.id ?: 0L
                                            viewModel.saveScannedTransaction(
                                                storeName = storeNameInput,
                                                amount = amount,
                                                categoryId = catId,
                                                accountId = accId,
                                                items = result.items,
                                                onSuccess = onTransactionSaved
                                            )
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MonzoTeal
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 14.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Simpan Kas",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tampilan Layar Izin jika Akses Kamera & Galeri belum diizinkan
 */
@Composable
private fun PermissionNoticeOverlay(
    onRequestPermissions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F171A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color(0xFF1E2D33),
                border = BorderStroke(1.dp, Color(0xFF2C4048))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = MonzoTeal,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Izin Kamera & Galeri Diperlukan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Untuk memindai struk belanja otomatis menggunakan AI dan memilih foto langsung dari galeri ponsel Anda, mohon izinkan akses Kamera dan Penyimpanan Foto.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFA1B0B8),
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onRequestPermissions() },
                shape = RoundedCornerShape(16.dp),
                color = MonzoTeal
            ) {
                Text(
                    text = "Izinkan Akses Sekarang",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * Mengambil daftar Uri 20 foto terbaru dari Galeri perangkat
 */
private fun queryRecentGalleryImages(context: Context): List<Uri> {
    val uris = mutableListOf<Uri>()
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED
    )
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    try {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var count = 0
            while (cursor.moveToNext() && count < 20) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                uris.add(contentUri)
                count++
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return uris
}

/**
 * Konversi Uri gambar ke ByteArray JPEG berukuran optimal untuk AI Scanner
 */
private suspend fun processUriToBytes(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val maxDimension = 1280
        val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
            val ratio = minOf(maxDimension.toFloat() / originalBitmap.width, maxDimension.toFloat() / originalBitmap.height)
            val newW = (originalBitmap.width * ratio).toInt().coerceAtLeast(1)
            val newH = (originalBitmap.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
        } else {
            originalBitmap
        }

        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, stream)
        stream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Konversi File jepretan kamera ke ByteArray JPEG berukuran optimal
 */
private suspend fun processFileToBytes(file: File): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val originalBitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null
        val maxDimension = 1280
        val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
            val ratio = minOf(maxDimension.toFloat() / originalBitmap.width, maxDimension.toFloat() / originalBitmap.height)
            val newW = (originalBitmap.width * ratio).toInt().coerceAtLeast(1)
            val newH = (originalBitmap.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
        } else {
            originalBitmap
        }

        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, stream)
        stream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
