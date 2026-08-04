package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BorderColor

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import android.speech.tts.TextToSpeech
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.util.Base64
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import com.example.ui.EbookViewModel
import com.example.ui.SettingsViewModel
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadScreen(
    bookId: Int,
    ebookViewModel: EbookViewModel,
    settingsViewModel: SettingsViewModel,
    scrollTo: Int? = null,
    onNavigateBack: () -> Unit
) {
    val books by ebookViewModel.allBooks.collectAsState()
    val book = books.find { it.id == bookId }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val themeIndex by settingsViewModel.themeIndex.collectAsState()
    val fontFamilyIndex by settingsViewModel.fontFamilyIndex.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val lineSpacing by settingsViewModel.lineSpacing.collectAsState()
    val wordSpacing by settingsViewModel.wordSpacing.collectAsState()
    val margins by settingsViewModel.margins.collectAsState()
    val scrollMode by settingsViewModel.scrollMode.collectAsState()

    var isFullScreen by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isFavorite by remember(book) { mutableStateOf(book?.isFavorite ?: false) }
    var showIndexSheet by remember { mutableStateOf(false) }
    var showEditTools by remember { mutableStateOf(false) }
    var activeEditTool by remember { mutableStateOf<String?>(null) }
    var highlightColor by remember { mutableStateOf(Color(0xFFFFD700)) }
    var underlineColor by remember { mutableStateOf(Color.Red) }
    var doodleColor by remember { mutableStateOf(Color(0xFF87CEEB)) }

    var bookContent by remember { mutableStateOf<String?>(null) }
    val readContext = LocalContext.current

    LaunchedEffect(book?.localUri) {
        val uriStr = book?.localUri
        if (!uriStr.isNullOrEmpty() && uriStr != "mock_path") {
            try {
                val uri = android.net.Uri.parse(uriStr)
                readContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    bookContent = Base64.encodeToString(inputStream.readBytes(), Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                bookContent = "Error reading file: ${e.message}"
            }
        }
    }




    val drawnLines = remember { androidx.compose.runtime.mutableStateListOf<DrawnLine>() }
    var loadedAnnotations by remember { mutableStateOf(false) }

    LaunchedEffect(book) {
        if (!loadedAnnotations && book != null) {
            val ann = book.annotations
            if (ann.isNotEmpty() && ann != "[]") {
                try {
                    val array = JSONArray(ann)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val colorVal = obj.getLong("color")
                        val strokeWidth = obj.getDouble("strokeWidth").toFloat()
                        val alpha = obj.getDouble("alpha").toFloat()
                        val tool = obj.getString("tool")
                        val pointsArray = obj.getJSONArray("points")
                        val points = mutableListOf<Offset>()
                        val path = Path()
                        for (j in 0 until pointsArray.length()) {
                            val pt = pointsArray.getJSONObject(j)
                            val x = pt.getDouble("x").toFloat()
                            val y = pt.getDouble("y").toFloat()
                            val offset = Offset(x, y)
                            points.add(offset)
                            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawnLines.add(DrawnLine(points, path, Color(colorVal.toULong()), strokeWidth, alpha, tool))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            loadedAnnotations = true
        }
    }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentLineColor by remember { mutableStateOf(Color.Black) }
    var currentStrokeWidth by remember { mutableStateOf(5f) }
    var currentAlpha by remember { mutableStateOf(1f) }
    var isPlayingTTS by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val ttsContent = bookContent ?: "No content available to read."

    var isAutoScrolling by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(scrollTo) {
        if (scrollTo != null) {
            delay(100)
            scrollState.scrollTo(scrollTo)
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isAutoScrolling) {
        if (isAutoScrolling) {
            while (isActive) {
                delay(30)
                if (scrollState.value < scrollState.maxValue) {
                    scrollState.scrollTo(scrollState.value + 2)
                } else {
                    isAutoScrolling = false
                }
            }
        }
    }

    val backgroundColor = when (themeIndex) {
        0 -> Color.White
        1 -> Color(0xFF1A1A2E)
        2 -> Color(0xFFF4ECD8)
        3 -> Color.Black
        else -> Color(0xFFF4ECD8)
    }
    val textColor = when (themeIndex) {
        0 -> Color.Black
        1 -> Color(0xFFE8E9F0)
        2 -> Color(0xFF4A3828)
        3 -> Color(0xFFF0F0F0)
        else -> Color(0xFF4A3828)
    }

    val selectedFontFamily = when(fontFamilyIndex) {
        0 -> FontFamily.SansSerif
        1 -> FontFamily.Serif
        2 -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Search in Book") },
            text = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search term") }
                )
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Search")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                SettingsDrawerContent(
                    settingsViewModel = settingsViewModel,
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (!isFullScreen) {
                    Column(
                        modifier = Modifier.background(backgroundColor)
                    ) {
                        // Top Bar 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .statusBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF87CEEB), // Light blue
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = book?.title?.uppercase() ?: "README",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .clickable(onClick = onNavigateBack)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Library",
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Library", fontSize = 14.sp, color = textColor)
                                    }
                                }

                                ActionButton(icon = Icons.Default.Settings, tint = Color(0xFF5BA4A4), borderColor = textColor.copy(alpha = 0.2f), onClick = { scope.launch { drawerState.open() } })
                                ActionButton(icon = Icons.Default.Search, tint = Color(0xFF5BA4A4), borderColor = textColor.copy(alpha = 0.2f), onClick = { showSearchDialog = true })
                                ActionButton(icon = Icons.Default.Fullscreen, tint = textColor.copy(alpha = 0.6f), borderColor = textColor.copy(alpha = 0.2f), onClick = { isFullScreen = true })
                            }
                        }

                        HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                        // Top Bar 2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable(onClick = { showIndexSheet = true })
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = "Index",
                                        tint = textColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("INDEX", fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (showEditTools) {
                                    ActionButton(
                                        icon = Icons.Default.FormatUnderlined,
                                        tint = if (activeEditTool == "underline") Color.White else Color(0xFFE29578),
                                        borderColor = if (activeEditTool == "underline") Color(0xFFE29578) else textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            activeEditTool = if (activeEditTool == "underline") null else "underline"
                                            scope.launch { snackbarHostState.showSnackbar(if (activeEditTool != null) "Underline mode activated" else "Edit mode deactivated") }
                                        }
                                    )
                                    ActionButton(
                                        icon = Icons.Default.BorderColor,
                                        tint = if (activeEditTool == "highlight") Color.White else Color(0xFFFFD700),
                                        borderColor = if (activeEditTool == "highlight") Color(0xFFFFD700) else textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            activeEditTool = if (activeEditTool == "highlight") null else "highlight"
                                            scope.launch { snackbarHostState.showSnackbar(if (activeEditTool != null) "Highlight mode activated" else "Edit mode deactivated") }
                                        }
                                    )
                                    ActionButton(
                                        icon = Icons.Default.Brush,
                                        tint = if (activeEditTool == "doodle") Color.White else Color(0xFF87CEEB),
                                        borderColor = if (activeEditTool == "doodle") Color(0xFF87CEEB) else textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            activeEditTool = if (activeEditTool == "doodle") null else "doodle"
                                            scope.launch { snackbarHostState.showSnackbar(if (activeEditTool != null) "Doodle mode activated" else "Edit mode deactivated") }
                                        }
                                    )
                                    ActionButton(
                                        icon = Icons.Default.Edit,
                                        tint = textColor,
                                        borderColor = textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            showEditTools = false
                                            activeEditTool = null
                                        }
                                    )
                                } else {
                                    TextButtonAction("A-", borderColor = textColor.copy(alpha = 0.2f), textColor = textColor, onClick = { settingsViewModel.setFontSize(maxOf(10f, fontSize - 2f)) })
                                    TextButtonAction("A+", borderColor = textColor.copy(alpha = 0.2f), textColor = textColor, onClick = { settingsViewModel.setFontSize(minOf(40f, fontSize + 2f)) })
                                    ActionButton(
                                        icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        tint = if (isFavorite) Color(0xFFFF5252) else textColor,
                                        borderColor = textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            isFavorite = !isFavorite
                                            book?.let {
                                                ebookViewModel.addBook(it.copy(isFavorite = isFavorite))
                                            }
                                            scope.launch { snackbarHostState.showSnackbar(if (isFavorite) "Added to favorites" else "Removed from favorites") }
                                        }
                                    )
                                    val bookmarksArray = try { org.json.JSONArray(book?.bookmarks ?: "[]") } catch(e:Exception){ org.json.JSONArray() }
                                    val isBookmarked = (0 until bookmarksArray.length()).any {
                                        try { Math.abs(bookmarksArray.getJSONObject(it).getInt("position") - scrollState.value) < 500 } catch(e:Exception){false}
                                    }
                                    ActionButton(
                                        icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        tint = Color(0xFFFFA500),
                                        borderColor = textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            book?.let {
                                                if (!isBookmarked) {
                                                    ebookViewModel.addBookmark(it.id, scrollState.value, "Bookmark at ${scrollState.value}")
                                                    scope.launch { snackbarHostState.showSnackbar("Bookmark added") }
                                                }
                                            }
                                        }
                                    )
                                    ActionButton(
                                        icon = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        tint = textColor,
                                        borderColor = textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            isAutoScrolling = !isAutoScrolling
                                            scope.launch { snackbarHostState.showSnackbar(if (isAutoScrolling) "Auto scroll started" else "Auto scroll stopped") }
                                        }
                                    )
                                    ActionButton(
                                        icon = if (isPlayingTTS) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                        tint = textColor,
                                        borderColor = textColor.copy(alpha = 0.2f),
                                        onClick = {
                                            isPlayingTTS = !isPlayingTTS
                                            if (isPlayingTTS) {
                                                tts?.speak(ttsContent, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
                                            } else {
                                                tts?.stop()
                                            }
                                            scope.launch { snackbarHostState.showSnackbar(if (isPlayingTTS) "TTS started" else "TTS stopped") }
                                        }
                                    )
                                    ActionButton(
                                        icon = Icons.Default.Edit,
                                        tint = textColor,
                                        borderColor = textColor.copy(alpha = 0.2f),
                                        onClick = { showEditTools = true }
                                    )
                                }
                            }
                        }

                        if (activeEditTool != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val colors = listOf(Color.Red, Color(0xFFE29578), Color(0xFFFFD700), Color(0xFF87CEEB), Color(0xFF5BA4A4), Color.Black, Color.White)
                                Text("Color:", fontSize = 12.sp, color = textColor)
                                colors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(color)
                                            .border(
                                                width = 2.dp,
                                                color = if ((activeEditTool == "highlight" && highlightColor == color) ||
                                                            (activeEditTool == "underline" && underlineColor == color) ||
                                                            (activeEditTool == "doodle" && doodleColor == color))
                                                        textColor else Color.Transparent,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .clickable {
                                                when (activeEditTool) {
                                                    "highlight" -> highlightColor = color
                                                    "underline" -> underlineColor = color
                                                    "doodle" -> doodleColor = color
                                                }
                                            }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                    }
                }
            },
            containerColor = backgroundColor
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .pointerInput(activeEditTool) {
                        if (activeEditTool != null) {
                            var currentPoints = mutableListOf<Offset>()
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints = mutableListOf(offset)
                                    val path = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = path
                                    currentLineColor = when (activeEditTool) {
                                        "highlight" -> highlightColor
                                        "underline" -> underlineColor
                                        else -> doodleColor
                                    }
                                    currentStrokeWidth = when (activeEditTool) {
                                        "highlight" -> 40f
                                        "underline" -> 5f
                                        else -> 8f
                                    }
                                    currentAlpha = when (activeEditTool) {
                                        "highlight" -> 0.4f
                                        else -> 0.8f
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentPoints.add(change.position)
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    val newPath = Path().apply { currentPath?.let { addPath(it) } }
                                    currentPath = newPath
                                },
                                onDragEnd = {
                                    currentPath?.let {
                                        drawnLines.add(DrawnLine(currentPoints.toList(), it, currentLineColor, currentStrokeWidth, currentAlpha, activeEditTool ?: "doodle"))
                                        book?.id?.let { ebookViewModel.updateAnnotations(it, drawnLines) }
                                    }
                                    currentPath = null
                                },
                                onDragCancel = { currentPath = null }
                            )
                        }
                    }
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowFileAccessFromFileURLs = true
                            settings.allowUniversalAccessFromFileURLs = true

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    if (bookContent != null) {
                                        val js = "if(window.loadBookFromBase64) { window.loadBookFromBase64('${book?.format ?: "TXT"}', '$bookContent'); } else { console.log('loadBookFromBase64 not found'); }"
                                        view?.evaluateJavascript(js, null)
                                    }
                                }
                            }
                            webChromeClient = WebChromeClient()
                            loadUrl("file:///android_asset/index.html")
                        }
                    },
                    update = { webView ->
                        // Javascript is injected when page is finished loading
                    }
                )

                    }

                    Canvas(modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    // Find closest line and remove it
                                    val threshold = 40f
                                    val lineToRemove = drawnLines.find { line ->
                                        line.points.any { pt ->
                                            val dx = pt.x - offset.x
                                            val dy = pt.y - offset.y
                                            (dx * dx + dy * dy) < threshold * threshold
                                        }
                                    }
                                    if (lineToRemove != null) {
                                        drawnLines.remove(lineToRemove)
                                        ebookViewModel.updateAnnotations(bookId, drawnLines)
                                    }
                                }
                            )
                        }
                    ) {
                        for (line in drawnLines) {
                            drawPath(
                                path = line.path,
                                color = line.color.copy(alpha = line.alpha),
                                style = Stroke(
                                    width = line.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                        currentPath?.let {
                            drawPath(
                                path = it,
                                color = currentLineColor.copy(alpha = currentAlpha),
                                style = Stroke(
                                    width = currentStrokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                if (isFullScreen) {
                    IconButton(
                        onClick = { isFullScreen = false },
                        modifier = Modifier
                            .padding(16.dp)

                            .statusBarsPadding()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                    }
                }
            }
        }

        if (showIndexSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
            ModalBottomSheet(
                onDismissRequest = { showIndexSheet = false },
                containerColor = backgroundColor,
                contentColor = textColor,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        "Table of Contents",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Divider(color = textColor.copy(alpha = 0.1f))

                    val chapters = listOf(
                        "Cover" to "1%",
                        "Title Page" to "2%",
                        "Copyright" to "3%",
                        "Dedication" to "4%",
                        "Introduction" to "5%",
                        "Chapter 1: The Beginning" to "8%",
                        "Chapter 2: The Journey" to "15%",
                        "Chapter 3: The Climax" to "45%",
                        "Chapter 4: The Resolution" to "78%",
                        "Epilogue" to "98%",
                        "Acknowledgments" to "99%"
                    )

                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(chapters) { (title, progress) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showIndexSheet = false }
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                                Text(
                                    text = progress,
                                    fontSize = 14.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                            Divider(color = textColor.copy(alpha = 0.05f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

@Composable
fun FileRow(fileName: String, description: String, textColor: Color, fontSize: Float, lineSpacing: Float, wordSpacing: Float, fontFamily: FontFamily) {
    Row(modifier = Modifier.padding(bottom = (fontSize * 0.8f).dp)) {
        Text(
            text = fileName,
            fontSize = fontSize.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = wordSpacing.sp,
            color = textColor,
            modifier = Modifier.weight(0.3f).padding(end = 16.dp)
        )
        Text(
            text = description,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * lineSpacing).sp,
            fontFamily = fontFamily,
            letterSpacing = wordSpacing.sp,
            color = textColor,
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, borderColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun TextButtonAction(text: String, borderColor: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}
