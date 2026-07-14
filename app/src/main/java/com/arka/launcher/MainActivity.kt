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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.arka.launcher.ui.components.AppContextMenu
import com.arka.launcher.ui.drawer.DrawerScreen
import com.arka.launcher.ui.home.HomeScreen
import com.arka.launcher.ui.home.HomeViewModel
import com.arka.launcher.ui.home.LauncherState
import com.arka.launcher.ui.theme.ArkaTheme
import com.arka.launcher.ui.theme.Bg0
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // No-op, the ViewModel will re-check on next launch or we could trigger a check here
    }

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
        Log.d("ArkaMainActivity", "onCreate called")
        enableEdgeToEdge()
        setContent {
            ArkaTheme {
                val viewModel: HomeViewModel = hiltViewModel()
                val launcherState by viewModel.launcherState.collectAsState()
                val apps by viewModel.apps.collectAsState()
                val selectedAppForMenu by viewModel.selectedAppForMenu.collectAsState()
                val dockPackages by viewModel.dockPackages.collectAsState()
                
                Log.d("ArkaMainActivity", "launcherState: $launcherState, apps count: ${apps.size}")

                BackHandler(enabled = launcherState == LauncherState.DRAWER) {
                    viewModel.setLauncherState(LauncherState.HOME)
                }

                Box(modifier = Modifier.fillMaxSize().background(Bg0)) {
                    AnimatedContent(
                        targetState = launcherState,
                        transitionSpec = {
                            if (targetState == LauncherState.DRAWER) {
                                slideInVertically { it } + fadeIn() togetherWith
                                        slideOutVertically { -it } + fadeOut()
                            } else {
                                slideInVertically { -it } + fadeIn() togetherWith
                                        slideOutVertically { it } + fadeOut()
                            }
                        },
                        label = "launcher_transition"
                    ) { state ->
                        when (state) {
                            LauncherState.HOME -> HomeScreen(viewModel, onSetDefaultLauncher = { openDefaultLauncherSettings() })
                            LauncherState.DRAWER -> DrawerScreen(viewModel)
                        }
                    }
                }

                if (selectedAppForMenu != null) {
                    val app = selectedAppForMenu!!
                    val isPinned = dockPackages.contains(app.packageName)
                    AppContextMenu(
                        app = app,
                        isPinned = isPinned,
                        onDismiss = { viewModel.showAppMenu(null) },
                        onPinToggle = {
                            if (isPinned) viewModel.unpinFromDock(app.packageName)
                            else viewModel.pinToDock(app.packageName)
                        }
                    )
                }
            }
        }
    }
}
