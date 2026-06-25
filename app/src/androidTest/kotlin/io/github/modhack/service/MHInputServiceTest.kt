package io.github.modhack.service

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.modhack.input.ComposeSequence
import io.github.modhack.input.DeadAccentSequence
import io.github.modhack.input.ModifierState
import io.github.modhack.input.WordComposer
import io.github.modhack.model.Modifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for MHInputService-related components.
 *
 * Since MHInputService is a system-managed service that cannot be
 * instantiated directly in tests, these tests verify the individual
 * components it depends on using real Android context.
 */
@RunWith(AndroidJUnit4::class)
class MHInputServiceTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ModifierState.clearAll()
    }

    @Test
    fun `context is available`() {
        assertNotNull(context)
    }

    @Test
    fun `InputMethodManager is accessible`() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        assertNotNull("InputMethodManager should be available", imm)
    }

    @Test
    fun `enabled input method list is not empty`() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = imm.enabledInputMethodList
        assertNotNull(enabledMethods)
        // At least one IME should be enabled (the default)
        assertTrue("At least one IME should be enabled", enabledMethods.isNotEmpty())
    }

    @Test
    fun `WordComposer integrates with service lifecycle`() {
        val composer = WordComposer()

        // Simulate typing
        composer.addKeyCode('h'.code)
        composer.addKeyCode('e'.code)
        composer.addKeyCode('l'.code)
        composer.addKeyCode('l'.code)
        composer.addKeyCode('o'.code)
        assertEquals("hello", composer.getTypedWord())

        // Simulate onFinishInput
        composer.reset()
        assertTrue(composer.isEmpty())
    }

    @Test
    fun `ModifierState integrates with service lifecycle`() {
        // Simulate shift press
        ModifierState.press(Modifier.SHIFT)
        assertTrue(ModifierState.isActive(Modifier.SHIFT))

        // Simulate onFinishInput
        ModifierState.clearAll()
        assertFalse(ModifierState.isActive(Modifier.SHIFT))
    }

    @Test
    fun `ComposeSequence integrates with service lifecycle`() {
        val compose = ComposeSequence()

        // Start compose sequence
        compose.onFirstKey('\'')
        assertTrue(compose.isActive)

        // Simulate onFinishInput
        compose.cancel()
        assertFalse(compose.isActive)
    }

    @Test
    fun `DeadAccentSequence integrates with service lifecycle`() {
        val dead = DeadAccentSequence()

        // Start dead key sequence
        dead.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        assertTrue(dead.isActive)

        // Simulate onFinishInput
        dead.cancel()
        assertFalse(dead.isActive)
    }

    @Test
    fun `EditorInfo contains expected fields for testing`() {
        val editorInfo = EditorInfo()
        editorInfo.inputType = android.text.InputType.TYPE_CLASS_TEXT
        editorInfo.packageName = "com.test.app"

        assertEquals(android.text.InputType.TYPE_CLASS_TEXT, editorInfo.inputType)
        assertEquals("com.test.app", editorInfo.packageName)
    }
}
