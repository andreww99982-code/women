package com.bloom.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bloom.app.ui.theme.LavenderContainer
import com.bloom.app.ui.theme.MintContainer
import com.bloom.app.ui.theme.PinkPrimaryContainer
import com.bloom.app.ui.theme.TextDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val PREFS_NAME = "bloom_prefs"
private const val KEY_CYCLE_LENGTH = "cycle_length"
private const val KEY_PERIODS = "period_records"
private const val DEFAULT_CYCLE_LENGTH = 28
private const val PREDICTED_PERIOD_DAYS = 5

private data class PeriodRecord(val start: Long, val end: Long? = null)

@Composable
fun CycleScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, 0) }
    var cycleLength by remember { mutableIntStateOf(prefs.getInt(KEY_CYCLE_LENGTH, DEFAULT_CYCLE_LENGTH)) }
    var periods by remember { mutableStateOf(loadPeriods(prefs.getStringSet(KEY_PERIODS, emptySet()))) }
    var calendarMonth by remember { mutableLongStateOf(monthStart(System.currentTimeMillis())) }

    val today = dayStart(System.currentTimeMillis())
    val latestPeriod = periods.maxByOrNull { it.start }
    val cycleStart = latestPeriod?.start ?: today
    val dayOfCycle = (TimeUnit.MILLISECONDS.toDays((today - cycleStart).coerceAtLeast(0)).toInt() % cycleLength) + 1
    val ovulationDay = (cycleLength - 14).coerceIn(8, cycleLength - 8)
    val fertileStart = (ovulationDay - 5).coerceAtLeast(1)
    val fertileEnd = (ovulationDay + 1).coerceAtMost(cycleLength)
    val daysUntilNext = cycleLength - dayOfCycle + 1
    val activePeriod = latestPeriod?.takeIf { it.end == null }

    fun savePeriods(updated: List<PeriodRecord>) {
        periods = updated.sortedByDescending { it.start }
        prefs.edit().putStringSet(KEY_PERIODS, periods.map { "${it.start}|${it.end ?: ""}" }.toSet()).apply()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Календарь цикла", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            if (activePeriod != null) "Менструация идёт с ${formatDate(activePeriod.start)}"
            else if (latestPeriod == null) "Отметьте начало менструации, чтобы вести историю"
            else "День $dayOfCycle из $cycleLength",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PinkPrimaryContainer)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (activePeriod == null) "Начало менструации" else "Менструация продолжается", fontWeight = FontWeight.Bold)
                Text(
                    if (activePeriod == null) "Укажите первый день — он сохранится в истории."
                    else "Первый день: ${formatDate(activePeriod.start)}"
                )
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (activePeriod == null) savePeriods(periods + PeriodRecord(today)) },
                        enabled = activePeriod == null,
                        modifier = Modifier.weight(1f)
                    ) { Text("Начались сегодня") }
                    Button(
                        onClick = {
                            activePeriod?.let { active ->
                                savePeriods(periods.map { if (it.start == active.start) it.copy(end = today) else it })
                            }
                        },
                        enabled = activePeriod != null,
                        modifier = Modifier.weight(1f)
                    ) { Text("Закончились сегодня") }
                }
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = today }
                        DatePickerDialog(context, { _, year, month, day ->
                            val selected = calendarFor(year, month, day)
                            if (activePeriod == null) savePeriods(periods + PeriodRecord(selected))
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    enabled = activePeriod == null,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) { Text("Выбрать дату начала") }
            }
        }

        Spacer(Modifier.height(20.dp))
        CycleCalendar(calendarMonth, periods, cycleStart, cycleLength, fertileStart, fertileEnd, today,
            onPreviousMonth = { calendarMonth = shiftMonth(calendarMonth, -1) },
            onNextMonth = { calendarMonth = shiftMonth(calendarMonth, 1) }
        )

        Spacer(Modifier.height(16.dp))
        if (latestPeriod != null) {
            Text(
                if (daysUntilNext == 1) "Следующий цикл ожидается завтра" else "До следующего цикла: $daysUntilNext дн.",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Фертильное окно", fontWeight = FontWeight.Bold)
                Text("Ориентировочно дни $fertileStart–$fertileEnd. Овуляция — около $ovulationDay-го дня.")
                Text(
                    "Календарь показывает ориентиры и не является методом контрацепции или медицинским прогнозом.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Настройки цикла", fontWeight = FontWeight.Bold)
                Text("Обычная длительность: $cycleLength дней")
                Slider(
                    value = cycleLength.toFloat(),
                    onValueChange = {
                        cycleLength = it.toInt()
                        prefs.edit().putInt(KEY_CYCLE_LENGTH, cycleLength).apply()
                    },
                    valueRange = 21f..35f,
                    steps = 13
                )
            }
        }

        if (periods.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("История менструаций", style = MaterialTheme.typography.titleMedium)
            periods.forEach { period ->
                val end = period.end?.let(::formatDate) ?: "продолжается"
                Text("${formatDate(period.start)} — $end", modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "При задержке, боли или вопросах о здоровье обратитесь к врачу.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CycleCalendar(
    month: Long,
    periods: List<PeriodRecord>,
    cycleStart: Long,
    cycleLength: Int,
    fertileStart: Int,
    fertileEnd: Int,
    today: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = month }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onPreviousMonth) { Text("‹") }
                Text(SimpleDateFormat("LLLL yyyy", Locale("ru")).format(Date(month)).replaceFirstChar { it.titlecase(Locale("ru")) }, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onNextMonth) { Text("›") }
            }
            Row(Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach {
                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                }
            }
            val cells = List(firstDayOffset) { null } + (1..daysInMonth).toList()
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
                            day?.let {
                                val timestamp = calendarFor(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), it)
                                val color = dayColor(timestamp, periods, cycleStart, cycleLength, fertileStart, fertileEnd)
                                Text(
                                    "$it",
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(color)
                                        .then(if (timestamp == today) Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                        .padding(top = 7.dp),
                                    color = if (color == Color.Transparent) MaterialTheme.colorScheme.onSurface else TextDark,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Legend(PinkPrimaryContainer, "Менструация")
                Legend(LavenderContainer, "Фертильные")
                Legend(MintContainer, "Овуляция")
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.size(12.dp).background(color, CircleShape))
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun dayColor(day: Long, periods: List<PeriodRecord>, cycleStart: Long, cycleLength: Int, fertileStart: Int, fertileEnd: Int): Color {
    if (periods.any { day >= it.start && day <= (it.end ?: dayStart(System.currentTimeMillis())) }) return PinkPrimaryContainer
    val cycleDay = Math.floorMod(TimeUnit.MILLISECONDS.toDays(day - cycleStart).toInt(), cycleLength) + 1
    return when {
        cycleDay in 1..PREDICTED_PERIOD_DAYS -> PinkPrimaryContainer.copy(alpha = 0.45f)
        cycleDay == fertileEnd - 1 -> MintContainer
        cycleDay in fertileStart..fertileEnd -> LavenderContainer
        else -> Color.Transparent
    }
}

private fun loadPeriods(values: Set<String>?): List<PeriodRecord> = values.orEmpty().mapNotNull { entry ->
    val parts = entry.split("|", limit = 2)
    parts.firstOrNull()?.toLongOrNull()?.let { start -> PeriodRecord(start, parts.getOrNull(1)?.toLongOrNull()) }
}.sortedByDescending { it.start }

private fun calendarFor(year: Int, month: Int, day: Int): Long = Calendar.getInstance().apply {
    clear()
    set(year, month, day, 12, 0, 0)
}.timeInMillis

private fun dayStart(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calendarFor(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
}

private fun monthStart(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return calendarFor(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1)
}

private fun shiftMonth(timestamp: Long, amount: Int): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    add(Calendar.MONTH, amount)
}.let { monthStart(it.timeInMillis) }

private fun formatDate(timestamp: Long): String = SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date(timestamp))
