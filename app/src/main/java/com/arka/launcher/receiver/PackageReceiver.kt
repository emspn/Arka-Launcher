package com.arka.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arka.launcher.data.repository.AppRepository
import com.arka.launcher.data.repository.DockRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: AppRepository

    @Inject
    lateinit var dockRepository: DockRepository

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        val action = intent.action

        CoroutineScope(Dispatchers.IO).launch {
            when (action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    repository.addApp(packageName)
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    repository.removeApp(packageName)
                    dockRepository.unpinApp(packageName)
                }
                Intent.ACTION_PACKAGE_CHANGED -> {
                    repository.addApp(packageName) // Refresh info
                }
            }
        }
    }
}
