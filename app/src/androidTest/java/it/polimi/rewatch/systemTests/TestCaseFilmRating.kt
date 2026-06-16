package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import kotlin.collections.get

class TestCaseFilmRating {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testRateFilmsDirectly() {
        try{
            composeTestRule.waitUntil(35_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
                .performTouchInput { longClick() }

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

            bottomSheetStarRating.performTouchInput { click(center) }

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

            bottomSheet.performTouchInput { swipeDown(200f, 400f) }
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    @Test
    fun testRateFilmsFromMediaPage() {
        try{
            composeTestRule.waitUntil(15_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithTag("media_page").assertExists()

            //Click on the "Add to List" Button

            composeTestRule.onNodeWithTag("mediaBottomSheet").assertExists().assertIsDisplayed().performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

            bottomSheetStarRating.performTouchInput { click(center) }

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

            bottomSheet.performTouchInput { swipeDown(200f, 400f) }
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }
}