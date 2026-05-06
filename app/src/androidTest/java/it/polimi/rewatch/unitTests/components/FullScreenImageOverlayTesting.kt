package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import it.polimi.rewatch.components.FullScreenImageOverlay
import it.polimi.rewatch.model.MovieId
import org.junit.Rule
import org.junit.Test

class FullScreenImageOverlayTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testImageOverlaySwipeDismiss(){

        composeTestRule.setContent { FullScreenImageOverlay("") {} }

        val asyncImage = composeTestRule.onNodeWithContentDescription("Full Screen").assertExists().assertIsDisplayed()

        asyncImage.performTouchInput { swipeUp(320f) }

        asyncImage.assertIsNotDisplayed()

    }

    @Test
    fun testImageOverlayTapDismiss(){

        composeTestRule.setContent { FullScreenImageOverlay("") {} }

        val imageBackground = composeTestRule.onRoot().assertExists()

        imageBackground.performTouchInput { click() }

        imageBackground.assertIsNotDisplayed()

    }

    @Test
    fun testImageOverlayCloseButtonDismiss(){

        composeTestRule.setContent { FullScreenImageOverlay("") {} }

        val overCloseButton = composeTestRule.onRoot().onChild();

        overCloseButton.assertExists().assertIsDisplayed()

        overCloseButton.performTouchInput { click() }

        overCloseButton.assertIsNotDisplayed()

    }
}