package com.arka.launcher.ui.home

import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.arka.launcher.R
import com.arka.launcher.data.local.InstalledApp
import com.arka.launcher.ui.components.AppIcon
import com.arka.launcher.ui.components.ArkaWheel
import com.arka.launcher.ui.components.PrabhaScene
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.arka.launcher.ui.components.PrabhaStatsWidget
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSetDefaultLauncher: () -> Unit
) {
    val theme = MaterialTheme.colorScheme
    val apps by viewModel.apps.collectAsState()
    val dockApps by viewModel.dockApps.collectAsState()
    val isPrabhaMode by viewModel.isPrabhaMode.collectAsState()
    val quickAccessApps by viewModel.quickAccessApps.collectAsState()
    val showDefaultLauncherPrompt by viewModel.showDefaultLauncherPrompt.collectAsState()
    val savedPage by viewModel.currentPage.collectAsState()

    val pagerState = rememberPagerState(initialPage = savedPage, pageCount = { 3 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentPage(pagerState.currentPage)
    }

    if (apps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(theme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = theme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Initializing Arka...", color = theme.onBackground)
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isPrabhaMode) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -25f && !isPrabhaMode) {
                        viewModel.setLauncherState(LauncherState.DRAWER)
                    }
                }
            }
    ) {
        KonarkSilhouette(modifier = Modifier.align(Alignment.BottomCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = pagerState.currentPage == 0 || isPrabhaMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp)
                ) {
                    if (!isPrabhaMode) {
                        Text(
                            text = "Arka",
                            color = theme.secondary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TopBarButton(
                            onClick = { viewModel.setLauncherState(LauncherState.SETTINGS) },
                            icon = Icons.Rounded.Settings,
                            contentDescription = "Arka Settings"
                        )
                        if (!isPrabhaMode) {
                            TopBarButton(
                                onClick = { viewModel.cycleTheme() },
                                icon = Icons.Rounded.Palette,
                                contentDescription = "Cycle Theme"
                            )
                            TopBarButton(
                                onClick = { viewModel.togglePrabhaMode() },
                                icon = painterResource(id = R.drawable.ic_feather),
                                contentDescription = "Toggle Prabha Focus Mode",
                                isToggled = isPrabhaMode
                            )
                        }
                    }
                }
            }

            if (showDefaultLauncherPrompt && !isPrabhaMode && pagerState.currentPage == 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    DefaultLauncherBanner(
                        onSetDefault = onSetDefaultLauncher,
                        onDismiss = { viewModel.dismissDefaultLauncherPrompt() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Pager
            Box(modifier = Modifier.weight(1f)) {
                if (isPrabhaMode) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        PrabhaScene()
                        Spacer(modifier = Modifier.height(14.dp))
                        ClockDisplay()
                        Text(text = "Focus Mode Active", color = theme.secondary, fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 14.dp))
                    }
                } else {
                    Column {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            beyondViewportPageCount = 1,
                            flingBehavior = PagerDefaults.flingBehavior(
                                state = pagerState,
                                snapAnimationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
                            )
                        ) { page ->
                            when (page) {
                                1 -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        ArkaWheel()
                                        Spacer(modifier = Modifier.height(14.dp))
                                        ClockDisplay()
                                    }
                                }
                                0 -> WidgetPage(viewModel)
                                2 -> QuickAccessPage(viewModel)
                            }
                        }
                        // Page Dots
                        Row(Modifier.height(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            repeat(3) { iteration ->
                                val color = if (pagerState.currentPage == iteration) theme.primary else theme.outline
                                Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(if (pagerState.currentPage == iteration) 16.dp else 6.dp, 6.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Dock Area
            AnimatedVisibility(
                visible = !isPrabhaMode && pagerState.currentPage == 1,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.outline, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setLauncherState(LauncherState.DRAWER) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = theme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Search Arka", color = theme.onSurfaceVariant, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    DraggableDock(viewModel = viewModel, dockApps = dockApps)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun DraggableDock(viewModel: HomeViewModel, dockApps: List<InstalledApp>) {
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    var currentList by remember(dockApps) { mutableStateOf(dockApps) }
    var draggedPkg by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemPositions = remember { mutableStateMapOf<String, Float>() }

    Row(
        modifier = Modifier.shadow(10.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(theme.surface).border(1.dp, theme.outline, RoundedCornerShape(28.dp)).padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 4) {
            val app = currentList.getOrNull(i)
            if (app != null) {
                key(app.packageName) {
                    val isDragging = draggedPkg == app.packageName
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { itemPositions[app.packageName] = it.positionInParent().x }
                            .zIndex(if (isDragging) 100f else 1f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationX = dragOffset
                                    scaleX = 1.25f
                                    scaleY = 1.25f
                                }
                            }
                            .pointerInput(app.packageName) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    val press = PressInteraction.Press(down.position)
                                    scope.launch { interactionSource.emit(press) }
                                    val timeout = viewConfiguration.longPressTimeoutMillis + 50L
                                    val slop = viewConfiguration.touchSlop
                                    var isLongPress = false
                                    var reorderActive = false
                                    var totalX = 0f
                                    try {
                                        withTimeout(timeout) {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.first()
                                                if (!change.pressed) {
                                                    scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                    intent?.let { context.startActivity(it) }
                                                    change.consume()
                                                    return@withTimeout
                                                }
                                                if ((change.position - down.position).getDistance() > slop) {
                                                    scope.launch { interactionSource.emit(PressInteraction.Cancel(press)) }
                                                    return@withTimeout 
                                                }
                                            }
                                        }
                                    } catch (e: Exception) { isLongPress = true }
                                    if (isLongPress) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.first()
                                            if (!change.pressed) {
                                                scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                                                if (reorderActive) viewModel.reorderDock(currentList.map { it.packageName })
                                                else viewModel.showAppMenu(app)
                                                draggedPkg = null; dragOffset = 0f; change.consume()
                                                break
                                            }
                                            val xDelta = change.positionChange().x
                                            totalX += Math.abs(xDelta)
                                            if (totalX > slop) reorderActive = true
                                            if (reorderActive) {
                                                draggedPkg = app.packageName
                                                dragOffset += xDelta
                                                change.consume()
                                                val currentIdx = currentList.indexOfFirst { it.packageName == app.packageName }
                                                val iconWidthPx = with(density) { 46.dp.toPx() }
                                                val currentX = (itemPositions[app.packageName] ?: 0f) + dragOffset + (iconWidthPx / 2)
                                                for (idx in currentList.indices) {
                                                    if (idx != currentIdx) {
                                                        val otherPkg = currentList[idx].packageName
                                                        val otherCenterX = (itemPositions[otherPkg] ?: 0f) + (iconWidthPx / 2)
                                                        if ((currentIdx < idx && currentX > otherCenterX) || (currentIdx > idx && currentX < otherCenterX)) {
                                                            val newList = currentList.toMutableList()
                                                            java.util.Collections.swap(newList, currentIdx, idx)
                                                            currentList = newList
                                                            val oldPos = itemPositions[app.packageName] ?: 0f
                                                            val newPos = itemPositions[otherPkg] ?: 0f
                                                            dragOffset -= (newPos - oldPos)
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    ) { AppIcon(packageName = app.packageName, interactionSource = interactionSource) }
                }
            } else {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(theme.outline.copy(alpha = 0.15f)).clickable { viewModel.setLauncherState(LauncherState.DRAWER) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = theme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun QuickAccessPage(viewModel: HomeViewModel) {
    val quickAccessApps by viewModel.quickAccessApps.collectAsState()
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    val iconStyle by viewModel.iconStyle.collectAsState()
    val iconSize by viewModel.iconSize.collectAsState()

    var currentList by remember(quickAccessApps) { mutableStateOf(quickAccessApps) }
    var draggedPkg by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val itemPositions = remember { mutableStateMapOf<String, Offset>() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quick Access", color = theme.secondary, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Text("Long press to manage", color = theme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(32.dp))
        
        if (quickAccessApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f).clip(RoundedCornerShape(32.dp)).background(theme.surfaceVariant.copy(alpha = 0.3f)).border(1.dp, theme.outline.copy(alpha = 0.2f), RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, null, tint = theme.outline, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Long press apps in drawer to pin here", color = theme.outline, fontSize = 12.sp)
                }
            }
        } else {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(currentList.size, key = { currentList[it].packageName }) { index ->
                    val app = currentList[index]
                    val isDragging = draggedPkg == app.packageName
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .onGloballyPositioned { itemPositions[app.packageName] = it.positionInParent() }
                            .zIndex(if (isDragging) 100f else 1f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationX = dragOffset.x
                                    translationY = dragOffset.y
                                    scaleX = 1.2f; scaleY = 1.2f
                                }
                            }
                            .pointerInput(app.packageName) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    val press = PressInteraction.Press(down.position)
                                    scope.launch { interactionSource.emit(press) }
                                    val timeout = viewConfiguration.longPressTimeoutMillis + 50L
                                    val slop = viewConfiguration.touchSlop
                                    var isLongPress = false
                                    var reorderActive = false
                                    var totalMoved = Offset.Zero
                                    
                                    try {
                                        withTimeout(timeout) {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.first()
                                                if (!change.pressed) {
                                                    scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                    intent?.let { context.startActivity(it) }
                                                    change.consume()
                                                    return@withTimeout
                                                }
                                                if ((change.position - down.position).getDistance() > slop) {
                                                    scope.launch { interactionSource.emit(PressInteraction.Cancel(press)) }
                                                    return@withTimeout
                                                }
                                            }
                                        }
                                    } catch (e: Exception) { isLongPress = true }
                                    if (isLongPress) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.first()
                                            if (!change.pressed) {
                                                scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                                                if (reorderActive) viewModel.reorderQuickAccess(currentList.map { it.packageName })
                                                else viewModel.showAppMenu(app)
                                                draggedPkg = null; dragOffset = Offset.Zero; change.consume()
                                                break
                                            }
                                            val delta = change.positionChange()
                                            totalMoved += delta
                                            if (totalMoved.getDistance() > slop) reorderActive = true
                                            if (reorderActive && delta != Offset.Zero) {
                                                draggedPkg = app.packageName
                                                dragOffset += delta
                                                change.consume()
                                                val currentIdx = currentList.indexOfFirst { it.packageName == app.packageName }
                                                val itemPos = itemPositions[app.packageName] ?: Offset.Zero
                                                val center = itemPos + dragOffset + Offset(with(density){26.dp.toPx()}, with(density){26.dp.toPx()})
                                                for (i in currentList.indices) {
                                                    if (i == currentIdx) continue
                                                    val otherPkg = currentList[i].packageName
                                                    val otherPos = itemPositions[otherPkg] ?: Offset.Zero
                                                    val otherCenter = otherPos + Offset(with(density){26.dp.toPx()}, with(density){26.dp.toPx()})
                                                    if ((center - otherCenter).getDistance() < with(density){45.dp.toPx()}) {
                                                        val newList = currentList.toMutableList()
                                                        java.util.Collections.swap(newList, currentIdx, i)
                                                        currentList = newList
                                                        dragOffset -= (otherPos - itemPos)
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        AppIcon(packageName = app.packageName, size = 52.dp, interactionSource = interactionSource, style = iconStyle, sizeFactor = iconSize)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(app.appName, color = theme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetPage(viewModel: HomeViewModel) {
    val screenTime by viewModel.screenTime.collectAsState()
    val focusStreak by viewModel.focusStreak.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        WeatherWidget()
        ClockWidget()
        PrabhaStatsWidget(screenTime = screenTime, focusStreak = focusStreak)
        DailyVerseWidget()
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun WeatherWidget() {
    val theme = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = theme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline), shape = RoundedCornerShape(22.dp)) {
        Row(modifier = Modifier.padding(18.dp, 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Today", color = theme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("28°C", color = theme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Clear skies", color = theme.onSurfaceVariant, fontSize = 11.sp)
            }
            Icon(Icons.Rounded.Cloud, null, tint = theme.secondary, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun ClockWidget() {
    val theme = MaterialTheme.colorScheme
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { currentTime = Calendar.getInstance(); delay(1000) } }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = theme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp, 20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            Text(timeFormat.format(currentTime.time), color = theme.onSurface, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(dateFormat.format(currentTime.time).uppercase(), color = theme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
fun DailyVerseWidget() {
    val theme = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = theme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp, 20.dp)) {
            Text("Daily verse", color = theme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\"The sun never says to the earth, you owe me.\"", color = theme.onSurface, fontSize = 15.sp, fontStyle = FontStyle.Italic, lineHeight = 22.sp)
            Text("— Rumi", color = theme.onSurfaceVariant, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun DefaultLauncherBanner(onSetDefault: () -> Unit, onDismiss: () -> Unit) {
    val theme = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = theme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Set Arka as home screen", color = theme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Experience the full theme", color = theme.onSurfaceVariant, fontSize = 12.sp)
            }
            TextButton(onClick = onDismiss) { Text("Dismiss", color = theme.onSurfaceVariant, fontSize = 13.sp) }
            Button(onClick = onSetDefault, colors = ButtonDefaults.buttonColors(theme.primary), shape = RoundedCornerShape(12.dp)) { Text("Set default", color = theme.onPrimary, fontSize = 13.sp) }
        }
    }
}

@Composable
fun ClockDisplay() {
    val theme = MaterialTheme.colorScheme
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) { while (true) { currentTime = Calendar.getInstance(); delay(1000) } }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(timeFormat.format(currentTime.time), color = theme.onBackground, fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Text(dateFormat.format(currentTime.time).uppercase(), color = theme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = 1.5.sp)
    }
}

@Composable
fun KonarkSilhouette(modifier: Modifier = Modifier) {
    val copper = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxWidth().height(130.dp)) {
        val w = size.width; val h = size.height; val opacity = 0.05f
        val path = Path().apply {
            moveTo(w * 0.29f, h); lineTo(w * 0.32f, h * 0.77f); lineTo(w * 0.37f, h * 0.77f); lineTo(w * 0.39f, h * 0.6f); lineTo(w * 0.44f, h * 0.6f); lineTo(w * 0.46f, h * 0.46f); lineTo(w * 0.54f, h * 0.46f); lineTo(w * 0.56f, h * 0.6f); lineTo(w * 0.61f, h * 0.6f); lineTo(w * 0.63f, h * 0.77f); lineTo(w * 0.68f, h * 0.77f); lineTo(w * 0.71f, h); close()
        }
        drawPath(path, copper, opacity, Stroke(1.4.dp.toPx()))
        drawRect(copper, Offset(w * 0.26f, h * 0.97f), androidx.compose.ui.geometry.Size(w * 0.48f, 4.dp.toPx()), opacity, Stroke(1.dp.toPx()))
    }
}

@Composable
fun TopBarButton(onClick: () -> Unit, icon: Any, contentDescription: String?, isToggled: Boolean = false) {
    val theme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring(Spring.DampingRatioLowBouncy), label = "scale")
    IconButton(onClick = onClick, interactionSource = interactionSource, modifier = Modifier.size(48.dp).graphicsLayer { scaleX = scale; scaleY = scale }) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(if (isToggled) theme.primaryContainer else theme.surface.copy(alpha = 0.8f)).border(1.dp, if (isToggled) theme.primary.copy(alpha = 0.5f) else theme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            if (icon is ImageVector) Icon(icon, contentDescription, tint = if (isToggled) theme.primary else theme.onSurface, modifier = Modifier.size(18.dp))
            if (icon is Painter) Icon(icon, contentDescription, tint = if (isToggled) theme.primary else theme.onSurface, modifier = Modifier.size(18.dp))
        }
    }
}
