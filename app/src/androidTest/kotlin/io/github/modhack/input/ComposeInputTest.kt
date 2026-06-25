package io.github.modhack.input

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [ComposeSequence] and [DeadAccentSequence].
 *
 * These run on a real Android device/emulator to verify that the
 * Unicode normalization and character combining work correctly
 * on the Android platform.
 */
@RunWith(AndroidJUnit4::class)
class ComposeInputTest {

    private lateinit var composeSequence: ComposeSequence
    private lateinit var deadAccent: DeadAccentSequence

    @Before
    fun setUp() {
        composeSequence = ComposeSequence()
        deadAccent = DeadAccentSequence()
    }

    // ── ComposeSequence Tests ────────────────────────────────────────

    @Test
    fun `compose sequence produces correct Unicode on Android`() {
        composeSequence.onFirstKey('\'')
        val result = composeSequence.onSecondKey('e')
        assertNotNull(result)
        assertEquals('\u00e9', result) // é
    }

    @Test
    fun `compose tilde + n produces ñ on Android`() {
        composeSequence.onFirstKey('~')
        val result = composeSequence.onSecondKey('n')
        assertEquals('\u00f1', result)
    }

    @Test
    fun `compose double-quote + a produces ä on Android`() {
        composeSequence.onFirstKey('"')
        val result = composeSequence.onSecondKey('a')
        assertEquals('\u00e4', result)
    }

    @Test
    fun `compose circumflex + o produces ô on Android`() {
        composeSequence.onFirstKey('^')
        val result = composeSequence.onSecondKey('o')
        assertEquals('\u00f4', result)
    }

    @Test
    fun `compose cedilla + c produces ç on Android`() {
        composeSequence.onFirstKey(',')
        val result = composeSequence.onSecondKey('c')
        assertEquals('\u00e7', result)
    }

    // ── DeadAccentSequence Tests ─────────────────────────────────────

    @Test
    fun `dead circumflex + a produces â via NFC on Android`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        val result = deadAccent.onBaseKey('a')
        assertEquals("\u00e2", result)
    }

    @Test
    fun `dead acute + e produces é via NFC on Android`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.ACUTE)
        val result = deadAccent.onBaseKey('e')
        assertEquals("\u00e9", result)
    }

    @Test
    fun `dead tilde + n produces ñ via NFC on Android`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.TILDE)
        val result = deadAccent.onBaseKey('n')
        assertEquals("\u00f1", result)
    }

    @Test
    fun `dead diaeresis + u produces ü via NFC on Android`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.DIAERESIS)
        val result = deadAccent.onBaseKey('u')
        assertEquals("\u00fc", result)
    }

    @Test
    fun `dead grave + a produces à via NFC on Android`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.GRAVE)
        val result = deadAccent.onBaseKey('a')
        assertEquals("\u00e0", result)
    }

    @Test
    fun `dead key with uppercase base produces uppercase result`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        val result = deadAccent.onBaseKey('A')
        assertEquals("\u00c2", result) // Â
    }

    @Test
    fun `dead key cancel returns literal character`() {
        deadAccent.pressDeadKey(DeadAccentSequence.DeadKey.CIRCUMFLEX)
        val result = deadAccent.cancel()
        assertEquals('^', result)
        assertFalse(deadAccent.isActive)
    }
}
