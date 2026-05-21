package com.example.vpn.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

import androidx.compose.runtime.collectAsState
import com.example.vpn.ui.SettingsViewModel
import com.example.vpn.ui.AppItemState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitTunnelingSheet(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val appItems by viewModel.appItems.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxHeight(0.8f)) {
            Text(
                text = "Split Tunneling",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Select apps that should bypass the VPN connection.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyColumn {
                items(appItems) { appState ->
                    AppItem(appState, onToggle = { viewModel.toggleAppBypass(appState) })
                }
            }
        }
    }
}

@Composable
fun AppItem(appState: AppItemState, onToggle: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = remember { appState.info.loadIcon(pm).toBitmap().asImageBitmap() }
        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = appState.label, modifier = Modifier.weight(1f))
        Checkbox(checked = appState.isBypassed, onCheckedChange = { onToggle() })
    }
}
