package com.arka.launcher.ui.home

import android.app.AppOpsManager
import android.app.role.RoleManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arka.launcher.data.local.InstalledApp
import com.arka.launcher.data.repository.AppRepository
import com.arka.launcher.data.repository.DockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class LauncherState {
    HOME, DRAWER, SETTINGS
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val dockRepository: DockRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _launcherState = MutableStateFlow(LauncherState.HOME)
    val launcherState: StateFlow<LauncherState> = _launcherState

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

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

    val quickAccessPackages = dockRepository.quickAccessPackages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quickAccessApps = combine(apps, quickAccessPackages) { appsList, packages ->
        packages.mapNotNull { pkg -> appsList.find { it.packageName == pkg } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val iconStyle = dockRepository.iconStyle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "natural"
    )

    val iconSize = dockRepository.iconSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "normal"
    )

    fun setIconStyle(style: String) {
        viewModelScope.launch { dockRepository.setIconStyle(style) }
    }

    fun setIconSize(size: String) {
        viewModelScope.launch { dockRepository.setIconSize(size) }
    }

    val isAppQuickAccess = { packageName: String ->
        quickAccessPackages.value.contains(packageName)
    }

    val filteredApps = combine(apps, _searchQuery) { appsList, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@combine appsList

        appsList.filter { 
            it.appName.contains(q, ignoreCase = true) 
        }.sortedWith(compareBy({ !it.appName.lowercase().startsWith(q) }, { it.appName.lowercase() }))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val groupedApps = filteredApps.map { appsList ->
        appsList.groupBy { it.appName.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val focusStreak = dockRepository.focusStreak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _screenTimeMillis = MutableStateFlow(0L)
    val screenTime = _screenTimeMillis.map { millis ->
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        "${hours}h ${minutes}m"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "0h 0m"
    )

    private val _hasUsageStatsPermission = MutableStateFlow(false)
    val hasUsageStatsPermission: StateFlow<Boolean> = _hasUsageStatsPermission

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
        val nextMode = !_isPrabhaMode.value
        _isPrabhaMode.value = nextMode
        if (nextMode) {
            viewModelScope.launch {
                dockRepository.recordFocusSession()
            }
        }
    }

    fun updateUsageStatsPermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        val granted = mode == AppOpsManager.MODE_ALLOWED
        _hasUsageStatsPermission.value = granted
        if (granted) {
            refreshScreenTime()
        }
    }

    fun refreshScreenTime() {
        if (!_hasUsageStatsPermission.value) return
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            val totalTime = stats?.sumOf { it.totalTimeInForeground } ?: 0L
            _screenTimeMillis.value = totalTime
        }
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

    fun addToQuickAccess(packageName: String) {
        viewModelScope.launch {
            dockRepository.addToQuickAccess(packageName)
        }
    }

    fun removeFromQuickAccess(packageName: String) {
        viewModelScope.launch {
            dockRepository.removeFromQuickAccess(packageName)
        }
    }

    fun reorderQuickAccess(newPackages: List<String>) {
        viewModelScope.launch {
            dockRepository.reorderQuickAccess(newPackages)
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
        updateUsageStatsPermission()
    }
}
