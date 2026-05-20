package com.example.vpn.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vpn.VpnStatus
import com.example.vpn.service.MyVpnService

@Composable
fun DashboardScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(VpnStatus.DISCONNECTED) }

    // Check if VPN is already running on startup
    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isVpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        
        if (isVpnActive) {
            status = VpnStatus.CONNECTED
        }
    }

    // Launcher for the VPN permission dialog
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, start the service
            startVpnService(context)
            status = VpnStatus.CONNECTED
        } else {
            // Permission denied
            status = VpnStatus.DISCONNECTED
        }
    }

    fun toggleVpn() {
        if (status == VpnStatus.CONNECTED) {
            // Disconnect
            val intent = Intent(context, MyVpnService::class.java)
            intent.action = MyVpnService.ACTION_DISCONNECT
            context.startService(intent)
            status = VpnStatus.DISCONNECTED
        } else {
            // Connect
            status = VpnStatus.CONNECTING
            val intent = VpnService.prepare(context)
            if (intent != null) {
                // We need to ask for permission
                vpnPermissionLauncher.launch(intent)
            } else {
                // We already have permission
                startVpnService(context)
                status = VpnStatus.CONNECTED
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aura VPN",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Security, contentDescription = "Settings")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Main Connection Button
            ConnectionButton(status = status, onClick = { toggleVpn() })

            Spacer(modifier = Modifier.height(32.dp))

            // Status Text
            Text(
                text = when (status) {
                    VpnStatus.DISCONNECTED -> "Not Protected"
                    VpnStatus.CONNECTING -> "Connecting..."
                    VpnStatus.CONNECTED -> "Connection Secured"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = when (status) {
                        VpnStatus.CONNECTED -> Color(0xFF4CAF50)
                        VpnStatus.CONNECTING -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            )

            if (status == VpnStatus.CONNECTED) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "AD BLOCKING ACTIVE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Stats Section
            StatsCard()
        }
    }
}

private fun startVpnService(context: Context) {
    val intent = Intent(context, MyVpnService::class.java)
    intent.action = MyVpnService.ACTION_CONNECT
    context.startService(intent)
}

@Composable
fun ConnectionButton(status: VpnStatus, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            VpnStatus.CONNECTED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            VpnStatus.CONNECTING -> Color(0xFFFF9800).copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(500), label = "bgColor"
    )

    val iconColor by animateColorAsState(
        targetValue = when (status) {
            VpnStatus.CONNECTED -> Color(0xFF4CAF50)
            VpnStatus.CONNECTING -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(500), label = "iconColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (status == VpnStatus.CONNECTED) 12.dp else 4.dp,
        label = "elevation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
            .shadow(elevation, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Toggle VPN",
                modifier = Modifier.size(80.dp),
                tint = iconColor
            )
        }
    }
}

@Composable
fun StatsCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(icon = Icons.Default.Public, label = "Location", value = "Netherlands")
            Divider(modifier = Modifier.height(40.dp).width(1.dp))
            StatItem(icon = Icons.Default.Speed, label = "Latency", value = "24ms")
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun Divider(modifier: Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.outlineVariant))
}
