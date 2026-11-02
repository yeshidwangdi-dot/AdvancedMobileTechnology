package com.example.advancedmobiletechnology

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.advancedmobiletechnology.ui.theme.AdvancedMobileTechnologyTheme
import java.util.Locale

// Demonstration rates: number of units of each currency equal to 1 AUD.
// These are illustrative, not current market exchange rates.
private val unitsPerAud = linkedMapOf(
    "AUD" to 1.0,
    "USD" to 0.66,
    "EUR" to 0.61,
    "GBP" to 0.52,
    "INR" to 55.0,
    "BTN" to 55.0,
    "JPY" to 99.0,
    "NZD" to 1.09,
    "CAD" to 0.90
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvancedMobileTechnologyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CurrencyConverterApp(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CurrencyConverterApp(modifier: Modifier = Modifier) {
    var amount by rememberSaveable { mutableStateOf("100") }
    var from by rememberSaveable { mutableStateOf("AUD") }
    var to by rememberSaveable { mutableStateOf("USD") }
    var showAbout by rememberSaveable { mutableStateOf(false) }

    val number = amount.toDoubleOrNull()
    val error = when {
        amount.isBlank() -> "Enter an amount."
        number == null || !number.isFinite() -> "Enter a valid number."
        number < 0 -> "Amount cannot be negative."
        else -> null
    }
    val converted = if (error == null && number != null) {
        number / unitsPerAud.getValue(from) * unitsPerAud.getValue(to)
    } else null

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Currency Converter", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Convert between currencies using offline demonstration rates.")
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            singleLine = true,
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier.fillMaxWidth()
        )
        CurrencyPicker("From", from, { from = it })
        CurrencyPicker("To", to, { to = it })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { val oldFrom = from; from = to; to = oldFrom }) {
                Text("Swap currencies")
            }
            OutlinedButton(onClick = { amount = ""; from = "AUD"; to = "USD" }) {
                Text("Reset")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Converted amount", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    converted?.let { String.format(Locale.US, "%,.2f %s", it, to) } ?: "—",
                    style = MaterialTheme.typography.headlineMedium
                )
                if (converted != null) {
                    Text("1 $from = " + String.format(Locale.US, "%.4f", unitsPerAud.getValue(to) / unitsPerAud.getValue(from)) + " $to")
                }
            }
        }
        OutlinedButton(onClick = { showAbout = !showAbout }) {
            Text(if (showAbout) "Hide About" else "About this app")
        }
        if (showAbout) {
            Text("Advanced Mobile Technology • Currency Converter")
            Text("Built with Kotlin and Jetpack Compose. Rates are fixed examples for learning purposes and are not live financial quotes.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPicker(label: String, selected: String, onSelect: (String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            unitsPerAud.keys.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = { onSelect(currency); expanded = false }
                )
            }
        }
    }
}
