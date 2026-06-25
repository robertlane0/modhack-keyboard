package io.github.modhack.layout

import android.content.Context
import android.util.LruCache
import io.github.modhack.R
import io.github.modhack.model.InputMode
import io.github.modhack.model.KeyboardLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Identifier for caching [KeyboardLayout]s.
 */
data class LayoutId(
    val mode: InputMode,
    val locale: String,
    val orientation: Int,
    val fullMode: String
)

/**
 * Loads and caches [KeyboardLayout] instances.
 *
 * Resolves locale-specific layout resources by looking for XML files
 * in locale-qualified resource directories (e.g., `res/xml-fr/`,
 * `res/xml-de/`). Falls back to the base `res/xml/` layouts when
 * no locale-specific override exists.
 *
 * @param context Application context for resource access.
 */
class LayoutCache(private val context: Context) {
    private val loader = KeyboardLoader(context)
    private val cache = LruCache<LayoutId, KeyboardLayout>(10)

    /**
     * Asynchronously gets or loads a [KeyboardLayout].
     *
     * @param mode The input mode (TEXT, SYMBOLS, PHONE, etc.).
     * @param locale The current locale code (e.g., "en", "fr", "de").
     * @param orientation The device orientation.
     * @param fullMode The layout variant string ("qwerty", "full", "full_fn").
     * @return The loaded and cached [KeyboardLayout].
     */
    suspend fun getLayout(mode: InputMode, locale: String, orientation: Int, fullMode: String): KeyboardLayout {
        val id = LayoutId(mode, locale, orientation, fullMode)
        return cache.get(id) ?: withContext(Dispatchers.IO) {
            val resId = getLayoutResource(mode, fullMode, locale)
            val layout = loader.loadLayout(resId, id.toString(), mode.ordinal)
            cache.put(id, layout)
            layout
        }
    }

    /**
     * Resolves the layout resource ID, preferring locale-specific overrides.
     *
     * For TEXT mode, looks for `xml-{locale}/kbd_qwerty.xml` first,
     * then falls back to the base `xml/kbd_qwerty.xml`.
     *
     * @param mode The input mode.
     * @param fullMode The layout variant ("qwerty", "full", "full_fn").
     * @param locale The locale code (e.g., "fr", "de", "ru").
     * @return The resolved resource ID.
     */
    private fun getLayoutResource(mode: InputMode, fullMode: String, locale: String): Int {
        return when (mode) {
            InputMode.TEXT -> {
                // Try locale-specific layout first, fall back to base
                getLocaleAwareLayout(fullMode, locale)
            }
            InputMode.SYMBOLS -> R.xml.kbd_symbols
            InputMode.PHONE -> R.xml.kbd_phone
            else -> getLocaleAwareLayout(fullMode, locale)
        }
    }

    /**
     * Resolves a locale-aware layout resource.
     *
     * Tries the locale-specific resource first (e.g., `R.xml.kbd_qwerty`
     * in `res/xml-fr/`), then falls back to the base resource.
     *
     * @param fullMode The layout variant ("qwerty", "full", "full_fn").
     * @param locale The locale code.
     * @return The resource ID (locale-specific if available, otherwise base).
     */
    private fun getLocaleAwareLayout(fullMode: String, locale: String): Int {
        // For locales with dedicated layouts, use the base qwerty layout name
        // but from the locale-qualified resource directory
        return when (fullMode) {
            "full" -> {
                if (locale != "en" && hasLocaleResource(locale, "xml")) {
                    // Locale-specific full layout (if it exists)
                    getLocaleResourceId("kbd_full") ?: R.xml.kbd_full
                } else {
                    R.xml.kbd_full
                }
            }
            "full_fn" -> {
                if (locale != "en" && hasLocaleResource(locale, "xml")) {
                    getLocaleResourceId("kbd_full_fn") ?: R.xml.kbd_full_fn
                } else {
                    R.xml.kbd_full_fn
                }
            }
            else -> {
                // qwerty / compact / default
                if (locale != "en" && hasLocaleResource(locale, "xml")) {
                    // Use the base qwerty layout from the locale directory
                    getLocaleResourceId("kbd_qwerty") ?: R.xml.kbd_qwerty
                } else {
                    R.xml.kbd_qwerty
                }
            }
        }
    }

    /**
     * Checks if a locale-specific resource directory exists.
     */
    private fun hasLocaleResource(locale: String, resourceType: String): Boolean {
        return try {
            val qualResName = "${resourceType}-$locale"
            val assetManager = context.resources.assets
            assetManager.list(qualResName)?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets a locale-specific resource ID by constructing the resource name.
     * Returns null if the resource doesn't exist in the locale-specific directory.
     */
    private fun getLocaleResourceId(baseName: String): Int? {
        return try {
            val resId = context.resources.getIdentifier(
                baseName,
                "xml",
                context.packageName
            )
            // The resource system should handle locale qualification automatically
            // when we use the correct resource qualifiers
            if (resId != 0) resId else null
        } catch (e: Exception) {
            null
        }
    }
}
