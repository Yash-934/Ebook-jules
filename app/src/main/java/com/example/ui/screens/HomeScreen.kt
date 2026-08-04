package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.Book
import com.example.ui.EbookViewModel
import com.example.ui.SettingsViewModel
import kotlinx.coroutines.launch

val BrownText = Color(0xFF4A3B32)
val LightCream = Color(0xFFFAF6EE)
val BeigeCardTop = Color(0xFFEBE3D1)
val BeigeCardBottom = Color(0xFFFDFBF7)
val GoldenOrange = Color(0xFFC78B20)
val GreyText = Color(0xFF8C7F70)
val DividerColor = Color(0xFFE0D5C1)
val SettingsBg = Color(0xFFF5EFE6)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    ebookViewModel: EbookViewModel,
    settingsViewModel: SettingsViewModel
) {
    val booksFlow by ebookViewModel.allBooks.collectAsState(initial = emptyList())
    var showAddBookOptions by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Recent") }
    var selectedFolder by remember { mutableStateOf("All") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var customFolders by remember { mutableStateOf(setOf<String>()) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            topBar = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightCream)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📚 My Library",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrownText
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SettingsBg)
                                    .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
                                    .clickable { scope.launch { drawerState.open() } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color(0xFF5DB2C6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Button(
                                onClick = { showAddBookOptions = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldenOrange,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("+ Add Book", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                }
            },
            containerColor = LightCream
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "📖 Your Bookshelf",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrownText
                    )
                }

                // Sort row
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sort:", color = GreyText, fontSize = 18.sp, modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterVertically))

                    val filters = listOf(
                        "Recent" to "🕐",
                        "Last Read" to "📖",
                        "Title" to "🔤",
                        "Favorites" to "❤️",
                        "Bookmarks" to "🔖"
                    )

                    filters.forEach { (filterName, emoji) ->
                        val isSelected = selectedFilter == filterName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filterName },
                            label = { Text("$emoji $filterName", fontSize = 16.sp, color = if (isSelected) Color.White else BrownText) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldenOrange,
                                selectedLabelColor = Color.White,
                                containerColor = Color.Transparent,
                                labelColor = BrownText
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = if (isSelected) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = DividerColor)
                        )
                    }
                }

                // Folders row
                val folders = remember(booksFlow, customFolders) {
                    val dbFolders = booksFlow.map { it.folder }
                    val allFolders = (dbFolders + customFolders).filter { it.isNotBlank() && it != "All" }.distinct().sorted()
                    listOf("All") + allFolders
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Folder:", color = GreyText, fontSize = 18.sp, modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterVertically))
                    folders.forEach { folderName ->
                        val isSelected = selectedFolder == folderName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFolder = folderName },
                            label = { Text(if (folderName == "All") "📂 All" else "📁 $folderName", fontSize = 16.sp, color = if (isSelected) Color.White else BrownText) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldenOrange,
                                selectedLabelColor = Color.White,
                                containerColor = Color.Transparent,
                                labelColor = BrownText
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = if (isSelected) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = DividerColor)
                        )
                    }

                    FilterChip(
                        selected = false,
                        onClick = { showNewFolderDialog = true },
                        label = { Text("➕ New", fontSize = 16.sp, color = BrownText) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = BrownText
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = GoldenOrange)
                    )
                }

                val filteredBooks = if (selectedFolder == "All") booksFlow else booksFlow.filter { it.folder == selectedFolder }
                val books = when (selectedFilter) {
                    "Title" -> filteredBooks.sortedBy { it.title }
                    "Last Read" -> filteredBooks.sortedByDescending { it.lastRead }
                    "Favorites" -> filteredBooks.filter { it.isFavorite }
                    "Bookmarks" -> filteredBooks.filter { try { org.json.JSONArray(it.bookmarks).length() > 0 } catch(e: Exception) { false } }
                    else -> filteredBooks.sortedByDescending { it.id }
                }

                if (selectedFilter == "Bookmarks") {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(books) { book ->
                            Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, DividerColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(modifier = Modifier.height(180.dp).width(120.dp)) {
                                        BookItem(
                                            book = book,
                                            onClick = { navController.navigate("read/${book.id}") },
                                            onDelete = { ebookViewModel.deleteBook(book.id) },
                                            onUpdateCover = { ebookViewModel.updateBookCover(book.id, it) }
                                        )
                                    }
                                    Column {
                                        Text(book.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrownText)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val bookmarksArray = try { org.json.JSONArray(book.bookmarks) } catch(e:Exception){ org.json.JSONArray() }
                                        for (i in 0 until bookmarksArray.length()) {
                                            val bm = bookmarksArray.getJSONObject(i)
                                            val pos = bm.getInt("position")
                                            val name = bm.optString("name", "Bookmark at $pos")
                                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                                navController.navigate("read/${book.id}?scrollTo=$pos")
                                            }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(24.dp).background(GoldenOrange, CircleShape), contentAlignment = Alignment.Center) {
                                                    Text("${i+1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(name, color = BrownText, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(books) { book ->
                            BookItem(
                                book = book,
                                onClick = { navController.navigate("read/${book.id}") },
                                onDelete = { ebookViewModel.deleteBook(book.id) },
                                onUpdateCover = { ebookViewModel.updateBookCover(book.id, it) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddBookOptions) {
            AddBookOptionsDialog(
                onDismiss = { showAddBookOptions = false },
                onAddBook = { title, author, format, coverUri, localUri ->
                    val destFolder = if (selectedFolder == "All") "Main" else selectedFolder
                    ebookViewModel.addBook(Book(title = title, author = author, format = format, localUri = localUri, coverUri = coverUri, folder = destFolder))
                }
            )
        }

        if (showNewFolderDialog) {
            AlertDialog(
                onDismissRequest = { showNewFolderDialog = false },
                title = { Text("New Folder") },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                customFolders = customFolders + newFolderName
                                selectedFolder = newFolderName
                                showNewFolderDialog = false
                                newFolderName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenOrange)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewFolderDialog = false }) {
                        Text("Cancel", color = GoldenOrange)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUpdateCover: (String) -> Unit
) {
    var showEditOptions by remember { mutableStateOf(false) }

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUpdateCover(uri.toString())
            showEditOptions = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {
                        if (showEditOptions) showEditOptions = false
                        else onClick()
                    },
                    onLongClick = { showEditOptions = true }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BeigeCardBottom),
            border = BorderStroke(1.dp, GoldenOrange),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5f)
                        .background(BeigeCardTop),
                    contentAlignment = Alignment.Center
                ) {
                    if (book.coverUri.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = book.coverUri,
                            contentDescription = "Book Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "📖",
                            fontSize = 64.sp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isBookmarked = try { org.json.JSONArray(book.bookmarks).length() > 0 } catch(e:Exception){ false }
                        if (book.isFavorite) {
                            Box(
                                modifier = Modifier
                                    .padding(end = if (isBookmarked) 4.dp else 0.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                                    .padding(4.dp)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Favorite", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            }
                        }
                        if (isBookmarked) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                                    .padding(4.dp)
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = Color(0xFFFFA500), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFDF4D6), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = book.format,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = BrownText
                        )
                        Text(
                            text = book.author,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = GreyText
                        )
                    }

                    Column {
                        LinearProgressIndicator(
                            progress = { book.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = GoldenOrange,
                            trackColor = BeigeCardTop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(book.progress * 100).toInt()}% read",
                            fontSize = 12.sp,
                            color = GreyText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (showEditOptions) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFFCE3D44), CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🗑️", fontSize = 16.sp)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFF4A8BAD), CircleShape)
                        .clickable { coverLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AddBookOptionsDialog(
    onDismiss: () -> Unit,
    onAddBook: (String, String, String, String, String) -> Unit
) {
    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            onAddBook("Imported Book 1", "Unknown Author", "PDF", "", uri.toString())
            onAddBook("Imported Book 2", "Unknown Author", "EPUB", "", uri.toString())
            onAddBook("Imported Book 3", "Unknown Author", "HTML", "", uri.toString())
            onDismiss()
        } else {
            onDismiss()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val fileLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {}

            // Get actual filename
            var fileName = "Unknown"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            if (fileName == "Unknown") {
                fileName = uri.lastPathSegment?.split("/")?.lastOrNull() ?: "Unknown"
            }

            val title = fileName.substringBeforeLast(".")
            val format = fileName.substringAfterLast(".", "HTML").uppercase()
            onAddBook(title, "Unknown Author", format, "", uri.toString())
            onDismiss()
        } else {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { folderLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenOrange)
                ) {
                    Text("Import All (Folder)")
                }

                Button(
                    onClick = { fileLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenOrange)
                ) {
                    Text("Import Individual (File)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GoldenOrange)
            }
        },
        containerColor = LightCream
    )
}
