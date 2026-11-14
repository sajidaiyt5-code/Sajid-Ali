package com.example.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun CalculatorScreen(
    onBack: () -> Unit
) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var showScientific by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    fun evaluateAndDisplay(expr: String): String {
        if (expr.isBlank()) return "0"
        return runCatching {
            val evaluated = evaluateMathExpression(expr)
            if (evaluated.isNaN() || evaluated.isInfinite()) "Error"
            else {
                BigDecimal.valueOf(evaluated)
                    .setScale(8, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        }.getOrElse { "Syntax Error" }
    }

    VirtualAppScaffold(
        title = "Calculator",
        subtitle = "UK Standard & £ VAT Helper",
        onBack = onBack
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxHeight < 480.dp
            val scrollState = rememberScrollState()
            val containerModifier = if (isCompact) {
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            }

            Column(
                modifier = containerModifier,
                verticalArrangement = if (isCompact) Arrangement.spacedBy(8.dp) else Arrangement.SpaceBetween
            ) {
                // History strip
                if (history.isNotEmpty() && !isCompact) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        reverseLayout = true
                    ) {
                        items(history) { (exp, res) ->
                            Text(
                                text = "$exp = $res",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Display Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isCompact) 12.dp else 18.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = expression.ifBlank { "0" },
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = if (isCompact) 18.sp else 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 6.dp))
                        Text(
                            text = resultText,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = if (isCompact) 26.sp else 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("calculator_result")
                        )
                    }
                }

                // UK Helpers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showScientific,
                        onClick = { showScientific = !showScientific },
                        label = { Text("Scientific") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            val base = expression.ifBlank { resultText }
                            val nextExpr = "($base)*1.20"
                            expression = nextExpr
                            resultText = "£" + evaluateAndDisplay(nextExpr)
                        },
                        label = { Text("+20% UK VAT (£)") }
                    )
                }

                if (showScientific) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("sin(", "cos(", "tan(", "sqrt(", "π").forEach { fn ->
                            Button(
                                onClick = {
                                    expression += if (fn == "π") "3.14159265" else fn
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(fn.removeSuffix("("), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Keypad
                val rows = listOf(
                    listOf("C", "(", ")", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "DEL", "=")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
                ) {
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
                        ) {
                            row.forEach { key ->
                                val isOperator = key in setOf("÷", "×", "-", "+", "=")
                                val isClear = key == "C"
                                val bg = when {
                                    key == "=" -> MaterialTheme.colorScheme.primary
                                    isOperator -> MaterialTheme.colorScheme.primaryContainer
                                    isClear -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                                val fg = when {
                                    key == "=" -> MaterialTheme.colorScheme.onPrimary
                                    isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
                                    isClear -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> {
                                                expression = ""
                                                resultText = "0"
                                            }
                                            "DEL" -> {
                                                if (expression.isNotEmpty()) {
                                                    expression = expression.dropLast(1)
                                                }
                                            }
                                            "=" -> {
                                                val res = evaluateAndDisplay(expression)
                                                resultText = res
                                                if (expression.isNotBlank() && !res.contains("Error")) {
                                                    history = (listOf(expression to res) + history).take(10)
                                                }
                                            }
                                            else -> {
                                                expression += key
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = bg,
                                        contentColor = fg
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .height(if (isCompact) 48.dp else 58.dp)
                                        .testTag("calc_btn_$key")
                                ) {
                                    if (key == "DEL") {
                                        Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace")
                                    } else {
                                        Text(
                                            text = key,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = if (isCompact) 18.sp else 20.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateMathExpression(raw: String): Double {
    val expr = raw.replace("×", "*").replace("÷", "/").replace(" ", "")
    var pos = -1
    var ch = 0

    fun nextChar() {
        ch = if (++pos < expr.length) expr[pos].code else -1
    }

    fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    lateinit var parseExpression: () -> Double

    fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor()
        if (eat('-'.code)) return -parseFactor()
        var x: Double
        val startPos = pos
        if (eat('('.code)) {
            x = parseExpression()
            eat(')'.code)
        } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
            while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
            x = expr.substring(startPos, pos).toDouble()
        } else if (ch in 'a'.code..'z'.code) {
            while (ch in 'a'.code..'z'.code) nextChar()
            val func = expr.substring(startPos, pos)
            x = parseFactor()
            x = when (func) {
                "sqrt" -> sqrt(x)
                "sin" -> sin(Math.toRadians(x))
                "cos" -> cos(Math.toRadians(x))
                "tan" -> tan(Math.toRadians(x))
                else -> throw IllegalArgumentException("Unknown function: $func")
            }
        } else {
            throw IllegalArgumentException("Unexpected char")
        }
        return x
    }

    fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            when {
                eat('*'.code) -> x *= parseFactor()
                eat('/'.code) -> x /= parseFactor()
                else -> return x
            }
        }
    }

    parseExpression = {
        var x = parseTerm()
        while (true) {
            when {
                eat('+'.code) -> x += parseTerm()
                eat('-'.code) -> x -= parseTerm()
                else -> break
            }
        }
        x
    }

    nextChar()
    val result = parseExpression()
    if (pos < expr.length) throw IllegalArgumentException("Unexpected character")
    return result
}
