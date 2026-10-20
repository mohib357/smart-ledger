package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.util.PinHelper

@Composable
fun PinConfirmDialog(
    onPinCorrect: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    fun submitPin() {
        val lockoutUntil = prefs.getLong("txn_pin_lockout_until", 0L)
        if (System.currentTimeMillis() < lockoutUntil) {
            val minsLeft = ((lockoutUntil - System.currentTimeMillis()) / 60000 + 1).toInt()
            errorMessage = context.getString(R.string.pin_dialog_locked, minsLeft)
            enteredPin = ""
            return
        }
        val storedHash = prefs.getString("txn_pin", "") ?: ""
        val attempts = prefs.getInt("txn_pin_attempts", 0)
        if (PinHelper.verify(enteredPin, storedHash)) {
            prefs.edit().putInt("txn_pin_attempts", 0).apply()
            onPinCorrect()
        } else {
            val newAttempts = attempts + 1
            enteredPin = ""
            if (newAttempts >= 3) {
                val lockUntil = System.currentTimeMillis() + 5 * 60 * 1000L
                prefs.edit()
                    .putLong("txn_pin_lockout_until", lockUntil)
                    .putInt("txn_pin_attempts", 0)
                    .apply()
                errorMessage = context.getString(R.string.pin_dialog_locked, 5)
            } else {
                prefs.edit().putInt("txn_pin_attempts", newAttempts).apply()
                errorMessage = context.getString(R.string.pin_dialog_wrong)
            }
        }
    }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            submitPin()
        }
    }

    val isLocked = System.currentTimeMillis() < prefs.getLong("txn_pin_lockout_until", 0L)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pin_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until 4) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < enteredPin.length) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                PinKeypad(
                    onDigit = { d ->
                        if (!isLocked && enteredPin.length < 4) enteredPin += d
                    },
                    onDelete = { if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1) },
                    onConfirm = { if (enteredPin.length == 4) submitPin() },
                    enabled = !isLocked
                )
            }
        }
    }
}

@Composable
internal fun PinKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    enabled: Boolean = true
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("⌫", "0", "✓")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    val bgColor = when (key) {
                        "⌫" -> MaterialTheme.colorScheme.errorContainer
                        "✓" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val textColor = when (key) {
                        "⌫" -> MaterialTheme.colorScheme.onErrorContainer
                        "✓" -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.4f))
                            .then(
                                if (enabled) Modifier.clickable {
                                    when (key) {
                                        "⌫" -> onDelete()
                                        "✓" -> onConfirm()
                                        else -> onDigit(key)
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (enabled) textColor else textColor.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
