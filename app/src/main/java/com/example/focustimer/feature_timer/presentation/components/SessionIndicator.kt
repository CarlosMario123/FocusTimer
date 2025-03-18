package com.example.focustimer.feature_timer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SessionIndicator(
    totalSessions: Int,
    currentSession: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF6C63FF),
    inactiveColor: Color = Color.Gray.copy(alpha = 0.3f)
) {
    Row(
        modifier = modifier
    ) {
        for (i in 0 until totalSessions) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (i == currentSession) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (i <= currentSession) activeColor else inactiveColor)
            )
        }
    }
}