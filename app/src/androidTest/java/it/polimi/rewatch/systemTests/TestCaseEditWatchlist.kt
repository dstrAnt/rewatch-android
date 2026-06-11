package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseEditWatchlist {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("In watchlist").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val contentPageNode = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onAllNodesWithTag("posterItem")[1].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("In watchlist").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val contentPageNode2 = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode2.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testWatchlistMovies() {
        composeTestRule.onNodeWithContentDescription("Movies").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        removeFromWatchlist()
    }

    @Test
    fun testWatchlistTvShows() {
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        removeFromWatchlist()
    }

    fun removeFromWatchlist(){
        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watchlist").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        watchedTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val firstItem = composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists().assertIsDisplayed()
        firstItem.performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("In watchlist").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val contentPageNode = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        firstItem.assertDoesNotExist()
    }


}