package com.arka.launcher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.data.local.InstalledApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: InstalledApp?,
    isPinned: Boolean,
    isQuickAccess: Boolean,
    onDismiss: () -> Unit,
    onPinToggle: () -> Unit,
    onQuickAccessToggle: () -> Unit
) {
    if (app == null) return
    
    val context = LocalContext.current
    val theme = MaterialTheme.colorScheme

    LaunchedEffect(app.packageName, isPinned, isQuickAccess) {
        Log.d("AppContextMenu", "Showing menu for ${app.packageName}, isPinned: $isPinned, isQuickAccess: $isQuickAccess")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        contentColor = theme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = theme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppIcon(
                    packageName = app.packageName, 
                    size = 34.dp,
                    contentDescription = "${app.appName} icon"
                )
                Text(
                    text = app.appName,
                    color = theme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Options
            MenuOption(
                text = "App info",
                icon = Icons.Rounded.Info,
                onClick = {
                    onDismiss()
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${app.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("AppContextMenu", "Error opening app info", e)
                    }
                }
            )
            
            MenuOption(
                text = if (isPinned) "Remove from dock" else "Pin to dock",
                icon = Icons.Rounded.PushPin,
                onClick = {
                    Log.d("AppContextMenu", "Toggle pin clicked for ${app.packageName}, current isPinned: $isPinned")
                    onDismiss()
                    onPinToggle()
                }
            )

            MenuOption(
                text = if (isQuickAccess) "Remove from Quick Access" else "Add to Quick Access",
                icon = Icons.Rounded.Star,
                onClick = {
                    onDismiss()
                    onQuickAccessToggle()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = theme.outline.copy(alpha = 0.3f), 
                thickness = 0.5.dp
            )

            MenuOption(
                text = "Uninstall",
                icon = Icons.Rounded.Delete,
                color = theme.error,
                onClick = {
                    onDismiss()
                    try {
                        val intent = Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:${app.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("AppContextMenu", "Error starting uninstall", e)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun MenuOption(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
