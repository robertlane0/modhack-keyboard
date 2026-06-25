package io.github.modhack.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalDensity
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
 *
 * @param key The key data to render.
 * @param service The input service for dispatching key actions.
 * @param keyboardHeight Total keyboard height in dp for proportional sizing.
 * @param onLongPress Callback when the key is long-pressed (for popup display).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyComposable(
    key: Key,
    service: MHInputService,
    keyboardHeight: Float = 200f,
    onLongPress: (Key) -> Unit = {}
) {
    val colors = LocalKeyboardColors.current
    val density = LocalDensity.current
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

    val keyWidthDp = with(density) {
        val baseUnit = keyboardHeight / 4.5f
        (key.width * baseUnit / 100).toDp()
    }

    val keyHeightDp = with(density) {
        val baseUnit = keyboardHeight / 5f
        (key.height * baseUnit / 100).toDp()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(keyWidthDp)
            .height(keyHeightDp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .combinedClickable(
                onClick = {
                    isPressed = false
                    service.onKey(key.codes.first())
                },
                onLongClick = {
                    isPressed = false
                    if (key.popupKeys != null) {
                        onLongPress(key)
                    }
                },
                onClickLabel = key.label
            )
    ) {
        if (key.icon != null) {
            Icon(
                imageVector = key.icon,
                contentDescription = key.label,
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
                contentDescription = key.label,
                tint = fgColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = displayLabel ?: "",
                color = fgColor,
                fontSize = with(density) { (prefs.labelScale * 18).sp.value }.sp,
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
