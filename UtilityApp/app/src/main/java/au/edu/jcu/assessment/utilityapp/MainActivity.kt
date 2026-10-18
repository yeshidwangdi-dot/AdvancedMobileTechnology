package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MobileTech_UtilityAppTheme { UtilityApp() } }
    }
}

class UtilityViewModel : ViewModel() {
    var amount by mutableStateOf("")
        private set
    var fromCurrency by mutableStateOf("AUD")
        private set
    var toCurrency by mutableStateOf("USD")
        private set
    var result by mutableStateOf("")
        private set
    var error by mutableStateOf("")
        private set

    // Illustrative constants, NOT live exchange rates.
    private val audPerUnit = mapOf("AUD" to 1.0, "USD" to 1.52, "EUR" to 1.76, "GBP" to 2.04, "BTN" to 0.018)
    val currencies = audPerUnit.keys.toList()

    fun updateAmount(value: String) { amount = value; result = ""; error = "" }
    fun setFrom(value: String) { fromCurrency = value; result = "" }
    fun setTo(value: String) { toCurrency = value; result = "" }
    fun swap() { val old = fromCurrency; fromCurrency = toCurrency; toCurrency = old; result = "" }
    fun convert() {
        val number = amount.toDoubleOrNull()
        if (number == null || !number.isFinite() || number < 0) {
            error = "Enter a valid non-negative amount"; result = ""; return
        }
        val converted = number * audPerUnit.getValue(fromCurrency) / audPerUnit.getValue(toCurrency)
        result = "%.2f %s".format(java.util.Locale.US, converted, toCurrency)
        error = ""
    }
    fun clear() { amount = ""; result = ""; error = "" }
}

@Composable
fun UtilityApp(model: UtilityViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf("Utility") }
    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(selected = selectedTab == "Utility", onClick = { selectedTab = "Utility" }, icon = { Icon(Icons.Default.Home, contentDescription = "Utility") }, label = { Text("Utility") })
            NavigationBarItem(selected = selectedTab == "About", onClick = { selectedTab = "About" }, icon = { Icon(Icons.Default.Settings, contentDescription = "About") }, label = { Text("About") })
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            if (selectedTab == "Utility") UtilityScreen(model) else AboutScreen()
        }
    }
}

@Composable
fun CurrencyPicker(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}

@Composable
fun UtilityScreen(model: UtilityViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Currency Converter", style = MaterialTheme.typography.headlineMedium)
        Text("Convert using sample reference rates. Not for financial transactions.")
        OutlinedTextField(value = model.amount, onValueChange = model::updateAmount, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CurrencyPicker("From", model.fromCurrency, model.currencies, model::setFrom)
            CurrencyPicker("To", model.toCurrency, model.currencies, model::setTo)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = model::convert) { Text("Convert") }
            OutlinedButton(onClick = model::swap) { Text("Swap") }
            TextButton(onClick = model::clear) { Text("Clear") }
        }
        if (model.error.isNotEmpty()) Text(model.error, color = MaterialTheme.colorScheme.error)
        if (model.result.isNotEmpty()) Text("Result: ${model.result}", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun AboutScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("About this app", style = MaterialTheme.typography.headlineMedium)
        Text("An educational currency conversion utility built with Kotlin, Jetpack Compose, Material 3 and ViewModel.")
        Text("Rates are illustrative fixed values relative to AUD, not live exchange rates.")
    }
}

@Preview(showBackground = true)
@Composable
fun UtilityAppPreview() { MobileTech_UtilityAppTheme { UtilityApp() } }
