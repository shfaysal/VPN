package com.example.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vpn.ui.AppNavigation
import com.example.vpn.ui.SettingsScreen
import com.example.vpn.ui.theme.VPNTheme
import dagger.hilt.android.AndroidEntryPoint

enum class VpnStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VPNTheme {
                AppNavigation()
            }
        }
    }
}
