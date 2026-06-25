package io.github.modhack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.modhack.service.MHInputService
import io.github.modhack.ui.theme.LocalKeyboardColors

/**
 * Renders the suggestion strip.
 */
@Composable
fun CandidateStripComposable(service: MHInputService) {
    val suggestions by service.suggestions.collectAsState()
    val colors = LocalKeyboardColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(colors.candidateBackground),
        contentAlignment = Alignment.Center
    ) {
        if (suggestions.isEmpty()) {
            Text("No suggestions", color = colors.candidateText)
        } else {
            LazyRow {
                items(suggestions.size) { index ->
                    Text(suggestions[index].word, color = colors.candidateText)
                }
            }
        }
    }
}
