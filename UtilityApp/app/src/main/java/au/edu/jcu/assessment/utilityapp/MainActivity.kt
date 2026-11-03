package au.edu.jcu.assessment.utilityapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import java.util.Locale

private val rates = linkedMapOf("AUD" to 1.0, "USD" to 1.52, "EUR" to 1.76, "GBP" to 2.04, "INR" to 0.018, "BTN" to 0.018)
private val flags = mapOf("AUD" to "🇦🇺", "USD" to "🇺🇸", "EUR" to "🇪🇺", "GBP" to "🇬🇧", "INR" to "🇮🇳", "BTN" to "🇧🇹")
private fun display(code: String) = flags.getValue(code) + " " + code

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MobileTech_UtilityAppTheme { UtilityApp() } }
    }
}

class UtilityViewModel : ViewModel() {
    var amount by mutableStateOf("")
    var from by mutableStateOf("AUD")
    var to by mutableStateOf("USD")
    var result by mutableStateOf("")
    var error by mutableStateOf("")
    val history = mutableStateListOf<String>()
    val favourites = mutableStateListOf<Pair<String, String>>()
    fun swap() { val old = from; from = to; to = old; result = "" }
    fun reset() { amount = ""; result = ""; error = "" }
    fun favourite() {
        val pair = from to to
        if (pair in favourites) favourites.remove(pair) else favourites.add(pair)
    }
    fun convert() {
        val number = amount.toDoubleOrNull()
        if (number == null || !number.isFinite() || number < 0) {
            error = "Enter a valid non-negative amount"
            result = ""
            return
        }
        val converted = number * rates.getValue(from) / rates.getValue(to)
        if (!converted.isFinite()) { error = "Amount too large"; result = ""; return }
        result = String.format(Locale.US, "%.2f %s", converted, to)
        error = ""
        history.add(0, amount + " " + from + " → " + result)
        if (history.size > 20) history.removeAt(history.lastIndex)
    }
}

@Composable
fun UtilityApp(model: UtilityViewModel = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                Row(Modifier.fillMaxWidth().padding(20.dp)) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = "App logo", tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("GlobeConvert", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Convert") })
                NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.Info, null) }, label = { Text("About") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> ConvertScreen(model)
                1 -> HistoryScreen(model)
                else -> AboutScreen()
            }
        }
    }
}

@Composable
fun CurrencyPicker(title: String, selected: String, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(display(selected) + " ▾") }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                rates.keys.forEach { code ->
                    DropdownMenuItem(text = { Text(display(code)) }, onClick = { choose(code); expanded = false })
                }
            }
        }
    }
}

@Composable
fun ConvertScreen(model: UtilityViewModel) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Currency Converter", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Convert with sample offline exchange rates.")
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = model.amount,
                        onValueChange = { model.amount = it; model.result = ""; model.error = "" },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CurrencyPicker("From", model.from) { model.from = it; model.result = "" }
                        IconButton(onClick = model::swap) { Icon(Icons.Default.SwapHoriz, "Swap") }
                        CurrencyPicker("To", model.to) { model.to = it; model.result = "" }
                    }
                    Button(onClick = model::convert, modifier = Modifier.fillMaxWidth()) { Text("Convert now") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = model::reset) { Text("Reset") }
                        OutlinedButton(onClick = model::favourite) {
                            Icon(if ((model.from to model.to) in model.favourites) Icons.Default.Star else Icons.Default.StarBorder, null)
                            Text(" Favourite")
                        }
                    }
                    if (model.error.isNotEmpty()) Text(model.error, color = MaterialTheme.colorScheme.error)
                    if (model.result.isNotEmpty()) {
                        HorizontalDivider()
                        Text("Result", style = MaterialTheme.typography.labelLarge)
                        Text(model.result, style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Conversion", model.result))
                        }) { Icon(Icons.Default.ContentCopy, null); Text(" Copy result") }
                    }
                }
            }
        }
        item {
            Text("Reference rate", style = MaterialTheme.typography.titleMedium)
            Text("1 " + model.from + " ≈ " + String.format(Locale.US, "%.4f", rates.getValue(model.from) / rates.getValue(model.to)) + " " + model.to)
            Text("Illustrative rates only — no live API", style = MaterialTheme.typography.bodySmall)
        }
        if (model.favourites.isNotEmpty()) {
            item { Text("Favourite pairs", style = MaterialTheme.typography.titleMedium) }
            items(model.favourites.toList()) { pair ->
                OutlinedButton(onClick = { model.from = pair.first; model.to = pair.second; model.result = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(display(pair.first) + " → " + display(pair.second))
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(model: UtilityViewModel) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("History", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = { model.history.clear() }) { Text("Clear all") }
        }
        if (model.history.isEmpty()) Text("No conversions yet.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(model.history.toList()) { entry ->
                ElevatedCard(Modifier.fillMaxWidth()) { Text(entry, Modifier.padding(16.dp)) }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.CurrencyExchange, "App logo", Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text("GlobeConvert", style = MaterialTheme.typography.headlineMedium)
        Text("A currency conversion utility built with Kotlin, Jetpack Compose, Material 3 and ViewModel.")
        Text("Features: currency flags, swap, reset, favourites, copy result, and conversion history.")
        Text("Rates are fixed demonstration values, not live exchange rates. Do not use for financial transactions.")
        Text("History and favourites are kept in memory and reset when the app process ends.")
    }
}
