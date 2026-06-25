package io.github.modhack.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import io.github.modhack.prefs.KeyboardPreferences

/**
 * Manages haptic and audio feedback for key presses.
 *
 * Reads feedback preferences from [KeyboardPreferences] and triggers
 * the appropriate system APIs (Vibrator / AudioManager) on each key press.
 */
class FeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * Plays key press feedback based on the current preferences.
     *
     * @param prefs Current keyboard preferences controlling feedback behavior.
     */
    fun playFeedback(prefs: KeyboardPreferences) {
        if (prefs.vibrateOn) {
            performVibration(prefs.vibrateLen)
        }
        if (prefs.soundOn) {
            performSound()
        }
    }

    private fun performVibration(durationMs: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(
                    durationMs.toLong(),
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs.toLong())
        }
    }

    private fun performSound() {
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1f)
    }
}
