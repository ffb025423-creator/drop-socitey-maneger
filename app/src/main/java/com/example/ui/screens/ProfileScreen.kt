package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InAppNotification
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessViewModel
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: BusinessViewModel,
    notifications: List<InAppNotification>,
    currencySymbol: String,
    onResetData: () -> Unit
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf(viewModel.securityPin.value) }

    var showPurgeConfirm by remember { mutableStateOf(false) }
    var showBackupRestoreSection by remember { mutableStateOf(false) }

    var ownerName by remember { mutableStateOf("Adnan Hasan") }
    var ownerPhone by remember { mutableStateOf("+880 1712-345678") }
    var ownerEmail by remember { mutableStateOf("contact@dropsociety.com") }
    var businessAddress by remember { mutableStateOf("Mirpur 11, Dhaka, Bangladesh") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Business Brand header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardBackground)
                    .border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DS",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DROP SOCIETY",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Premium Apparel Business Hub",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }

        // --- Notification Feed Alerts ---
        if (notifications.isNotEmpty()) {
            item {
                Text(
                    text = "LIVE SYSTEM ALERTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed,
                    letterSpacing = 1.sp
                )
            }

            items(notifications) { notif ->
                val notifColor = when (notif.type) {
                    "WARNING" -> WarningYellow
                    "SUCCESS" -> SuccessGreen
                    else -> Color.Cyan
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(notifColor.copy(alpha = 0.08f))
                        .border(1.dp, notifColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (notif.type == "WARNING") Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = notifColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(notif.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(notif.message, color = TextGray, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- Brand Identity Editable Info ---
        item {
            Text(
                text = "BUSINESS DETAILS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditableDetailRow(label = "Owner Name", value = ownerName, onValueChange = { ownerName = it })
                    EditableDetailRow(label = "Primary Phone", value = ownerPhone, onValueChange = { ownerPhone = it })
                    EditableDetailRow(label = "Email Address", value = ownerEmail, onValueChange = { ownerEmail = it })
                    EditableDetailRow(label = "Warehouse Address", value = businessAddress, onValueChange = { businessAddress = it })
                }
            }
        }

        // --- System Settings Controls ---
        item {
            Text(
                text = "SYSTEM SETTINGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Dark theme toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Theme Color (Dark Mode)", color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = viewModel.isDarkTheme.value,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryRed, checkedTrackColor = PrimaryRed.copy(alpha = 0.5f))
                        )
                    }

                    HorizontalDivider(color = GlassWhite)

                    // Currency chooser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Business Currency", color = Color.White, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("৳", "$", "€").forEach { symbol ->
                                val isSelected = currencySymbol == symbol
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PrimaryRed else Color(0xFF2C2C2C))
                                        .clickable { viewModel.setCurrency(symbol) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(symbol, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = GlassWhite)

                    // Pin Lock setup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PIN Lock Protection", color = Color.White, fontSize = 14.sp)
                            Text(
                                text = if (viewModel.securityPin.value.isEmpty()) "Currently Disabled" else "Lock Active",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { showPinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.securityPin.value.isEmpty()) PrimaryRed else Color.DarkGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (viewModel.securityPin.value.isEmpty()) "Set PIN" else "Change PIN",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- Backups & Restore Section ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.border(1.dp, GlassWhite, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBackupRestoreSection = !showBackupRestoreSection },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cloud Backup & Restore", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (showBackupRestoreSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    if (showBackupRestoreSection) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Backup DB
                            Button(
                                onClick = {
                                    viewModel.exportBackup { success, path ->
                                        if (success) {
                                            // Share file
                                            try {
                                                val uri = Uri.parse(path)
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/json"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    putExtra(Intent.EXTRA_SUBJECT, "DROP SOCIETY Database Backup")
                                                    putExtra(Intent.EXTRA_TEXT, "Here is the local JSON database backup of your business records for DROP SOCIETY.")
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Export Backup JSON File"))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Exporting file failed", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Backup compilation failed.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup DB", fontSize = 11.sp, color = Color.White)
                            }

                            // Restore DB
                            Button(
                                onClick = {
                                    // Let's read the cache backup file as a fallback restore mock
                                    try {
                                        val backupFile = File(context.cacheDir, "drop_society_backup.json")
                                        if (backupFile.exists()) {
                                            viewModel.restoreBackup(backupFile.readText()) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "No backup file found in cache to restore. Export a backup first!", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Restoration failure: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore DB", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- Purge Clean System Data ---
        item {
            Button(
                onClick = { showPurgeConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ErrorRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete System Records & Logout", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // --- PIN setup dialog ---
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = CardBackground,
            title = { Text("Set Access PIN", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter a 4-digit security PIN to restrict database access. Clear it to disable the protection screen.", color = TextGray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = { if (it.length <= 4) pinValue = it },
                        label = { Text("4 Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("form_pin_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = GlassWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPin(pinValue)
                        showPinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // --- Purge confirmation ---
    if (showPurgeConfirm) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirm = false },
            containerColor = CardBackground,
            title = { Text("CRITICAL WARNING: PURGE ALL RECORDS?", color = ErrorRed, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all products, order history, analytics logs, and settings parameters? This completely clears local database tables.", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purgeAllData {
                            Toast.makeText(context, "Local business data has been purged.", Toast.LENGTH_SHORT).show()
                            onResetData()
                        }
                        showPurgeConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Confirm Purge", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirm = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun EditableDetailRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        HorizontalDivider(color = GlassWhite, thickness = 0.5.dp)
    }
}

@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        modifier = modifier,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(PrimaryRed)
    )
}
