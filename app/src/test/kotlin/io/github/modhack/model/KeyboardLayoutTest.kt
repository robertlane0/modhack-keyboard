package io.github.modhack.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for data models: [KeyboardLayout], [Key], [Row],
 * [KeyboardState], and [InputMode].
 */
class KeyboardLayoutTest {

    @Test
    fun `KeyboardLayout stores properties correctly`() {
        val key = Key(
            codes = listOf(65),
            label = "a",
            shiftLabel = "A",
            width = 10,
            height = 10
        )
        val row = Row(keys = listOf(key), defaultHeight = 10, defaultWidth = 10)
        val layout = KeyboardLayout(
            id = "test_layout",
            rows = listOf(row),
            width = 100,
            height = 40,
            mode = 0
        )

        assertEquals("test_layout", layout.id)
        assertEquals(1, layout.rows.size)
        assertEquals(100, layout.width)
        assertEquals(40, layout.height)
        assertEquals(0, layout.mode)
    }

    @Test
    fun `Key stores all properties correctly`() {
        val key = Key(
            codes = listOf(65, 66),
            label = "a",
            shiftLabel = "A",
            hint = "1",
            altHint = "!",
            width = 15,
            height = 10,
            x = 0,
            y = 0,
            isModifier = false,
            isRepeatable = true,
            isCursor = false,
            popupKeys = listOf("á", "à", "â")
        )

        assertEquals(listOf(65, 66), key.codes)
        assertEquals("a", key.label)
        assertEquals("A", key.shiftLabel)
        assertEquals("1", key.hint)
        assertEquals("!", key.altHint)
        assertEquals(15, key.width)
        assertEquals(10, key.height)
        assertEquals(0, key.x)
        assertEquals(0, key.y)
        assertFalse(key.isModifier)
        assertTrue(key.isRepeatable)
        assertFalse(key.isCursor)
        assertEquals(listOf("á", "à", "â"), key.popupKeys)
    }

    @Test
    fun `Key default values are correct`() {
        val key = Key(codes = listOf(65), label = "a", width = 10, height = 10)

        assertEquals("", key.shiftLabel)
        assertNull(key.hint)
        assertNull(key.altHint)
        assertNull(key.icon)
        assertEquals(0, key.iconResId)
        assertEquals(0, key.x)
        assertEquals(0, key.y)
        assertFalse(key.isModifier)
        assertFalse(key.isRepeatable)
        assertFalse(key.isCursor)
        assertNull(key.popupKeys)
    }

    @Test
    fun `Row stores properties correctly`() {
        val keys = listOf(
            Key(codes = listOf(65), label = "a", width = 10, height = 10),
            Key(codes = listOf(66), label = "b", width = 10, height = 10)
        )
        val row = Row(
            keys = keys,
            defaultHeight = 10,
            defaultWidth = 10,
            mode = 1,
            isExtension = true
        )

        assertEquals(2, row.keys.size)
        assertEquals(10, row.defaultHeight)
        assertEquals(10, row.defaultWidth)
        assertEquals(1, row.mode)
        assertTrue(row.isExtension)
    }

    @Test
    fun `Row default values are correct`() {
        val row = Row(keys = emptyList(), defaultHeight = 10, defaultWidth = 10)

        assertEquals(0, row.mode)
        assertFalse(row.isExtension)
    }

    @Test
    fun `KeyboardState default values are correct`() {
        val state = KeyboardState()

        assertEquals("", state.layoutId)
        assertNull(state.layout)
        assertEquals(InputMode.TEXT, state.mode)
        assertEquals(ShiftState.OFF, state.shiftState)
        assertTrue(state.activeModifiers.isEmpty())
        assertFalse(state.isComposing)
        assertFalse(state.isCapsLock)
    }

    @Test
    fun `KeyboardState copy preserves values`() {
        val state = KeyboardState(
            layoutId = "test",
            mode = InputMode.SYMBOLS,
            shiftState = ShiftState.ON,
            isComposing = true
        )
        val copied = state.copy(isComposing = false)

        assertEquals("test", copied.layoutId)
        assertEquals(InputMode.SYMBOLS, copied.mode)
        assertEquals(ShiftState.ON, copied.shiftState)
        assertFalse(copied.isComposing)
    }

    @Test
    fun `InputMode enum has correct values`() {
        assertEquals(7, InputMode.entries.size)
        assertNotNull(InputMode.TEXT)
        assertNotNull(InputMode.SYMBOLS)
        assertNotNull(InputMode.PHONE)
        assertNotNull(InputMode.URL)
        assertNotNull(InputMode.EMAIL)
        assertNotNull(InputMode.IM)
        assertNotNull(InputMode.WEB)
    }

    @Test
    fun `ShiftState enum has correct values`() {
        assertEquals(3, ShiftState.entries.size)
        assertNotNull(ShiftState.OFF)
        assertNotNull(ShiftState.ON)
        assertNotNull(ShiftState.LOCKED)
    }
}
