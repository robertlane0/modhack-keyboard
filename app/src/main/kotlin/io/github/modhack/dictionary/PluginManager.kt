package io.github.modhack.dictionary

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/**
 * Data class representing an external dictionary plugin.
 */
data class DictionaryPlugin(
    val packageName: String,
    val label: String,
    val locale: String
)

/**
 * Detects external dictionary plugins via BroadcastReceiver/PackageManager.
 */
class PluginManager {
    /**
     * Discovers installed dictionary plugins.
     */
    fun discoverPlugins(context: Context): List<DictionaryPlugin> {
        val plugins = mutableListOf<DictionaryPlugin>()
        val pm = context.packageManager
        
        // This is a stub implementation. In a real scenario, we'd query for
        // intents like "io.github.modhack.DICTIONARY_PLUGIN".
        val intent = Intent("io.github.modhack.DICTIONARY_PLUGIN")
        
        val resolveInfoFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_META_DATA
        }
        
        // Example: pm.queryIntentServices(intent, resolveInfoFlags)
        
        return plugins
    }
}
