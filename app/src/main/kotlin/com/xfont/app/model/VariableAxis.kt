package com.xfont.app.model

data class VariableAxis(
    val tag: String,
    val min: Float,
    val max: Float,
    val defaultValue: Float,
    var currentValue: Float = defaultValue,
    val label: String = tagToHumanReadable(tag),
    val snapStep: Float = calculateSnapStep(tag, min, max)
) {
    companion object {
        fun calculateSnapStep(tag: String, min: Float, max: Float): Float {
            val range = max - min
            return when {
                tag.equals("wght", ignoreCase = true) -> if (range >= 400f) 50f else 25f
                tag.equals("wdth", ignoreCase = true) -> if (range >= 100f) 5f else 1f
                tag.equals("slnt", ignoreCase = true) || tag.equals("ital", ignoreCase = true) -> 1f
                range <= 1.0f -> 0.1f
                range <= 5.0f -> 0.5f
                range <= 20.0f -> 1.0f
                range <= 100.0f -> 5.0f
                range <= 500.0f -> 10.0f
                else -> 25.0f
            }
        }

        fun tagToHumanReadable(tag: String): String {
            return when (tag.lowercase().trim()) {
                // Registered OpenType Axes
                "wght" -> "Weight"
                "wdth" -> "Width"
                "slnt" -> "Slant"
                "ital" -> "Italic"
                "opsz" -> "Optical Size"
                "grad" -> "Grade"

                // Common Custom / Expressive Axes
                "casl" -> "Casual"
                "crsv" -> "Cursive"
                "fill" -> "Fill"
                "flar" -> "Flare"
                "volm" -> "Volume"
                "soft" -> "Softness / Rounded Corners"
                "wonk" -> "Wonkiness / Quirky"
                "spac" -> "Letter Spacing (Tracking)"
                "term" -> "Terminal Shape"
                "mono" -> "Monospace"
                "blnd" -> "Blend"
                "rond" -> "Roundness"
                "cntr" -> "Stroke Contrast"
                "trap" -> "Ink Trap"
                "warn" -> "Warmth"
                "serf" -> "Serif Shape"
                "bops" -> "Baseline Optical Size"
                "deco" -> "Decorative Detail"
                "curl" -> "Curliness"
                "dots" -> "Dot Size"
                "grid" -> "Grid Alignment"
                "sans" -> "Sans to Serif"
                "shdw" -> "Shadow Depth"
                "extr" -> "Extrusion"
                "anim" -> "Animation Phase"
                "expr" -> "Expression"
                "dist" -> "Distortion"
                "geom" -> "Geometric Factor"
                "shrn" -> "Shrink Factor"
                "hrot" -> "Horizontal Rotation"
                "vrot" -> "Vertical Rotation"
                "inpt" -> "Inline Padding"
                "over" -> "Overshoot"
                "hght" -> "Ascent Height"
                "deep" -> "Descender Depth"
                "size" -> "Glyph Sizing"
                "mask" -> "Mask Inversion"

                // Parametric & Roboto Flex axes
                "xopq" -> "Thick Stroke Width (XOPQ)"
                "yopq", "yopk" -> "Thin Stroke Width (YOPQ)"
                "xtra" -> "Counter Width (XTRA)"
                "ytra" -> "Counter Height (YTRA)"
                "ytlc" -> "Lowercase Height (x-Height)"
                "ytuc" -> "Uppercase Height (Cap Height)"
                "ytas" -> "Ascender Height (YTAS)"
                "ytde" -> "Descender Depth (YTDE)"
                "ytfi" -> "Figure / Number Height (YTFI)"
                "elzn" -> "Element Zero (ELZN)"
                "elzx" -> "Element X (ELZX)"
                "dha " -> "Dot Height"

                else -> "Axis ${tag.uppercase()}"
            }
        }
    }
}
