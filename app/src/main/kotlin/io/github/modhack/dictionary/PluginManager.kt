package io.github.modhack.dictionary

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/**
 * Data class representing an external dictionary plugin.
 *
 * @property packageName The Android package name of the plugin app.
 * @property label Human-readable name of the plugin.
 * @property locale The locale this plugin provides dictionary data for.
 * @property serviceClass The fully-qualified service class name handling the plugin intent.
 */
data class DictionaryPlugin(
    val packageName: String,
    val label: String,
    val locale: String,
    val serviceClass: String = ""
)

/**
 * Detects external dictionary plugins via PackageManager.
 *
 * Queries for services that respond to the
 * `io.github.modhack.DICTIONARY_PLUGIN` intent action, as defined
 * in the plugin's AndroidManifest.xml.
 *
 * Example plugin AndroidManifest.xml:
 * ```xml
 * <service android:name=".MyDictService"
 *          android:exported="true">
 *     <intent-filter>
 *         <action android:name="io.github.modhack.DICTIONARY_PLUGIN" />
 *     </intent-filter>
 *     <meta-data android:name="locale" android:value="en" />
 * </service>
 * ```
 */
class PluginManager {

    companion object {
        /** Intent action used to discover dictionary plugins. */
        const val ACTION_DICTIONARY_PLUGIN = "io.github.modhack.DICTIONARY_PLUGIN"
    }

    /**
     * Discovers installed dictionary plugins by querying PackageManager
     * for services that handle the plugin intent action.
     *
     * @param context Application context for PackageManager access.
     * @return A list of discovered [DictionaryPlugin] instances.
     */
    fun discoverPlugins(context: Context): List<DictionaryPlugin> {
        val plugins = mutableListOf<DictionaryPlugin>()
        val pm = context.packageManager

        val intent = Intent(ACTION_DICTIONARY_PLUGIN)

        val resolveInfoFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_META_DATA
        }

        val resolveInfos = pm.queryIntentServices(intent, resolveInfoFlags)

        for (info in resolveInfos) {
            val serviceInfo = info.serviceInfo ?: continue
            val packageName = serviceInfo.packageName

            // Skip our own package
            if (packageName == context.packageName) continue

            val label = try {
                serviceInfo.loadLabel(pm).toString()
            } catch (_: Exception) {
                packageName
            }

            val locale = serviceInfo.metaData?.getString("locale")
                ?: serviceInfo.metaData?.getString("io.github.modhack.DICTIONARY_LOCALE")
                ?: "en"

            plugins.add(
                DictionaryPlugin(
                    packageName = packageName,
                    label = label,
                    locale = locale,
                    serviceClass = serviceInfo.name
                )
            )
        }

        return plugins
    }

    /**
     * Checks if a specific plugin package is installed and valid.
     *
     * @param context Application context.
     * @param packageName The package name to check.
     * @return `true` if the package is installed and declares a valid plugin service.
     */
    fun isPluginInstalled(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = Intent(ACTION_DICTIONARY_PLUGIN).setPackage(packageName)
            val resolveInfoFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PackageManager.ResolveInfoFlags.of(0L)
            } else {
                @Suppress("DEPRECATION")
                0
            }
            pm.queryIntentServices(intent, resolveInfoFlags).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}
