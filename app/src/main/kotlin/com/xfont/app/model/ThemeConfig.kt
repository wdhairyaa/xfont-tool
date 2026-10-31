package com.xfont.app.model

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val accentIndex: Int = 0,
    val isAmoled: Boolean = false
)

