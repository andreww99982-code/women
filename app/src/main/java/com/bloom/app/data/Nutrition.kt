package com.bloom.app.data

import kotlin.math.abs
import kotlin.math.roundToInt

/** Уровень физической активности и коэффициент к базовому обмену. */
enum class ActivityLevel(val title: String, val hint: String, val factor: Double) {
    LOW("Минимальная", "сидячая работа, почти нет тренировок", 1.2),
    LIGHT("Лёгкая", "прогулки, 1–3 тренировки в неделю", 1.375),
    MEDIUM("Средняя", "3–5 тренировок в неделю", 1.55),
    HIGH("Высокая", "6–7 тренировок в неделю", 1.725),
    VERY_HIGH("Очень высокая", "тяжёлый физический труд, спорт дважды в день", 1.9)
}

/** Цель пользователя по весу. */
enum class GoalType(val title: String) {
    LOSE("Сбросить вес"),
    MAINTAIN("Удержать вес"),
    GAIN("Набрать вес")
}

/** Килокалории в одном килограмме массы тела (приблизительно). */
private const val KCAL_PER_KG = 7700.0

/** Минимально безопасная калорийность для женщины без наблюдения специалиста. */
const val MIN_SAFE_CALORIES = 1200

/** Максимальный дефицит и профицит относительно суточной нормы. */
private const val MAX_DEFICIT_RATIO = 0.25
private const val MAX_SURPLUS_RATIO = 0.20

/** Дефицит и профицит в абсолютных значениях, если процент от нормы получается совсем маленьким. */
private const val MAX_DEFICIT_KCAL = 500.0
private const val MAX_SURPLUS_KCAL = 500.0

/** Рекомендуемый безопасный темп изменения веса, кг в неделю. */
private const val MAX_LOSS_PER_WEEK = 1.0
private const val MAX_GAIN_PER_WEEK = 0.5

data class NutritionProfile(
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val goal: GoalType,
    val targetWeightKg: Double,
    val weeks: Int,
    val activity: ActivityLevel
) {
    val isValid: Boolean
        get() = age in 14..99 && heightCm in 120..220 && weightKg in 30.0..250.0 &&
            (goal == GoalType.MAINTAIN || (targetWeightKg in 30.0..250.0 && weeks in 1..104))
}

data class CaloriePlan(
    val bmr: Int,
    val maintenance: Int,
    val targetCalories: Int,
    val dailyDelta: Int,
    val weeklyChangeKg: Double,
    val realisticWeeks: Int?,
    val bmi: Double,
    val bmiCategory: String,
    val healthyWeightMin: Double,
    val healthyWeightMax: Double,
    val waterMl: Int,
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int,
    val notes: List<String>
)

data class DietMacros(
    val calories: Int,
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int
)

data class Diet(
    val name: String,
    val subtitle: String,
    val description: String,
    val suitableFor: String,
    val rules: List<String>,
    val proteinPercent: Int,
    val fatPercent: Int,
    val carbsPercent: Int,
    val goals: Set<GoalType>,
    val minCalories: Int = MIN_SAFE_CALORIES
) {
    /** Раскладка КБЖУ конкретной диеты под рассчитанную норму калорий. */
    fun macrosFor(calories: Int): DietMacros {
        val safeCalories = calories.coerceAtLeast(minCalories)
        return DietMacros(
            calories = safeCalories,
            proteinG = (safeCalories * proteinPercent / 100.0 / 4).roundToInt(),
            fatG = (safeCalories * fatPercent / 100.0 / 9).roundToInt(),
            carbsG = (safeCalories * carbsPercent / 100.0 / 4).roundToInt()
        )
    }
}

val diets = listOf(
    Diet(
        name = "Средиземноморская",
        subtitle = "Овощи, рыба и цельные продукты",
        description = "Основа рациона — овощи, фрукты, бобовые, цельные злаки, орехи, оливковое масло и рыба. Красное мясо и сладости — умеренно.",
        suitableFor = "Сбалансированное питание на каждый день",
        rules = listOf(
            "Овощи и фрукты — не меньше 5 порций в день",
            "Рыба или морепродукты — 2–3 раза в неделю",
            "Оливковое масло вместо сливочного, орехи — горсть в день",
            "Красное мясо — не чаще 1–2 раз в неделю, сладости — по праздникам"
        ),
        proteinPercent = 20,
        fatPercent = 35,
        carbsPercent = 45,
        goals = setOf(GoalType.LOSE, GoalType.MAINTAIN, GoalType.GAIN)
    ),
    Diet(
        name = "DASH",
        subtitle = "Рацион для поддержки здоровья сердца",
        description = "Много овощей, фруктов, цельных злаков и нежирных молочных продуктов; меньше соли, насыщенных жиров и сладких напитков.",
        suitableFor = "Тем, кто хочет следить за давлением и качеством рациона",
        rules = listOf(
            "Соль — до 5–6 г в день (примерно 1 чайная ложка)",
            "Овощи и фрукты — 8–10 порций в день",
            "Нежирные молочные продукты — 2–3 порции в день",
            "Сладкие напитки и десерты — не чаще 5 раз в неделю"
        ),
        proteinPercent = 18,
        fatPercent = 27,
        carbsPercent = 55,
        goals = setOf(GoalType.LOSE, GoalType.MAINTAIN)
    ),
    Diet(
        name = "Вегетарианская",
        subtitle = "Без мяса и рыбы",
        description = "Включает овощи, фрукты, бобовые, злаки, орехи, яйца и молочные продукты. Важно разнообразие источников белка и витамина B12.",
        suitableFor = "При осознанном планировании и полноценном составе меню",
        rules = listOf(
            "Белок в каждый приём пищи: бобовые, тофу, яйца, творог",
            "Сочетайте бобовые со злаками, чтобы получить полный аминокислотный профиль",
            "Следите за B12, железом, цинком и омега-3 — обсудите добавки с врачом",
            "Цельные злаки вместо рафинированных"
        ),
        proteinPercent = 18,
        fatPercent = 30,
        carbsPercent = 52,
        goals = setOf(GoalType.MAINTAIN, GoalType.GAIN)
    ),
    Diet(
        name = "Сбалансированная тарелка",
        subtitle = "Простой способ собирать приём пищи",
        description = "Половина тарелки — овощи и фрукты, четверть — источник белка, четверть — цельные злаки. Добавьте воду и немного полезных жиров.",
        suitableFor = "Для мягкого изменения привычек без строгих ограничений",
        rules = listOf(
            "½ тарелки — овощи и зелень, ¼ — белок, ¼ — цельные злаки",
            "3 основных приёма пищи и 1–2 перекуса",
            "Вода вместо сладких напитков",
            "Не пропускайте завтрак и не оставляйте перерывы дольше 5 часов"
        ),
        proteinPercent = 20,
        fatPercent = 30,
        carbsPercent = 50,
        goals = setOf(GoalType.LOSE, GoalType.MAINTAIN, GoalType.GAIN)
    ),
    Diet(
        name = "Низкоуглеводная",
        subtitle = "Меньше быстрых углеводов",
        description = "Ограничивает сахар, сладкие напитки и рафинированные продукты, делая акцент на белке, овощах и полезных жирах.",
        suitableFor = "Только после оценки индивидуальных потребностей со специалистом",
        rules = listOf(
            "Углеводы — примерно 25–30% калорий, в основном овощи и ягоды",
            "Белок в каждый приём пищи, жиры — из рыбы, орехов и масла",
            "Без сахара, сладких напитков и белой муки",
            "Не подходит при беременности и ряде заболеваний — нужна консультация врача"
        ),
        proteinPercent = 30,
        fatPercent = 40,
        carbsPercent = 30,
        goals = setOf(GoalType.LOSE)
    ),
    Diet(
        name = "Белково-силовая",
        subtitle = "Поддержка мышц при наборе веса",
        description = "Повышенная доля белка и регулярные приёмы пищи вместе с силовыми тренировками помогают набирать вес за счёт мышц, а не только жира.",
        suitableFor = "Набор веса и работа над формой вместе с тренировками",
        rules = listOf(
            "Белок 1,6–2,2 г на кг веса, распределённый на 4–5 приёмов пищи",
            "Углеводы вокруг тренировки для энергии и восстановления",
            "Плотные по калориям продукты: орехи, авокадо, крупы, творог",
            "Силовые тренировки 3–4 раза в неделю и сон 7–9 часов"
        ),
        proteinPercent = 25,
        fatPercent = 28,
        carbsPercent = 47,
        goals = setOf(GoalType.GAIN, GoalType.MAINTAIN)
    )
)

/** Расчёт основного обмена по формуле Миффлина — Сан Жеора (для женщин). */
fun basalMetabolicRate(weightKg: Double, heightCm: Int, age: Int): Double =
    10 * weightKg + 6.25 * heightCm - 5 * age - 161

fun bodyMassIndex(weightKg: Double, heightCm: Int): Double {
    val meters = heightCm / 100.0
    return weightKg / (meters * meters)
}

fun bmiCategory(bmi: Double): String = when {
    bmi < 18.5 -> "недостаточный вес"
    bmi < 25 -> "норма"
    bmi < 30 -> "избыточный вес"
    else -> "ожирение"
}

/**
 * Считает норму калорий и КБЖУ под цель пользователя.
 * Темп изменения веса ограничивается безопасными значениями,
 * а срок пересчитывается, если исходный слишком агрессивный.
 */
fun buildCaloriePlan(profile: NutritionProfile): CaloriePlan {
    val bmr = basalMetabolicRate(profile.weightKg, profile.heightCm, profile.age)
    val maintenance = bmr * profile.activity.factor
    val notes = mutableListOf<String>()

    val requestedDelta = when (profile.goal) {
        GoalType.MAINTAIN -> 0.0
        else -> (profile.targetWeightKg - profile.weightKg) * KCAL_PER_KG / (profile.weeks * 7.0)
    }

    val lowerBound = -maxOf(maintenance * MAX_DEFICIT_RATIO, MAX_DEFICIT_KCAL)
    val upperBound = maxOf(maintenance * MAX_SURPLUS_RATIO, MAX_SURPLUS_KCAL)
    var dailyDelta = requestedDelta.coerceIn(lowerBound, upperBound)

    var target = maintenance + dailyDelta
    if (target < MIN_SAFE_CALORIES) {
        target = MIN_SAFE_CALORIES.toDouble()
        dailyDelta = target - maintenance
    }
    if (abs(requestedDelta - dailyDelta) > 50) {
        notes += "Выбранный темп слишком резкий, план пересчитан на безопасный."
    }

    val weeklyChangeKg = dailyDelta * 7 / KCAL_PER_KG
    val realisticWeeks = if (profile.goal == GoalType.MAINTAIN || abs(weeklyChangeKg) < 0.01) {
        null
    } else {
        val weeks = abs(profile.targetWeightKg - profile.weightKg) / abs(weeklyChangeKg)
        weeks.roundToInt().coerceAtLeast(1)
    }

    when (profile.goal) {
        GoalType.LOSE -> if (abs(weeklyChangeKg) > MAX_LOSS_PER_WEEK) {
            notes += "Безопасный темп снижения веса — до 1 кг в неделю."
        }
        GoalType.GAIN -> if (weeklyChangeKg > MAX_GAIN_PER_WEEK) {
            notes += "Набирать вес спокойнее — около 0,5 кг в неделю, так растёт больше мышц."
        }
        GoalType.MAINTAIN -> Unit
    }
    if (target <= MIN_SAFE_CALORIES) {
        notes += "Питание ниже $MIN_SAFE_CALORIES ккал в день возможно только под наблюдением врача."
    }

    val bmi = bodyMassIndex(profile.weightKg, profile.heightCm)
    val meters = profile.heightCm / 100.0
    val targetCalories = target.roundToInt()

    // Базовая раскладка: белок 1,6 г/кг, жиры 30% калорий, остальное — углеводы.
    val proteinG = (profile.weightKg * 1.6).roundToInt()
    val fatG = (targetCalories * 0.3 / 9).roundToInt()
    val carbsG = ((targetCalories - proteinG * 4 - fatG * 9) / 4.0).roundToInt().coerceAtLeast(0)

    return CaloriePlan(
        bmr = bmr.roundToInt(),
        maintenance = maintenance.roundToInt(),
        targetCalories = targetCalories,
        dailyDelta = dailyDelta.roundToInt(),
        weeklyChangeKg = weeklyChangeKg,
        realisticWeeks = realisticWeeks,
        bmi = bmi,
        bmiCategory = bmiCategory(bmi),
        healthyWeightMin = 18.5 * meters * meters,
        healthyWeightMax = 24.9 * meters * meters,
        waterMl = (profile.weightKg * 30).roundToInt(),
        proteinG = proteinG,
        fatG = fatG,
        carbsG = carbsG,
        notes = notes
    )
}

/** Диеты, которые подходят под выбранную цель. */
fun dietsForGoal(goal: GoalType): List<Diet> = diets.filter { goal in it.goals }

/** Калорийность приёма пищи по белкам, жирам и углеводам. */
fun caloriesFromMacros(proteinG: Int, fatG: Int, carbsG: Int): Int = proteinG * 4 + fatG * 9 + carbsG * 4
