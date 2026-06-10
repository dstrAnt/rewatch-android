package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseEditWatchlist {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        /*composeTestRule.setContent {
            MainActivity()
        }*/
    }

    @Test
    fun testWatchlistMovies() {
        composeTestRule.onNodeWithContentDescription("Movies").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchlistNode = composeTestRule.onNodeWithText("Watchlist").assertExists().assertIsDisplayed()
        watchlistNode.performClick()

        val watchlistTab = watchlistNode.onParent().assertExists()
        watchlistTab.assertIsSelected()
    }

    @Test
    fun testWatchlistTvShows() {
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchlistNode = composeTestRule.onNodeWithText("Watchlist").assertExists().assertIsDisplayed()
        watchlistNode.performClick()

        val watchlistTab = watchlistNode.onParent().assertExists()
        watchlistTab.assertIsSelected()
    }
}