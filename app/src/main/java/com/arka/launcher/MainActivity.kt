package com.arka.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arka.launcher.ui.components.AppContextMenu
import com.arka.launcher.ui.drawer.DrawerScreen
import com.arka.launcher.ui.home.HomeScreen
import com.arka.launcher.ui.home.HomeViewModel
import com.arka.launcher.ui.home.LauncherState
import com.arka.launcher.ui.home.SettingsScreen
import com.arka.launcher.ui.theme.ArkaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    private fun openDefaultLauncherSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                requestRoleLauncher.launch(intent)
            } else {
                openLegacyDefaultLauncherSettings()
            }
        } else {
            openLegacyDefaultLauncherSettings()
        }
    }

    private fun openLegacyDefaultLauncherSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HomeViewModel = hiltViewModel()
            val themeKey by viewModel.themeKey.collectAsState()
            
            ArkaTheme(themeKey = themeKey) {
                val launcherState by viewModel.launcherState.collectAsState()
                val selectedAppForMenu by viewModel.selectedAppForMenu.collectAsState()
                val dockPackages by viewModel.dockPackages.collectAsState()
                
                val theme = MaterialTheme.colorScheme

                BackHandler(enabled = launcherState != LauncherState.HOME) {
                    viewModel.setLauncherState(LauncherState.HOME)
                }

                Box(modifier = Modifier.fillMaxSize().background(theme.background)) {
                    AnimatedContent(
                        targetState = launcherState,
                        transitionSpec = {
                            if (targetState == LauncherState.DRAWER || targetState == LauncherState.SETTINGS) {
                                (fadeIn(animationSpec = tween(400)) + 
                                 slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } +
                                 scaleIn(initialScale = 0.85f, animationSpec = spring(stiffness = Spring.StiffnessLow)))
                                .togetherWith(
                                 fadeOut(animationSpec = tween(300)) + 
                                 scaleOut(targetScale = 1.1f, animationSpec = tween(300)))
                            } else {
                                (fadeIn(animationSpec = tween(400)) + 
                                 scaleIn(initialScale = 1.1f, animationSpec = spring(stiffness = Spring.StiffnessLow)))
                                .togetherWith(
                                 fadeOut(animationSpec = tween(300)) + 
                                 slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) { it / 2 } +
                                 scaleOut(targetScale = 0.85f, animationSpec = tween(300)))
                            }
                        },
                        label = "launcher_transition"
                    ) { state ->
                        when (state) {
                            LauncherState.HOME -> HomeScreen(viewModel, onSetDefaultLauncher = { openDefaultLauncherSettings() })
                            LauncherState.DRAWER -> DrawerScreen(viewModel)
                            LauncherState.SETTINGS -> SettingsScreen(viewModel, onSetDefaultLauncher = { openDefaultLauncherSettings() })
                        }
                    }
                }

                if (selectedAppForMenu != null) {
                    val app = selectedAppForMenu!!
                    val isPinned = dockPackages.any { it.trim().equals(app.packageName.trim(), ignoreCase = true) }
                    val isQuickAccess = viewModel.isAppQuickAccess(app.packageName)

                    AppContextMenu(
                        app = app,
                        isPinned = isPinned,
                        isQuickAccess = isQuickAccess,
                        onDismiss = { viewModel.showAppMenu(null) },
                        onPinToggle = {
                            if (isPinned) {
                                viewModel.unpinFromDock(app.packageName)
                            } else {
                                viewModel.pinToDock(app.packageName)
                            }
                        },
                        onQuickAccessToggle = {
                            if (isQuickAccess) {
                                viewModel.removeFromQuickAccess(app.packageName)
                            } else {
                                viewModel.addToQuickAccess(app.packageName)
                            }
                        }
                    )
                }
            }
        }
    }
}
