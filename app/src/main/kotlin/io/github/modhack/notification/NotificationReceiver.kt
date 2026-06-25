package io.github.modhack.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import io.github.modhack.R

/**
 * Handles broadcast intents for showing the keyboard switcher notification
 * and opening the settings activity.
 *
 * Intent actions:
 * - [ACTION_SHOW]: Shows the system input method picker.
 * - [ACTION_SETTINGS]: Opens the [SettingsActivity].
 *
 * This receiver is declared with `android:exported="false"` in the manifest
 * and requires [RECEIVER_NOT_EXPORTED] flag on API 34+.
 *
 * @see android.Manifest.permission.RECEIVER_NOT_EXPORTED
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        /** Intent action to show the input method picker. */
        const val ACTION_SHOW = "io.github.modhack.ACTION_SHOW"

        /** Intent action to open the settings activity. */
        const val ACTION_SETTINGS = "io.github.modhack.ACTION_SETTINGS"

        /** Notification channel ID for the persistent keyboard status notification. */
        const val CHANNEL_ID = "modhack_keyboard_status"

        /** Notification ID for the persistent keyboard status notification. */
        const val NOTIFICATION_ID = 1001

        /**
         * Creates the notification channel for the persistent keyboard status notification.
         *
         * Must be called on API 26+ before posting any notifications on that channel.
         *
         * @param context The context used to access the NotificationManager.
         */
        @JvmStatic
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = context.getString(R.string.notification_channel_desc)
                    setShowBadge(false)
                }

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        /**
         * Builds and shows the persistent keyboard status notification.
         *
         * @param context The context used to post the notification.
         */
        @JvmStatic
        fun showNotification(context: Context) {
            createNotificationChannel(context)

            val showIntent = Intent(ACTION_SHOW).apply {
                setPackage(context.packageName)
            }
            val showPendingIntent = PendingIntent.getBroadcast(
                context, 0, showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val settingsIntent = Intent(context, io.github.modhack.activity.SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val settingsPendingIntent = PendingIntent.getActivity(
                context, 1, settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_tap_to_settings))
                .setContentIntent(settingsPendingIntent)
                .addAction(
                    R.mipmap.ic_launcher,
                    context.getString(R.string.btn_select_ime),
                    showPendingIntent
                )
                .setOngoing(true)
                .setSilent(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Called when a broadcast intent is received.
     *
     * @param context The context in which the receiver is running.
     * @param intent The intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SHOW -> handleShow(context)
            ACTION_SETTINGS -> handleSettings(context)
        }
    }

    /**
     * Shows the system input method picker by invoking the InputMethodManager.
     */
    private fun handleShow(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    /**
     * Opens the settings activity via an explicit intent.
     */
    private fun handleSettings(context: Context) {
        val intent = Intent(context, io.github.modhack.activity.SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
