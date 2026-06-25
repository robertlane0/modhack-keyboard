package io.github.modhack.input

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DeadAccentSequence] — dead key / accent combining engine.
 */
class DeadAccentSequenceTest {

    private lateinit var deadAccent: DeadAccentSequence

    @Before
    fun setUp() {
        deadAccent = DeadAccentSequence()
    }

    @Test
    fun `initial state is not active`() {
        assertFalse(deadAccent.isActive)
        assertNull(deadAccent.currentDeadKey)
    }

    @Test
    fun `pressDeadKey activates sequence`() {
        val result = deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        assertNull(result)
        assertTrue(deadAccent.isActive)
        assertEquals(DeadAccentSequence.DeadKey.CIRCUMFLEX, deadAccent.currentDeadKey)
    }

    @Test
    fun `pressDeadKey always returns null`() {
        val result = deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.ACUTE)
        assertNull(result)
    }

    @Test
    fun `onBaseKey with combining produces precomposed character`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        val result = deadAccent.onBaseKey('a')
        assertEquals("\u00e2", result) // â
        assertFalse(deadAccent.isActive)
    }

    @Test
    fun `onBaseKey without dead key returns character as-is`() {
        val result = deadAccent.onBaseKey('a')
        assertEquals("a", result)
    }

    @Test
    fun `dead acute combines correctly`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.ACUTE)
        assertEquals("\u00e9", deadAccent.onBaseKey('e')) // é

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.ACUTE)
        assertEquals("\u00e1", deadAccent.onBaseKey('a')) // á

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.ACUTE)
        assertEquals("\u00ed", deadAccent.onBaseKey('i')) // í
    }

    @Test
    fun `dead circumflex combines correctly`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        assertEquals("\u00e2", deadAccent.onBaseKey('a')) // â

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        assertEquals("\u00ea", deadAccent.onBaseKey('e')) // ê

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        assertEquals("\u00f4", deadAccent.onBaseKey('o')) // ô
    }

    @Test
    fun `dead tilde combines correctly`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.TILDE)
        assertEquals("\u00f1", deadAccent.onBaseKey('n')) // ñ

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.TILDE)
        assertEquals("\u00e3", deadAccent.onBaseKey('a')) // ã
    }

    @Test
    fun `dead diaeresis combines correctly`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.DIAERESIS)
        assertEquals("\u00e4", deadAccent.onBaseKey('a')) // ä

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.DIAERESIS)
        assertEquals("\u00f6", deadAccent.onBaseKey('o')) // ö

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.DIAERESIS)
        assertEquals("\u00fc", deadAccent.onBaseKey('u')) // ü
    }

    @Test
    fun `dead grave combines correctly`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.GRAVE)
        assertEquals("\u00e0", deadAccent.onBaseKey('a')) // à

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.GRAVE)
        assertEquals("\u00e8", deadAccent.onBaseKey('e')) // è
    }

    @Test
    fun `cancel returns dead key literal and clears state`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        val result = deadAccent.cancel()
        assertEquals('^', result)
        assertFalse(deadAccent.isActive)
        assertNull(deadAccent.currentDeadKey)
    }

    @Test
    fun `cancel on inactive state returns null`() {
        val result = deadAccent.cancel()
        assertNull(result)
    }

    @Test
    fun `DeadKey fromChar finds correct key`() {
        assertEquals(
            DeadAccentSequence.DeadKey.CIRCUMFLEX,
            DeadAccentSequence.DeadKey.fromChar('^')
        )
        assertEquals(
            DeadAccentSequence.DeadKey.ACUTE,
            DeadAccentSequence.DeadKey.fromChar('\'')
        )
        assertNull(DeadAccentSequence.DeadKey.fromChar('z'))
    }

    @Test
    fun `DeadKey entries have unique literals`() {
        val literals = DeadAccentSequence.DeadKey.entries.map { it.literal }.toSet()
        assertEquals(DeadAccentSequence.DeadKey.entries.size, literals.size)
    }

    @Test
    fun `uppercase base characters combine correctly`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        assertEquals("\u00c2", deadAccent.onBaseKey('A')) // Â

        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.DIAERESIS)
        assertEquals("\u00c4", deadAccent.onBaseKey('A')) // Ä
    }
}
