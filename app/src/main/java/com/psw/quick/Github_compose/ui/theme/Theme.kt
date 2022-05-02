package com.psw.quick.Github_compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val DarkColorPalette = darkColors(
    primary = teal700,
    primaryVariant = tealDark,
    secondary = redDark,
    background = Color.DarkGray,
    surface = Color.DarkGray,
    onPrimary = Color.White,
)

private val LightColorPalette = lightColors(
    primary = teal700,
    primaryVariant = tealDark,
    secondary = red100,
    onPrimary = Color.White,
)

@Composable
fun Colors.headerBackgroundcolor() : Color {
    return if(isSystemInDarkTheme()) Color(0xFF838281) else Color(0xFFA2A0A0)
}

@Composable
fun Colors.headerForegroundcolor() : Color {
    return if(isSystemInDarkTheme()) Color(0xFFB1B1B1) else Color(0xFFFFFFFF)
}

@Composable
fun Colors.bottomNaviBackgroundcolor() : Color {
    return if(isSystemInDarkTheme()) Color(0xFF838281) else Color(0xFFC5C5C5)
}

@Composable
fun Colors.bottomNaviForegroundcolor() : Color {
    return if(isSystemInDarkTheme()) Color(0xFFC5C5C5) else Color(0xFF333333)
}



@Composable
fun GithubComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}