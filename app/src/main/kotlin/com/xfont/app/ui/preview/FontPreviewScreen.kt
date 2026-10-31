package com.xfont.app.ui.preview

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xfont.app.engine.FvarParser
import com.xfont.app.manager.FontFileManager
import com.xfont.app.model.FontItem
import com.xfont.app.model.VariableAxis
import com.xfont.app.ui.theme.XFontTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontPreviewScreen(
    fontItem: FontItem,
    fileManager: FontFileManager,
    onBack: () -> Unit,
    onApplyFont: (FontItem, Map<String, Float>) -> Unit
) {
    var detectedAxes by remember { mutableStateOf<List<VariableAxis>>(emptyList()) }
    var fontTypeface by remember { mutableStateOf<Typeface?>(null) }
    var cachedFontFile by remember { mutableStateOf<File?>(null) }
    val axisValues = remember { mutableStateMapOf<String, Float>() }
    val axesScrollState = rememberScrollState()

    LaunchedEffect(fontItem.uri) {
        withContext(Dispatchers.IO) {
            val bytes = fileManager.readFontBytes(fontItem.uri)
            val tf = fileManager.getOrLoadTypefaceAsync(fontItem.uri, fontItem.name)
            val file = fileManager.getCachedFontFile(fontItem.uri, fontItem.name)
            val axes = if (bytes != null) FvarParser.parseAxes(bytes) else emptyList()
            withContext(Dispatchers.Main) {
                fontTypeface = tf
                cachedFontFile = file
                detectedAxes = axes
                axisValues.clear()
                axes.forEach { axis ->
                    axisValues[axis.tag] = axis.defaultValue
                }
            }
        }
    }

    val variationSettings by remember {
        derivedStateOf {
            if (axisValues.isEmpty()) ""
            else axisValues.entries.joinToString(", ") { (tag, value) ->
                "'$tag' ${String.format(Locale.US, "%.1f", value)}"
            }
        }
    }

    val dynamicTypeface = remember(fontTypeface, cachedFontFile, variationSettings) {
        val file = cachedFontFile
        if (file != null && variationSettings.isNotEmpty()) {
            try {
                Typeface.Builder(file).setFontVariationSettings(variationSettings).build() ?: fontTypeface
            } catch (_: Exception) {
                fontTypeface
            }
        } else {
            fontTypeface
        }
    }

    val customFontFamily = remember(dynamicTypeface) {
        if (dynamicTypeface != null) {
            FontFamily(dynamicTypeface)
        } else {
            FontFamily.Default
        }
    }

    val isVariable = detectedAxes.isNotEmpty()
    var isManualCompact by remember { mutableStateOf(false) }

    val defaultPreviewSentence = "The quick brown fox jumps over the lazy dog."
    var customPreviewText by remember(fontItem.uri) { mutableStateOf(defaultPreviewSentence) }

    // Dynamic scroll collapse ratio (0f = top/expanded, 1f = scrolled down/compact)
    val scrollRatio by remember {
        derivedStateOf {
            if (isManualCompact) 1f
            else if (!isVariable) 0f
            else (axesScrollState.value / 80f).coerceIn(0f, 1f)
        }
    }
    val isCompact = scrollRatio > 0.4f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = fontItem.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = XFontTypography.body400,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isVariable) "Variable Typography (${detectedAxes.size} axes)" else "Static Typeface",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = XFontTypography.body400,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // STICKY LIVE PREVIEW CARD (Smoothly collapses as axes are scrolled)
            val dynamicFontSize = (21 - (scrollRatio * 6)).sp
            val dynamicLineHeight = (28 - (scrollRatio * 8)).sp
            val dynamicCardPadding = (18 - (scrollRatio * 10)).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (isCompact) 4.dp else 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompact) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isCompact) 4.dp else 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dynamicCardPadding),
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 14.dp)
                    ) {
                        // Header info in preview card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCompact) "Live Preview" else "Sentence Case (Editable)",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = XFontTypography.previewLabel300,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isVariable) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isCompact) "Compact" else "Expanded",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = XFontTypography.previewLabel300,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { isManualCompact = !isManualCompact },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Tune,
                                            contentDescription = "Toggle compact preview",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Main pangram sentence (Editable & dynamically scales down smoothly with scroll)
                        BasicTextField(
                            value = customPreviewText,
                            onValueChange = { customPreviewText = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = dynamicFontSize,
                                lineHeight = dynamicLineHeight,
                                fontFamily = customFontFamily,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = if (isCompact) 2 else 4
                        )

                        // Secondary rows: Shown only when expanded; completely removed/hidden in compact mode
                        AnimatedVisibility(
                            visible = !isCompact,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Line 2: ALL CAPS Pangram (Instantly tracks custom preview text)
                                Column {
                                    Text(
                                        text = "Uppercase (All Caps)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = XFontTypography.previewLabel300,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val uppercaseText = (if (customPreviewText.isNotBlank()) customPreviewText else defaultPreviewSentence).uppercase()
                                    Text(
                                        text = uppercaseText,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),
                                        fontFamily = customFontFamily,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Emoji row: ONLY shown for static fonts; completely removed in variable mode per request
                                if (!isVariable) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Column {
                                        Text(
                                            text = "Emoji Glyph Support",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = XFontTypography.previewLabel300,
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\uD83D\uDE02 ❤️ \uD83E\uDD7A \uD83D\uDE2D \uD83D\uDE0D ✨ \uD83D\uDD25 \uD83D\uDE0A \uD83D\uDC80 \uD83D\uDC4D \uD83E\uDD73 \uD83D\uDE34 \uD83E\uDD29 \uD83D\uDE44 \uD83D\uDE4F",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp, lineHeight = 30.sp),
                                            fontFamily = customFontFamily
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Line 3: Numbers
                                Column {
                                    Text(
                                        text = "Numerals",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = XFontTypography.previewLabel300,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "0 1 2 3 4 5 6 7 8 9",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
                                        fontFamily = customFontFamily,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SCROLLABLE AREA BELOW (Houses the Variable Tuning Sliders)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(axesScrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // VARIABLE FONT AXES SECTION
                if (isVariable && detectedAxes.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Variable Font Tuning",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = XFontTypography.settingsTitle700,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Adjust axes below. The preview card above updates live as you scroll and tune.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = XFontTypography.body400,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            detectedAxes.forEach { axis ->
                                val currentVal = axisValues[axis.tag] ?: axis.defaultValue
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = axis.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = XFontTypography.previewLabel300,
                                            fontWeight = FontWeight.Light
                                        )
                                        val formattedVal = if (axis.snapStep >= 1.0f) {
                                            String.format(Locale.US, "%.0f", currentVal)
                                        } else {
                                            String.format(Locale.US, "%.1f", currentVal)
                                        }
                                        Text(
                                            text = formattedVal,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    val step = axis.snapStep
                                    Slider(
                                        value = currentVal,
                                        onValueChange = { newVal ->
                                            val snapped = if (step > 0f) {
                                                val stepCount = Math.round((newVal - axis.min) / step)
                                                (axis.min + stepCount * step).coerceIn(axis.min, axis.max)
                                            } else newVal
                                            axisValues[axis.tag] = snapped
                                        },
                                        valueRange = axis.min..axis.max,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // Floating Apply Font Button with subtle shadow for contrast
        Button(
            onClick = { onApplyFont(fontItem, axisValues.toMap()) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Apply Font",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = XFontTypography.button500,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
}
