package com.xfont.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object ExpressiveMotion {
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
