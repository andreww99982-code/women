package com.bloom.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val affirmations = listOf(
    "Я достойна любви и уважения просто потому, что я есть.",
    "Сегодня я выбираю быть добрее к себе.",
    "Мои чувства важны и имеют значение.",
    "Я расту и учусь каждый день, и это прекрасно.",
    "Я имею право на отдых без чувства вины.",
    "Моя ценность не зависит от чужого мнения.",
    "Я доверяю себе и своим решениям.",
    "Каждый маленький шаг вперёд — это победа.",
    "Я благодарна себе за то, что не сдаюсь.",
    "Я привлекаю в свою жизнь только хорошее.",
    "Моё тело — мой дом, и я забочусь о нём с любовью.",
    "Я разрешаю себе быть счастливой прямо сейчас.",
    "Я справлюсь с любыми трудностями, ведь я сильная.",
    "Сегодня будет хороший день, и я это заслуживаю.",
    "Я горжусь тем, какой путь уже прошла."
)

@Composable
fun AffirmationsScreen() {
    var currentIndex by remember { mutableIntStateOf(0) }
    var liked by remember { mutableStateOf(false) }

    val heartScale by animateFloatAsState(
        targetValue = if (liked) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heartScale"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Немного вдохновения",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    (slideInHorizontally { it } togetherWith slideOutHorizontally { -it })
                },
                label = "affirmationText"
            ) { index ->
                Text(
                    affirmations[index],
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Нравится",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer(scaleX = heartScale, scaleY = heartScale)
                .clickable { liked = !liked }
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            liked = false
            currentIndex = (currentIndex + 1 + (0 until affirmations.size).random()) % affirmations.size
        }) {
            Text("Новая фраза \u2728")
        }
    }
}
