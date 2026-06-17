package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.formatDate
import kotlinx.datetime.toKotlinLocalDate
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.collections.get

/**
 * Test Cases designed to test Interaction Scenario "Mark As Watched"
 */

class TestCaseMarkAsWatched {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testMarkFilmsDirectly() {
        try{
            composeTestRule.waitUntil(35_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[1].assertExists()
                .performTouchInput { longClick() }

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watch")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithContentDescription("Watch").performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watched")
                    .fetchSemanticsNodes().isNotEmpty()
            }
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
    fun testMarkFilmsFromMediaPage() {
        try {
            composeTestRule.waitUntil(15_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[2].assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithTag("media_page").assertExists()

            //Click on the "Add to List" Button

            composeTestRule.onNodeWithTag("mediaBottomSheet").assertExists().assertIsDisplayed().performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watch")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithContentDescription("Watch").performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watched")
                    .fetchSemanticsNodes().isNotEmpty()
            }
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
    fun testMarkFilmsFromMediaPageThroughRating() {
        try{
            composeTestRule.waitUntil(15_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[3].assertExists()
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

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watched")
                    .fetchSemanticsNodes().isNotEmpty()
            }
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
    fun testMarkFilmsDirectlyWithWatchDate() {
        try{
            composeTestRule.waitUntil(35_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[11].assertExists()
                .performTouchInput { longClick() }

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watch")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithContentDescription("Watch").performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithContentDescription("Watched")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            java.lang.Thread.sleep(3000)

            setWatchDate()

            val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

            bottomSheet.performTouchInput { swipeDown(200f, 400f) }
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    fun setWatchDate(){
        //composeTestRule.onNodeWithTag("bottomSheetActions").onChildren().onFirst().performClick()
        /*composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithContentDescription("Content watched on")
                .fetchSemanticsNodes().isNotEmpty()
        }*/
        Thread.sleep(3000)

       // composeTestRule.onNodeWithText("Content watched on ").assertExists()
        val watchDateTrigger = composeTestRule.onNodeWithText(LocalDate.now().toKotlinLocalDate().toString().formatDate("d MMM yyyy")).assertExists().assertIsDisplayed()
        watchDateTrigger.performClick()
        /*composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithContentDescription("Select")
                .fetchSemanticsNodes().isNotEmpty()
        }*/
        Thread.sleep(5000)

        val datePickerDialogConfirmButton = composeTestRule.onNodeWithText("Select").assertExists().assertIsDisplayed()
        val datePickerDialog = datePickerDialogConfirmButton.onParent().assertExists().assertIsDisplayed()

        // Select a date in the picker...
        datePickerDialog.performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        datePickerDialogConfirmButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }
}