package io.github.modhack.input

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ComposeSequence] — X11 compose key sequence engine.
 */
class ComposeSequenceTest {

    private lateinit var composeSequence: ComposeSequence

    @Before
    fun setUp() {
        composeSequence = ComposeSequence()
    }

    @Test
    fun `initial state is not active`() {
        assertFalse(composeSequence.isActive)
    }

    @Test
    fun `onFirstKey starts sequence and returns null`() {
        val result = composeSequence.onFirstKey('\'')
        assertNull(result)
        assertTrue(composeSequence.isActive)
    }

    @Test
    fun `onSecondKey with valid sequence returns combined char`() {
        composeSequence.onFirstKey('\'')
        val result = composeSequence.onSecondKey('e')
        assertEquals('\u00e9', result) // é
        assertFalse(composeSequence.isActive)
    }

    @Test
    fun `onSecondKey with invalid sequence returns null`() {
        composeSequence.onFirstKey('\'')
        val result = composeSequence.onSecondKey('1')
        assertNull(result)
        assertFalse(composeSequence.isActive)
    }

    @Test
    fun `acute accent produces correct accented characters`() {
        val expected = mapOf(
            'a' to '\u00e1', // á
            'e' to '\u00e9', // é
            'i' to '\u00ed', // í
            'o' to '\u00f3', // ó
            'u' to '\u00fa', // ú
            'A' to '\u00c1', // Á
            'E' to '\u00c9', // É
        )

        for ((char, expectedChar) in expected) {
            composeSequence.onFirstKey('\'')
            assertEquals("compose(' + $char)", expectedChar, composeSequence.onSecondKey(char))
        }
    }

    @Test
    fun `diaeresis produces correct umlaut characters`() {
        val expected = mapOf(
            'a' to '\u00e4', // ä
            'o' to '\u00f6', // ö
            'u' to '\u00fc', // ü
            'A' to '\u00c4', // Ä
            'O' to '\u00d6', // Ö
            'U' to '\u00dc', // Ü
        )

        for ((char, expectedChar) in expected) {
            composeSequence.onFirstKey('"')
            assertEquals("compose(\" + $char)", expectedChar, composeSequence.onSecondKey(char))
        }
    }

    @Test
    fun `grave accent produces correct characters`() {
        composeSequence.onFirstKey('`')
        assertEquals('\u00e0', composeSequence.onSecondKey('a')) // à

        composeSequence.onFirstKey('`')
        assertEquals('\u00e8', composeSequence.onSecondKey('e')) // è
    }

    @Test
    fun `tilde produces correct characters`() {
        composeSequence.onFirstKey('~')
        assertEquals('\u00f1', composeSequence.onSecondKey('n')) // ñ

        composeSequence.onFirstKey('~')
        assertEquals('\u00e3', composeSequence.onSecondKey('a')) // ã
    }

    @Test
    fun `circumflex produces correct characters`() {
        composeSequence.onFirstKey('^')
        assertEquals('\u00e2', composeSequence.onSecondKey('a')) // â

        composeSequence.onFirstKey('^')
        assertEquals('\u00ea', composeSequence.onSecondKey('e')) // ê
    }

    @Test
    fun `cedilla produces correct characters`() {
        composeSequence.onFirstKey(',')
        assertEquals('\u00e7', composeSequence.onSecondKey('c')) // ç

        composeSequence.onFirstKey(',')
        assertEquals('\u00c7', composeSequence.onSecondKey('C')) // Ç
    }

    @Test
    fun `cancel stops sequence`() {
        composeSequence.onFirstKey('\'')
        assertTrue(composeSequence.isActive)
        composeSequence.cancel()
        assertFalse(composeSequence.isActive)
    }

    @Test
    fun `onSecondKey without first key returns null`() {
        val result = composeSequence.onSecondKey('e')
        assertNull(result)
    }

    @Test
    fun `special symbol sequences work`() {
        composeSequence.onFirstKey('<')
        assertEquals('\u00ab', composeSequence.onSecondKey('<')) // «

        composeSequence.onFirstKey('>')
        assertEquals('\u00bb', composeSequence.onSecondKey('>')) // »
    }

    @Test
    fun `copyright and registered sequences work`() {
        composeSequence.onFirstKey('c')
        assertEquals('\u00a9', composeSequence.onSecondKey('o')) // ©

        composeSequence.onFirstKey('c')
        assertEquals('\u00ae', composeSequence.onSecondKey('r')) // ®
    }
}
