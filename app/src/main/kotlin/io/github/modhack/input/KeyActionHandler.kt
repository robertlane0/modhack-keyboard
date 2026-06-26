package io.github.modhack.input

import android.view.KeyEvent
import io.github.modhack.keycodes.KeyCodes
import io.github.modhack.model.Modifier
import io.github.modhack.model.ShiftState
import io.github.modhack.service.InputConnectionManager
import io.github.modhack.service.MHInputService

/**
 * Handles routing and execution of actions when a key is pressed.
 *
 * Supports:
 * - Character input with shift/capitalization
 * - Modifier chording (Ctrl+C, Alt+Tab, etc.)
 * - One-shot shift (auto-release after next character)
 * - Navigation, function keys, and special keys (Globe, Escape, Tab)
 * - Backspace with word composer integration
 */
class KeyActionHandler(
    private val service: MHInputService,
    private val inputConnectionManager: InputConnectionManager,
    private val wordComposer: WordComposer
) {
    /**
     * Tracks whether shift should auto-release after the next character (one-shot shift).
     */
    private var shiftOneShot = false

    /** Whether shift is in one-shot mode (auto-release after next key). */
    val isShiftOneShot: Boolean get() = shiftOneShot

    /**
     * Main entry point for key dispatch.
     *
     * @param primaryCode The main keycode emitted by the pressed key.
     */
    fun onKey(primaryCode: Int) {
        when {
            primaryCode == KeyCodes.SHIFT -> handleShift()
            primaryCode == KeyCodes.DELETE -> handleBackspace()
            primaryCode == KeyCodes.SPACE -> {
                wordComposer.reset()
                inputConnectionManager.commitText(" ")
                releaseOneShotShift()
            }
            primaryCode == KeyCodes.ENTER -> {
                wordComposer.reset()
                inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                releaseOneShotShift()
            }
            primaryCode == KeyCodes.TAB -> {
                sendTab()
                releaseOneShotShift()
            }
            primaryCode == KeyCodes.ESCAPE -> {
                sendEscape()
                releaseOneShotShift()
            }
            primaryCode == KeyCodes.SYMBOL -> handleSymbolSwitch()
            primaryCode == KeyCodes.NEXT_LANGUAGE -> handleNextLanguage()
            primaryCode == KeyCodes.PREV_LANGUAGE -> handleNextLanguage()
            primaryCode == KeyCodes.F1 -> handleSpecialKey(KeyEvent.KEYCODE_F1)
            primaryCode == KeyCodes.SETTINGS -> handleSettings()
            KeyCodes.isModifier(primaryCode) -> {
                Modifier.fromKeyCode(primaryCode)?.let { handleModifier(it) }
            }
            KeyCodes.isNavigation(primaryCode) -> {
                handleNavigation(primaryCode)
            }
            KeyCodes.isFunctionKey(primaryCode) -> {
                handleFunctionKey(primaryCode)
            }
            primaryCode > 0 -> {
                handleCharacter(primaryCode)
            }
        }
    }

    private fun handleCharacter(code: Int) {
        val metaState = ModifierState.getMetaState()
        val hasNonShiftModifier = metaState != 0 && metaState != KeyEvent.META_SHIFT_ON

        if (hasNonShiftModifier) {
            inputConnectionManager.sendModifiedKeyEvent(code, ModifierState.activeModifiers.value)
        } else {
            val isShifted = ModifierState.isActive(Modifier.SHIFT)
            val char = if (isShifted) code.toChar().uppercaseChar() else code.toChar()
            
            wordComposer.addKeyCode(char.code)
            inputConnectionManager.commitText(char.toString())
        }
        releaseOneShotShift()
    }

    private fun handleBackspace() {
        if (!wordComposer.isEmpty()) {
            wordComposer.deleteLastCode()
            inputConnectionManager.deleteSurroundingText(1, 0)
        } else {
            inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        }
    }

    private fun handleShift() {
        val currentShift = ModifierState.isActive(Modifier.SHIFT)
        if (currentShift && !shiftOneShot) {
            // Caps lock is engaged — release it
            ModifierState.release(Modifier.SHIFT)
            shiftOneShot = false
        } else if (!currentShift) {
            // Turn on shift in one-shot mode (auto-release after next key)
            ModifierState.press(Modifier.SHIFT)
            shiftOneShot = true
        } else {
            // shiftOneShot is true — pressing shift again locks it
            shiftOneShot = false
        }
    }

    /**
     * Enables one-shot shift (auto-release after next character).
     * Used by auto-cap and other external callers.
     */
    fun enableShiftOneShot() {
        ModifierState.press(Modifier.SHIFT)
        shiftOneShot = true
    }

    /**
     * Releases one-shot shift if active (after the next character is typed).
     */
    private fun releaseOneShotShift() {
        if (shiftOneShot) {
            shiftOneShot = false
            ModifierState.release(Modifier.SHIFT)
        }
    }

    private fun handleModifier(modifier: Modifier) {
        ModifierState.toggle(modifier)
    }

    private fun handleNextLanguage() {
        service.switchToNextLanguage()
    }

    private fun handleSpecialKey(androidKeyCode: Int) {
        inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, androidKeyCode))
        inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, androidKeyCode))
    }

    private fun handleSettings() {
        // Settings launch handled by the service or activity starter
    }

    private fun sendTab() {
        inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
        inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
    }

    private fun sendEscape() {
        inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
        inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE))
    }

    private fun handleNavigation(code: Int) {
        val androidCode = KeyCodes.toAndroidKeyCode(code) ?: return
        inputConnectionManager.sendModifiedKeyEvent(androidCode, ModifierState.activeModifiers.value)
    }

    private fun handleFunctionKey(code: Int) {
        val androidCode = KeyCodes.toAndroidKeyCode(code) ?: return
        inputConnectionManager.sendModifiedKeyEvent(androidCode, ModifierState.activeModifiers.value)
    }

    private fun handleSymbolSwitch() {
        service.toggleSymbolsMode()
    }
}
