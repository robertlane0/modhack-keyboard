package io.github.modhack.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.modhack.model.Suggestion
import io.github.modhack.service.MHInputService
import io.github.modhack.ui.theme.LocalKeyboardColors

/**
 * Renders the suggestion strip above the keyboard rows.
 *
 * Features:
 * - Displays a horizontal scrollable list of word suggestions
 * - Tap a suggestion to commit it to the text field
 * - Long-press a suggestion to delete it from the user dictionary
 * - Shows "No suggestions" when the list is empty
 *
 * @param service The input method service providing suggestions and text commitment.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CandidateStripComposable(service: MHInputService) {
    val suggestions by service.suggestions.collectAsState()
    val colors = LocalKeyboardColors.current
    var showDeleteConfirm by remember { mutableStateOf<Suggestion?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(colors.candidateBackground),
        contentAlignment = Alignment.CenterStart
    ) {
        if (suggestions.isEmpty()) {
            Text(
                text = "",
                color = colors.candidateText.copy(alpha = 0.4f),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(suggestions, key = { it.word }) { suggestion ->
                    CandidateWord(
                        suggestion = suggestion,
                        colors = colors,
                        onClick = {
                            if (suggestion.word.isEmpty()) return@CandidateWord
                            service.onKey(suggestion.word.first().code)
                            for (i in 1 until suggestion.word.length) {
                                service.onKey(suggestion.word[i].code)
                            }
                        },
                        onLongClick = {
                            showDeleteConfirm = suggestion
                        }
                    )
                }
            }
        }

        showDeleteConfirm?.let { suggestion ->
            DeleteConfirmationOverlay(
                suggestion = suggestion,
                colors = colors,
                onConfirm = {
                    showDeleteConfirm = null
                },
                onDismiss = {
                    showDeleteConfirm = null
                }
            )
        }
    }
}

/**
 * A single candidate word with tap and long-press support.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CandidateWord(
    suggestion: Suggestion,
    colors: io.github.modhack.ui.theme.KeyboardColors,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Text(
            text = suggestion.word,
            color = colors.candidateText,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Simple overlay shown when long-pressing a candidate word.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeleteConfirmationOverlay(
    suggestion: Suggestion,
    colors: io.github.modhack.ui.theme.KeyboardColors,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(colors.candidateBackground)
            .combinedClickable(
                onClick = onDismiss,
                onLongClick = onConfirm
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Delete \"${suggestion.word}\"?",
                color = colors.candidateText,
                fontSize = 14.sp
            )
        }
    }
}
