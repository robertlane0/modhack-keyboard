package io.github.modhack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.modhack.ui.theme.LocalKeyboardColors

/**
 * Renders a popup preview of a key, shown during long-press or key hover.
 *
 * @param keyLabel The primary label of the key being previewed.
 * @param popupKeys Optional list of alternative characters to show in the popup.
 * @param onPopupKeySelected Callback when a popup key is tapped.
 */
@Composable
fun PopupComposable(
    keyLabel: String,
    popupKeys: List<String>? = null,
    onPopupKeySelected: (String) -> Unit = {}
) {
    val colors = LocalKeyboardColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .background(colors.keyBackground, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        // Primary key preview (large)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .widthIn(min = 48.dp)
                .height(56.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = keyLabel,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = colors.keyForeground
            )
        }

        // Popup keys row (if any)
        if (!popupKeys.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                popupKeys.forEach { popupKey ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .widthIn(min = 36.dp)
                            .height(40.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = popupKey,
                            fontSize = 18.sp,
                            color = colors.keyForeground
                        )
                    }
                }
            }
        }
    }
}
