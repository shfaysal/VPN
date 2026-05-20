package com.example.vpn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showSplitTunneling by remember { mutableStateOf(false) }
    var showDnsDialog by remember { mutableStateOf(false) }
    val selectedProfile by viewModel.selectedProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                SettingsCategory(title = "Connection")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Dns,
                    title = "DNS Server",
                    subtitle = selectedProfile.name,
                    onClick = { showDnsDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Apps,
                    title = "Split Tunneling",
                    subtitle = "Manage apps that bypass VPN",
                    onClick = { showSplitTunneling = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Apps,
                    title = "Split Tunneling",
                    subtitle = "Manage apps that bypass VPN",
                    onClick = { showSplitTunneling = true }
                )
            }
        }
    }

    if (showDnsDialog) {
        DnsSelectionDialog(
            currentDns = selectedProfile.dnsServer,
            onDismiss = { showDnsDialog = false },
            onSelected = { name, dns ->
                viewModel.setDns(name, dns)
                showDnsDialog = false
            }
        )
    }

    if (showSplitTunneling) {
        SplitTunnelingSheet(
            viewModel = viewModel,
            onDismiss = { showSplitTunneling = false }
        )
    }
}

@Composable
fun DnsSelectionDialog(
    currentDns: String,
    onDismiss: () -> Unit,
    onSelected: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select DNS Server") },
        text = {
            Column {
                DnsOption("AdGuard DNS", "94.140.14.14", currentDns, onSelected)
                DnsOption("Google DNS", "8.8.8.8", currentDns, onSelected)
                DnsOption("Cloudflare DNS", "1.1.1.1", currentDns, onSelected)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DnsOption(name: String, dns: String, currentDns: String, onSelected: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(name, dns) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = dns == currentDns, onClick = null)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Text(text = dns, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
