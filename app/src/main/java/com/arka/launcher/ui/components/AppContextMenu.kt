package com.arka.launcher.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onDismiss: () -> Unit,
    onPinToggle: () -> Unit
) {
    if (app == null) return
    
    val context = LocalContext.current
    val theme = MaterialTheme.colorScheme

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
                AppIcon(packageName = app.packageName, size = 34.dp)
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
                onClick = {
                    onDismiss()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                    context.startActivity(intent)
                }
            )
            
            Divider(color = theme.outline, thickness = 0.5.dp)

            MenuOption(
                text = if (isPinned) "Remove from dock" else "Pin to dock",
                onClick = {
                    onDismiss()
                    onPinToggle()
                }
            )

            Divider(color = theme.outline, thickness = 0.5.dp)

            MenuOption(
                text = "Uninstall",
                color = theme.tertiary, // Ember
                onClick = {
                    onDismiss()
                    val intent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                    }
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun MenuOption(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 13.sp
        )
    }
}
