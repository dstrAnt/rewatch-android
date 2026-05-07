package it.polimi.rewatch.unitTests.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
                loadingContent = { Text("Hi, I'm loading...") }
            ) { }
        }

        composeTestRule.onNodeWithTag("animatedUiState").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Hi, I'm loading...").assertExists().assertIsDisplayed()
    }

    @Test
    fun testAnimatedUIStateSuccess() {
        composeTestRule.setContent {
            AnimatedUiState(
                state = UiState.Success("String"), modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { data -> Row(){ Text("I am going to show some data:") } }
        }

        composeTestRule.onNodeWithTag("animatedUiState").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("I am going to show some data:").assertExists().assertIsDisplayed()
    }

    @Test
    fun testAnimatedUIStateError() {
        composeTestRule.setContent {
            AnimatedUiState(
                UiState.Error("Hi, I'm an error!"), modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { }
        }

        composeTestRule.onNodeWithTag("animatedUiState").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Error: Hi, I'm an error!").assertExists().assertIsDisplayed()
    }
}