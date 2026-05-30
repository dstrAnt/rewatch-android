package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.screens.tracking.WatchTabItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackingPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val type: WatchTabItem = WatchTabItem.WATCHED

    @Before
    fun setup() {

        val actions = NavigationActions(onNavigateToPerson = { id -> },

            onNavigateToMedia = { id -> },
            onNavigateBack = {

            },
            onFullscreenImage = { url ->

            },
            onBottomSheet = { info ->  },
            onNavigateToLogin = {

            },
            onBackToHome = { tab ->

            })

        val outerPadding = PaddingValues();

        composeTestRule.setContent {
            TrackingPage(actions = actions, outerPadding = outerPadding, type = MediaType.MOVIE)
        }
    }

    @Test
    fun testSortByDate() {
        val mediaType = type

        val dateFilter = composeTestRule.onNodeWithTag("filterChip_Date")
    }

    @Test
    fun testSortByRating() {
        val mediaType = type

        val ratingFilter = composeTestRule.onNodeWithTag("filterChip_Rating")
    }

    @Test
    fun testSortAlphabetically() {
        val mediaType = type

        val alphabeticFilter = composeTestRule.onNodeWithTag("filterChip_A-Z")
    }

    @Test
    fun testSortByPopularity() {
        val mediaType = WatchTabItem.WATCHLIST

        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_Popularity")
    }
}