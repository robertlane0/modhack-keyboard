package io.github.modhack.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.modhack.model.Key
import io.github.modhack.model.Modifier as ModelModifier
import io.github.modhack.model.ShiftState
import io.github.modhack.service.MHInputService
import io.github.modhack.ui.theme.LocalKeyboardColors

/**
 * Renders a single key with full interaction support.
 *
 * Features:
 * - Tap to commit the key's primary character
 * - Long-press to show popup keys
 * - Visual distinction for modifier keys (Shift, Ctrl, Alt, Meta, Fn)
 * - Pressed state highlighting
 * - Key hint text (e.g., numbers on alpha keys)
 * - Icon support for special keys (backspace, enter, etc.)
 * - Full accessibility support with content descriptions for TalkBack
 *
 * @param key The key data to render.
 * @param service The input service for dispatching key actions.
 * @param keyHeight Row height in dp used to scale the key vertically.
 * @param rowDefaultHeight Abstract height unit of the parent row.
 * @param onLongPress Callback when the key is long-pressed (for popup display).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyComposable(
    key: Key,
    service: MHInputService,
    keyHeight: Float = 70f,
    rowDefaultHeight: Int = 10,
    onLongPress: (Key) -> Unit = {}
) {
    val colors = LocalKeyboardColors.current
    val prefs by service.preferences.collectAsState()
    val keyboardState by service.keyboardState.collectAsState()

    var isPressed by remember { mutableStateOf(false) }

    val isShifted = keyboardState.shiftState == ShiftState.ON ||
            keyboardState.shiftState == ShiftState.LOCKED
    val displayLabel = when {
        key.icon != null -> null
        key.iconResId != 0 -> null
        key.isModifier -> key.label
        isShifted && key.shiftLabel.isNotEmpty() -> key.shiftLabel
        else -> key.label
    }

    val isModifierActive = key.isModifier && ModelModifier.fromKeyCode(key.codes.firstOrNull() ?: 0)
        ?.let { keyboardState.activeModifiers.contains(it) } == true

    val bgColor = when {
        isPressed -> colors.keyPressedBackground
        isModifierActive -> colors.keyPressedBackground
        key.isModifier -> colors.modifierKeyBackground
        else -> colors.keyBackground
    }

    val fgColor = when {
        isModifierActive -> colors.modifierKeyForeground
        key.isModifier -> colors.modifierKeyForeground
        else -> colors.keyForeground
    }

    val keyHeightDp = (keyHeight * key.height / rowDefaultHeight).dp

    // Build accessible content description
    val contentDescription = buildKeyContentDescription(key, isShifted, isModifierActive)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(keyHeightDp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .semantics {
                this.contentDescription = contentDescription
            }
            .combinedClickable(
                onClick = {
                    isPressed = false
                    val code = key.codes.firstOrNull() ?: return@combinedClickable
                    service.onKey(code)
                },
                onLongClick = {
                    isPressed = false
                    if (key.popupKeys != null) {
                        onLongPress(key)
                    }
                },
                onClickLabel = contentDescription
            )
    ) {
        if (key.icon != null) {
            Icon(
                imageVector = key.icon,
                contentDescription = contentDescription,
                tint = fgColor,
                modifier = Modifier.size(24.dp)
            )
        } else if (key.iconResId != 0) {
            Icon(
                imageVector = ImageVector.Builder(
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f
                ).build(),
                contentDescription = contentDescription,
                tint = fgColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = displayLabel ?: "",
                color = fgColor,
                fontSize = (prefs.labelScale * 18).sp,
                fontWeight = if (key.isModifier) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }

        // Hint text in bottom-right corner
        if (key.hint != null && !key.isModifier && key.icon == null && key.iconResId == 0) {
            Text(
                text = key.hint,
                color = fgColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 2.dp)
            )
        }
    }
}

/**
 * Builds an accessible content description for a key.
 *
 * Provides meaningful descriptions for TalkBack users, including:
 * - Modifier state (e.g., "Shift active")
 * - Key function (e.g., "Backspace", "Enter")
 * - Popup availability (e.g., "a, long press for accented characters")
 *
 * @param key The key to describe.
 * @param isShifted Whether shift is currently active.
 * @param isModifierActive Whether this modifier key is currently active.
 * @return A descriptive string for accessibility services.
 */
private fun buildKeyContentDescription(
    key: Key,
    isShifted: Boolean,
    isModifierActive: Boolean
): String {
    return when {
        // Special keys with specific functions
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.DELETE -> "Backspace"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.ENTER -> "Enter"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.SPACE -> "Space"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.TAB -> "Tab"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.ESCAPE -> "Escape"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.SYMBOL -> "Symbols keyboard"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.NEXT_LANGUAGE -> "Switch language"
        key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.SETTINGS -> "Open settings"

        // Modifier keys with state
        key.isModifier && key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.SHIFT -> {
            if (isModifierActive) "Shift active" else "Shift"
        }
        key.isModifier && key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.CTRL_LEFT -> {
            if (isModifierActive) "Control active" else "Control"
        }
        key.isModifier && key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.ALT_LEFT -> {
            if (isModifierActive) "Alt active" else "Alt"
        }
        key.isModifier && key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.META_LEFT -> {
            if (isModifierActive) "Meta active" else "Meta"
        }
        key.isModifier && key.codes.firstOrNull() == io.github.modhack.keycodes.KeyCodes.FN -> {
            if (isModifierActive) "Function active" else "Function"
        }

        // Regular character keys
        key.label.isNotEmpty() && key.popupKeys != null -> {
            val shiftedLabel = if (isShifted && key.shiftLabel.isNotEmpty()) key.shiftLabel else key.label
            "$shiftedLabel, long press for alternatives"
        }

        key.label.isNotEmpty() -> {
            if (isShifted && key.shiftLabel.isNotEmpty()) key.shiftLabel else key.label
        }

        // Fallback
        else -> "Key"
    }
}
