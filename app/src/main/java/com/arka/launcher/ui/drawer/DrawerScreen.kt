package com.arka.launcher.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.ui.components.AppIcon
import com.arka.launcher.ui.home.HomeViewModel
import com.arka.launcher.ui.home.LauncherState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerScreen(viewModel: HomeViewModel) {
    val groupedApps by viewModel.groupedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

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
                        if (dragAmount > 25f) { // Swipe down to close
                            viewModel.setLauncherState(LauncherState.HOME)
                        }
                    }
                }
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arka",
                    color = theme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                TextButton(
                    onClick = { viewModel.setLauncherState(LauncherState.HOME) }
                ) {
                    Text("Close", color = theme.onBackground, fontSize = 11.sp)
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search Arka", fontSize = 13.sp) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Search", 
                        modifier = Modifier.size(14.dp)
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = theme.surface,
                    unfocusedContainerColor = theme.surface,
                    focusedBorderColor = theme.outline,
                    unfocusedBorderColor = theme.outline,
                    focusedTextColor = theme.onSurface,
                    unfocusedTextColor = theme.onSurface
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App List
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(theme.background)
                                .padding(vertical = 8.dp)
                        )
                    }
                    items(apps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.showAppMenu(app)
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIcon(
                                packageName = app.packageName, 
                                size = 30.dp,
                                contentDescription = "Launch ${app.appName}"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Highlighting search results
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
                                } else {
                                    Text(text = name, color = theme.onBackground, fontSize = 13.sp)
                                }
                            } else {
                                Text(text = name, color = theme.onBackground, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Alphabet Fast Scroll
        val alphabet = ('A'..'Z').toList() + '#'
        var alphabetHeight by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .fillMaxHeight()
                .onGloballyPositioned { alphabetHeight = it.size.height }
                .pointerInput(groupedApps.keys) {
                    coroutineScope {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                val y = change.position.y
                                val index = ((y / alphabetHeight) * alphabet.size).toInt().coerceIn(0, alphabet.size - 1)
                                val char = alphabet[index]
                                
                                val sections = groupedApps.keys.toList()
                                val sectionIndex = if (char == '#') {
                                    sections.indexOfFirst { it == '#' }
                                } else {
                                    sections.indexOfFirst { it != null && it.uppercaseChar() >= char && it != '#' }
                                }

                                if (sectionIndex != -1) {
                                    var itemIndex = 0
                                    for (i in 0 until sectionIndex) {
                                        val key = sections[i]
                                        itemIndex += (groupedApps[key]?.size ?: 0) + 1
                                    }
                                    // Use scrollToItem for instantaneous feedback during drag
                                    launch { listState.scrollToItem(itemIndex) }
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
                                scope.launch {
                                    val sections = groupedApps.keys.toList()
                                    val sectionIndex = if (char == '#') {
                                        sections.indexOfFirst { it == '#' }
                                    } else {
                                        sections.indexOfFirst { it != null && it.uppercaseChar() >= char && it != '#' }
                                    }

                                    if (sectionIndex != -1) {
                                        var itemIndex = 0
                                        for (i in 0 until sectionIndex) {
                                            val key = sections[i]
                                            itemIndex += (groupedApps[key]?.size ?: 0) + 1
                                        }
                                        listState.animateScrollToItem(itemIndex)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        color = theme.secondary.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
