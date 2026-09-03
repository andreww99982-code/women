package com.bloom.app.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bloom.app.data.ActivityLevel
import com.bloom.app.data.CaloriePlan
import com.bloom.app.data.Diet
import com.bloom.app.data.GoalType
import com.bloom.app.data.NutritionProfile
import com.bloom.app.data.buildCaloriePlan
import com.bloom.app.data.caloriesFromMacros
import com.bloom.app.data.diets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val PREFS_NAME = "bloom_prefs"
private const val KEY_AGE = "diet_age"
private const val KEY_HEIGHT = "diet_height"
private const val KEY_WEIGHT = "diet_weight"
private const val KEY_TARGET_WEIGHT = "diet_target_weight"
private const val KEY_WEEKS = "diet_weeks"
private const val KEY_GOAL = "diet_goal"
private const val KEY_ACTIVITY = "diet_activity"
private const val KEY_MEALS = "diet_meals"
private const val KEY_MEALS_DAY = "diet_meals_day"

private const val FALLBACK_CALORIES = 1800
private const val FALLBACK_PROTEIN = 90
private const val FALLBACK_FAT = 60
private const val FALLBACK_CARBS = 220

private data class Meal(
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int
)

@Composable
fun DietScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, 0) }

    var age by remember { mutableStateOf(prefs.getString(KEY_AGE, "") ?: "") }
    var height by remember { mutableStateOf(prefs.getString(KEY_HEIGHT, "") ?: "") }
    var weight by remember { mutableStateOf(prefs.getString(KEY_WEIGHT, "") ?: "") }
    var targetWeight by remember { mutableStateOf(prefs.getString(KEY_TARGET_WEIGHT, "") ?: "") }
    var weeks by remember { mutableStateOf(prefs.getString(KEY_WEEKS, "") ?: "") }
    var goal by remember {
        mutableStateOf(enumValueOrDefault(prefs.getString(KEY_GOAL, null), GoalType.LOSE))
    }
    var activity by remember {
        mutableStateOf(enumValueOrDefault(prefs.getString(KEY_ACTIVITY, null), ActivityLevel.LIGHT))
    }
    var meals by remember { mutableStateOf(loadMeals(prefs)) }

    fun saveProfile() {
        prefs.edit()
            .putString(KEY_AGE, age)
            .putString(KEY_HEIGHT, height)
            .putString(KEY_WEIGHT, weight)
            .putString(KEY_TARGET_WEIGHT, targetWeight)
            .putString(KEY_WEEKS, weeks)
            .putString(KEY_GOAL, goal.name)
            .putString(KEY_ACTIVITY, activity.name)
            .apply()
    }

    fun saveMeals(updated: List<Meal>) {
        meals = updated
        prefs.edit()
            .putString(KEY_MEALS, updated.joinToString("\n") { meal ->
                "${meal.name.replace('|', ' ').replace('\n', ' ')}|${meal.calories}|${meal.protein}|${meal.fat}|${meal.carbs}"
            })
            .putString(KEY_MEALS_DAY, todayKey())
            .apply()
    }

    val profile = buildProfile(age, height, weight, goal, targetWeight, weeks, activity)
    val plan = profile?.let { buildCaloriePlan(it) }

    val goalCalories = plan?.targetCalories ?: FALLBACK_CALORIES
    val goalProtein = plan?.proteinG ?: FALLBACK_PROTEIN
    val goalFat = plan?.fatG ?: FALLBACK_FAT
    val goalCarbs = plan?.carbsG ?: FALLBACK_CARBS

    val eatenCalories = meals.sumOf { it.calories }
    val eatenProtein = meals.sumOf { it.protein }
    val eatenFat = meals.sumOf { it.fat }
    val eatenCarbs = meals.sumOf { it.carbs }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Питание", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Укажи параметры и цель — приложение посчитает норму калорий и КБЖУ, а затем подскажет подходящую диету.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ProfileCard(
                    age = age, onAgeChange = { age = it.digitsOnly(3); saveProfile() },
                    height = height, onHeightChange = { height = it.digitsOnly(3); saveProfile() },
                    weight = weight, onWeightChange = { weight = it.decimalOnly(); saveProfile() },
                    targetWeight = targetWeight, onTargetWeightChange = { targetWeight = it.decimalOnly(); saveProfile() },
                    weeks = weeks, onWeeksChange = { weeks = it.digitsOnly(3); saveProfile() },
                    goal = goal, onGoalChange = { goal = it; saveProfile() },
                    activity = activity, onActivityChange = { activity = it; saveProfile() }
                )
            }
            item {
                if (plan != null && profile != null) {
                    PlanCard(plan, profile)
                } else {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Расчёт нормы калорий", fontWeight = FontWeight.Bold)
                            Text(
                                "Заполни возраст, рост и вес" +
                                    if (goal == GoalType.MAINTAIN) "" else ", а также желаемый вес и срок в неделях",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                MealDiaryCard(
                    calories = eatenCalories, protein = eatenProtein, fat = eatenFat, carbs = eatenCarbs,
                    goalCalories = goalCalories, goalProtein = goalProtein, goalFat = goalFat, goalCarbs = goalCarbs,
                    hasPlan = plan != null,
                    meals = meals,
                    onAdd = { saveMeals(meals + it) },
                    onRemove = { index -> saveMeals(meals.filterIndexed { i, _ -> i != index }) },
                    onClear = { saveMeals(emptyList()) }
                )
            }
            item {
                Column {
                    Text("Диеты и их нормы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (plan == null) "КБЖУ показаны для ориентировочных $FALLBACK_CALORIES ккал — заполни профиль для личного расчёта."
                        else "КБЖУ рассчитаны на твою норму $goalCalories ккал в день.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(diets.sortedByDescending { goal in it.goals }, key = { it.name }) { diet ->
                DietCard(diet, goalCalories, recommended = goal in diet.goals, goal = goal)
            }
            item {
                Text(
                    "Расчёты ориентировочные: формула Миффлина — Сан Жеора не учитывает состав тела, беременность, " +
                        "лактацию и заболевания. Перед изменением питания посоветуйся с врачом или диетологом.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileCard(
    age: String, onAgeChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    targetWeight: String, onTargetWeightChange: (String) -> Unit,
    weeks: String, onWeeksChange: (String) -> Unit,
    goal: GoalType, onGoalChange: (GoalType) -> Unit,
    activity: ActivityLevel, onActivityChange: (ActivityLevel) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Мои параметры", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Возраст", age, onAgeChange, Modifier.weight(1f), "лет")
                NumberField("Рост", height, onHeightChange, Modifier.weight(1f), "см")
                NumberField("Вес", weight, onWeightChange, Modifier.weight(1f), "кг", decimal = true)
            }
            Text("Цель", fontWeight = FontWeight.Medium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalType.entries.forEach { item ->
                    FilterChip(selected = goal == item, onClick = { onGoalChange(item) }, label = { Text(item.title) })
                }
            }
            if (goal != GoalType.MAINTAIN) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Желаемый вес", targetWeight, onTargetWeightChange, Modifier.weight(1f), "кг", decimal = true)
                    NumberField("Срок", weeks, onWeeksChange, Modifier.weight(1f), "недель")
                }
            }
            Text("Активность", fontWeight = FontWeight.Medium)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.entries.forEach { item ->
                    FilterChip(selected = activity == item, onClick = { onActivityChange(item) }, label = { Text(item.title) })
                }
            }
            Text(activity.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlanCard(plan: CaloriePlan, profile: NutritionProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Твоя норма", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${plan.targetCalories} ккал в день", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                when {
                    plan.dailyDelta > 0 -> "Профицит +${plan.dailyDelta} ккал к норме поддержания ${plan.maintenance} ккал"
                    plan.dailyDelta < 0 -> "Дефицит ${plan.dailyDelta} ккал к норме поддержания ${plan.maintenance} ккал"
                    else -> "Норма поддержания веса: ${plan.maintenance} ккал"
                }
            )
            Text("Белки ${plan.proteinG} г · жиры ${plan.fatG} г · углеводы ${plan.carbsG} г")
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text("Основной обмен (BMR): ${plan.bmr} ккал")
            Text("ИМТ: ${plan.bmi.round1()} — ${plan.bmiCategory}")
            Text("Здоровый вес при твоём росте: ${plan.healthyWeightMin.round1()}–${plan.healthyWeightMax.round1()} кг")
            Text("Вода: около ${plan.waterMl} мл в день")
            if (profile.goal != GoalType.MAINTAIN) {
                val change = abs(plan.weeklyChangeKg).round1()
                val direction = if (plan.weeklyChangeKg < 0) "снижение" else "набор"
                Text("Темп: $direction около $change кг в неделю")
                plan.realisticWeeks?.let {
                    val diff = abs(profile.targetWeightKg - profile.weightKg).round1()
                    Text("Реальный срок для $diff кг: примерно $it нед.")
                }
            }
            plan.notes.forEach { note ->
                Text("• $note", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Совет: подойдут диеты с пометкой «Под твою цель» ниже.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MealDiaryCard(
    calories: Int, protein: Int, fat: Int, carbs: Int,
    goalCalories: Int, goalProtein: Int, goalFat: Int, goalCarbs: Int,
    hasPlan: Boolean,
    meals: List<Meal>,
    onAdd: (Meal) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mealCalories by remember { mutableStateOf("") }
    var mealProtein by remember { mutableStateOf("") }
    var mealFat by remember { mutableStateOf("") }
    var mealCarbs by remember { mutableStateOf("") }

    val protein0 = mealProtein.toIntOrNull() ?: 0
    val fat0 = mealFat.toIntOrNull() ?: 0
    val carbs0 = mealCarbs.toIntOrNull() ?: 0
    val autoCalories = caloriesFromMacros(protein0, fat0, carbs0)
    val enteredCalories = mealCalories.toIntOrNull()
    val resultCalories = enteredCalories ?: autoCalories
    val canAdd = resultCalories > 0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Дневник за сегодня", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                if (hasPlan) "Цели взяты из твоего расчёта. Записывай приёмы пищи — остаток посчитается сам."
                else "Пока показаны ориентировочные цели: заполни профиль выше для личного расчёта.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MetricProgress("Калории", calories, goalCalories, "ккал")
            MetricProgress("Белки", protein, goalProtein, "г")
            MetricProgress("Жиры", fat, goalFat, "г")
            MetricProgress("Углеводы", carbs, goalCarbs, "г")

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Что съела") },
                placeholder = { Text("Например: овсянка с ягодами") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Белки", mealProtein, { mealProtein = it.digitsOnly(4) }, Modifier.weight(1f), "г")
                NumberField("Жиры", mealFat, { mealFat = it.digitsOnly(4) }, Modifier.weight(1f), "г")
                NumberField("Углеводы", mealCarbs, { mealCarbs = it.digitsOnly(4) }, Modifier.weight(1f), "г")
            }
            NumberField(
                "Калории", mealCalories, { mealCalories = it.digitsOnly(4) }, Modifier.fillMaxWidth(),
                if (enteredCalories == null && autoCalories > 0) "посчитаем сами: $autoCalories ккал" else "ккал"
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onAdd(
                            Meal(
                                name = name.ifBlank { "Приём пищи" },
                                calories = resultCalories,
                                protein = protein0,
                                fat = fat0,
                                carbs = carbs0
                            )
                        )
                        name = ""
                        mealCalories = ""
                        mealProtein = ""
                        mealFat = ""
                        mealCarbs = ""
                    },
                    enabled = canAdd,
                    modifier = Modifier.weight(1f)
                ) { Text("Добавить") }
                OutlinedButton(onClick = onClear, enabled = meals.isNotEmpty(), modifier = Modifier.weight(1f)) {
                    Text("Очистить день")
                }
            }
            if (meals.isNotEmpty()) {
                HorizontalDivider()
                meals.forEachIndexed { index, meal ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(meal.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${meal.calories} ккал · Б ${meal.protein} · Ж ${meal.fat} · У ${meal.carbs}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onRemove(index) }) { Text("Удалить") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DietCard(diet: Diet, calories: Int, recommended: Boolean, goal: GoalType) {
    val macros = diet.macrosFor(calories)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (recommended) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (recommended) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(diet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (recommended) {
                    Text("Под твою цель: ${goal.title.lowercase(Locale("ru"))}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(diet.subtitle, style = MaterialTheme.typography.bodySmall)
            Text(diet.description)
            Text(
                "Норма на день: ${macros.calories} ккал · Б ${macros.proteinG} г · Ж ${macros.fatG} г · У ${macros.carbsG} г " +
                    "(${diet.proteinPercent}/${diet.fatPercent}/${diet.carbsPercent}%)",
                fontWeight = FontWeight.Medium
            )
            Text("Правила диеты:", fontWeight = FontWeight.Medium)
            diet.rules.forEach { rule -> Text("• $rule", style = MaterialTheme.typography.bodyMedium) }
            Text("Подходит: ${diet.suitableFor}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetricProgress(label: String, value: Int, goal: Int, unit: String) {
    val remaining = (goal - value).coerceAtLeast(0)
    val over = (value - goal).coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Medium)
            Text("$value/$goal $unit")
        }
        LinearProgressIndicator(
            progress = { if (goal <= 0) 0f else (value.toFloat() / goal).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            when {
                over > 0 -> "Превышение: $over $unit"
                remaining == 0 -> "Цель достигнута"
                else -> "Осталось: $remaining $unit"
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    suffix: String? = null,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = { if (suffix != null) Text(suffix, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier
    )
}

private fun buildProfile(
    age: String,
    height: String,
    weight: String,
    goal: GoalType,
    targetWeight: String,
    weeks: String,
    activity: ActivityLevel
): NutritionProfile? {
    val ageValue = age.toIntOrNull() ?: return null
    val heightValue = height.toIntOrNull() ?: return null
    val weightValue = weight.replace(',', '.').toDoubleOrNull() ?: return null
    val targetValue = if (goal == GoalType.MAINTAIN) weightValue
    else targetWeight.replace(',', '.').toDoubleOrNull() ?: return null
    val weeksValue = if (goal == GoalType.MAINTAIN) 1 else weeks.toIntOrNull() ?: return null
    if (goal == GoalType.LOSE && targetValue >= weightValue) return null
    if (goal == GoalType.GAIN && targetValue <= weightValue) return null
    val profile = NutritionProfile(
        age = ageValue,
        heightCm = heightValue,
        weightKg = weightValue,
        goal = goal,
        targetWeightKg = targetValue,
        weeks = weeksValue,
        activity = activity
    )
    return profile.takeIf { it.isValid }
}

private fun loadMeals(prefs: SharedPreferences): List<Meal> {
    if (prefs.getString(KEY_MEALS_DAY, null) != todayKey()) return emptyList()
    return prefs.getString(KEY_MEALS, "").orEmpty().lineSequence().mapNotNull { line ->
        val parts = line.split("|")
        if (parts.size != 5) return@mapNotNull null
        Meal(
            name = parts[0],
            calories = parts[1].toIntOrNull() ?: return@mapNotNull null,
            protein = parts[2].toIntOrNull() ?: 0,
            fat = parts[3].toIntOrNull() ?: 0,
            carbs = parts[4].toIntOrNull() ?: 0
        )
    }.toList()
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default

private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun String.digitsOnly(max: Int): String = filter { it.isDigit() }.take(max)

private fun String.decimalOnly(): String {
    val cleaned = replace(',', '.').filter { it.isDigit() || it == '.' }
    val firstDot = cleaned.indexOf('.')
    val normalized = if (firstDot < 0) cleaned
    else cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "")
    return normalized.take(6)
}

private fun Double.round1(): String = ((this * 10).roundToInt() / 10.0).toString()
