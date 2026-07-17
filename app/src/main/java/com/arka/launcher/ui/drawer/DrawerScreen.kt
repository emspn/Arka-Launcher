package com.arka.launcher.ui.drawer

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.ui.components.AppIcon
import com.arka.launcher.ui.home.HomeViewModel
import com.arka.launcher.ui.home.LauncherState
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerScreen(viewModel: HomeViewModel) {
    val groupedApps by viewModel.groupedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val iconSize by viewModel.iconSize.collectAsState()
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Small delay to ensure transition animation starts before keyboard pops up
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
    }

    // High-performance index mapping
    val sectionIndices = remember(groupedApps) {
        val mapping = mutableMapOf<Char, Int>()
        var currentIndex = 0
        groupedApps.keys.forEach { char ->
            if (char != null) {
                mapping[char.uppercaseChar()] = currentIndex
                currentIndex += (groupedApps[char]?.size ?: 0) + 1
            }
        }
        mapping
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 25f) viewModel.setLauncherState(LauncherState.HOME)
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Arka", color = theme.secondary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                TextButton(onClick = { viewModel.setLauncherState(LauncherState.HOME) }) {
                    Text("Close", color = theme.onBackground, fontSize = 11.sp)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search Arka", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(14.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = theme.surface,
                    unfocusedContainerColor = theme.surface,
                    focusedBorderColor = theme.outline,
                    unfocusedBorderColor = theme.outline,
                    focusedTextColor = theme.onSurface,
                    unfocusedTextColor = theme.onSurface
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        val firstApp = groupedApps.values.firstOrNull()?.firstOrNull()
                        if (firstApp != null) {
                            val intent = context.packageManager.getLaunchIntentForPackage(firstApp.packageName)
                            intent?.let { 
                                it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(it)
                                viewModel.setLauncherState(LauncherState.HOME)
                            }
                        }
                        keyboardController?.hide()
                    }
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 44.dp, top = 8.dp, bottom = 8.dp)
            ) {
                groupedApps.forEach { (letter, apps) ->
                    stickyHeader(key = "header_$letter") {
                        Text(
                            text = letter.toString(),
                            color = theme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().background(theme.background).padding(vertical = 8.dp)
                        )
                    }
                    items(apps ?: emptyList(), key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        intent?.let { context.startActivity(it) }
                                    },
                                    onLongClick = { viewModel.showAppMenu(app) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(packageName = app.packageName, size = 30.dp, style = iconStyle, sizeFactor = iconSize)
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            val name = app.appName
                            if (searchQuery.isNotEmpty() && name.contains(searchQuery, ignoreCase = true)) {
                                val startIndex = name.lowercase().indexOf(searchQuery.lowercase())
                                val endIndex = startIndex + searchQuery.length
                                if (startIndex >= 0) {
                                    val annotatedString = androidx.compose.ui.text.AnnotatedString.Builder().apply {
                                        append(name.substring(0, startIndex))
                                        pushStyle(androidx.compose.ui.text.SpanStyle(color = theme.primary, fontWeight = FontWeight.Bold))
                                        append(name.substring(startIndex, endIndex))
                                        pop()
                                        append(name.substring(endIndex))
                                    }.toAnnotatedString()
                                    Text(text = annotatedString, color = theme.onBackground, fontSize = 13.sp)
                                } else Text(text = name, color = theme.onBackground, fontSize = 13.sp)
                            } else Text(text = name, color = theme.onBackground, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Optimized Alphabet Sidebar
        val alphabet = remember { ('A'..'Z').toList() + '#' }
        var alphabetHeight by remember { mutableIntStateOf(0) }
        var lastTargetChar by remember { mutableStateOf<Char?>(null) }
        var scrollJob by remember { mutableStateOf<Job?>(null) }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .fillMaxHeight()
                .onGloballyPositioned { alphabetHeight = it.size.height }
                .pointerInput(sectionIndices) {
                    coroutineScope {
                        detectDragGestures(
                            onDragStart = { lastTargetChar = null },
                            onDrag = { change, _ ->
                                val y = change.position.y
                                val fraction = (y / alphabetHeight).coerceIn(0f, 1f)
                                val index = (fraction * (alphabet.size - 1)).toInt()
                                val char = alphabet[index]
                                
                                if (char != lastTargetChar) {
                                    lastTargetChar = char
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                    val targetIndex = if (char == '#') {
                                        sectionIndices['#'] ?: sectionIndices.values.lastOrNull() ?: 0
                                    } else {
                                        val available = sectionIndices.keys.filter { it != '#' }.sorted()
                                        val targetChar = available.find { it >= char } ?: '#'
                                        sectionIndices[targetChar] ?: 0
                                    }
                                    scrollJob?.cancel()
                                    scrollJob = launch { listState.scrollToItem(targetIndex) }
                                }
                            }
                        )
                    }
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { char ->
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 18.dp)
                        .pointerInput(char) {
                            detectTapGestures {
                                scrollJob?.cancel()
                                scrollJob = scope.launch {
                                    val targetIndex = if (char == '#') {
                                        sectionIndices['#'] ?: sectionIndices.values.lastOrNull() ?: 0
                                    } else {
                                        val available = sectionIndices.keys.filter { it != '#' }.sorted()
                                        val targetChar = available.find { it >= char } ?: '#'
                                        sectionIndices[targetChar] ?: 0
                                    }
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char.toString(), color = theme.secondary.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
