package com.xfont.app.ui.home

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.xfont.app.ui.theme.pressScale
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xfont.app.manager.FontFileManager
import com.xfont.app.model.BreadcrumbItem
import com.xfont.app.model.ClipboardOp
import com.xfont.app.model.ClipboardState
import com.xfont.app.model.FontItem
import com.xfont.app.ui.theme.ExpressiveIconShapes
import com.xfont.app.ui.theme.XFontTypography
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    fileManager: FontFileManager,
    currentFolder: File,
    onCurrentFolderChange: (File) -> Unit,
    breadcrumbs: SnapshotStateList<BreadcrumbItem>,
    lazyListState: LazyListState,
    lastTappedItemUri: Uri? = null,
    onLastTappedItemUriChange: (Uri?) -> Unit = {},
    folderScrollPositions: MutableMap<String, Pair<Int, Int>> = remember { mutableMapOf() },
    searchFocusTrigger: Int = 0,
    isFabExpanded: Boolean = false,
    onFabExpandedChange: (Boolean) -> Unit = {},
    isActive: Boolean = true,
    triggerAddFont: Int = 0,
    triggerAddFolder: Int = 0,
    triggerCreateFolder: Int = 0,
    onResetTriggerAddFont: () -> Unit = {},
    onResetTriggerAddFolder: () -> Unit = {},
    onResetTriggerCreateFolder: () -> Unit = {},
    onFontSelected: (FontItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val breadcrumbsListState = rememberLazyListState()

    LaunchedEffect(breadcrumbs.size) {
        if (breadcrumbs.isNotEmpty()) {
            breadcrumbsListState.animateScrollToItem(breadcrumbs.size - 1)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var items by remember(currentFolder) {
        mutableStateOf(fileManager.listFilesInDirSync(currentFolder))
    }

    val searchFocusRequester = remember { FocusRequester() }
    LaunchedEffect(searchFocusTrigger) {
        if (searchFocusTrigger > 0) {
            searchFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    // Multi-select & CRUD state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedUris = remember { mutableStateListOf<Uri>() }
    var clipboardState by remember { mutableStateOf<ClipboardState?>(null) }

    // Dialog states
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FontItem?>(null) }
    var renameNewName by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Document pickers for Add Font & Add Folder
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                isLoading = true
                for (uri in uris) {
                    fileManager.addFontFromUri(uri, currentFolder)
                }
                items = fileManager.listFilesInDir(currentFolder)
                isLoading = false
            }
        }
    }

    LaunchedEffect(triggerAddFont) {
        if (triggerAddFont > 0) {
            fontPickerLauncher.launch(
                arrayOf(
                    "font/otf",
                    "font/ttf",
                    "font/*",
                    "application/font-sfnt",
                    "application/x-font-opentype",
                    "application/x-font-ttf",
                    "application/vnd.ms-opentype",
                    "application/octet-stream",
                    "*/*"
                )
            )
            onResetTriggerAddFont()
        }
    }

    val folderTreePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            scope.launch {
                isLoading = true
                fileManager.addFolderFromTreeUri(treeUri, currentFolder)
                items = fileManager.listFilesInDir(currentFolder)
                isLoading = false
            }
        }
    }

    LaunchedEffect(triggerAddFolder) {
        if (triggerAddFolder > 0) {
            folderTreePickerLauncher.launch(null)
            onResetTriggerAddFolder()
        }
    }

    LaunchedEffect(triggerCreateFolder) {
        if (triggerCreateFolder > 0) {
            newFolderName = ""
            showNewFolderDialog = true
            onResetTriggerCreateFolder()
        }
    }

    fun refreshCurrentDirectory(forceRefresh: Boolean = false) {
        isLoading = true
        scope.launch {
            if (searchQuery.isNotBlank()) {
                items = fileManager.searchAllFonts(searchQuery)
            } else {
                items = fileManager.listFilesInDir(currentFolder, forceRefresh = forceRefresh)
            }
            isLoading = false
        }
    }

    LaunchedEffect(currentFolder, searchQuery) {
        refreshCurrentDirectory()
    }

    LaunchedEffect(currentFolder, items) {
        if (items.isNotEmpty()) {
            val saved = folderScrollPositions[currentFolder.absolutePath]
            if (saved != null) {
                val safeIndex = saved.first.coerceAtMost(items.size - 1)
                lazyListState.scrollToItem(safeIndex, saved.second)
                onLastTappedItemUriChange(null)
            } else if (lastTappedItemUri != null) {
                val targetIdx = items.indexOfFirst {
                    it.uri == lastTappedItemUri || (lastTappedItemUri.path != null && it.file?.absolutePath == lastTappedItemUri.path)
                }
                if (targetIdx != -1) {
                    lazyListState.scrollToItem(targetIdx)
                } else {
                    lazyListState.scrollToItem(0, 0)
                }
                onLastTappedItemUriChange(null)
            } else {
                lazyListState.scrollToItem(0, 0)
            }
        }
    }

    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            fileManager.prewarmFonts(items)
        }
    }

    // Intercept Back Gesture cleanly (only active when on Home tab)
    BackHandler(enabled = isActive) {
        if (isFabExpanded) {
            onFabExpandedChange(false)
        } else if (isSelectionMode) {
            isSelectionMode = false
            selectedUris.clear()
        } else if (isSearchFocused || searchQuery.isNotEmpty()) {
            focusManager.clearFocus()
            searchQuery = ""
        } else if (breadcrumbs.size > 1) {
            folderScrollPositions[currentFolder.absolutePath] = Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset)
            onLastTappedItemUriChange(Uri.fromFile(currentFolder))
            breadcrumbs.removeLastOrNull()
            val parent = breadcrumbs.lastOrNull()?.file ?: fileManager.getRootFontsDirectory()
            onCurrentFolderChange(parent)
        } else {
            showExitConfirmDialog = true
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                // Header Bar (Equal 10dp gap between status bar and header, and header and searchbar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedUris.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit selection")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedUris.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (selectedUris.size == items.size) {
                                selectedUris.clear()
                            } else {
                                selectedUris.clear()
                                selectedUris.addAll(items.map { it.uri })
                            }
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                    } else {
                        if (breadcrumbs.size > 1) {
                            IconButton(onClick = {
                                folderScrollPositions[currentFolder.absolutePath] = Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset)
                                onLastTappedItemUriChange(Uri.fromFile(currentFolder))
                                breadcrumbs.removeLastOrNull()
                                val parent = breadcrumbs.lastOrNull()?.file ?: fileManager.getRootFontsDirectory()
                                onCurrentFolderChange(parent)
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "xFont Manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = XFontTypography.title800,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        // Note: Refresh button removed on top per user request (swipe-down used instead)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Sleek Material 3 Search Bar (Positioned ABOVE Breadcrumb row)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = XFontTypography.body400,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(searchFocusRequester)
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    placeholder = {
                        Text(
                            text = "Search fonts & subfolders...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = XFontTypography.body400,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty() || isSearchFocused) {
                            IconButton(onClick = {
                                searchQuery = ""
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // 2. Horizontally scrollable Breadcrumb Bar (Below Search Bar)
                if (searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        state = breadcrumbsListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(breadcrumbs) { crumb ->
                            val isCurrent = crumb.file?.absolutePath == currentFolder.absolutePath
                            FilterChip(
                                modifier = Modifier.pressScale(0.95f),
                                selected = isCurrent,
                                onClick = {
                                    val idx = breadcrumbs.indexOf(crumb)
                                    if (idx != -1 && idx < breadcrumbs.size - 1) {
                                        folderScrollPositions[currentFolder.absolutePath] = Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset)
                                        val nextCrumb = breadcrumbs.getOrNull(idx + 1)
                                        if (nextCrumb?.uri != null) {
                                            onLastTappedItemUriChange(nextCrumb.uri)
                                        }
                                        while (breadcrumbs.size > idx + 1) {
                                            breadcrumbs.removeLast()
                                        }
                                        crumb.file?.let { onCurrentFolderChange(it) }
                                    }
                                },
                                label = {
                                    Text(
                                        text = crumb.title,
                                        fontFamily = XFontTypography.body400,
                                        fontWeight = FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (crumb.title == "Fonts") {
                                    { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = MaterialTheme.shapes.small
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {}
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { refreshCurrentDirectory(forceRefresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                if (isFabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onFabExpandedChange(false) }
                )
            }
            if (items.isEmpty() && searchQuery.isBlank() && !isLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusManager.clearFocus()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "This folder is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap + to add fonts, folders, or copy files to Android/media/com.xfont.app/fonts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else if (items.isEmpty() && searchQuery.isNotBlank() && !isLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusManager.clearFocus()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No fonts found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Searched across all subfolders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.uri.toString() }) { item ->
                        val isSelected = item.uri in selectedUris

                        val fontKey = remember(item.uri) { item.file?.absolutePath ?: item.uri.toString() }
                        var liveFontFamily by remember(fontKey) {
                            mutableStateOf(if (item.isDirectory) FontFamily.Default else fileManager.getCachedFontFamily(fontKey))
                        }

                        LaunchedEffect(fontKey, item.isDirectory) {
                            if (!item.isDirectory && liveFontFamily == null) {
                                val loaded = fileManager.loadFontFamilyAsync(item)
                                if (loaded != null) {
                                    liveFontFamily = loaded
                                }
                            }
                        }

                        val expressiveShape = ExpressiveIconShapes.rememberRandomShape()
                        val lowerName = remember(item.name) { item.name.lowercase() }
                        val isFont = remember(lowerName) {
                            lowerName.endsWith(".ttf") || lowerName.endsWith(".otf") || lowerName.endsWith(".ttc") || lowerName.endsWith(".woff") || lowerName.endsWith(".woff2")
                        }

                        Card(
                            modifier = Modifier
                                .animateItem()
                                .pressScale(0.97f)
                                .fillMaxWidth()
                                .height(72.dp)
                                .combinedClickable(
                                    onClick = {
                                        focusManager.clearFocus()
                                        if (isSelectionMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (isSelected) selectedUris.remove(item.uri)
                                            else selectedUris.add(item.uri)
                                            if (selectedUris.isEmpty()) isSelectionMode = false
                                        } else {
                                            folderScrollPositions[currentFolder.absolutePath] = Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset)
                                            onLastTappedItemUriChange(item.uri)
                                            if (item.isDirectory && item.file != null) {
                                                breadcrumbs.add(
                                                    BreadcrumbItem(title = item.name, uri = item.uri, file = item.file)
                                                )
                                                onCurrentFolderChange(item.file)
                                                searchQuery = ""
                                            } else {
                                                onFontSelected(item)
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!isSelectionMode) {
                                            isSelectionMode = true
                                            selectedUris.add(item.uri)
                                        }
                                    }
                                ),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedUris.add(item.uri)
                                            else selectedUris.remove(item.uri)
                                            if (selectedUris.isEmpty()) isSelectionMode = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                val shapeBgColor = if (item.isDirectory) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else if (isFont) {
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(expressiveShape)
                                        .background(shapeBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isDirectory) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else if (isFont) {
                                        Text(
                                            text = "T",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.SansSerif
                                            ),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = if (item.isDirectory) FontFamily.Default else (liveFontFamily ?: FontFamily.Default),
                                        fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (item.subfolderPath.isNotBlank()) {
                                        Text(
                                            text = item.subfolderPath,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else if (item.isDirectory) {
                                        Text(
                                            text = "Folder",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    } else {
                                        val kb = item.sizeBytes / 1024
                                        val format = if (item.name.lowercase().endsWith(".otf")) "OTF" else "TTF"
                                        Text(
                                            text = "$format • ${kb} KB",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tapping empty space below items dismisses search focus
                    item(key = "__empty_spacer_bottom__") {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    focusManager.clearFocus()
                                }
                        )
                    }
                }
            }
        }

            // Selection Contextual Action Bar (Floating Pill)
            AnimatedVisibility(
                visible = isSelectionMode && selectedUris.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 86.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                    tonalElevation = 6.dp,
                    shape = CircleShape,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val selectedItems = items.filter { it.uri in selectedUris }
                                clipboardState = ClipboardState(ClipboardOp.CUT, selectedItems)
                                isSelectionMode = false
                                selectedUris.clear()
                            },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val selectedItems = items.filter { it.uri in selectedUris }
                                clipboardState = ClipboardState(ClipboardOp.COPY, selectedItems)
                                isSelectionMode = false
                                selectedUris.clear()
                            },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }

                        if (selectedUris.size == 1) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val item = items.find { it.uri in selectedUris }
                                    if (item != null) {
                                        renameTarget = item
                                        renameNewName = if (!item.isDirectory && item.name.contains(".")) {
                                            item.name.substringBeforeLast(".")
                                        } else {
                                            item.name
                                        }
                                        showRenameDialog = true
                                    }
                                },
                                modifier = Modifier.pressScale()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename")
                            }
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDeleteConfirmDialog = true
                            },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Clipboard Action Bar (Floating Pill: Paste / New Folder / Cancel)
            AnimatedVisibility(
                visible = clipboardState != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 86.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 8.dp,
                    tonalElevation = 6.dp,
                    shape = CircleShape,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val count = clipboardState?.items?.size ?: 0
                        val opText = if (clipboardState?.op == ClipboardOp.CUT) "Move" else "Copy"
                        Text(
                            text = "$count item(s) ($opText)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        // Create New Folder button without losing copied/cut state
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                newFolderName = ""
                                showNewFolderDialog = true
                            },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val state = clipboardState
                                if (state != null) {
                                    scope.launch {
                                        fileManager.copyOrMove(
                                            items = state.items,
                                            targetDir = currentFolder,
                                            isCut = state.op == ClipboardOp.CUT
                                        )
                                        clipboardState = null
                                        refreshCurrentDirectory()
                                    }
                                }
                            },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                clipboardState = null
                            },
                            modifier = Modifier.pressScale()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                }
            }
        }
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            shape = RoundedCornerShape(28.dp),
            onDismissRequest = {
                showNewFolderDialog = false
                newFolderName = ""
            },
            title = {
                Text(
                    text = "Create New Folder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isNotEmpty()) {
                            scope.launch {
                                fileManager.createFolder(currentFolder, name)
                                showNewFolderDialog = false
                                newFolderName = ""
                                refreshCurrentDirectory()
                            }
                        }
                    }
                ) {
                    Text("Create", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNewFolderDialog = false
                    newFolderName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog (Locked file extensions so fonts never disappear)
    if (showRenameDialog && renameTarget != null) {
        val target = renameTarget!!
        val originalExt = if (!target.isDirectory && target.name.contains(".")) {
            "." + target.name.substringAfterLast(".")
        } else ""

        AlertDialog(
            shape = RoundedCornerShape(28.dp),
            onDismissRequest = {
                showRenameDialog = false
                renameTarget = null
            },
            title = {
                Text(
                    text = if (target.isDirectory) "Rename Folder" else "Rename Font",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = renameNewName,
                        onValueChange = { renameNewName = it },
                        label = { Text("Name") },
                        trailingIcon = if (originalExt.isNotEmpty()) {
                            {
                                Text(
                                    text = originalExt,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (originalExt.isNotEmpty()) {
                        Text(
                            text = "Extension ($originalExt) is protected to prevent the font from disappearing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val baseName = renameNewName.trim()
                        if (baseName.isNotEmpty()) {
                            val finalName = if (target.isDirectory || originalExt.isEmpty()) {
                                baseName
                            } else if (baseName.endsWith(originalExt, ignoreCase = true)) {
                                baseName
                            } else {
                                "$baseName$originalExt"
                            }
                            scope.launch {
                                fileManager.renameItem(target, finalName)
                                showRenameDialog = false
                                renameTarget = null
                                isSelectionMode = false
                                selectedUris.clear()
                                refreshCurrentDirectory()
                            }
                        }
                    }
                ) {
                    Text("Rename", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    renameTarget = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            shape = RoundedCornerShape(28.dp),
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Are you sure you want to delete ${selectedUris.size} item(s)? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val targets = items.filter { it.uri in selectedUris }
                            for (target in targets) {
                                fileManager.deleteItem(target)
                            }
                            showDeleteConfirmDialog = false
                            isSelectionMode = false
                            selectedUris.clear()
                            refreshCurrentDirectory()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            shape = RoundedCornerShape(28.dp),
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "Exit xFont",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = { Text("Do you want to exit xFont?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Exit", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
