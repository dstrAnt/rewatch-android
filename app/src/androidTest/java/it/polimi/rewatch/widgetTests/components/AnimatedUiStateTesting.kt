package it.polimi.rewatch.widgetTests.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.components.AnimatedUiState
import org.junit.Rule
import org.junit.Test

/**
 * Test Case designed to test Composable AnimatedUiState
 */


class AnimatedUiStateTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    /* Testing how callbacks are invoked in Composable AnimatedUiState, for Success, Error and Loading UiState*/

    @Test
    fun testAnimatedUIStateLoading() {
        composeTestRule.setContent {
            AnimatedUiState(
                UiState.Loading, modifier = Modifier.testTag("animatedUiState"),
                loadingContent = { Text("Hi, I'm loading...") }
            ) { }
        }

        Thread.sleep(5000)
        composeTestRule.onNodeWithTag("animatedUiState").assertExists()
        composeTestRule.onNodeWithText("Hi, I'm loading...").assertExists().assertIsDisplayed()
    }

    @Test
    fun testAnimatedUIStateSuccess() {
        composeTestRule.setContent {
            AnimatedUiState(
                state = UiState.Success("String"), modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { data -> Row(){ Text("I am going to show some data: $data") } }
        }

        Thread.sleep(5000)
        composeTestRule.onNodeWithTag("animatedUiState").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("I am going to show some data: String").assertExists().assertIsDisplayed()
    }

    @Test
    fun testAnimatedUIStateError() {
        composeTestRule.setContent {
            AnimatedUiState(
                UiState.Error("Hi, I'm an error!"), modifier = Modifier.testTag("animatedUiState"),
                loadingContent = {  }
            ) { }
        }

        Thread.sleep(5000)
        composeTestRule.onNodeWithTag("animatedUiState").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Error: Hi, I'm an error!").assertExists().assertIsDisplayed()
    }
}