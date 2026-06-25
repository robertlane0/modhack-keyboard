package io.github.modhack.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.modhack.model.Row
import io.github.modhack.service.MHInputService

/**
 * Renders a row of keys.
 */
@Composable
fun RowComposable(row: Row, service: MHInputService) {
    // Stub implementation
    Box(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Row")
    }
}
