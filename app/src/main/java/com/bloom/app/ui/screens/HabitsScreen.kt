package com.bloom.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloom.app.data.BloomDatabase
import com.bloom.app.data.Habit
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HabitsScreen() {
    val context = LocalContext.current
    val db = remember { BloomDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val habits by db.habitDao().getAll().collectAsState(initial = emptyList())

    var newHabitText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Твои привычки",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newHabitText,
                onValueChange = { newHabitText = it },
                label = { Text("Новая привычка") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (newHabitText.isNotBlank()) {
                    scope.launch {
                        db.habitDao().insert(Habit(title = newHabitText))
                        newHabitText = ""
                    }
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(habits, key = { it.id }) { habit ->
                val doneToday = isSameDay(habit.lastCompletedDate, System.currentTimeMillis())
                val scale by animateFloatAsState(
                    targetValue = if (doneToday) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "checkScale"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (doneToday)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (doneToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Статус",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .clickable {
                                    scope.launch {
                                        val today = System.currentTimeMillis()
                                        if (!doneToday) {
                                            db.habitDao().update(
                                                habit.copy(
                                                    streak = habit.streak + 1,
                                                    lastCompletedDate = today
                                                )
                                            )
                                        }
                                    }
                                }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(habit.title, fontWeight = FontWeight.Medium)
                            AnimatedContent(targetState = habit.streak, label = "streakCount") { streak ->
                                Text(
                                    "Серия: $streak \uD83D\uDD25",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { scope.launch { db.habitDao().delete(habit) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            }
        }
    }
}

private fun isSameDay(timestamp: Long?, other: Long): Boolean {
    if (timestamp == null) return false
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance().apply { timeInMillis = other }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
