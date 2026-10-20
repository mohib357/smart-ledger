package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.util.Formatters

@Composable
fun QuickCalculatorDialog(
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onUseResult: (Double) -> Unit = {}
) {
    var expression by remember { mutableStateOf("") }
    var displayResult by remember { mutableStateOf("0") }

    fun append(char: String) {
        expression += char
        val eval = Formatters.evaluateExpression(expression)
        if (eval != null) {
            displayResult = Formatters.formatTaka(eval, useBengali = useBengaliDigits)
        }
    }

    fun calculate() {
        val eval = Formatters.evaluateExpression(expression)
        if (eval != null) {
            val numStr = if (eval % 1.0 == 0.0) eval.toLong().toString() else eval.toString()
            expression = numStr
            displayResult = Formatters.formatTaka(eval, useBengali = useBengaliDigits)
        }
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            val eval = Formatters.evaluateExpression(expression)
            displayResult = if (eval != null) Formatters.formatTaka(eval, useBengali = useBengaliDigits) else "0"
        }
    }

    fun clear() {
        expression = ""
        displayResult = "0"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dialog_calculator_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                // Display Screen
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isEmpty()) "0" else if (useBengaliDigits) Formatters.toBengaliDigits(expression) else expression,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayResult,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Keypad
                val buttons = listOf(
                    listOf("C", "÷", "×", "DEL"),
                    listOf("7", "8", "9", "-"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "="),
                    listOf("0", "00", ".", "")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttons.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { btn ->
                                if (btn.isEmpty()) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val isOp = btn in listOf("+", "-", "×", "÷", "=")
                                    val isDel = btn == "DEL"
                                    val isClear = btn == "C"

                                    val bg = when {
                                        btn == "=" -> MaterialTheme.colorScheme.primary
                                        isOp -> MaterialTheme.colorScheme.primaryContainer
                                        isDel || isClear -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    val fg = when {
                                        btn == "=" -> MaterialTheme.colorScheme.onPrimary
                                        isOp -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isDel || isClear -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(bg)
                                            .clickable {
                                                when (btn) {
                                                    "C" -> clear()
                                                    "DEL" -> backspace()
                                                    "=" -> calculate()
                                                    "÷" -> append("÷")
                                                    "×" -> append("×")
                                                    "+" -> append("+")
                                                    "-" -> append("-")
                                                    else -> append(btn)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDel) {
                                            Icon(
                                                Icons.Default.Backspace,
                                                contentDescription = stringResource(R.string.action_delete),
                                                tint = fg,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            val label = if (useBengaliDigits && btn in "0".."9") {
                                                Formatters.toBengaliDigits(btn)
                                            } else if (useBengaliDigits && btn == "00") {
                                                "০০"
                                            } else btn
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = fg
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action to transfer/use result
                Button(
                    onClick = {
                        val eval = Formatters.evaluateExpression(expression) ?: 0.0
                        onUseResult(eval)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("calculator_use_result_btn")
                ) {
                    Text(stringResource(R.string.dialog_calculator_use_result), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
