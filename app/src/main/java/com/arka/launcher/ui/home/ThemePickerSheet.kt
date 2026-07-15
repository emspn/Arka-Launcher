package com.arka.launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arka.launcher.ui.theme.ArkaThemes
import com.arka.launcher.ui.theme.THEME_KEYS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(
    currentThemeKey: String,
    onThemeSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        contentColor = theme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = theme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Kala Theme",
                color = theme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 14.dp),
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                THEME_KEYS.forEach { key ->
                    val themeColors = ArkaThemes[key]!!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onThemeSelect(key) }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        0f to themeColors.bg1,
                                        1f to themeColors.copper
                                    )
                                )
                                .then(
                                    if (key == currentThemeKey) {
                                        Modifier.border(2.dp, theme.primary, CircleShape)
                                    } else {
                                        Modifier.border(1.dp, theme.outline, CircleShape)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = themeColors.name,
                            color = theme.onSurfaceVariant,
                            fontSize = 9.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}
