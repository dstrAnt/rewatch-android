package it.polimi.rewatch.systemTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseFilmRating {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testRateFilmsDirectly() {
        composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

        bottomSheetStarRating.performTouchInput { click(center) }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val contentPageNode = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testRateFilmsFromMediaPage() {
        composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("media_page").assertExists()

        //Click on the "Add to List" Button

        composeTestRule.onNodeWithTag("mediaBottomSheet").assertExists().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

        bottomSheetStarRating.performTouchInput { click(center) }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val contentPageNode = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }
}