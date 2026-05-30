package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbShow
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.screens.tracking.TrackingViewModel
import it.polimi.rewatch.screens.tracking.WatchTabItem
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackingPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val type: WatchTabItem = WatchTabItem.WATCHED
    val trackingViewModel = TrackingViewModel(tmdbRepo = TmdbRepository(Tmdb3 {  }), persistRepo = FakeRepository())

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
            TrackingPage(actions = actions, outerPadding = outerPadding, type = MediaType.MOVIE, viewModel = trackingViewModel)
        }
        composeTestRule.waitForIdle()
        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        //val watchlistTab = composeTestRule.onNodeWithTag("Tab_Watchlist").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        if(type == WatchTabItem.WATCHED){
            watchedTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)
        }
    }

    @Test
    fun testSortByDate() {
        val dateFilter = composeTestRule.onNodeWithTag("filterChip_Date")

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("April").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("March").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("2024").assertExists().assertIsDisplayed()

//        assert(trackingViewModel.watchlist[])
    }

    @Test
    fun testSortByRating() {
        val ratingFilter = composeTestRule.onNodeWithTag("filterChip_Rating")

        ratingFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        //composeTestRule.onAllNodesWithTag("watchlist_posterItems").assertCountEquals(11)
    }

    @Test
    fun testSortAlphabetically() {
        val alphabeticFilter = composeTestRule.onNodeWithTag("filterChip_A-Z")

        alphabeticFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        //composeTestRule.onAllNodesWithTag("watchlist_posterItems").assertCountEquals(11)
    }

    @Test
    fun testSortByPopularity() {
        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_Popularity")

        popularityFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        //composeTestRule.onAllNodesWithTag("watchlist_posterItems").assertCountEquals(11)
    }
}