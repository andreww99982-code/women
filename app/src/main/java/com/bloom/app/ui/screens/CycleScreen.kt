package com.bloom.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val PREFS_NAME = "bloom_prefs"
private const val KEY_START_DATE = "cycle_start_date"
private const val KEY_CYCLE_LENGTH = "cycle_length"
private const val DEFAULT_CYCLE_LENGTH = 28

private data class FertilityDay(val day: Int, val probability: String, val note: String)

@Composable
fun CycleScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, 0) }
    var startDate by remember {
        mutableLongStateOf(prefs.getLong(KEY_START_DATE, System.currentTimeMillis()))
    }
    var cycleLength by remember {
        mutableIntStateOf(prefs.getInt(KEY_CYCLE_LENGTH, DEFAULT_CYCLE_LENGTH))
    }

    val daysPassed = TimeUnit.MILLISECONDS.toDays(
        (System.currentTimeMillis() - startDate).coerceAtLeast(0)
    ).toInt()
    val dayOfCycle = (daysPassed % cycleLength) + 1
    val ovulationDay = (cycleLength - 14).coerceIn(8, cycleLength - 8)
    val fertileStart = (ovulationDay - 5).coerceAtLeast(1)
    val fertileEnd = (ovulationDay + 1).coerceAtMost(cycleLength)
    val daysUntilNext = cycleLength - dayOfCycle + 1

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Календарь цикла", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Ориентир для планирования, а не медицинский прогноз",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))

        CycleProgress(dayOfCycle, cycleLength)
        Spacer(Modifier.height(16.dp))
        Text(
            if (daysUntilNext == 1) "Следующий цикл ожидается завтра"
            else "До следующего цикла: $daysUntilNext дн.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Фертильное окно", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Примерно дни $fertileStart–$fertileEnd. Овуляция — около $ovulationDay-го дня.")
                Text(
                    "Вероятность выше всего за 1–2 дня до овуляции. Для защиты используйте контрацепцию.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Ориентировочная вероятность зачатия", style = MaterialTheme.typography.titleMedium)
        Text(
            "Оценка для незащищённого контакта у здоровой пары; индивидуальные значения отличаются.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        fertilityTable(ovulationDay, fertileStart, fertileEnd)

        Spacer(Modifier.height(20.dp))
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
                Text(
                    "Первый день последней менструации: ${formatDate(startDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                startDate = Calendar.getInstance().apply {
                                    set(year, month, day, 12, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                prefs.edit().putLong(KEY_START_DATE, startDate).apply()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Изменить дату")
                }
                Button(
                    onClick = {
                        startDate = System.currentTimeMillis()
                        prefs.edit().putLong(KEY_START_DATE, startDate).apply()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Цикл начался сегодня")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Если цикл нерегулярный, расчёт может быть неточным. При задержке, боли или вопросах о здоровье обратитесь к врачу.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CycleProgress(day: Int, length: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(MaterialTheme.colorScheme.surfaceVariant, -90f, 360f, false, Stroke(stroke, cap = StrokeCap.Round), topLeft, arcSize)
            drawArc(MaterialTheme.colorScheme.primary, -90f, 360f * day / length, false, Stroke(stroke, cap = StrokeCap.Round), topLeft, arcSize)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("День $day", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("из $length", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun fertilityTable(ovulationDay: Int, start: Int, end: Int) {
    val rows = (-5..1).map { offset ->
        val day = ovulationDay + offset
        val probability = when (offset) {
            -5 -> "5%"
            -4 -> "10%"
            -3 -> "15%"
            -2 -> "20%"
            -1 -> "30%"
            0 -> "25%"
            else -> "10%"
        }
        FertilityDay(day, probability, if (day in start..end) "фертильный день" else "ориентир")
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("День ${row.day}: ${row.note}")
                Text(row.probability, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date(timestamp))
