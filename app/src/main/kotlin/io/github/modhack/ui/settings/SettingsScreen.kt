package io.github.modhack.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Settings screen composable.
 *
 * Displays all preference sections from §13.2:
 * - Appearance (theme, label scale)
 * - Layout (portrait/landscape mode, height)
 * - Behavior (auto-cap, shift lock, ConnectBot hack)
 * - Input (swipe actions, long press duration)
 * - Feedback (vibration, sound, popup)
 * - Text Correction (quick fixes, auto-complete, recorrection)
 *
 * @param onBack Callback when the back button is pressed.
 * @param viewModel The [SettingsViewModel] providing preference state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val prefs by viewModel.preferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ModHack Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Appearance Section ──────────────────────────────────
            SectionHeader("Appearance")

            ThemePreference(
                currentTheme = prefs.theme,
                onThemeSelected = viewModel::updateTheme
            )

            SliderPreference(
                label = "Key Label Scale",
                value = prefs.labelScale,
                valueRange = 0.5f..2.0f,
                onValueChange = viewModel::updateLabelScale
            )

            // ── Layout Section ──────────────────────────────────────
            SectionHeader("Layout")

            DropdownPreference(
                label = "Portrait Layout",
                currentValue = prefs.modePortrait,
                options = listOf(
                    "qwerty" to "4-Row QWERTY",
                    "full" to "5-Row Full (F-keys)",
                    "full_fn" to "5-Row Full (Fn-keys)",
                    "compact" to "Compact"
                ),
                onOptionSelected = viewModel::updateModePortrait
            )

            DropdownPreference(
                label = "Landscape Layout",
                currentValue = prefs.modeLandscape,
                options = listOf(
                    "qwerty" to "4-Row QWERTY",
                    "full" to "5-Row Full (F-keys)",
                    "full_fn" to "5-Row Full (Fn-keys)",
                    "compact" to "Compact"
                ),
                onOptionSelected = viewModel::updateModeLandscape
            )

            SliderPreference(
                label = "Keyboard Height (Portrait)",
                value = prefs.heightPortrait,
                valueRange = 0.15f..0.75f,
                onValueChange = viewModel::updateHeightPortrait
            )

            SliderPreference(
                label = "Keyboard Height (Landscape)",
                value = prefs.heightLandscape,
                valueRange = 0.15f..0.75f,
                onValueChange = viewModel::updateHeightLandscape
            )

            // ── Behavior Section ────────────────────────────────────
            SectionHeader("Behavior")

            SwitchPreference(
                label = "Auto-capitalize",
                checked = prefs.autoCap,
                onCheckedChange = viewModel::updateAutoCap
            )

            SwitchPreference(
                label = "Shift Lock Modifiers",
                summary = "Shift lock also locks Ctrl/Alt/Meta",
                checked = prefs.shiftLockModifiers,
                onCheckedChange = viewModel::updateShiftLockModifiers
            )

            SwitchPreference(
                label = "ConnectBot Tab Hack",
                summary = "Send Ctrl+I instead of Tab",
                checked = prefs.connectbotTabHack,
                onCheckedChange = viewModel::updateConnectbotTabHack
            )

            // ── Input Section ───────────────────────────────────────
            SectionHeader("Input")

            DropdownPreference(
                label = "Swipe Up Action",
                currentValue = prefs.swipeUp,
                options = listOf(
                    "none" to "None",
                    "hide" to "Hide Keyboard",
                    "next_language" to "Next Language"
                ),
                onOptionSelected = viewModel::updateSwipeUp
            )

            DropdownPreference(
                label = "Swipe Down Action",
                currentValue = prefs.swipeDown,
                options = listOf(
                    "none" to "None",
                    "hide" to "Hide Keyboard",
                    "next_language" to "Next Language"
                ),
                onOptionSelected = viewModel::updateSwipeDown
            )

            SliderPreference(
                label = "Long Press Duration",
                value = prefs.longPressDuration.toFloat(),
                valueRange = 100f..800f,
                steps = 6,
                onValueChange = { viewModel.updateLongPressDuration(it.toInt()) }
            )

            // ── Feedback Section ────────────────────────────────────
            SectionHeader("Feedback")

            SwitchPreference(
                label = "Haptic Feedback",
                checked = prefs.vibrateOn,
                onCheckedChange = viewModel::updateVibrateOn
            )

            SliderPreference(
                label = "Vibration Duration",
                value = prefs.vibrateLen.toFloat(),
                valueRange = 10f..200f,
                steps = 8,
                onValueChange = { viewModel.updateVibrateLen(it.toInt()) }
            )

            SwitchPreference(
                label = "Sound on Keypress",
                checked = prefs.soundOn,
                onCheckedChange = viewModel::updateSoundOn
            )

            SwitchPreference(
                label = "Key Preview Popup",
                checked = prefs.popupOn,
                onCheckedChange = viewModel::updatePopupOn
            )

            // ── Text Correction Section ─────────────────────────────
            SectionHeader("Text Correction")

            SwitchPreference(
                label = "Quick Fixes",
                checked = prefs.quickFixes,
                onCheckedChange = viewModel::updateQuickFixes
            )

            SwitchPreference(
                label = "Auto-complete",
                checked = prefs.autoComplete,
                onCheckedChange = viewModel::updateAutoComplete
            )

            SwitchPreference(
                label = "Recorrection",
                checked = prefs.recorrection,
                onCheckedChange = viewModel::updateRecorrection
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section header with distinct styling.
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * A switch toggle preference with optional summary.
 */
@Composable
private fun SwitchPreference(
    label: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (summary != null) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * A slider preference with current value display.
 */
@Composable
private fun SliderPreference(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A dropdown preference using radio buttons shown in an expandable section.
 */
@Composable
private fun DropdownPreference(
    label: String,
    currentValue: String,
    options: List<Pair<String, String>>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = options.firstOrNull { it.first == currentValue }?.second ?: currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                options.forEach { (key, displayName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = key == currentValue,
                                onClick = {
                                    onOptionSelected(key)
                                    expanded = false
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = key == currentValue,
                            onClick = {
                                onOptionSelected(key)
                                expanded = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Theme selector using radio buttons.
 */
@Composable
private fun ThemePreference(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        "material_you" to "Material You",
        "material_dark" to "Material Dark",
        "material_light" to "Material Light",
        "amoled_black" to "AMOLED Black",
        "neon_tribute" to "Neon Tribute",
        "ics_tribute" to "ICS Tribute"
    )

    DropdownPreference(
        label = "Theme",
        currentValue = currentTheme,
        options = themes,
        onOptionSelected = onThemeSelected
    )
}
