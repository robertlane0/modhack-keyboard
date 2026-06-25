package io.github.modhack.input

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [WordComposer].
 *
 * Tests character accumulation, deletion, reset, and word retrieval.
 */
class WordComposerTest {

    private lateinit var composer: WordComposer

    @Before
    fun setUp() {
        composer = WordComposer()
    }

    @Test
    fun `initial state is empty`() {
        assertTrue(composer.isEmpty())
        assertEquals(0, composer.size)
        assertEquals("", composer.getTypedWord())
    }

    @Test
    fun `addKeyCode appends character`() {
        composer.addKeyCode('h'.code)
        composer.addKeyCode('i'.code)
        assertEquals("hi", composer.getTypedWord())
        assertEquals(2, composer.size)
    }

    @Test
    fun `addKeyCode single character`() {
        composer.addKeyCode('a'.code)
        assertEquals("a", composer.getTypedWord())
        assertEquals(1, composer.size)
    }

    @Test
    fun `deleteLastCode removes last character`() {
        composer.addKeyCode('h'.code)
        composer.addKeyCode('e'.code)
        composer.addKeyCode('l'.code)
        composer.deleteLastCode()
        assertEquals("he", composer.getTypedWord())
        assertEquals(2, composer.size)
    }

    @Test
    fun `deleteLastCode on empty composer does nothing`() {
        composer.deleteLastCode()
        assertTrue(composer.isEmpty())
    }

    @Test
    fun `deleteLastCode on single character leaves empty`() {
        composer.addKeyCode('x'.code)
        composer.deleteLastCode()
        assertTrue(composer.isEmpty())
        assertEquals("", composer.getTypedWord())
    }

    @Test
    fun `reset clears all characters`() {
        composer.addKeyCode('h'.code)
        composer.addKeyCode('e'.code)
        composer.addKeyCode('l'.code)
        composer.addKeyCode('l'.code)
        composer.addKeyCode('o'.code)
        composer.reset()
        assertTrue(composer.isEmpty())
        assertEquals("", composer.getTypedWord())
        assertEquals(0, composer.size)
    }

    @Test
    fun `reset on empty composer is no-op`() {
        composer.reset()
        assertTrue(composer.isEmpty())
    }

    @Test
    fun `getTypedWord reflects full composition`() {
        val chars = "hello"
        chars.forEach { composer.addKeyCode(it.code) }
        assertEquals("hello", composer.getTypedWord())
    }

    @Test
    fun `size reflects number of added characters`() {
        assertEquals(0, composer.size)
        composer.addKeyCode('a'.code)
        assertEquals(1, composer.size)
        composer.addKeyCode('b'.code)
        assertEquals(2, composer.size)
        composer.deleteLastCode()
        assertEquals(1, composer.size)
    }

    @Test
    fun `add and delete sequence produces correct result`() {
        composer.addKeyCode('t'.code)
        composer.addKeyCode('e'.code)
        composer.addKeyCode('s'.code)
        composer.addKeyCode('t'.code)
        composer.deleteLastCode()
        composer.deleteLastCode()
        composer.addKeyCode('a'.code)
        composer.addKeyCode('r'.code)
        assertEquals("tear", composer.getTypedWord())
    }
}
