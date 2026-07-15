package com.arka.launcher.ui.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.ui.components.AppIcon
import com.arka.launcher.ui.home.HomeViewModel
import com.arka.launcher.ui.home.LauncherState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerScreen(viewModel: HomeViewModel) {
    val groupedApps by viewModel.groupedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val theme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .systemBarsPadding()
            .padding(top = 16.dp)
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            groupedApps.forEach { (letter, apps) ->
                stickyHeader {
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
                        Text(
                            text = app.appName,
                            color = theme.onBackground,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
