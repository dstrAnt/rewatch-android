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
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.home.HomeTabItem
import it.polimi.rewatch.screens.tracking.SortFilterChips
import it.polimi.rewatch.screens.tracking.SortOption
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.screens.tracking.TrackingViewModel
import it.polimi.rewatch.screens.tracking.WatchTabItem
import it.polimi.rewatch.screens.tracking.sortedByDirection
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrackingPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val type: WatchTabItem = WatchTabItem.WATCHED
    val mediaType: MediaType = MediaType.MOVIE
    //val trackingViewModel = TrackingViewModel(tmdbRepo = TmdbRepository(Tmdb3 {  }), persistRepo = FakeRepository())
    val fakeRepo = FakeRepository()

    @Before
    fun setup() {

       /* val actions = NavigationActions(onNavigateToPerson = { id -> },

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
        }*/


    }

    @Test
    fun testSortByDate() {

        val values = if (type == WatchTabItem.WATCHED) fakeRepo.watchedFlow else fakeRepo.watchlistFlow
        val expectedResult = if (type == WatchTabItem.WATCHED) fakeRepo.watchedFlow else fakeRepo.watchlistFlow

        val actualValues = runBlocking { values.map { result -> result.getOrNull() }.first() }

        assertEquals(
            expectedResult,
            actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(true) { it.date }
        )

        assertNotEquals(
            expectedResult,
            actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(false) { it.date }
        )

        var selected: SortOption? = null

        composeTestRule.setContent {
            SortFilterChips(
                options = SortOption.entries,
                selectedSort = SortOption.DATE,
                isAscending = false,
                onSortSelected = {
                    selected = it
                },
                label = SortOption::label
            )
        }

        val dateFilter = composeTestRule.onNodeWithTag("filterChip_DATE").assertExists()

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        assertEquals(
            SortOption.DATE,
            selected
        )

        composeTestRule.onNodeWithText("April").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("March").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("2024").assertExists().assertIsDisplayed()
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

    @Test
    fun testEmptyListCase(){
        //UiState.Success(emptyList())

        composeTestRule
            .onNodeWithText("Explore")
            .assertExists()

        composeTestRule
            .onNodeWithText("No Movies in Watchlist")
            .assertExists()
    }

    @Test
    fun testTopTabBar(){
        composeTestRule
            .onNodeWithText("Watchlist")
            .assertExists()

        composeTestRule
            .onNodeWithText("Watched")
            .assertExists()

        var selected = WatchTabItem.WATCHLIST

        composeTestRule.setContent {
         /*   TopTabBar(
                ...
            selectedTab = selected,
            onClick = { selected = it }
            )*/
        }
        composeTestRule
            .onNodeWithText("Watched")
            .performClick()

        assertEquals(
            WatchTabItem.WATCHED,
            selected
        )
    }

    @Test
    fun testOnBackToHome(){
        //val actions = NavigationActions(
        //            ...
        //        onBackToHome = {
        //            destination = it
        //        }
        //        )
        var destination: HomeTabItem? = null

        composeTestRule
            .onNodeWithText("Explore")
            .performClick()

        assertEquals(
            HomeTabItem.MOVIES,
            destination
        )
    }
}