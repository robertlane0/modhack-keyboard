package io.github.modhack.input

import android.view.KeyEvent
import io.github.modhack.keycodes.KeyCodes
import io.github.modhack.model.Modifier
import io.github.modhack.service.InputConnectionManager
import io.github.modhack.service.MHInputService

/**
 * Handles routing and execution of actions when a key is pressed.
 */
class KeyActionHandler(
    private val service: MHInputService,
    private val inputConnectionManager: InputConnectionManager,
    private val wordComposer: WordComposer
) {
    /**
     * Main entry point for key dispatch.
     *
     * @param primaryCode The main keycode emitted by the pressed key.
     */
    fun onKey(primaryCode: Int) {
        when {
            KeyCodes.isModifier(primaryCode) -> {
                Modifier.fromKeyCode(primaryCode)?.let { handleModifier(it) }
            }
            KeyCodes.isNavigation(primaryCode) -> {
                handleNavigation(primaryCode)
            }
            KeyCodes.isFunctionKey(primaryCode) -> {
                handleFunctionKey(primaryCode)
            }
            primaryCode == KeyCodes.DELETE -> handleBackspace()
            primaryCode == KeyCodes.SHIFT -> handleShift()
            primaryCode == KeyCodes.TAB -> sendTab()
            primaryCode == KeyCodes.ESCAPE -> sendEscape()
            primaryCode == KeyCodes.SYMBOL -> handleSymbolSwitch()
            primaryCode == KeyCodes.SPACE -> {
                wordComposer.reset()
                inputConnectionManager.commitText(" ")
            }
            primaryCode == KeyCodes.ENTER -> {
                wordComposer.reset()
                inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnectionManager.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            primaryCode > 0 -> {
                handleCharacter(primaryCode)
            }
        }
    }

    private fun handleCharacter(code: Int) {
        val metaState = ModifierState.getMetaState()
        if (metaState != 0 && metaState != KeyEvent.META_SHIFT_ON) {
            // Send as modified key event rather than text
            inputConnectionManager.sendModifiedKeyEvent(code, ModifierState.activeModifiers.value)
        } else {
            // Apply shift if needed (basic stub implementation)
            val isShifted = ModifierState.isActive(Modifier.SHIFT)
            val char = if (isShifted) code.toChar().uppercaseChar() else code.toChar()
            
            wordComposer.addKeyCode(char.code)
            inputConnectionManager.commitText(char.toString())
        }
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
        ModifierState.toggle(Modifier.SHIFT)
    }

    private fun handleModifier(modifier: Modifier) {
        ModifierState.toggle(modifier)
    }

    private fun sendTab() {
        // connectbot tab hack can be checked here using preferences flow
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
        // Mode switching is handled by the service observing this action
        service.toggleSymbolsMode()
    }
}
