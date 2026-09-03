package com.bloom.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            "Популярные подходы к питанию — выбери тот, что подходит твоему образу жизни.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn {
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
                    "Диета не заменяет консультацию врача или диетолога. При беременности, хронических заболеваниях или расстройствах пищевого поведения обсуди рацион со специалистом.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
