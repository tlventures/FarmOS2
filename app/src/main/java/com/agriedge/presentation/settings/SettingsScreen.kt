package com.agriedge.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var wifiOnlySync by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var transactionNotifications by remember { mutableStateOf(true) }
    var reminderNotifications by remember { mutableStateOf(true) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Language Settings
            SettingsSection(title = "Language") {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "App Language",
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
            }

            Divider()

            // Sync Settings
            SettingsSection(title = "Sync Settings") {
                SettingsSwitchItem(
                    icon = Icons.Default.Wifi,
                    title = "WiFi Only Sync",
                    subtitle = "Sync data only when connected to WiFi",
                    checked = wifiOnlySync,
                    onCheckedChange = { wifiOnlySync = it }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Sync,
                    title = "Automatic Sync",
                    subtitle = "Automatically sync data in background",
                    checked = autoSync,
                    onCheckedChange = { autoSync = it }
                )
            }

            Divider()

            // Notification Settings
            SettingsSection(title = "Notifications") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    subtitle = "Receive app notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                if (notificationsEnabled) {
                    SettingsSwitchItem(
                        icon = Icons.Default.ShoppingCart,
                        title = "Transaction Updates",
                        subtitle = "Notifications for market transactions",
                        checked = transactionNotifications,
                        onCheckedChange = { transactionNotifications = it }
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Alarm,
                        title = "Pickup Reminders",
                        subtitle = "Reminders for scheduled pickups",
                        checked = reminderNotifications,
                        onCheckedChange = { reminderNotifications = it }
                    )
                }
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "View terms and conditions",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "View privacy policy",
                    onClick = {}
                )
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                selectedLanguage = language
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "English",
        "हिंदी (Hindi)",
        "मराठी (Marathi)",
        "தமிழ் (Tamil)",
        "తెలుగు (Telugu)",
        "ಕನ್ನಡ (Kannada)",
        "বাংলা (Bengali)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = language,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
