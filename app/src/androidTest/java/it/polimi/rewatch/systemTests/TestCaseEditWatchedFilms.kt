package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.model.MediaType
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseEditWatchedFilms {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

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

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[1].assertExists()
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

        val bottomSheet2 = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet2.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testWatchedMovies() {
        composeTestRule.onNodeWithContentDescription("Movies").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        removeFromWatchedFilms()
    }

    @Test
    fun testWatchedTvShows() {
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        removeFromWatchedFilms()
    }

    fun removeFromWatchedFilms(){
        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        watchedTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val firstItem = composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists().assertIsDisplayed()
        firstItem.performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Watch").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val contentPageNode = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        firstItem.assertDoesNotExist()
    }

}