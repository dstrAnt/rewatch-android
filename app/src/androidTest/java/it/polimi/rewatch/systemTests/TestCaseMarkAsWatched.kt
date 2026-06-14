package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import it.polimi.rewatch.MainActivity
import org.junit.Rule
import org.junit.Test

class TestCaseMarkAsWatched {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<MainActivity>()

    @Test
    fun testMarkFilmsDirectly() {
        composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Watch").performClick()

        composeTestRule.waitUntil(10_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watched")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testMarkFilmsFromMediaPage() {
        composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("media_page").assertExists()

        //Click on the "Add to List" Button

        composeTestRule.onNodeWithTag("mediaBottomSheet").assertExists().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Watch").performClick()

        composeTestRule.waitUntil(10_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watched")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testMarkFilmsFromMediaPageThroughRating() {
        composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
            .performTouchInput { longClick() }

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

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }
}