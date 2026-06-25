package io.github.modhack.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.modhack.input.GestureProcessor
import io.github.modhack.service.MHInputService
import io.github.modhack.ui.theme.LocalKeyboardColors

/**
 * Root composable for the keyboard UI.
 *
 * Renders the candidate strip at the top, followed by each row
 * of keys from the currently loaded [KeyboardLayout]. Handles
 * swipe gestures for hiding the keyboard and switching languages.
 *
 * Properly handles window insets for edge-to-edge display on
 * modern Android devices (API 30+).
 *
 * @param service The input method service providing state and key dispatch.
 */
@Composable
fun KeyboardUI(service: MHInputService) {
    val keyboardState by service.keyboardState.collectAsState()
    val prefs by service.preferences.collectAsState()
    val colors = LocalKeyboardColors.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val gestureProcessor = remember { GestureProcessor() }
    val swipeUpAction = remember(prefs.swipeUp) { gestureProcessor.parseAction(prefs.swipeUp) }
    val swipeDownAction = remember(prefs.swipeDown) { gestureProcessor.parseAction(prefs.swipeDown) }

    val screenHeight = with(density) {
        configuration.screenHeightDp.dp.toPx()
    }
    val keyboardHeightPx = screenHeight * prefs.heightPortrait
    val keyboardHeightDp = with(density) { keyboardHeightPx.toDp().value }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.keyboardBackground)
            // Handle navigation bar insets for edge-to-edge display
            .windowInsetsPadding(WindowInsets.navigationBars)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {},
                    onDragCancel = {},
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val (dx, dy) = dragAmount
                        val threshold = 50f

                        if (kotlin.math.abs(dy) > kotlin.math.abs(dx) && kotlin.math.abs(dy) > threshold) {
                            if (dy > 0) {
                                // Swipe down
                                when (swipeDownAction) {
                                    GestureProcessor.SwipeAction.HIDE -> service.hideWindow()
                                    GestureProcessor.SwipeAction.NEXT_LANGUAGE -> service.switchToNextLanguage()
                                    GestureProcessor.SwipeAction.NONE -> {}
                                }
                            } else {
                                // Swipe up
                                when (swipeUpAction) {
                                    GestureProcessor.SwipeAction.HIDE -> service.hideWindow()
                                    GestureProcessor.SwipeAction.NEXT_LANGUAGE -> service.switchToNextLanguage()
                                    GestureProcessor.SwipeAction.NONE -> {}
                                }
                            }
                        }
                    }
                )
            }
    ) {
        CandidateStripComposable(service)

        keyboardState.layout?.rows?.forEach { row ->
            RowComposable(
                row = row,
                service = service,
                keyboardHeight = keyboardHeightDp
            )
        }
    }
}
