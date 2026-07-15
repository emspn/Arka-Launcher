package com.arka.launcher.ui.home

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arka.launcher.data.local.InstalledApp
import com.arka.launcher.data.repository.AppRepository
import com.arka.launcher.data.repository.DockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LauncherState {
    HOME, DRAWER
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val dockRepository: DockRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _launcherState = MutableStateFlow(LauncherState.HOME)
    val launcherState: StateFlow<LauncherState> = _launcherState

    private val _isPrabhaMode = MutableStateFlow(false)
    val isPrabhaMode: StateFlow<Boolean> = _isPrabhaMode

    private val _selectedAppForMenu = MutableStateFlow<InstalledApp?>(null)
    val selectedAppForMenu: StateFlow<InstalledApp?> = _selectedAppForMenu

    private val _showHomeSettings = MutableStateFlow(false)
    val showHomeSettings: StateFlow<Boolean> = _showHomeSettings

    private val _showThemePicker = MutableStateFlow(false)
    val showThemePicker: StateFlow<Boolean> = _showThemePicker

    private val _showDefaultLauncherPrompt = MutableStateFlow(false)
    val showDefaultLauncherPrompt: StateFlow<Boolean> = _showDefaultLauncherPrompt

    val themeKey = dockRepository.selectedTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "sandstone"
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val apps = repository.allApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dockPackages = dockRepository.dockPackages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dockApps = combine(apps, dockPackages) { appsList, packages ->
        packages.mapNotNull { pkg -> appsList.find { it.packageName == pkg } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredApps = combine(apps, _searchQuery) { appsList, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@combine appsList

        val starts = mutableListOf<InstalledApp>()
        val includes = mutableListOf<InstalledApp>()
        
        for (app in appsList) {
            val name = app.appName.lowercase()
            if (name.startsWith(q)) {
                starts.add(app)
            } else if (name.contains(q)) {
                includes.add(app)
            }
        }
        starts + includes
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val groupedApps = filteredApps.map { appsList ->
        appsList.groupBy { it.appName.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setLauncherState(state: LauncherState) {
        android.util.Log.d("ArkaViewModel", "setLauncherState: $state")
        _launcherState.value = state
        if (state == LauncherState.HOME) {
            _searchQuery.value = ""
        }
    }

    fun togglePrabhaMode() {
        _isPrabhaMode.value = !_isPrabhaMode.value
    }

    fun pinToDock(packageName: String) {
        viewModelScope.launch {
            dockRepository.pinApp(packageName)
        }
    }

    fun unpinFromDock(packageName: String) {
        viewModelScope.launch {
            dockRepository.unpinApp(packageName)
        }
    }

    fun reorderDock(newPackages: List<String>) {
        viewModelScope.launch {
            dockRepository.reorderDock(newPackages)
        }
    }

    fun showAppMenu(app: InstalledApp?) {
        _selectedAppForMenu.value = app
    }

    fun showHomeSettings(show: Boolean) {
        _showHomeSettings.value = show
    }

    fun showThemePicker(show: Boolean) {
        _showThemePicker.value = show
    }

    fun setTheme(key: String) {
        viewModelScope.launch {
            dockRepository.setTheme(key)
        }
    }

    fun cycleTheme() {
        viewModelScope.launch {
            val current = themeKey.value
            val nextIndex = (com.arka.launcher.ui.theme.THEME_KEYS.indexOf(current) + 1) % com.arka.launcher.ui.theme.THEME_KEYS.size
            dockRepository.setTheme(com.arka.launcher.ui.theme.THEME_KEYS[nextIndex])
        }
    }

    fun dismissDefaultLauncherPrompt() {
        _showDefaultLauncherPrompt.value = false
    }

    private fun checkDefaultLauncher() {
        val isDefault = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
        } else {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        }
        _showDefaultLauncherPrompt.value = !isDefault
    }

    init {
        viewModelScope.launch {
            repository.refreshApps()
        }
        checkDefaultLauncher()
    }
}
