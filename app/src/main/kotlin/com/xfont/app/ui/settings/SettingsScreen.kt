package com.xfont.app.ui.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xfont.app.manager.FontFileManager
import com.xfont.app.model.ThemeConfig
import com.xfont.app.model.ThemeMode
import com.xfont.app.ui.theme.CuratedPalettes
import com.xfont.app.ui.theme.XFontTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeConfig: ThemeConfig,
    onThemeConfigChange: (ThemeConfig) -> Unit,
    fileManager: FontFileManager
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = XFontTypography.settingsTitle700,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // THEME MODE CARD
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
                            imageVector = Icons.Outlined.SettingsBrightness,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Appearance Theme Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = XFontTypography.settingsTitle700,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeConfig.mode == ThemeMode.SYSTEM,
                            onClick = { onThemeConfigChange(themeConfig.copy(mode = ThemeMode.SYSTEM)) },
                            label = { Text("Auto", fontFamily = XFontTypography.body400) },
                            leadingIcon = {
                                Icon(Icons.Outlined.SettingsBrightness, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeConfig.mode == ThemeMode.LIGHT,
                            onClick = { onThemeConfigChange(themeConfig.copy(mode = ThemeMode.LIGHT)) },
                            label = { Text("Light", fontFamily = XFontTypography.body400) },
                            leadingIcon = {
                                Icon(Icons.Outlined.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = themeConfig.mode == ThemeMode.DARK,
                            onClick = { onThemeConfigChange(themeConfig.copy(mode = ThemeMode.DARK)) },
                            label = { Text("Dark", fontFamily = XFontTypography.body400) },
                            leadingIcon = {
                                Icon(Icons.Outlined.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // AMOLED PITCH BLACK SWITCH
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = "AMOLED Pitch Black",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = XFontTypography.settingsTitle700,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pure #000000 black background in dark mode, keeping cards in Material You tint.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = XFontTypography.body400,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = themeConfig.isAmoled,
                            onCheckedChange = { onThemeConfigChange(themeConfig.copy(isAmoled = it)) }
                        )
                    }
                }
            }

            // MATERIAL YOU & COLOR CARD
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Material You Dynamic Color",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = XFontTypography.settingsTitle700,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Extract accent colors from your wallpaper",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = XFontTypography.body400,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = themeConfig.dynamicColor,
                            onCheckedChange = { checked ->
                                onThemeConfigChange(themeConfig.copy(dynamicColor = checked))
                            }
                        )
                    }

                    // 8 Curated Palettes when Dynamic Color is OFF
                    AnimatedVisibility(
                        visible = !themeConfig.dynamicColor,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Choose Expressive Accent Palette",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = XFontTypography.settingsTitle700,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                itemsIndexed(CuratedPalettes) { index, palette ->
                                    val isSelected = themeConfig.accentIndex == index
                                    val swatchColor = palette.light.primary
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else swatchColor.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onThemeConfigChange(themeConfig.copy(accentIndex = index))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = palette.name,
                                                tint = palette.light.onPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            val activePalette = CuratedPalettes.getOrElse(themeConfig.accentIndex) { CuratedPalettes.first() }
                            Text(
                                text = "Current Palette: ${activePalette.name}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = XFontTypography.body400,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // ABOUT CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "About xFont",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = XFontTypography.settingsTitle700,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "xFont v0.14 · Dedicated Realme UI 6.0 / ColorOS 15 Font Manager & Variable Typography Engine. Pure Material 3 Expressive, no bloat, no advertisements.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = XFontTypography.body400,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
