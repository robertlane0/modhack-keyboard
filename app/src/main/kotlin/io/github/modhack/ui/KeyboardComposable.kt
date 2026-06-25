package io.github.modhack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.modhack.service.MHInputService
import io.github.modhack.ui.theme.LocalKeyboardColors

/**
 * Root composable for the keyboard UI.
 */
@Composable
fun KeyboardUI(service: MHInputService) {
    val keyboardState by service.keyboardState.collectAsState()
    val colors = LocalKeyboardColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.keyboardBackground)
    ) {
        CandidateStripComposable(service)
        
        // This is a stub for Phase 4 rendering.
        // It should render rows and keys.
    }
}
