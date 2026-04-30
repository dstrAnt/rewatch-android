package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
    fun testImageOverlay(){

        val content = composeTestRule.setContent { FullScreenImageOverlay("", {}) }

        val asyncImage = composeTestRule.onNodeWithContentDescription("Full Screen").assertExists().assertIsDisplayed()

        asyncImage.performTouchInput { swipeUp(320f) }

        asyncImage.assertIsNotDisplayed()

    }
}