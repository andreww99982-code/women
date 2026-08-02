package com.bloom.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import com.bloom.app.data.BloomDatabase
import com.bloom.app.data.JournalEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val moods = listOf("😊", "😍", "😢", "😡", "😴", "🥰", "😌", "😰")

@Composable
fun JournalScreen() {
    val context = LocalContext.current
    val db = remember { BloomDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val entries by db.journalDao().getAll().collectAsState(initial = emptyList())

    var selectedMood by remember { mutableStateOf(moods[0]) }
    var text by remember { mutableStateOf("") }
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(showSaved) {
        if (showSaved) {
            delay(1500)
            showSaved = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Как ты себя чувствуешь?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            moods.forEach { mood ->
                val scale by animateFloatAsState(
                    targetValue = if (mood == selectedMood) 1.25f else 1f,
                    label = "moodScale"
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (mood == selectedMood)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clickable { selectedMood = mood }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(mood, fontSize = 20.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Что у тебя на душе?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                if (text.isNotBlank()) {
                    scope.launch {
                        db.journalDao().insert(
                            JournalEntry(
                                timestamp = System.currentTimeMillis(),
                                mood = selectedMood,
                                text = text
                            )
                        )
                        text = ""
                        showSaved = true
                    }
                }
            }) {
                Text("Сохранить")
            }

            Spacer(Modifier.width(12.dp))

            AnimatedVisibility(visible = showSaved, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    "Сохранено \u2728",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "История",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(entries, key = { it.id }) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(entry.mood, fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.text)
                            Text(
                                formatDate(entry.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { scope.launch { db.journalDao().delete(entry) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale("ru"))
    return format.format(Date(timestamp))
}
