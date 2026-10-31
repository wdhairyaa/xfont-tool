package com.xfont.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

object ExpressiveIconShapes {

    /**
     * Creates a scalloped/petal flower shape (Cookie / Clover / Flower / Sunny / Burst)
     * with [numPetals], [innerRatio], and smoothly connected lobes.
     */
    class ScallopedShape(
        private val numPetals: Int,
        private val innerRatio: Float = 0.82f
    ) : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = minOf(cx, cy)
            val innerR = outerR * innerRatio
            val totalPoints = numPetals * 2
            val angleStep = (2.0 * Math.PI) / totalPoints

            for (i in 0 until totalPoints) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = i * angleStep - Math.PI / 2.0
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevAngle = (i - 1) * angleStep - Math.PI / 2.0
                    val prevR = if ((i - 1) % 2 == 0) outerR else innerR
                    val px = cx + (prevR * cos(prevAngle)).toFloat()
                    val py = cy + (prevR * sin(prevAngle)).toFloat()
                    val midX = (px + x) / 2f
                    val midY = (py + y) / 2f
                    path.quadraticTo(px, py, midX, midY)
                }
            }
            path.close()
            return Outline.Generic(path)
        }
    }

    val shapes: List<Shape> = listOf(
        // Rounded Polygons & Squircles
        RoundedCornerShape(12.dp), // Squircle
        RoundedCornerShape(18.dp), // Soft Pill Squircle
        CircleShape, // Circle
        CutCornerShape(percent = 28), // Diamond
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp), // Arch
        RoundedCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 6.dp, bottomStart = 6.dp), // Slanted
        CutCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 6.dp, bottomEnd = 6.dp), // Gem

        // Scalloped Cookies, Clovers, Sunny, Bursts & Flowers
        ScallopedShape(numPetals = 4, innerRatio = 0.80f), // Clover 4-Leaf
        ScallopedShape(numPetals = 6, innerRatio = 0.83f), // Cookie 6-Sided
        ScallopedShape(numPetals = 8, innerRatio = 0.85f), // Sunny 8-Point
        ScallopedShape(numPetals = 12, innerRatio = 0.88f), // Soft Burst / Cookie 12-Sided
        ScallopedShape(numPetals = 5, innerRatio = 0.80f), // Flower 5-Petal
        ScallopedShape(numPetals = 7, innerRatio = 0.84f), // Cookie 7-Sided
        ScallopedShape(numPetals = 9, innerRatio = 0.87f), // Cookie 9-Sided
        ScallopedShape(numPetals = 8, innerRatio = 0.76f), // Burst
        ScallopedShape(numPetals = 6, innerRatio = 0.75f)  // Boom
    )

    @Composable
    fun rememberRandomShape(): Shape = remember { shapes.random() }
}
