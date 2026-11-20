package com.xfont.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xfont.app.manager.FontFileManager
import com.xfont.app.model.BreadcrumbItem
import com.xfont.app.model.FontItem
import com.xfont.app.model.ThemeConfig
import com.xfont.app.model.ThemeMode
import com.xfont.app.ui.apply.ApplyFontScreen
import com.xfont.app.ui.home.HomeScreen
import com.xfont.app.ui.preview.FontPreviewScreen
import com.xfont.app.ui.settings.SettingsScreen
import com.xfont.app.ui.theme.XFontTheme
import com.xfont.app.ui.theme.XFontTypography
import java.io.File

sealed class AppDestination {
    data object MainTabs : AppDestination()
    data class Preview(val fontItem: FontItem) : AppDestination()
    data class Apply(val fontItem: FontItem, val axisValues: Map<String, Float>) : AppDestination()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        XFontTypography.init(this)

        val fileManager = FontFileManager(this)
        val prefs = getSharedPreferences("xfont_theme_prefs", MODE_PRIVATE)

        setContent {
            var themeMode by remember {
                val modeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
                mutableStateOf(ThemeMode.valueOf(modeStr))
            }
            var dynamicColor by remember {
                mutableStateOf(prefs.getBoolean("dynamic_color", true))
            }
            var accentIndex by remember {
                mutableIntStateOf(prefs.getInt("accent_index", 0))
            }
            var isAmoled by remember {
                mutableStateOf(prefs.getBoolean("is_amoled", false))
            }

            val themeConfig = remember(themeMode, dynamicColor, accentIndex, isAmoled) {
                ThemeConfig(
                    mode = themeMode,
                    dynamicColor = dynamicColor,
                    accentIndex = accentIndex,
                    isAmoled = isAmoled
                )
            }

            fun updateTheme(newConfig: ThemeConfig) {
                themeMode = newConfig.mode
                dynamicColor = newConfig.dynamicColor
                accentIndex = newConfig.accentIndex
                isAmoled = newConfig.isAmoled
                prefs.edit()
                    .putString("theme_mode", newConfig.mode.name)
                    .putBoolean("dynamic_color", newConfig.dynamicColor)
                    .putInt("accent_index", newConfig.accentIndex)
                    .putBoolean("is_amoled", newConfig.isAmoled)
                    .apply()
            }

            // Hoist file manager folder, breadcrumbs, and list scroll state across navigation
            val rootFontsDir = remember { fileManager.getRootFontsDirectory() }
            var currentFolder by remember { mutableStateOf(rootFontsDir) }
            val breadcrumbs = remember {
                mutableStateListOf(
                    BreadcrumbItem(title = "Fonts", uri = Uri.fromFile(rootFontsDir), file = rootFontsDir)
                )
            }
            val homeListState = rememberLazyListState()
            var lastTappedItemUri by remember { mutableStateOf<Uri?>(null) }
            val folderScrollPositions = remember { mutableMapOf<String, Pair<Int, Int>>() }
            var searchFocusTrigger by remember { mutableIntStateOf(0) }
            var isFabExpanded by remember { mutableStateOf(false) }
            var triggerAddFont by remember { mutableIntStateOf(0) }
            var triggerAddFolder by remember { mutableIntStateOf(0) }
            var triggerCreateFolder by remember { mutableIntStateOf(0) }

            XFontTheme(config = themeConfig) {
                val currentDensity = LocalDensity.current
                val scaledDensity = remember(currentDensity) {
                    Density(
                        density = currentDensity.density * 0.95f,
                        fontScale = currentDensity.fontScale * 0.95f
                    )
                }

                CompositionLocalProvider(LocalDensity provides scaledDensity) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.MainTabs) }
                        var selectedTab by remember { mutableIntStateOf(0) }
                        val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
                        val pagerScope = rememberCoroutineScope()

                        // Synchronize selected tab with horizontal pager swipe position
                        LaunchedEffect(pagerState.currentPage) {
                            selectedTab = pagerState.currentPage
                        }

                        BackHandler(enabled = currentDestination is AppDestination.MainTabs && selectedTab == 1) {
                            pagerScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }

                        BackHandler(enabled = currentDestination !is AppDestination.MainTabs) {
                            when (currentDestination) {
                                is AppDestination.Apply -> {
                                    val current = currentDestination as AppDestination.Apply
                                    currentDestination = AppDestination.Preview(current.fontItem)
                                }
                                is AppDestination.Preview -> {
                                    currentDestination = AppDestination.MainTabs
                                }
                                AppDestination.MainTabs -> {}
                            }
                        }

                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = {
                                if (targetState is AppDestination.MainTabs) {
                                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                                            (slideOutHorizontally { it } + fadeOut())
                                } else {
                                    (slideInHorizontally { it } + fadeIn()) togetherWith
                                            (slideOutHorizontally { -it } + fadeOut())
                                }
                            },
                            label = "NavigationTransition"
                        ) { dest ->
                            when (dest) {
                                is AppDestination.MainTabs -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        // Seamless interactive swipe gesture between Home and Settings
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize(),
                                            beyondViewportPageCount = 1
                                        ) { page ->
                                            when (page) {
                                                0 -> {
                                                    HomeScreen(
                                                        fileManager = fileManager,
                                                        currentFolder = currentFolder,
                                                        onCurrentFolderChange = { currentFolder = it },
                                                        breadcrumbs = breadcrumbs,
                                                        lazyListState = homeListState,
                                                        lastTappedItemUri = lastTappedItemUri,
                                                        onLastTappedItemUriChange = { lastTappedItemUri = it },
                                                        folderScrollPositions = folderScrollPositions,
                                                        searchFocusTrigger = searchFocusTrigger,
                                                        isFabExpanded = isFabExpanded,
                                                        onFabExpandedChange = { isFabExpanded = it },
                                                        isActive = (selectedTab == 0),
                                                        triggerAddFont = triggerAddFont,
                                                        triggerAddFolder = triggerAddFolder,
                                                        triggerCreateFolder = triggerCreateFolder,
                                                        onResetTriggerAddFont = { triggerAddFont = 0 },
                                                        onResetTriggerAddFolder = { triggerAddFolder = 0 },
                                                        onResetTriggerCreateFolder = { triggerCreateFolder = 0 },
                                                        onFontSelected = { fontItem ->
                                                            folderScrollPositions[currentFolder.absolutePath] = Pair(homeListState.firstVisibleItemIndex, homeListState.firstVisibleItemScrollOffset)
                                                            lastTappedItemUri = fontItem.uri
                                                            currentDestination = AppDestination.Preview(fontItem)
                                                        }
                                                    )
                                                }
                                                1 -> {
                                                    SettingsScreen(
                                                        themeConfig = themeConfig,
                                                        onThemeConfigChange = { updateTheme(it) },
                                                        fileManager = fileManager
                                                    )
                                                }
                                            }
                                        }

                                        // Modern Centered Floating Navbar with Left Capsule Tabs and Right Squircle FAB
                                        FloatingNavBar(
                                            selectedTab = selectedTab,
                                            onTabSelected = { newTab ->
                                                if (selectedTab != newTab) {
                                                    isFabExpanded = false
                                                    pagerScope.launch {
                                                        pagerState.animateScrollToPage(newTab)
                                                    }
                                                }
                                            },
                                            isFabExpanded = isFabExpanded,
                                            onFabToggle = { isFabExpanded = !isFabExpanded },
                                            onSearchClick = {
                                                isFabExpanded = false
                                                pagerScope.launch {
                                                    pagerState.animateScrollToPage(0)
                                                }
                                                searchFocusTrigger++
                                            },
                                            onAddFont = { triggerAddFont++ },
                                            onAddFolder = { triggerAddFolder++ },
                                            onCreateFolder = { triggerCreateFolder++ },
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .navigationBarsPadding()
                                                .padding(horizontal = 24.dp, vertical = 14.dp)
                                        )
                                    }
                                }
                                is AppDestination.Preview -> {
                                    FontPreviewScreen(
                                        fontItem = dest.fontItem,
                                        fileManager = fileManager,
                                        onBack = { currentDestination = AppDestination.MainTabs },
                                        onApplyFont = { font, axes ->
                                            currentDestination = AppDestination.Apply(font, axes)
                                        }
                                    )
                                }
                                is AppDestination.Apply -> {
                                    ApplyFontScreen(
                                        fontItem = dest.fontItem,
                                        axisValues = dest.axisValues,
                                        fileManager = fileManager,
                                        onBack = { currentDestination = AppDestination.Preview(dest.fontItem) },
                                        onFinish = { currentDestination = AppDestination.MainTabs }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    onSearchClick: () -> Unit,
    onAddFont: () -> Unit,
    onAddFolder: () -> Unit,
    onCreateFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Speed dial popup options aligned directly above the centered FAB
        AnimatedVisibility(
            visible = selectedTab == 0 && isFabExpanded,
            enter = fadeIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)) +
                    expandVertically(
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                        expandFrom = Alignment.Bottom,
                        clip = false
                    ) +
                    scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.85f),
            exit = fadeOut(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)) +
                   shrinkVertically(
                       animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
                       shrinkTowards = Alignment.Bottom,
                       clip = false
                   ) +
                   scaleOut(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow), targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onFabToggle()
                        onAddFont()
                    },
                    icon = { Icon(Icons.AutoMirrored.Outlined.InsertDriveFile, contentDescription = null) },
                    text = { Text("Add Font", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        onFabToggle()
                        onAddFolder()
                    },
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                    text = { Text("Add Folder", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        onFabToggle()
                        onCreateFolder()
                    },
                    icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                    text = { Text("Create New Folder", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                )
            }
        }

        // Centered cluster: Pill Capsule + 12.dp gap + Squircle FAB
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill Capsule: Home & Settings tabs with morphing pill indicator
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 8.dp,
                tonalElevation = 6.dp,
                modifier = Modifier.height(58.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FloatingNavTabItem(
                        selected = selectedTab == 0,
                        icon = Icons.Outlined.Folder,
                        label = "Home",
                        targetWidth = 108.dp,
                        onClick = { onTabSelected(0) }
                    )
                    FloatingNavTabItem(
                        selected = selectedTab == 1,
                        icon = Icons.Outlined.Settings,
                        label = "Settings",
                        targetWidth = 124.dp,
                        onClick = { onTabSelected(1) }
                    )
                }
            }

            // Right Rounded Square FAB
            val fabRotation by animateFloatAsState(
                targetValue = if (selectedTab == 0 && isFabExpanded) 45f else 0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "fabRotation"
            )

            Surface(
                onClick = {
                    if (selectedTab == 0) {
                        onFabToggle()
                    } else {
                        onSearchClick()
                    }
                },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shadowElevation = 8.dp,
                tonalElevation = 6.dp,
                modifier = Modifier.size(58.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            (scaleIn(spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.7f) + fadeIn(tween(180))) togetherWith
                                    (scaleOut(spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow), targetScale = 0.7f) + fadeOut(tween(130)))
                        },
                        label = "fabIconTransition"
                    ) { tab ->
                        if (tab == 0) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isFabExpanded) "Close add options" else "Add options",
                                modifier = Modifier
                                    .size(26.dp)
                                    .graphicsLayer { rotationZ = fabRotation }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search fonts",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingNavTabItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    targetWidth: Dp,
    onClick: () -> Unit
) {
    val tabWidth by animateDpAsState(
        targetValue = if (selected) targetWidth else 46.dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tabWidth"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    val activeBg = MaterialTheme.colorScheme.surface
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tabContentColor"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(tabWidth)
            .clip(CircleShape)
            .background(
                color = activeBg.copy(alpha = contentAlpha),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            if (contentAlpha > 0.05f) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = XFontTypography.body400,
                    fontWeight = FontWeight.Normal,
                    color = contentColor,
                    maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = contentAlpha }
                )
            }
        }
    }
}
