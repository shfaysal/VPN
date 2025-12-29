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

import android.content.pm.ServiceInfo

class MyVpnService : VpnService() {

    private var interfaceDescriptor: ParcelFileDescriptor? = null
    private var isRunning = false

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
        
        // In a real app, you would parse the WireGuard config here.
        // For now, we are creating a dummy interface to simulate connection.
        
        try {
            // Builder configuration
            val builder = Builder()
            
            // Set a dummy MTU
            builder.setMtu(1500)
            
            // Add a dummy address (this would normally come from the server config)
            builder.addAddress("10.0.0.2", 24)
            
            // --- AD BLOCKING CONFIGURATION ---
            // Route DNS requests to AdGuard DNS (Default: Ad-Blocking + Tracking Protection)
            builder.addDnsServer("94.140.14.14")
            builder.addDnsServer("94.140.15.15")
            
            // To ensure the system actually uses our DNS, we can route the DNS IPs themselves
            // builder.addRoute("94.140.14.14", 32)
            // builder.addRoute("94.140.15.15", 32)
            // ----------------------------------
            
            // Route all traffic through the VPN (0.0.0.0/0)
            // Be careful: this blocks internet if no actual tunnel is running!
            // For testing UI without blocking internet, we might want to route a specific dummy IP.
            // builder.addRoute("0.0.0.0", 0) 
            
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
            .setContentText("Ad blocking active. Your connection is secure.")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Using default icon for now
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.service.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.service.DISCONNECTED"
        private const val TAG = "MyVpnService"
        private const val NOTIFICATION_ID = 1
    }
}
