package com.xfont.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class ExpressivePalette(
    val name: String,
    val light: ColorScheme,
    val dark: ColorScheme
)

private fun createLightPalette(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    surface: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    surfaceContainer: Color,
    surfaceContainerHigh: Color,
    outline: Color
): ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = surface,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainer = surfaceContainer,
        surfaceContainerLow = surface,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHigh,
        outline = outline,
        outlineVariant = surfaceVariant
    )
}

private fun createDarkPalette(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    surface: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    surfaceContainer: Color,
    surfaceContainerHigh: Color,
    outline: Color
): ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = surface,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainer = surfaceContainer,
        surfaceContainerLow = surface,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHigh,
        outline = outline,
        outlineVariant = surfaceVariant
    )
}

val CuratedPalettes = listOf(
    // 0: Vivid Violet
    ExpressivePalette(
        name = "Vivid Violet",
        light = createLightPalette(
            primary = Color(0xFF7C4DFF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEDE7F6),
            onPrimaryContainer = Color(0xFF311B92),
            secondary = Color(0xFF7E57C2),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFEADDFF),
            onSecondaryContainer = Color(0xFF21005D),
            surface = Color(0xFFFCFAFF),
            onSurface = Color(0xFF1D1B20),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurfaceVariant = Color(0xFF49454F),
            surfaceContainer = Color(0xFFF3EDF7),
            surfaceContainerHigh = Color(0xFFECE6F0),
            outline = Color(0xFF79747E)
        ),
        dark = createDarkPalette(
            primary = Color(0xFFB388FF),
            onPrimary = Color(0xFF311B92),
            primaryContainer = Color(0xFF651FFF),
            onPrimaryContainer = Color(0xFFEDE7F6),
            secondary = Color(0xFFD1C4E9),
            onSecondary = Color(0xFF311B92),
            secondaryContainer = Color(0xFF4F378B),
            onSecondaryContainer = Color(0xFFEADDFF),
            surface = Color(0xFF141218),
            onSurface = Color(0xFFE6E0E9),
            surfaceVariant = Color(0xFF49454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            surfaceContainer = Color(0xFF211F26),
            surfaceContainerHigh = Color(0xFF2B2930),
            outline = Color(0xFF938F99)
        )
    ),

    // 1: Royal Indigo
    ExpressivePalette(
        name = "Royal Indigo",
        light = createLightPalette(
            primary = Color(0xFF3D5AFE),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8EAF6),
            onPrimaryContainer = Color(0xFF1A237E),
            secondary = Color(0xFF536DFE),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFDEE0FF),
            onSecondaryContainer = Color(0xFF00105C),
            surface = Color(0xFFFBF8FD),
            onSurface = Color(0xFF1B1B1F),
            surfaceVariant = Color(0xFFE3E1EC),
            onSurfaceVariant = Color(0xFF46464F),
            surfaceContainer = Color(0xFFF0EEF6),
            surfaceContainerHigh = Color(0xFFEAE8F0),
            outline = Color(0xFF767680)
        ),
        dark = createDarkPalette(
            primary = Color(0xFF8C9EFF),
            onPrimary = Color(0xFF1A237E),
            primaryContainer = Color(0xFF304FFE),
            onPrimaryContainer = Color(0xFFE8EAF6),
            secondary = Color(0xFFC5CAE9),
            onSecondary = Color(0xFF283593),
            secondaryContainer = Color(0xFF3D5AFE),
            onSecondaryContainer = Color(0xFFE8EAF6),
            surface = Color(0xFF131318),
            onSurface = Color(0xFFE4E1E6),
            surfaceVariant = Color(0xFF46464F),
            onSurfaceVariant = Color(0xFFC7C5D0),
            surfaceContainer = Color(0xFF1F1F25),
            surfaceContainerHigh = Color(0xFF2A2A30),
            outline = Color(0xFF90909A)
        )
    ),

    // 2: Ocean Cyan
    ExpressivePalette(
        name = "Ocean Cyan",
        light = createLightPalette(
            primary = Color(0xFF0091EA),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE1F5FE),
            onPrimaryContainer = Color(0xFF01579B),
            secondary = Color(0xFF00B0FF),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFB3E5FC),
            onSecondaryContainer = Color(0xFF004D40),
            surface = Color(0xFFF6FAFD),
            onSurface = Color(0xFF171C1F),
            surfaceVariant = Color(0xFFDDE3EA),
            onSurfaceVariant = Color(0xFF41474D),
            surfaceContainer = Color(0xFFEBF2F6),
            surfaceContainerHigh = Color(0xFFE3ECF0),
            outline = Color(0xFF71787E)
        ),
        dark = createDarkPalette(
            primary = Color(0xFF80D8FF),
            onPrimary = Color(0xFF01579B),
            primaryContainer = Color(0xFF0091EA),
            onPrimaryContainer = Color(0xFFE1F5FE),
            secondary = Color(0xFFB2EBF2),
            onSecondary = Color(0xFF006064),
            secondaryContainer = Color(0xFF00838F),
            onSecondaryContainer = Color(0xFFE0F7FA),
            surface = Color(0xFF0F1417),
            onSurface = Color(0xFFDEE3E6),
            surfaceVariant = Color(0xFF41474D),
            onSurfaceVariant = Color(0xFFC1C7CE),
            surfaceContainer = Color(0xFF1B2023),
            surfaceContainerHigh = Color(0xFF252B2E),
            outline = Color(0xFF8B9198)
        )
    ),

    // 3: Radiant Emerald
    ExpressivePalette(
        name = "Radiant Emerald",
        light = createLightPalette(
            primary = Color(0xFF00C853),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8F5E9),
            onPrimaryContainer = Color(0xFF1B5E20),
            secondary = Color(0xFF00E676),
            onSecondary = Color(0xFF1B5E20),
            secondaryContainer = Color(0xFFC8E6C9),
            onSecondaryContainer = Color(0xFF004D40),
            surface = Color(0xFFF6FAF5),
            onSurface = Color(0xFF171D18),
            surfaceVariant = Color(0xFFDCE5DC),
            onSurfaceVariant = Color(0xFF414942),
            surfaceContainer = Color(0xFFEBF3EA),
            surfaceContainerHigh = Color(0xFFE3ECE2),
            outline = Color(0xFF717972)
        ),
        dark = createDarkPalette(
            primary = Color(0xFF69F0AE),
            onPrimary = Color(0xFF1B5E20),
            primaryContainer = Color(0xFF00BFA5),
            onPrimaryContainer = Color(0xFFE8F5E9),
            secondary = Color(0xFFA7FFEB),
            onSecondary = Color(0xFF004D40),
            secondaryContainer = Color(0xFF00796B),
            onSecondaryContainer = Color(0xFFE0F2F1),
            surface = Color(0xFF0F1511),
            onSurface = Color(0xFFDFE4DD),
            surfaceVariant = Color(0xFF414942),
            onSurfaceVariant = Color(0xFFC0C9C0),
            surfaceContainer = Color(0xFF1A211C),
            surfaceContainerHigh = Color(0xFF242C26),
            outline = Color(0xFF8A938B)
        )
    ),

    // 4: Golden Amber
    ExpressivePalette(
        name = "Golden Amber",
        light = createLightPalette(
            primary = Color(0xFFFFAB00),
            onPrimary = Color(0xFF2E1C00),
            primaryContainer = Color(0xFFFFF8E1),
            onPrimaryContainer = Color(0xFFFF6F00),
            secondary = Color(0xFFFFD600),
            onSecondary = Color(0xFF261D00),
            secondaryContainer = Color(0xFFFFF9C4),
            onSecondaryContainer = Color(0xFF241A00),
            surface = Color(0xFFFCF9F2),
            onSurface = Color(0xFF1D1B16),
            surfaceVariant = Color(0xFFE8E2D4),
            onSurfaceVariant = Color(0xFF49463D),
            surfaceContainer = Color(0xFFF4EEE1),
            surfaceContainerHigh = Color(0xFFECE6D9),
            outline = Color(0xFF7B776C)
        ),
        dark = createDarkPalette(
            primary = Color(0xFFFFD54F),
            onPrimary = Color(0xFF3E2723),
            primaryContainer = Color(0xFFFF6D00),
            onPrimaryContainer = Color(0xFFFFF8E1),
            secondary = Color(0xFFFFF59D),
            onSecondary = Color(0xFF332A00),
            secondaryContainer = Color(0xFFFF8F00),
            onSecondaryContainer = Color(0xFFFFF8E1),
            surface = Color(0xFF141310),
            onSurface = Color(0xFFE7E2DA),
            surfaceVariant = Color(0xFF49463D),
            onSurfaceVariant = Color(0xFFCCC5B8),
            surfaceContainer = Color(0xFF22201B),
            surfaceContainerHigh = Color(0xFF2C2A24),
            outline = Color(0xFF969083)
        )
    ),

    // 5: Sunset Coral
    ExpressivePalette(
        name = "Sunset Coral",
        light = createLightPalette(
            primary = Color(0xFFFF3D00),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFBE9E7),
            onPrimaryContainer = Color(0xFFBF360C),
            secondary = Color(0xFFFF6E40),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFFFCCBC),
            onSecondaryContainer = Color(0xFF4E1D13),
            surface = Color(0xFFFCF8F6),
            onSurface = Color(0xFF1C1B1A),
            surfaceVariant = Color(0xFFE8E0DC),
            onSurfaceVariant = Color(0xFF4A4542),
            surfaceContainer = Color(0xFFF4ECE8),
            surfaceContainerHigh = Color(0xFFECE4E0),
            outline = Color(0xFF7B7571)
        ),
        dark = createDarkPalette(
            primary = Color(0xFFFF8A80),
            onPrimary = Color(0xFFBF360C),
            primaryContainer = Color(0xFFDD2C00),
            onPrimaryContainer = Color(0xFFFBE9E7),
            secondary = Color(0xFFFFCCBC),
            onSecondary = Color(0xFF5D1005),
            secondaryContainer = Color(0xFFBF360C),
            onSecondaryContainer = Color(0xFFFFEBE5),
            surface = Color(0xFF151211),
            onSurface = Color(0xFFE8E1DE),
            surfaceVariant = Color(0xFF4A4542),
            onSurfaceVariant = Color(0xFFCCC4BF),
            surfaceContainer = Color(0xFF221E1C),
            surfaceContainerHigh = Color(0xFF2D2826),
            outline = Color(0xFF968E8A)
        )
    ),

    // 6: Deep Rose
    ExpressivePalette(
        name = "Deep Rose",
        light = createLightPalette(
            primary = Color(0xFFF50057),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFCE4EC),
            onPrimaryContainer = Color(0xFF880E4F),
            secondary = Color(0xFFFF4081),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF8BBD0),
            onSecondaryContainer = Color(0xFF560027),
            surface = Color(0xFFFCF7F9),
            onSurface = Color(0xFF1E1A1C),
            surfaceVariant = Color(0xFFEAE0E4),
            onSurfaceVariant = Color(0xFF4B4448),
            surfaceContainer = Color(0xFFF5ECF0),
            surfaceContainerHigh = Color(0xFFEDE4E8),
            outline = Color(0xFF7D7478)
        ),
        dark = createDarkPalette(
            primary = Color(0xFFFF80AB),
            onPrimary = Color(0xFF880E4F),
            primaryContainer = Color(0xFFC51162),
            onPrimaryContainer = Color(0xFFFCE4EC),
            secondary = Color(0xFFF8BBD0),
            onSecondary = Color(0xFF560027),
            secondaryContainer = Color(0xFF880E4F),
            onSecondaryContainer = Color(0xFFFCE4EC),
            surface = Color(0xFF181113),
            onSurface = Color(0xFFEEDFE1),
            surfaceVariant = Color(0xFF514347),
            onSurfaceVariant = Color(0xFFD5C2C6),
            surfaceContainer = Color(0xFF251D1F),
            surfaceContainerHigh = Color(0xFF30282A),
            outline = Color(0xFF9E8C90)
        )
    ),

    // 7: Electric Teal
    ExpressivePalette(
        name = "Electric Teal",
        light = createLightPalette(
            primary = Color(0xFF00BFA5),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE0F2F1),
            onPrimaryContainer = Color(0xFF004D40),
            secondary = Color(0xFF1DE9B6),
            onSecondary = Color(0xFF004D40),
            secondaryContainer = Color(0xFFB2DFDB),
            onSecondaryContainer = Color(0xFF00251A),
            surface = Color(0xFFF4FAF8),
            onSurface = Color(0xFF161C1B),
            surfaceVariant = Color(0xFFDBE5E2),
            onSurfaceVariant = Color(0xFF3F4947),
            surfaceContainer = Color(0xFFE9F3F0),
            surfaceContainerHigh = Color(0xFFE1EBE8),
            outline = Color(0xFF6F7977)
        ),
        dark = createDarkPalette(
            primary = Color(0xFFA7FFEB),
            onPrimary = Color(0xFF004D40),
            primaryContainer = Color(0xFF00796B),
            onPrimaryContainer = Color(0xFFE0F2F1),
            secondary = Color(0xFF80CBC4),
            onSecondary = Color(0xFF004D40),
            secondaryContainer = Color(0xFF00695C),
            onSecondaryContainer = Color(0xFFE0F2F1),
            surface = Color(0xFF0E1514),
            onSurface = Color(0xFFDDE4E2),
            surfaceVariant = Color(0xFF3F4947),
            onSurfaceVariant = Color(0xFFBFC9C6),
            surfaceContainer = Color(0xFF192120),
            surfaceContainerHigh = Color(0xFF242C2B),
            outline = Color(0xFF899390)
        )
    )
)
