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
 */
class LayoutCache(private val context: Context) {
    private val loader = KeyboardLoader(context)
    private val cache = LruCache<LayoutId, KeyboardLayout>(10)

    /**
     * Asynchronously gets or loads a [KeyboardLayout].
     */
    suspend fun getLayout(mode: InputMode, locale: String, orientation: Int, fullMode: String): KeyboardLayout {
        val id = LayoutId(mode, locale, orientation, fullMode)
        return cache.get(id) ?: withContext(Dispatchers.IO) {
            val resId = getLayoutResource(mode, fullMode)
            val layout = loader.loadLayout(resId, id.toString(), mode.ordinal)
            cache.put(id, layout)
            layout
        }
    }

    private fun getLayoutResource(mode: InputMode, fullMode: String): Int {
        return when (mode) {
            InputMode.TEXT -> {
                when (fullMode) {
                    "full" -> R.xml.kbd_full
                    "full_fn" -> R.xml.kbd_full_fn
                    else -> R.xml.kbd_qwerty
                }
            }
            InputMode.SYMBOLS -> R.xml.kbd_symbols
            InputMode.PHONE -> R.xml.kbd_phone
            else -> R.xml.kbd_qwerty
        }
    }
}
