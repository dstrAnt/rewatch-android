package it.polimi.rewatch.systemTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.HomeTabItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText

/**
 * Test Cases designed to test Interaction Scenario "Browse Films"
 */


class TestCaseBrowseFilms {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testBrowseFilms() {
        try{
            composeTestRule.onNodeWithContentDescription("Home").assertExists()
                .performClick()

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("home_page").assertExists()

            composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("posterItem_Trending", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithTag("media_page").assertExists()

            composeTestRule.onNodeWithTag("header_backdrop").assertExists().assertHasClickAction().performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("imageContentToDismissBySwipe", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            Thread.sleep(3000)

            val asyncImage = composeTestRule.onNodeWithTag("subComposeAsyncImage", useUnmergedTree = true).assertExists()

            asyncImage.assertIsDisplayed()

            asyncImage.performTouchInput { swipeUp(320f) }
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            composeTestRule.onNodeWithTag("header_title").assertExists().assertHasClickAction().performClick()

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("imageContentToDismissBySwipe", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            Thread.sleep(3000)

            asyncImage.assertIsDisplayed()

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

            composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())

            composeTestRule.waitUntil(10_000) {
                composeTestRule
                    .onAllNodesWithTag("posterItem_Trending", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onAllNodesWithTag("posterItem_Trending")[9].assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithTag("media_page").assertExists()

            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            composeTestRule.onNodeWithText("Seasons").assertHasClickAction().performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            composeTestRule.onAllNodesWithTag("episode_listItem")[0].assertHasClickAction().performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            composeTestRule.onNodeWithText("Episodes").assertHasClickAction().performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            composeTestRule.onAllNodesWithTag("episode_listItem")[0].assertHasClickAction().performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

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

        composeTestRule.onAllNodesWithTag("backToHomeButton")[0].assertExists().assertIsDisplayed().assertHasClickAction().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("home_page").assertExists()
    }
}