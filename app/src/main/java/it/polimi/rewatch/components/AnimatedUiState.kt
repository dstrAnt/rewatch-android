package it.polimi.rewatch.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.UiState

/**
 * [AnimatedUiState] is a component that masks the underlying [AnimatedContent]
 * for code readability and to ease the reuse of the [ErrorContent], that is always the same
 */
@Composable
fun <T> AnimatedUiState(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    animationDurationMillis: Int = 1000,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (String) -> Unit = ::ErrorContent,
    successContent: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            if (initialState is UiState.Success && targetState is UiState.Success) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                fadeIn(tween(animationDurationMillis)) togetherWith fadeOut()
            }
        },
        modifier = modifier.fillMaxSize(),
        label = "uiStateTransition",
    ) { targetState ->
        when (targetState) {
            is UiState.Loading -> loadingContent()
            is UiState.Success -> successContent(targetState.data)
            is UiState.Error -> errorContent(targetState.msg)
        }
    }
}

/**
 * [ErrorContent] displays a blank page with a customizable error message
 */
@Composable
private fun ErrorContent(msg: String) {
    // Default error UI (can be overridden per screen)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
            Text("Error: $msg", color = Color.White)
        }
    }
}