package io.github.modhack.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.modhack.model.Key
import io.github.modhack.service.MHInputService

/**
 * Renders a single key.
 */
@Composable
fun KeyComposable(key: Key, service: MHInputService) {
    // Stub implementation
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(key.label ?: "")
    }
}
