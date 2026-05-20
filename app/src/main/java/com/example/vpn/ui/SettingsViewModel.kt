package com.example.vpn.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vpn.data.AppBypass
import com.example.vpn.data.AppBypassDao
import com.example.vpn.data.VpnProfile
import com.example.vpn.data.VpnProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppItemState(
    val info: ApplicationInfo,
    val label: String,
    val isBypassed: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val appBypassDao: AppBypassDao,
    private val vpnProfileDao: VpnProfileDao
) : AndroidViewModel(application) {
    private val pm = application.packageManager

    val selectedProfile: StateFlow<VpnProfile> = vpnProfileDao.getAllProfiles().map { profiles ->
        profiles.find { it.isSelected } ?: profiles.firstOrNull() ?: VpnProfile(name = "AdGuard DNS", dnsServer = "94.140.14.14", isSelected = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VpnProfile(name = "AdGuard DNS", dnsServer = "94.140.14.14", isSelected = true))

    private val _installedApps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val appItems: StateFlow<List<AppItemState>> = combine(
        _installedApps,
        appBypassDao.getAllBypassApps()
    ) { apps, bypassed ->
        val bypassedPackages = bypassed.map { it.packageName }.toSet()
        apps.map { app ->
            AppItemState(
                info = app,
                label = app.loadLabel(pm).toString(),
                isBypassed = bypassedPackages.contains(app.packageName)
            )
        }
    }.collectAsStateFlow(viewModelScope, emptyList())

    init {
        loadApps()
        ensureDefaultProfile()
    }

    private fun ensureDefaultProfile() {
        viewModelScope.launch {
            val profile = vpnProfileDao.getSelectedProfile()
            if (profile == null) {
                vpnProfileDao.insertProfile(VpnProfile(name = "AdGuard DNS", dnsServer = "94.140.14.14", isSelected = true))
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { it.loadLabel(pm).toString() }
            _installedApps.value = apps
        }
    }

    fun toggleAppBypass(appState: AppItemState) {
        viewModelScope.launch {
            if (appState.isBypassed) {
                appBypassDao.deleteBypassApp(AppBypass(appState.info.packageName, appState.label))
            } else {
                appBypassDao.insertBypassApp(AppBypass(appState.info.packageName, appState.label))
            }
        }
    }

    fun setDns(name: String, dns: String) {
        viewModelScope.launch {
            val current = selectedProfile.value
            vpnProfileDao.insertProfile(current.copy(name = name, dnsServer = dns, isSelected = true))
            vpnProfileDao.selectProfile(current.id) // This is a bit redundant but ensures UI update if ID changes
        }
    }
}

// Extension to collect Flow as StateFlow with initial value for ViewModel
fun <T> kotlinx.coroutines.flow.Flow<T>.collectAsStateFlow(
    scope: kotlinx.coroutines.CoroutineScope,
    initialValue: T
): StateFlow<T> {
    val state = MutableStateFlow(initialValue)
    scope.launch {
        collect { state.value = it }
    }
    return state
}
