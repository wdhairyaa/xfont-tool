package com.xfont.app.ui.theme

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily

object XFontTypography {
    private var isInitialized = false

    // Weight 800 with ROND max for app title "xFont Manager"
    var title800: FontFamily = FontFamily.Default
        private set

    // Weight 700 with ROND max for settings title & headers
    var settingsTitle700: FontFamily = FontFamily.Default
        private set

    // Weight 500 with ROND max for apply font button
    var button500: FontFamily = FontFamily.Default
        private set

    // Weight 400 with ROND max for navbar text, searchbar, breadcrumbs, settings description & preview top title
    var body400: FontFamily = FontFamily.Default
        private set

    // Weight 300 with ROND max for preview card labels ("Sentence Case", "Uppercase", etc.)
    var previewLabel300: FontFamily = FontFamily.Default
        private set

    fun init(context: Context) {
        if (isInitialized) return
        try {
            title800 = createGoogleSansFlex(context, weight = 800)
            settingsTitle700 = createGoogleSansFlex(context, weight = 700)
            button500 = createGoogleSansFlex(context, weight = 500)
            body400 = createGoogleSansFlex(context, weight = 400)
            previewLabel300 = createGoogleSansFlex(context, weight = 300)
            isInitialized = true
        } catch (_: Exception) {
            // Fallback gracefully to default system font
        }
    }

    private fun createGoogleSansFlex(context: Context, weight: Int): FontFamily {
        return try {
            val tf = Typeface.Builder(context.assets, "fonts/google_sans_flex.ttf")
                .setFontVariationSettings("'ROND' 100, 'wght' $weight")
                .build()
            if (tf != null) FontFamily(tf) else FontFamily.Default
        } catch (_: Exception) {
            FontFamily.Default
        }
    }
}
