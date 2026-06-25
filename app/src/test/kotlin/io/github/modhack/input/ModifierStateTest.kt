package io.github.modhack.input

import io.github.modhack.model.Modifier
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ModifierState] singleton.
 *
 * Tests thread-safe modifier state management including
 * press, release, toggle, clearAll, and getMetaState.
 */
class ModifierStateTest {

    @Before
    fun setUp() {
        ModifierState.clearAll()
    }

    @Test
    fun `initial state has no active modifiers`() {
        assertTrue(ModifierState.activeModifiers.value.isEmpty())
    }

    @Test
    fun `press activates a modifier`() {
        ModifierState.press(Modifier.SHIFT)
        assertTrue(ModifierState.isActive(Modifier.SHIFT))
        assertEquals(1, ModifierState.activeModifiers.value.size)
    }

    @Test
    fun `press multiple modifiers accumulates them`() {
        ModifierState.press(Modifier.SHIFT)
        ModifierState.press(Modifier.CTRL)
        assertTrue(ModifierState.isActive(Modifier.SHIFT))
        assertTrue(ModifierState.isActive(Modifier.CTRL))
        assertEquals(2, ModifierState.activeModifiers.value.size)
    }

    @Test
    fun `release deactivates a modifier`() {
        ModifierState.press(Modifier.SHIFT)
        ModifierState.release(Modifier.SHIFT)
        assertFalse(ModifierState.isActive(Modifier.SHIFT))
        assertTrue(ModifierState.activeModifiers.value.isEmpty())
    }

    @Test
    fun `release only removes specified modifier`() {
        ModifierState.press(Modifier.SHIFT)
        ModifierState.press(Modifier.CTRL)
        ModifierState.release(Modifier.SHIFT)
        assertFalse(ModifierState.isActive(Modifier.SHIFT))
        assertTrue(ModifierState.isActive(Modifier.CTRL))
    }

    @Test
    fun `toggle activates inactive modifier`() {
        ModifierState.toggle(Modifier.ALT)
        assertTrue(ModifierState.isActive(Modifier.ALT))
    }

    @Test
    fun `toggle deactivates active modifier`() {
        ModifierState.press(Modifier.ALT)
        ModifierState.toggle(Modifier.ALT)
        assertFalse(ModifierState.isActive(Modifier.ALT))
    }

    @Test
    fun `clearAll removes all modifiers`() {
        ModifierState.press(Modifier.SHIFT)
        ModifierState.press(Modifier.CTRL)
        ModifierState.press(Modifier.ALT)
        ModifierState.clearAll()
        assertTrue(ModifierState.activeModifiers.value.isEmpty())
    }

    @Test
    fun `getMetaState returns correct flag combination`() {
        ModifierState.press(Modifier.SHIFT)
        ModifierState.press(Modifier.CTRL)
        val metaState = ModifierState.getMetaState()
        assertTrue(metaState and Modifier.SHIFT.metaFlag != 0)
        assertTrue(metaState and Modifier.CTRL.metaFlag != 0)
        assertTrue(metaState and Modifier.ALT.metaFlag == 0)
    }

    @Test
    fun `getMetaState returns 0 when no modifiers active`() {
        assertEquals(0, ModifierState.getMetaState())
    }

    @Test
    fun `press same modifier twice does not duplicate`() {
        ModifierState.press(Modifier.SHIFT)
        ModifierState.press(Modifier.SHIFT)
        assertEquals(1, ModifierState.activeModifiers.value.size)
    }
}
