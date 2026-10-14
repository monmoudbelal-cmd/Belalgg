package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.FileItemEntity
import com.example.ui.viewmodel.EduViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

// ----------------- PDF Viewer Screen -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    fileItem: FileItemEntity,
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateBack()
    }

    var currentPageIndex by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(1) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    // Load PDF file via PdfRenderer
    LaunchedEffect(fileItem.uriString, currentPageIndex) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val file = if (fileItem.uriString.startsWith("content://")) {
                    // Copy content URI to temporary cache file
                    val uri = Uri.parse(fileItem.uriString)
                    val temp = File(context.cacheDir, "temp_render.pdf")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output ->
                            input.copyTo(output)
                        }
                    }
                    temp
                } else {
                    File(fileItem.uriString)
                }

                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "ملف PDF غير موجود على الجهاز"
                        isLoading = false
                    }
                    return@withContext
                }

                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                val pageIdx = currentPageIndex.coerceIn(0, count - 1)

                val page = renderer.openPage(pageIdx)
                val width = page.width * 2
                val height = page.height * 2
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                withContext(Dispatchers.Main) {
                    totalPages = count
                    currentBitmap = bmp
                    isLoading = false
                    errorMessage = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorMessage = "تعذر قراءة ملف PDF: ${e.localizedMessage}"
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(fileItem.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            text = if (totalPages > 0) "الصفحة ${currentPageIndex + 1} من $totalPages" else "عارض PDF",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("pdf_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3f) },
                        modifier = Modifier.testTag("pdf_zoom_in")
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "تكبير")
                    }
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f) },
                        modifier = Modifier.testTag("pdf_zoom_out")
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "تصغير")
                    }
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val uri = if (fileItem.uriString.startsWith("content://")) {
                                        Uri.parse(fileItem.uriString)
                                    } else {
                                        Uri.fromFile(File(fileItem.uriString))
                                    }
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "فتح ملف PDF بواسطة"))
                            } catch (e: Exception) {
                                viewModel.showSnackbar("لا يوجد تطبيق قارئ PDF مثبت")
                            }
                        },
                        modifier = Modifier.testTag("pdf_open_external")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "فتح خارجياً")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentPageIndex > 0) currentPageIndex--
                        },
                        enabled = currentPageIndex > 0,
                        modifier = Modifier.testTag("pdf_prev_page")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("السابقة")
                    }

                    Text(
                        text = "${currentPageIndex + 1} / $totalPages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = {
                            if (currentPageIndex < totalPages - 1) currentPageIndex++
                        },
                        enabled = currentPageIndex < totalPages - 1,
                        modifier = Modifier.testTag("pdf_next_page")
                    ) {
                        Text("التالية")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("جاري عرض صفحة PDF...", fontSize = 13.sp)
                }
            } else if (errorMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            } else if (currentBitmap != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .graphicsLayer(scaleX = zoomScale, scaleY = zoomScale),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "صفحة PDF",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (fileItem.extraContent.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("ملخص وشرح المذكرة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(fileItem.extraContent, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------- Video Player Screen -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    fileItem: FileItemEntity,
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler {
        viewModel.navigateBack()
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(0) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isBuffering by remember { mutableStateOf(true) }

    // Position updater ticker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                currentPositionMs = vv.currentPosition
                if (totalDurationMs == 0 && vv.duration > 0) {
                    totalDurationMs = vv.duration
                }
            }
            delay(500)
        }
    }

    fun formatTime(ms: Int): String {
        val totalSecs = (ms / 1000).coerceAtLeast(0)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(fileItem.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("مشغل المحاضرات والفيديوهات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            videoViewRef?.stopPlayback()
                            viewModel.navigateBack()
                        },
                        modifier = Modifier.testTag("video_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
        ) {
            // Video View Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val uri = Uri.parse(fileItem.uriString)
                            setVideoURI(uri)

                            setOnPreparedListener { mp ->
                                totalDurationMs = mp.duration
                                isBuffering = false
                                mp.start()
                                isPlaying = true
                            }

                            setOnCompletionListener {
                                isPlaying = false
                                currentPositionMs = totalDurationMs
                            }

                            setOnErrorListener { _, _, _ ->
                                isBuffering = false
                                false
                            }

                            videoViewRef = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isBuffering) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // Player Controls Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Time and Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatTime(currentPositionMs), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = if (totalDurationMs > 0) currentPositionMs.toFloat() / totalDurationMs else 0f,
                            onValueChange = { fraction ->
                                val targetMs = (fraction * totalDurationMs).toInt()
                                videoViewRef?.seekTo(targetMs)
                                currentPositionMs = targetMs
                            },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).testTag("video_seek_slider")
                        )
                        Text(formatTime(totalDurationMs), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Playback Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newPos = (currentPositionMs - 10000).coerceAtLeast(0)
                                videoViewRef?.seekTo(newPos)
                                currentPositionMs = newPos
                            },
                            modifier = Modifier.testTag("video_rewind_10")
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "تراجع 10 ثوان", modifier = Modifier.size(30.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    videoViewRef?.let { vv ->
                                        if (vv.isPlaying) {
                                            vv.pause()
                                            isPlaying = false
                                        } else {
                                            vv.start()
                                            isPlaying = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = {
                                val newPos = (currentPositionMs + 10000).coerceAtMost(totalDurationMs)
                                videoViewRef?.seekTo(newPos)
                                currentPositionMs = newPos
                            },
                            modifier = Modifier.testTag("video_forward_10")
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "تقديم 10 ثوان", modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }

            // Description / Lesson Notes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(fileItem.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (fileItem.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(fileItem.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (fileItem.extraContent.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("عناصر الدرس والنقاط المستفادة:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fileItem.extraContent, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- Image Viewer Screen -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    fileItem: FileItemEntity,
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(fileItem.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("عرض الصورة والمخطط", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("image_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    }) {
                        Icon(Icons.Default.Replay, contentDescription = "إعادة ضبط الحجم")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(0.8f, 4f)
                offset += panChange
            }

            if (fileItem.uriString.contains("drawable/img_edu_hero")) {
                Image(
                    painter = painterResource(id = R.drawable.img_edu_hero),
                    contentDescription = fileItem.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformableState)
                )
            } else {
                AsyncImage(
                    model = fileItem.uriString,
                    contentDescription = fileItem.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformableState)
                )
            }
        }
    }
}
