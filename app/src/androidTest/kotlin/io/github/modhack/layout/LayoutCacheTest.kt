package io.github.modhack.layout

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.modhack.model.InputMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [LayoutCache] and [KeyboardLoader].
 *
 * Tests layout loading, caching, and locale-specific resolution
 * using the actual Android resource system.
 */
@RunWith(AndroidJUnit4::class)
class LayoutCacheTest {

    private lateinit var context: Context
    private lateinit var layoutCache: LayoutCache
    private lateinit var keyboardLoader: KeyboardLoader

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        layoutCache = LayoutCache(context)
        keyboardLoader = KeyboardLoader(context)
    }

    @Test
    fun `KeyboardLoader loads QWERTY layout from XML`() {
        val resId = context.resources.getIdentifier("kbd_qwerty", "xml", context.packageName)
        assertTrue("kbd_qwerty.xml resource should exist", resId != 0)

        val layout = keyboardLoader.loadLayout(resId, "test_qwerty", InputMode.TEXT.ordinal)
        assertNotNull("Layout should not be null", layout)
        assertEquals("test_qwerty", layout.id)
        assertTrue("Layout should have rows", layout.rows.isNotEmpty())
        assertEquals("QWERTY should have 4 rows", 4, layout.rows.size)
    }

    @Test
    fun `KeyboardLoader loads QWERTY layout with correct key count`() {
        val resId = context.resources.getIdentifier("kbd_qwerty", "xml", context.packageName)
        val layout = keyboardLoader.loadLayout(resId, "test", InputMode.TEXT.ordinal)

        // QWERTY layout: 10 + 9 + 10 + 5 = 34 keys
        val totalKeys = layout.rows.sumOf { it.keys.size }
        assertTrue("QWERTY should have at least 30 keys", totalKeys >= 30)
    }

    @Test
    fun `KeyboardLoader loads symbols layout`() {
        val resId = context.resources.getIdentifier("kbd_symbols", "xml", context.packageName)
        assertTrue("kbd_symbols.xml resource should exist", resId != 0)

        val layout = keyboardLoader.loadLayout(resId, "test_symbols", InputMode.SYMBOLS.ordinal)
        assertNotNull(layout)
        assertTrue(layout.rows.isNotEmpty())
    }

    @Test
    fun `LayoutCache returns layout for TEXT mode`() = runBlocking {
        val layout = layoutCache.getLayout(InputMode.TEXT, "en", android.content.res.Configuration.ORIENTATION_PORTRAIT, "qwerty")
        assertNotNull("Layout should not be null", layout)
        assertTrue("Layout should have rows", layout.rows.isNotEmpty())
    }

    @Test
    fun `LayoutCache returns layout for SYMBOLS mode`() = runBlocking {
        val layout = layoutCache.getLayout(InputMode.SYMBOLS, "en", android.content.res.Configuration.ORIENTATION_PORTRAIT, "qwerty")
        assertNotNull(layout)
        assertTrue(layout.rows.isNotEmpty())
    }

    @Test
    fun `LayoutCache returns cached layout on repeated access`() = runBlocking {
        val layout1 = layoutCache.getLayout(InputMode.TEXT, "en", android.content.res.Configuration.ORIENTATION_PORTRAIT, "qwerty")
        val layout2 = layoutCache.getLayout(InputMode.TEXT, "en", android.content.res.Configuration.ORIENTATION_PORTRAIT, "qwerty")
        assertEquals("Cached layout should be same instance", layout1.id, layout2.id)
    }

    @Test
    fun `LayoutCache resolves full layout variant`() = runBlocking {
        val layout = layoutCache.getLayout(InputMode.TEXT, "en", android.content.res.Configuration.ORIENTATION_PORTRAIT, "full")
        assertNotNull(layout)
        assertTrue(layout.rows.isNotEmpty())
    }

    @Test
    fun `LayoutCache resolves full_fn layout variant`() = runBlocking {
        val layout = layoutCache.getLayout(InputMode.TEXT, "en", android.content.res.Configuration.ORIENTATION_PORTRAIT, "full_fn")
        assertNotNull(layout)
        assertTrue(layout.rows.isNotEmpty())
    }

    @Test
    fun `LayoutCache resolves locale-specific layout`() = runBlocking {
        val layout = layoutCache.getLayout(InputMode.TEXT, "es", android.content.res.Configuration.ORIENTATION_PORTRAIT, "qwerty")
        assertNotNull(layout)
        assertTrue(layout.rows.isNotEmpty())
    }

    @Test
    fun `KeyboardLoader parses Key codes correctly`() {
        val resId = context.resources.getIdentifier("kbd_qwerty", "xml", context.packageName)
        val layout = keyboardLoader.loadLayout(resId, "test", InputMode.TEXT.ordinal)

        // First key in QWERTY should be 'q' (code 113)
        val firstKey = layout.rows.first().keys.first()
        assertEquals(113, firstKey.codes.first()) // 'q'
        assertEquals("q", firstKey.label)
    }

    @Test
    fun `KeyboardLoader parses shift key as modifier`() {
        val resId = context.resources.getIdentifier("kbd_qwerty", "xml", context.packageName)
        val layout = keyboardLoader.loadLayout(resId, "test", InputMode.TEXT.ordinal)

        // Third row, first key should be Shift
        val thirdRow = layout.rows[2]
        val shiftKey = thirdRow.keys.first()
        assertTrue("Shift key should be a modifier", shiftKey.isModifier)
    }

    @Test
    fun `KeyboardLoader parses backspace as repeatable`() {
        val resId = context.resources.getIdentifier("kbd_qwerty", "xml", context.packageName)
        val layout = keyboardLoader.loadLayout(resId, "test", InputMode.TEXT.ordinal)

        // Third row, last key should be Backspace
        val thirdRow = layout.rows[2]
        val backspaceKey = thirdRow.keys.last()
        assertTrue("Backspace key should be repeatable", backspaceKey.isRepeatable)
    }
}
