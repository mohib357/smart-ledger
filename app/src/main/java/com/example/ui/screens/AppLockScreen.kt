package com.example.ui.screens

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.ui.components.PinKeypad
import com.example.util.PinHelper

@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var showPinEntry by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    fun launchBiometric() {
        if (activity == null) {
            showPinEntry = true
            return
        }
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onUnlocked()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        showPinEntry = true
                    }
                }
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.app_lock_biometric_prompt_title))
                .setSubtitle(context.getString(R.string.app_lock_biometric_prompt_subtitle))
                .setNegativeButtonText(context.getString(R.string.app_lock_use_pin))
                .build()
            prompt.authenticate(info)
        } else {
            showPinEntry = true
        }
    }

    LaunchedEffect(Unit) {
        launchBiometric()
    }

    fun submitPin() {
        val lockoutUntil = prefs.getLong("app_pin_lockout_until", 0L)
        if (System.currentTimeMillis() < lockoutUntil) {
            val minsLeft = ((lockoutUntil - System.currentTimeMillis()) / 60000 + 1).toInt()
            errorMessage = context.getString(R.string.app_lock_locked, minsLeft)
            enteredPin = ""
            return
        }
        val storedHash = prefs.getString("app_pin", "") ?: ""
        val attempts = prefs.getInt("app_pin_attempts", 0)
        if (PinHelper.verify(enteredPin, storedHash)) {
            prefs.edit().putInt("app_pin_attempts", 0).apply()
            onUnlocked()
        } else {
            val newAttempts = attempts + 1
            enteredPin = ""
            if (newAttempts >= 3) {
                val lockUntil = System.currentTimeMillis() + 5 * 60 * 1000L
                prefs.edit()
                    .putLong("app_pin_lockout_until", lockUntil)
                    .putInt("app_pin_attempts", 0)
                    .apply()
                errorMessage = context.getString(R.string.app_lock_locked, 5)
            } else {
                prefs.edit().putInt("app_pin_attempts", newAttempts).apply()
                errorMessage = context.getString(R.string.app_lock_wrong_pin)
            }
        }
    }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length >= 4) {
            submitPin()
        }
    }

    val isLocked = System.currentTimeMillis() < prefs.getLong("app_pin_lockout_until", 0L)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "হিসাব",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_lock_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!showPinEntry) {
                TextButton(onClick = { showPinEntry = true }) {
                    Text(stringResource(R.string.app_lock_use_pin))
                }
            } else {
                // PIN dot display (4–6 digits)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until 6) {
                        val isFilled = i < enteredPin.length
                        val isActive = i < 6 // show up to 6
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isFilled -> MaterialTheme.colorScheme.primary
                                        i < 4 -> MaterialTheme.colorScheme.outlineVariant
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }
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
                        if (!isLocked && enteredPin.length < 6) enteredPin += d
                    },
                    onDelete = { if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1) },
                    onConfirm = { if (enteredPin.length >= 4) submitPin() },
                    enabled = !isLocked
                )
            }
        }
    }
}
