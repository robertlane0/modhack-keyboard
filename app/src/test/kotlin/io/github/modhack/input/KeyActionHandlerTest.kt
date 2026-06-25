package io.github.modhack.input

import io.github.modhack.keycodes.KeyCodes
import io.github.modhack.model.Modifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [KeyActionHandler].
 *
 * Since KeyActionHandler requires MHInputService and InputConnectionManager,
 * these tests focus on the pure logic aspects: modifier key handling,
 * shift state management, and code routing logic.
 *
 * Full integration tests with the service are in instrumented tests.
 */
class KeyActionHandlerTest {

    @Before
    fun setUp() {
        ModifierState.clearAll()
    }

    @Test
    fun `isModifier correctly identifies modifier keycodes`() {
        assertTrue(KeyCodes.isModifier(KeyCodes.SHIFT))
        assertTrue(KeyCodes.isModifier(KeyCodes.CTRL_LEFT))
        assertTrue(KeyCodes.isModifier(KeyCodes.ALT_LEFT))
        assertTrue(KeyCodes.isModifier(KeyCodes.META_LEFT))
        assertTrue(KeyCodes.isModifier(KeyCodes.FN))
    }

    @Test
    fun `isModifier rejects non-modifier keycodes`() {
        assertFalse(KeyCodes.isModifier(KeyCodes.SPACE))
        assertFalse(KeyCodes.isModifier(KeyCodes.ENTER))
        assertFalse(KeyCodes.isModifier(KeyCodes.DELETE))
        assertFalse(KeyCodes.isModifier(65)) // 'A'
    }

    @Test
    fun `isNavigation correctly identifies navigation keycodes`() {
        assertTrue(KeyCodes.isNavigation(KeyCodes.DPAD_UP))
        assertTrue(KeyCodes.isNavigation(KeyCodes.DPAD_DOWN))
        assertTrue(KeyCodes.isNavigation(KeyCodes.DPAD_LEFT))
        assertTrue(KeyCodes.isNavigation(KeyCodes.DPAD_RIGHT))
        assertTrue(KeyCodes.isNavigation(KeyCodes.HOME))
        assertTrue(KeyCodes.isNavigation(KeyCodes.END))
        assertTrue(KeyCodes.isNavigation(KeyCodes.PAGE_UP))
        assertTrue(KeyCodes.isNavigation(KeyCodes.PAGE_DOWN))
    }

    @Test
    fun `isNavigation rejects non-navigation keycodes`() {
        assertFalse(KeyCodes.isNavigation(KeyCodes.SPACE))
        assertFalse(KeyCodes.isNavigation(KeyCodes.ENTER))
        assertFalse(KeyCodes.isNavigation(KeyCodes.SHIFT))
    }

    @Test
    fun `isFunctionKey correctly identifies function keycodes`() {
        assertTrue(KeyCodes.isFunctionKey(KeyCodes.FUNC_F1))
        assertTrue(KeyCodes.isFunctionKey(KeyCodes.FUNC_F5))
        assertTrue(KeyCodes.isFunctionKey(KeyCodes.FUNC_F12))
    }

    @Test
    fun `isFunctionKey rejects non-function keycodes`() {
        assertFalse(KeyCodes.isFunctionKey(KeyCodes.F1))
        assertFalse(KeyCodes.isFunctionKey(KeyCodes.SPACE))
        assertFalse(KeyCodes.isFunctionKey(-130))
        assertFalse(KeyCodes.isFunctionKey(-143))
    }

    @Test
    fun `toAndroidKeyCode maps navigation keys correctly`() {
        assertEquals(android.view.KeyEvent.KEYCODE_DPAD_UP, KeyCodes.toAndroidKeyCode(KeyCodes.DPAD_UP))
        assertEquals(android.view.KeyEvent.KEYCODE_DPAD_DOWN, KeyCodes.toAndroidKeyCode(KeyCodes.DPAD_DOWN))
        assertEquals(android.view.KeyEvent.KEYCODE_DPAD_LEFT, KeyCodes.toAndroidKeyCode(KeyCodes.DPAD_LEFT))
        assertEquals(android.view.KeyEvent.KEYCODE_DPAD_RIGHT, KeyCodes.toAndroidKeyCode(KeyCodes.DPAD_RIGHT))
        assertEquals(android.view.KeyEvent.KEYCODE_MOVE_HOME, KeyCodes.toAndroidKeyCode(KeyCodes.HOME))
        assertEquals(android.view.KeyEvent.KEYCODE_MOVE_END, KeyCodes.toAndroidKeyCode(KeyCodes.END))
        assertEquals(android.view.KeyEvent.KEYCODE_PAGE_UP, KeyCodes.toAndroidKeyCode(KeyCodes.PAGE_UP))
        assertEquals(android.view.KeyEvent.KEYCODE_PAGE_DOWN, KeyCodes.toAndroidKeyCode(KeyCodes.PAGE_DOWN))
    }

    @Test
    fun `toAndroidKeyCode maps function keys correctly`() {
        assertEquals(android.view.KeyEvent.KEYCODE_F1, KeyCodes.toAndroidKeyCode(KeyCodes.FUNC_F1))
        assertEquals(android.view.KeyEvent.KEYCODE_F12, KeyCodes.toAndroidKeyCode(KeyCodes.FUNC_F12))
    }

    @Test
    fun `toAndroidKeyCode returns null for unmapped codes`() {
        assertNull(KeyCodes.toAndroidKeyCode(KeyCodes.SPACE))
        assertNull(KeyCodes.toAndroidKeyCode(999))
    }

    @Test
    fun `Modifier fromKeyCode maps correctly`() {
        assertEquals(Modifier.SHIFT, Modifier.fromKeyCode(KeyCodes.SHIFT))
        assertEquals(Modifier.CTRL, Modifier.fromKeyCode(KeyCodes.CTRL_LEFT))
        assertEquals(Modifier.ALT, Modifier.fromKeyCode(KeyCodes.ALT_LEFT))
        assertEquals(Modifier.META, Modifier.fromKeyCode(KeyCodes.META_LEFT))
        assertEquals(Modifier.FN, Modifier.fromKeyCode(KeyCodes.FN))
    }

    @Test
    fun `Modifier fromKeyCode returns null for non-modifiers`() {
        assertNull(Modifier.fromKeyCode(KeyCodes.SPACE))
        assertNull(Modifier.fromKeyCode(65))
    }

    @Test
    fun `Modifier metaFlag values are distinct`() {
        val flags = Modifier.entries.map { it.metaFlag }.toSet()
        assertEquals(Modifier.entries.size, flags.size)
    }

    @Test
    fun `special key codes have correct values`() {
        assertEquals(-1, KeyCodes.SHIFT)
        assertEquals(-5, KeyCodes.DELETE)
        assertEquals(32, KeyCodes.SPACE)
        assertEquals(10, KeyCodes.ENTER)
        assertEquals(9, KeyCodes.TAB)
        assertEquals(-111, KeyCodes.ESCAPE)
        assertEquals(-2, KeyCodes.SYMBOL)
        assertEquals(-104, KeyCodes.NEXT_LANGUAGE)
        assertEquals(-100, KeyCodes.SETTINGS)
        assertEquals(-10024, KeyCodes.COMPOSE)
    }
}
