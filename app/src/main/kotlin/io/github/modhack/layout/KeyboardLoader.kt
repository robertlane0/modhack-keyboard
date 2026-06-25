package io.github.modhack.layout

import android.content.Context
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import android.util.Xml
import io.github.modhack.R
import io.github.modhack.keycodes.KeyCodes
import io.github.modhack.model.Key
import io.github.modhack.model.KeyboardLayout
import io.github.modhack.model.Row
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * Parses keyboard layout XML files into immutable [KeyboardLayout] objects.
 */
class KeyboardLoader(private val context: Context) {

    /**
     * Parses the given XML resource [resId] into a [KeyboardLayout].
     */
    fun loadLayout(resId: Int, id: String, mode: Int): KeyboardLayout {
        val parser = context.resources.getXml(resId)
        return try {
            parseKeyboard(parser, id, mode)
        } finally {
            parser.close()
        }
    }

    private fun parseKeyboard(parser: XmlResourceParser, id: String, keyboardMode: Int): KeyboardLayout {
        val rows = mutableListOf<Row>()
        var eventType = parser.eventType
        var kbWidth = 0
        var kbHeight = 0

        var defaultWidth = 10
        var defaultHeight = 10

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Keyboard" -> {
                            val a = context.obtainStyledAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard)
                            defaultWidth = getDimensionOrFraction(a, R.styleable.Keyboard_keyWidth, 100, 10)
                            defaultHeight = getDimensionOrFraction(a, R.styleable.Keyboard_keyHeight, 100, 10)
                            a.recycle()
                        }
                        "Row" -> {
                            val row = parseRow(parser, defaultWidth, defaultHeight)
                            if (row != null) {
                                rows.add(row)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        
        // Compute Y positions for each key within each row
        var y = 0
        val positionedRows = rows.map { row ->
            var x = 0
            val positionedKeys = row.keys.map { key ->
                val positioned = key.copy(x = x, y = y)
                x += key.width
                positioned
            }
            val positionedRow = row.copy(keys = positionedKeys)
            y += row.defaultHeight
            positionedRow
        }

        return KeyboardLayout(id, positionedRows, 100, y, keyboardMode)
    }

    private fun parseRow(parser: XmlResourceParser, defaultKbWidth: Int, defaultKbHeight: Int): Row? {
        val a = context.obtainStyledAttributes(Xml.asAttributeSet(parser), R.styleable.Row)
        val defaultWidth = getDimensionOrFraction(a, R.styleable.Row_keyWidth, 100, defaultKbWidth)
        val defaultHeight = getDimensionOrFraction(a, R.styleable.Row_keyHeight, 100, defaultKbHeight)
        val mode = a.getInt(R.styleable.Row_mode, 0)
        val isExtension = a.getBoolean(R.styleable.Row_isExtension, false)
        a.recycle()

        val keys = mutableListOf<Key>()
        var eventType = parser.next()
        var currentX = 0
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "Key") {
                val key = parseKey(parser, currentX, defaultWidth, defaultHeight)
                keys.add(key)
                currentX += key.width
            } else if (eventType == XmlPullParser.END_TAG && parser.name == "Row") {
                break
            }
            eventType = parser.next()
        }

        return Row(keys, defaultHeight, defaultWidth, mode, isExtension)
    }

    private fun parseKey(parser: XmlResourceParser, xPos: Int, rowDefaultWidth: Int, rowDefaultHeight: Int): Key {
        val a = context.obtainStyledAttributes(Xml.asAttributeSet(parser), R.styleable.Key)
        val width = getDimensionOrFraction(a, R.styleable.Key_keyWidth, 100, rowDefaultWidth)
        val height = getDimensionOrFraction(a, R.styleable.Key_keyHeight, 100, rowDefaultHeight)
        val codesStr = a.getString(R.styleable.Key_codes) ?: ""
        val label = a.getString(R.styleable.Key_label) ?: ""
        val shiftLabel = a.getString(R.styleable.Key_shiftLabel) ?: ""
        val hint = a.getString(R.styleable.Key_hint)
        val altHint = a.getString(R.styleable.Key_altHint)
        val iconResId = a.getResourceId(R.styleable.Key_iconSrc, 0)
        val isModifier = a.getBoolean(R.styleable.Key_isModifier, false)
        val isRepeatable = a.getBoolean(R.styleable.Key_isRepeatable, false)
        val isCursor = a.getBoolean(R.styleable.Key_isCursor, false)
        val popupKeysStr = a.getString(R.styleable.Key_popupKeys)
        a.recycle()

        val codes = parseCodes(codesStr)
        val popupKeys = popupKeysStr?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }

        return Key(
            codes = codes,
            label = label,
            shiftLabel = shiftLabel,
            hint = hint,
            altHint = altHint,
            icon = null, // Icon loaded by UI layer via iconResId
            iconResId = iconResId,
            width = width,
            height = height,
            x = xPos,
            y = 0,
            isModifier = isModifier,
            isRepeatable = isRepeatable,
            isCursor = isCursor,
            popupKeys = popupKeys
        )
    }

    private fun parseCodes(codesStr: String): List<Int> {
        if (codesStr.isEmpty()) return emptyList()
        return codesStr.split(",").mapNotNull {
            val s = it.trim()
            if (s.isEmpty()) null
            else if (s.length == 1 && !s[0].isDigit() && s != "-") s[0].code
            else s.toIntOrNull()
        }
    }

    private fun getDimensionOrFraction(a: android.content.res.TypedArray, index: Int, base: Int, defValue: Int): Int {
        val value = a.peekValue(index) ?: return defValue
        return if (value.type == android.util.TypedValue.TYPE_FRACTION) {
            a.getFraction(index, base, base, defValue.toFloat()).toInt()
        } else if (value.type == android.util.TypedValue.TYPE_DIMENSION) {
            a.getDimensionPixelSize(index, defValue)
        } else {
            defValue
        }
    }
}
