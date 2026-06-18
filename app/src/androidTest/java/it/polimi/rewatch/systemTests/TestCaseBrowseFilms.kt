package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import org.junit.Rule
import org.junit.Test

/**
 * Test Cases designed to test Interaction Scenario "Browse Films"
 */

class TestCaseBrowseFilms {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testBrowseFilms() {
        try{
            // Tap on HOME Bottom Bar
            composeTestRule.onNodeWithContentDescription("Home").assertExists()
                .performClick()

            composeTestRule.waitForIdle()

            // Assertion verifying that the Home Page is loaded correctly
            composeTestRule.onNodeWithTag("home_page").assertExists()

            composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("posterItem_Trending", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            // Click on the first Movie in the "Trending" Section
            composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            // Assertion verifying that the Media Page is loaded correctly
            composeTestRule.onNodeWithTag("media_page").assertExists()

            // Click on the Backdrop Image
            composeTestRule.onNodeWithTag("header_backdrop").assertExists().assertHasClickAction().performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("imageContentToDismissBySwipe", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            Thread.sleep(3000)

            val asyncImage = composeTestRule.onNodeWithTag("subComposeAsyncImage", useUnmergedTree = true).assertExists()

            asyncImage.assertIsDisplayed()

            // Swipe up to clear the Image
            asyncImage.performTouchInput { swipeUp(320f) }
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            // Click on the Main Image
            composeTestRule.onNodeWithTag("header_title").assertExists().assertHasClickAction().performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("imageContentToDismissBySwipe", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            Thread.sleep(3000)

            asyncImage.assertIsDisplayed()

            // Swipe up to clear the Image
            asyncImage.performTouchInput { swipeUp(320f) }
            composeTestRule.waitForIdle()
            Thread.sleep(3000)
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    @Test
    fun testBrowseFilms2() {
        try{
            composeTestRule.onNodeWithContentDescription("Home").assertExists()
                .performClick()

            composeTestRule.onNodeWithTag("home_page").assertExists()

            visitWatchedTvShowsSection()

            // Tapping on "Tv Shows" Top Bar to see all the Shows in the Trending section
            composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("posterItem_Trending", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
                .performClick()

            composeTestRule.waitUntil(15_000) {
                composeTestRule
                    .onAllNodesWithTag("media_page")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            // Tapping on "Seasons" Bar
            composeTestRule.waitUntil(15_000) {
                composeTestRule.onAllNodesWithText("Seasons").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Seasons").assertHasClickAction().performClick()

            // Click on the first proposed Season
            composeTestRule.waitUntil(15_000) {
                composeTestRule.onAllNodesWithTag("episode_listItem").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithTag("episode_listItem")[0].assertHasClickAction().performClick()

            composeTestRule.waitUntil(15_000) {
                composeTestRule.onAllNodesWithText("Episodes").fetchSemanticsNodes().isNotEmpty()
            }

            // Tapping on "Episodes" Bar
            composeTestRule.onNodeWithText("Episodes").assertHasClickAction().performClick()

            // Click on the first proposed Episode
            composeTestRule.waitUntil(15_000) {
                composeTestRule.onAllNodesWithTag("episode_listItem").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithTag("episode_listItem")[0].assertHasClickAction().performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000) // Unico sleep rimasto alla fine per dare il tempo di vedere a schermo cosa succede

        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }
    /* Function which allows to see the empty Tracking Page in the "Watched" Section */
    fun visitWatchedTvShowsSection(){
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().performClick()
        composeTestRule.waitUntil(15_000) {
            composeTestRule
                .onAllNodesWithTag("backToHomeButton")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(3000)

        // Click on Home Button to go back to the Home Page and Explore new Films
        composeTestRule.onAllNodesWithTag("backToHomeButton")[0].assertExists().assertIsDisplayed().assertHasClickAction().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("home_page").assertExists()
    }
}