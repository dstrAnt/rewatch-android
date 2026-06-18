package it.polimi.rewatch.unitTests.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import it.polimi.rewatch.animationSpec
import it.polimi.rewatch.components.FullScreenImageOverlay
import it.polimi.rewatch.model.MovieId
import org.junit.Rule
import org.junit.Test

/**
 * Test Case designed to test Composable FullScreenImageOverlay
 */


class FullScreenImageOverlayTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    fun <T> animationSpec() = tween<T>(durationMillis = 300)

    @Test
    fun testImageOverlaySwipeDismiss(){

        // Setting Content for FullScreenImageOverlay
        composeTestRule.setContent {
            var isOverlayVisible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = isOverlayVisible,
                enter = fadeIn(animationSpec = animationSpec()),
                exit = fadeOut(animationSpec = animationSpec()),
            ){
                FullScreenImageOverlay("",
                    onDismiss =  { isOverlayVisible = false } )
            }
        }

        Thread.sleep(5000)

        val asyncImage = composeTestRule.onNodeWithTag("subComposeAsyncImage", useUnmergedTree = true).assertExists()

        asyncImage.assertIsDisplayed()

        asyncImage.performTouchInput { swipeUp(320f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        asyncImage.assertIsNotDisplayed()

    }

    @Test
    fun testImageOverlayTapDismiss(){

        composeTestRule.setContent {
            var isOverlayVisible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = isOverlayVisible,
                enter = fadeIn(animationSpec = animationSpec()),
                exit = fadeOut(animationSpec = animationSpec()),
            ){
                FullScreenImageOverlay("", onDismiss =  { isOverlayVisible = false } )
            }
        }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val imageBackground = composeTestRule.onRoot().assertExists()

        imageBackground.performTouchInput { click() }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Full Screen").assertIsNotDisplayed()

    }

    @Test
    fun testImageOverlayCloseButtonDismiss(){

        composeTestRule.setContent {
            var isOverlayVisible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = isOverlayVisible,
                enter = fadeIn(animationSpec = animationSpec()),
                exit = fadeOut(animationSpec = animationSpec()),
            ){
                FullScreenImageOverlay("", onDismiss =  { isOverlayVisible = false } )
            }
        }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val overCloseButton = composeTestRule.onNodeWithContentDescription("Close").onParent();

        overCloseButton.assertExists().assertIsDisplayed()

        overCloseButton.performTouchInput { click() }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Full Screen").assertIsNotDisplayed()

    }
}