package io.github.modhack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.modhack.model.Key
import io.github.modhack.model.Row
import io.github.modhack.service.MHInputService

/**
 * Renders a row of keys within the keyboard.
 *
 * Each key in the row is rendered as a [KeyComposable] with proportional
 * width based on the key's width in abstract units relative to the
 * total row width.
 *
 * @param row The row data containing keys to render.
 * @param service The input service for dispatching key actions.
 * @param keyboardHeight Total keyboard height in dp for proportional sizing.
 * @param totalLayoutHeight Total layout height in abstract units (sum of all row heights).
 */
@Composable
fun RowComposable(
    row: Row,
    service: MHInputService,
    keyboardHeight: Float = 200f,
    totalLayoutHeight: Int = 40
) {
    var popupKey by remember { mutableStateOf<Key?>(null) }

    val rowHeight = keyboardHeight * row.defaultHeight / totalLayoutHeight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        row.keys.forEach { key ->
            Box(modifier = Modifier.weight(key.width.toFloat())) {
                KeyComposable(
                    key = key,
                    service = service,
                    keyHeight = rowHeight,
                    rowDefaultHeight = row.defaultHeight,
                    onLongPress = { popupKey = it }
                )
            }
        }
    }

    popupKey?.let { key ->
        PopupComposable(
            keyLabel = key.shiftLabel.ifEmpty { key.label },
            popupKeys = key.popupKeys,
            onPopupKeySelected = { selected ->
                popupKey = null
                // Commit the selected popup character directly
                selected.firstOrNull()?.let { char ->
                    service.onKey(char.code)
                }
            }
        )
    }
}
