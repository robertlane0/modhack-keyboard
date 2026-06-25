package io.github.modhack.service

import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import io.github.modhack.model.Modifier

/**
 * A safe wrapper around the [InputConnection] that provides utility
 * methods for committing text, deleting text, and sending key events.
 */
class InputConnectionManager(private val service: InputMethodService) {

    private val inputConnection: InputConnection?
        get() = service.currentInputConnection

    /**
     * Commits [text] to the text field and positions the cursor at the end.
     */
    fun commitText(text: CharSequence) {
        inputConnection?.commitText(text, 1)
    }

    /**
     * Deletes [before] characters before the cursor and [after] characters after.
     */
    fun deleteSurroundingText(before: Int, after: Int) {
        inputConnection?.deleteSurroundingText(before, after)
    }

    /**
     * Sends a raw [KeyEvent].
     */
    fun sendKeyEvent(event: KeyEvent) {
        inputConnection?.sendKeyEvent(event)
    }

    /**
     * Builds and sends a [KeyEvent] for [keyCode] with the specified [modifiers] applied.
     * Both ACTION_DOWN and ACTION_UP events are sent.
     */
    fun sendModifiedKeyEvent(keyCode: Int, modifiers: Set<Modifier>) {
        val ic = inputConnection ?: return
        var metaState = 0
        modifiers.forEach { metaState = metaState or it.metaFlag }

        val eventTime = SystemClock.uptimeMillis()
        ic.sendKeyEvent(
            KeyEvent(
                eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, metaState
            )
        )
        ic.sendKeyEvent(
            KeyEvent(
                eventTime, eventTime,
                KeyEvent.ACTION_UP, keyCode, 0, metaState
            )
        )
    }

    /**
     * Returns [length] characters of text before the current cursor position.
     */
    fun getTextBeforeCursor(length: Int): CharSequence? {
        return inputConnection?.getTextBeforeCursor(length, 0)
    }

    /**
     * Returns [length] characters of text after the current cursor position.
     */
    fun getTextAfterCursor(length: Int): CharSequence? {
        return inputConnection?.getTextAfterCursor(length, 0)
    }

    /**
     * Replaces the currently composing text with [text].
     */
    fun setComposingText(text: CharSequence) {
        inputConnection?.setComposingText(text, 1)
    }

    /**
     * Finishes composing text, committing it to the text field.
     */
    fun finishComposingText() {
        inputConnection?.finishComposingText()
    }
}
