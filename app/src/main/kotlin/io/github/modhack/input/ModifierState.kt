package io.github.modhack.input

import io.github.modhack.model.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thread-safe singleton that tracks the currently active modifier keys.
 *
 * Exposes a [StateFlow] so that the Compose UI can reactively update
 * key appearances (e.g. highlighting pressed modifiers).
 */
object ModifierState {
    private val _activeModifiers = MutableStateFlow(emptySet<Modifier>())
    
    /** Flow of currently active modifiers. */
    val activeModifiers: StateFlow<Set<Modifier>> = _activeModifiers.asStateFlow()

    /**
     * Presses (activates) the given [modifier].
     */
    fun press(modifier: Modifier) {
        _activeModifiers.value = _activeModifiers.value + modifier
    }

    /**
     * Releases (deactivates) the given [modifier].
     */
    fun release(modifier: Modifier) {
        _activeModifiers.value = _activeModifiers.value - modifier
    }

    /**
     * Toggles the state of the given [modifier].
     */
    fun toggle(modifier: Modifier) {
        val current = _activeModifiers.value
        _activeModifiers.value = if (current.contains(modifier)) {
            current - modifier
        } else {
            current + modifier
        }
    }

    /**
     * Checks if the given [modifier] is currently active.
     */
    fun isActive(modifier: Modifier): Boolean {
        return _activeModifiers.value.contains(modifier)
    }

    /**
     * Clears all active modifiers.
     */
    fun clearAll() {
        _activeModifiers.value = emptySet()
    }

    /**
     * Combines the [android.view.KeyEvent] meta flags of all currently
     * active modifiers. This is useful for constructing synthetic key events.
     */
    fun getMetaState(): Int {
        var metaState = 0
        for (modifier in _activeModifiers.value) {
            metaState = metaState or modifier.metaFlag
        }
        return metaState
    }
}
