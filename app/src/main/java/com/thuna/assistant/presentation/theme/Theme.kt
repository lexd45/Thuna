package com.thuna.assistant.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1A237E),
    secondary = Color(0xFF3949AB),
    tertiary = Color(0xFF5C6BC0),
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF1A1A1A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A237E),
    secondary = Color(0xFF3949AB),
    tertiary = Color(0xFF5C6BC0),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun ThunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            )
        ),
        content = content
    )
}
