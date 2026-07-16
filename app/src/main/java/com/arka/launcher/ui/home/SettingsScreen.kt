package com.arka.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.ui.theme.THEME_KEYS

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onSetDefaultLauncher: () -> Unit
) {
    val theme = MaterialTheme.colorScheme
    val currentThemeKey by viewModel.themeKey.collectAsState()
    val isPrabhaMode by viewModel.isPrabhaMode.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val iconSize by viewModel.iconSize.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .systemBarsPadding()
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setLauncherState(LauncherState.HOME) }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = theme.onBackground)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Arka Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            SettingsSection(title = "Appearance", icon = Icons.Rounded.Palette) {
                Text("Select Theme", fontSize = 12.sp, color = theme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 3
                ) {
                    THEME_KEYS.forEach { key ->
                        val isSelected = key == currentThemeKey
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) theme.primary else theme.surface)
                                .border(1.dp, if (isSelected) theme.primary else theme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { viewModel.setTheme(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key.replaceFirstChar { it.uppercase() },
                                color = if (isSelected) theme.onPrimary else theme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Icon Style", fontSize = 12.sp, color = theme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("natural", "minimal", "ultraminimal").forEach { style ->
                        val isSelected = style == iconStyle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) theme.primary else theme.surface)
                                .border(1.dp, if (isSelected) theme.primary else theme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { viewModel.setIconStyle(style) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style.replaceFirstChar { it.uppercase() },
                                color = if (isSelected) theme.onPrimary else theme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Icon Size", fontSize = 12.sp, color = theme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("small", "normal", "large").forEach { size ->
                        val isSelected = size == iconSize
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) theme.primary else theme.surface)
                                .border(1.dp, if (isSelected) theme.primary else theme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { viewModel.setIconSize(size) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = size.replaceFirstChar { it.uppercase() },
                                color = if (isSelected) theme.onPrimary else theme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Prabha Mode Section
            SettingsSection(title = "Focus", icon = Icons.Rounded.Settings) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prabha Focus Mode", fontWeight = FontWeight.SemiBold, color = theme.onSurface, fontSize = 14.sp)
                        Text("Minimize distractions on home screen", fontSize = 11.sp, color = theme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                    Switch(
                        checked = isPrabhaMode,
                        onCheckedChange = { viewModel.togglePrabhaMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.onPrimary,
                            checkedTrackColor = theme.primary,
                            uncheckedThumbColor = theme.outline,
                            uncheckedTrackColor = theme.surfaceVariant
                        )
                    )
                }
            }

            // System Section
            SettingsSection(title = "System", icon = Icons.Rounded.Settings) {
                Button(
                    onClick = onSetDefaultLauncher,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.secondaryContainer, contentColor = theme.onSecondaryContainer)
                ) {
                    Text("Set Arka as Default Launcher", fontSize = 13.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.updateUsageStatsPermission() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant, contentColor = theme.onSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline.copy(alpha = 0.5f))
                ) {
                    Text("Refresh Stats Permission", fontSize = 13.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    val theme = MaterialTheme.colorScheme
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = theme.primary, letterSpacing = 0.5.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}
