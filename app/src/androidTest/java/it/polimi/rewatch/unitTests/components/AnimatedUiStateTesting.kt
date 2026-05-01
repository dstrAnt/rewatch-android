package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import org.junit.Test
import org.junit.Rule

class AnimatedUiStateTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAnimatedUIStateLoading() {
        composeTestRule.setContent {
            AnimatedUiState(
                UiState.Loading, modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { }
        }
    }

    /*@Test
    fun testAnimatedUIStateSuccess() {
        composeTestRule.setContent {
            AnimatedUiState(
                UiState.Success<String>, modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { }
        }
    }

    @Test
    fun testAnimatedUIStateError() {
        composeTestRule.setContent {
            AnimatedUiState(
                UiState.Error, modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { }
        }
    }*/
}