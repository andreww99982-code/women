package com.bloom.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private const val PREFS_NAME = "bloom_prefs"
private const val KEY_CALORIES = "diet_calories"
private const val KEY_PROTEIN = "diet_protein"
private const val KEY_FAT = "diet_fat"
private const val KEY_CARBS = "diet_carbs"
private const val GOAL_CALORIES = 1800
private const val GOAL_PROTEIN = 90
private const val GOAL_FAT = 60
private const val GOAL_CARBS = 220

private data class Diet(
    val name: String,
    val subtitle: String,
    val description: String,
    val suitableFor: String
)

private val diets = listOf(
    Diet(
        "Средиземноморская",
        "Овощи, рыба и цельные продукты",
        "Основа рациона — овощи, фрукты, бобовые, цельные злаки, орехи, оливковое масло и рыба. Красное мясо и сладости — умеренно.",
        "Сбалансированное питание на каждый день"
    ),
    Diet(
        "DASH",
        "Рацион для поддержки здоровья сердца",
        "Много овощей, фруктов, цельных злаков и нежирных молочных продуктов; меньше соли, насыщенных жиров и сладких напитков.",
        "Тем, кто хочет следить за давлением и качеством рациона"
    ),
    Diet(
        "Вегетарианская",
        "Без мяса и рыбы",
        "Включает овощи, фрукты, бобовые, злаки, орехи, яйца и молочные продукты. Важно разнообразие источников белка и витамина B12.",
        "При осознанном планировании и полноценном составе меню"
    ),
    Diet(
        "Сбалансированная тарелка",
        "Простой способ собирать приём пищи",
        "Половина тарелки — овощи и фрукты, четверть — источник белка, четверть — цельные злаки. Добавьте воду и немного полезных жиров.",
        "Для мягкого изменения привычек без строгих ограничений"
    ),
    Diet(
        "Низкоуглеводная",
        "Меньше быстрых углеводов",
        "Ограничивает сахар, сладкие напитки и рафинированные продукты, делая акцент на белке, овощах и полезных жирах.",
        "Только после оценки индивидуальных потребностей со специалистом"
    )
)

@Composable
fun DietScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Питание", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Считай калории, белки, жиры и углеводы за день, а ниже смотри подходы к питанию.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            item { NutritionTrackerCard() }
            item {
                Spacer(Modifier.height(16.dp))
                Text("Идеи рационов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
            }
            items(diets, key = { it.name }) { diet ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(diet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(diet.subtitle, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(diet.description)
                        Spacer(Modifier.height(8.dp))
                        Text("Подходит: ${diet.suitableFor}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Дневные цели примерные: подбирай их под себя со специалистом. Диета не заменяет консультацию врача или диетолога.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NutritionTrackerCard() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, 0) }
    var calories by remember { mutableStateOf(prefs.getInt(KEY_CALORIES, 0)) }
    var protein by remember { mutableStateOf(prefs.getInt(KEY_PROTEIN, 0)) }
    var fat by remember { mutableStateOf(prefs.getInt(KEY_FAT, 0)) }
    var carbs by remember { mutableStateOf(prefs.getInt(KEY_CARBS, 0)) }
    var mealCalories by remember { mutableStateOf("") }
    var mealProtein by remember { mutableStateOf("") }
    var mealFat by remember { mutableStateOf("") }
    var mealCarbs by remember { mutableStateOf("") }

    fun saveTotals() {
        prefs.edit()
            .putInt(KEY_CALORIES, calories)
            .putInt(KEY_PROTEIN, protein)
            .putInt(KEY_FAT, fat)
            .putInt(KEY_CARBS, carbs)
            .apply()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Счётчик на сегодня", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "КБЖУ — это калории, белки, жиры и углеводы. Добавляй приёмы пищи, чтобы видеть остаток до цели.",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            MetricProgress("Калории", calories, GOAL_CALORIES, "ккал")
            MetricProgress("Белки", protein, GOAL_PROTEIN, "г")
            MetricProgress("Жиры", fat, GOAL_FAT, "г")
            MetricProgress("Углеводы", carbs, GOAL_CARBS, "г")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroInputField("ккал", mealCalories, { mealCalories = it }, Modifier.weight(1f))
                MacroInputField("Б", mealProtein, { mealProtein = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroInputField("Ж", mealFat, { mealFat = it }, Modifier.weight(1f))
                MacroInputField("У", mealCarbs, { mealCarbs = it }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        calories += mealCalories.toIntOrNull() ?: 0
                        protein += mealProtein.toIntOrNull() ?: 0
                        fat += mealFat.toIntOrNull() ?: 0
                        carbs += mealCarbs.toIntOrNull() ?: 0
                        mealCalories = ""
                        mealProtein = ""
                        mealFat = ""
                        mealCarbs = ""
                        saveTotals()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = listOf(mealCalories, mealProtein, mealFat, mealCarbs).any { it.toIntOrNull() != null }
                ) { Text("Добавить") }
                OutlinedButton(
                    onClick = {
                        calories = 0
                        protein = 0
                        fat = 0
                        carbs = 0
                        saveTotals()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Сбросить день") }
            }
        }
    }
}

@Composable
private fun MetricProgress(label: String, value: Int, goal: Int, unit: String) {
    val remaining = (goal - value).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Medium)
            Text("$value/$goal $unit")
        }
        LinearProgressIndicator(
            progress = (value.toFloat() / goal).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (remaining == 0) "Цель достигнута" else "Осталось: $remaining $unit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun MacroInputField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
