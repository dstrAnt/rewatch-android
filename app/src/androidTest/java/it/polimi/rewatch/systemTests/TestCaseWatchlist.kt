package it.polimi.rewatch.systemTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.tracking.TrackingPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseWatchlist {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {

        composeTestRule.setContent {
            MainActivity()
        }
    }

    @Test
    fun testWatchlistMovies() {
        composeTestRule.onNodeWithContentDescription("Movies").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        //...
    }

    @Test
    fun testWatchlistTvShows() {
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        //...
    }
}