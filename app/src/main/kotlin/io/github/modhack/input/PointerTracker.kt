package io.github.modhack.input

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange

/**
 * Tracks pointers for multitouch chording.
 *
 * Maintains a map of active pointer IDs to their last known positions,
 * enabling the keyboard to detect multi-finger modifier chording
 * (e.g., holding Ctrl with one finger while tapping a key with another).
 */
class PointerTracker {

    /**
     * Represents the state of a single tracked pointer.
     *
     * @property id The unique pointer ID from the input event.
     * @property position Current position of the pointer.
     * @property keyId The key currently being pressed by this pointer, or -1 if none.
     */
    data class PointerState(
        val id: Int,
        val position: Offset = Offset.Zero,
        val keyId: Int = -1
    )

    private val activePointers = mutableMapOf<Int, PointerState>()

    /** The number of currently active pointers. */
    val pointerCount: Int get() = activePointers.size

    /** All currently active pointer states. */
    val activeStates: List<PointerState> get() = activePointers.values.toList()

    /**
     * Updates the position of a pointer when it moves.
     *
     * @param pointerId The unique identifier for this pointer.
     * @param position The new position of the pointer.
     */
    fun onPointerMove(pointerId: Int, position: Offset) {
        val existing = activePointers[pointerId]
        activePointers[pointerId] = (existing ?: PointerState(pointerId)).copy(position = position)
    }

    /**
     * Records which key a pointer is currently pressing.
     *
     * @param pointerId The unique identifier for this pointer.
     * @param keyId The keycode the pointer is pressing, or -1 if not on any key.
     */
    fun onKeyEnter(pointerId: Int, keyId: Int) {
        val existing = activePointers[pointerId] ?: return
        activePointers[pointerId] = existing.copy(keyId = keyId)
    }

    /**
     * Marks a pointer as newly pressed (down event).
     *
     * @param pointerId The unique identifier for this pointer.
     * @param position The initial position of the pointer.
     */
    fun onPointerDown(pointerId: Int, position: Offset) {
        activePointers[pointerId] = PointerState(pointerId, position)
    }

    /**
     * Removes a pointer from tracking when it is released (up event).
     *
     * @param pointerId The unique identifier for the released pointer.
     * @return The final [PointerState] of the removed pointer, or null if it was not tracked.
     */
    fun onPointerUp(pointerId: Int): PointerState? {
        return activePointers.remove(pointerId)
    }

    /**
     * Returns the key ID being pressed by a specific pointer, or -1 if none.
     *
     * @param pointerId The pointer to query.
     */
    fun getKeyForPointer(pointerId: Int): Int {
        return activePointers[pointerId]?.keyId ?: -1
    }

    /**
     * Returns a list of all key IDs currently being pressed across all pointers.
     */
    fun getAllPressedKeys(): List<Int> {
        return activePointers.values
            .filter { it.keyId != -1 }
            .map { it.keyId }
    }

    /**
     * Checks if a specific key is being pressed by any pointer.
     *
     * @param keyId The key to check.
     */
    fun isKeyPressed(keyId: Int): Boolean {
        return activePointers.values.any { it.keyId == keyId }
    }

    /**
     * Clears all tracked pointers.
     */
    fun clear() {
        activePointers.clear()
    }
}
