package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatLineSpacing
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawerContent(
    settingsViewModel: SettingsViewModel,
    onClose: () -> Unit
) {
    val themeIndex by settingsViewModel.themeIndex.collectAsState()
    val fontFamilyIndex by settingsViewModel.fontFamilyIndex.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val lineSpacing by settingsViewModel.lineSpacing.collectAsState()
    val wordSpacing by settingsViewModel.wordSpacing.collectAsState()
    val margins by settingsViewModel.margins.collectAsState()
    val scrollMode by settingsViewModel.scrollMode.collectAsState()

    val fontFamilies = listOf("Sans-Serif", "Serif", "Monospace")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF5BA4A4)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Settings sections
        Column(modifier = Modifier.padding(16.dp)) {

            // THEME
            SettingsHeader(icon = Icons.Default.ColorLens, title = "THEME")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            ) {
                ThemeCircle(color = Color.White, isSelected = themeIndex == 0) { settingsViewModel.setTheme(0) }
                ThemeCircle(color = Color(0xFF1A1A2E), isSelected = themeIndex == 1) { settingsViewModel.setTheme(1) }
                ThemeCircle(color = Color(0xFFF4ECD8), isSelected = themeIndex == 2) { settingsViewModel.setTheme(2) }
                ThemeCircle(color = Color.Black, isSelected = themeIndex == 3) { settingsViewModel.setTheme(3) }
            }

            // FONT FAMILY
            SettingsHeader(icon = Icons.Outlined.TextFormat, title = "FONT FAMILY")
            OutlinedButton(
                onClick = {
                    val nextIndex = (fontFamilyIndex + 1) % fontFamilies.size
                    settingsViewModel.setFontFamily(nextIndex)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(fontFamilies.getOrElse(fontFamilyIndex) { "Sans-Serif" }, fontSize = 16.sp)
                    Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            // FONT SIZE
            SettingsHeader(icon = Icons.Outlined.Straighten, title = "FONT SIZE: ${fontSize.roundToInt()}PX")
            Slider(
                value = fontSize,
                onValueChange = { settingsViewModel.setFontSize(it) },
                valueRange = 10f..40f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // LINE SPACING
            SettingsHeader(icon = Icons.Outlined.FormatLineSpacing, title = "LINE SPACING: ${String.format("%.1f", lineSpacing)}")
            Slider(
                value = lineSpacing,
                onValueChange = { settingsViewModel.setLineSpacing(it) },
                valueRange = 1.0f..3.0f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // WORD SPACING
            SettingsHeader(icon = Icons.Default.SpaceBar, title = "WORD SPACING: ${wordSpacing.roundToInt()}PX")
            Slider(
                value = wordSpacing,
                onValueChange = { settingsViewModel.setWordSpacing(it) },
                valueRange = 0f..20f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // MARGINS
            SettingsHeader(icon = Icons.AutoMirrored.Outlined.MenuBook, title = "MARGINS: ${margins.roundToInt()}PX")
            Slider(
                value = margins,
                onValueChange = { settingsViewModel.setMargins(it) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // READING MODE
            SettingsHeader(icon = Icons.AutoMirrored.Outlined.MenuBook, title = "READING MODE")
            OutlinedButton(
                onClick = { settingsViewModel.setScrollMode(!scrollMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderUtils.border(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = Color(0xFFD2B48C), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (scrollMode) "Scroll Mode" else "Paginated Mode", fontSize = 16.sp)
            }

            // DATA MANAGEMENT
            SettingsHeader(icon = Icons.Outlined.Save, title = "DATA MANAGEMENT")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFFD2691E), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Highlights & Progress")
            }
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFFD2691E), modifier = Modifier.size(20.dp)) // using FileUpload as placeholder
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Data")
            }
            Button(
                onClick = { /* TODO: Hook to clear books in ViewModel */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30), contentColor = Color.White)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Library", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF8A9A9A), // grayish blue/green
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFF8A9A9A),
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ThemeCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFFB8860B) else Color.LightGray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

object BorderUtils {
    @Composable
    fun border(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
}
