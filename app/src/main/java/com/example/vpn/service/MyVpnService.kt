package com.example.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.vpn.MainActivity
import com.example.vpn.R
import com.example.vpn.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.content.pm.ServiceInfo

class MyVpnService : VpnService() {

    private var interfaceDescriptor: ParcelFileDescriptor? = null
    private var isRunning = false
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            disconnect()
            return START_NOT_STICKY
        }

        connect()
        return START_STICKY
    }

    private fun connect() {
        if (isRunning) return
        
        // Start Foreground Service to keep app alive
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@MyVpnService)
                val bypassedApps = db.appBypassDao().getAllBypassApps().first()
                val selectedProfile = db.vpnProfileDao().getSelectedProfile() ?: db.vpnProfileDao().getAllProfiles().first().firstOrNull()

                // Builder configuration
                val builder = Builder()
                
                // Set a dummy MTU
                builder.setMtu(1500)
                
                // Add a dummy address (this would normally come from the server config)
                builder.addAddress("10.0.0.2", 24)
                
                // --- DNS CONFIGURATION ---
                val dnsServer = selectedProfile?.dnsServer ?: "94.140.14.14" // Default to AdGuard if none found
                builder.addDnsServer(dnsServer)
                Log.d(TAG, "Using DNS Server: $dnsServer (${selectedProfile?.name ?: "Default"})")
                
                // --- SPLIT TUNNELING ---
                bypassedApps.forEach { app ->
                    try {
                        builder.addDisallowedApplication(app.packageName)
                        Log.d(TAG, "Bypassing app: ${app.packageName}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to disallow app: ${app.packageName}", e)
                    }
                }
                
                // Let's route a dummy subnet to be safe for this prototype
                builder.addRoute("192.168.99.0", 24)
                
                builder.setSession("Aura VPN")
                builder.setBlocking(false)

                // Create the interface
                interfaceDescriptor = builder.establish()
                
                isRunning = true
                Log.i(TAG, "VPN Interface established")
                
                // TODO: Start the thread that reads/writes packets to the interfaceDescriptor
                
            } catch (e: Exception) {
                Log.e(TAG, "Error establishing VPN", e)
                stopSelf()
            }
        }
    }

    private fun disconnect() {
        try {
            interfaceDescriptor?.close()
            interfaceDescriptor = null
            isRunning = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            Log.i(TAG, "VPN Interface closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN", e)
        }
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "vpn_service_channel"
        val channelName = "VPN Connection Status"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Aura VPN is Connected")
            .setContentText("Your connection is secure.")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        disconnect()
    }

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.service.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.service.DISCONNECTED"
        private const val TAG = "MyVpnService"
        private const val NOTIFICATION_ID = 1
    }
}
