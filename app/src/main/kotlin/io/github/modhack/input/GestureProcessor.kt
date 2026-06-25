package io.github.modhack.input

import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

/**
 * Processes gestures (swipes, etc.) detected on the keyboard.
 *
 * Supports:
 * - Swipe down to hide the keyboard
 * - Swipe up to switch to the next language
 * - Configurable via [KeyboardPreferences.swipeUp] and [KeyboardPreferences.swipeDown]
 */
class GestureProcessor {

    /**
     * The action to perform when a swipe gesture is detected.
     */
    enum class SwipeAction {
        /** No action. */
        NONE,
        /** Hide the keyboard. */
        HIDE,
        /** Switch to the next language. */
        NEXT_LANGUAGE
    }

    companion object {
        /** Minimum horizontal distance (in dp) to qualify as a swipe. */
        const val MIN_SWIPE_THRESHOLD = 50f

        /** Maximum vertical movement allowed while still recognizing a horizontal swipe. */
        const val MAX_VERTICAL_TOLERANCE = 100f

        /** Maximum horizontal movement allowed while still recognizing a vertical swipe. */
        const val MAX_HORIZONTAL_TOLERANCE = 100f
    }

    /**
     * Parses a preference string into a [SwipeAction].
     *
     * @param actionStr The preference value ("none", "hide", "next_language").
     * @return The corresponding [SwipeAction].
     */
    fun parseAction(actionStr: String): SwipeAction = when (actionStr) {
        "hide" -> SwipeAction.HIDE
        "next_language" -> SwipeAction.NEXT_LANGUAGE
        else -> SwipeAction.NONE
    }
}
