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
import it.polimi.rewatch.model.TrackingMedia
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
        val actualValues = runBlocking { values.map { result -> result.getOrNull() }.first() }

        val expectedResult = actualValues?.sortedWith(compareBy { it.date })

        assertEquals(expectedResult, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(true) { it.date })

        val expectedResultDescending = actualValues?.sortedWith(
            compareByDescending { it.date }
        )

        assertEquals(expectedResultDescending, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(false) { it.date })

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

        if (type == WatchTabItem.WATCHED) {
            composeTestRule.onNodeWithText("April").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("March").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("2024").assertExists().assertIsDisplayed()
        }
        else{
            composeTestRule.onNodeWithText("February").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("May").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("2025").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun testSortByRating() {
        val values = if (type == WatchTabItem.WATCHED) fakeRepo.watchedFlow else fakeRepo.watchlistFlow
        val actualValues = runBlocking { values.map { result -> result.getOrNull() }.first() }

        val expectedResult = actualValues?.sortedWith(compareBy { it.rating })

        assertEquals(expectedResult, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(true) { it.rating })

        val expectedResultDescending = actualValues?.sortedWith(
            compareByDescending { it.rating }
        )

        assertEquals(expectedResultDescending, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(false) { it.rating })

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

        val ratingFilter = composeTestRule.onNodeWithTag("filterChip_RATING")

        ratingFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        assertEquals(
            SortOption.RATING,
            selected
        )

        composeTestRule.onNodeWithText("Unrated").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Masterpiece").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Great").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Average").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Below Average").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortAlphabetically() {
        val values = if (type == WatchTabItem.WATCHED) fakeRepo.watchedFlow else fakeRepo.watchlistFlow
        val actualValues = runBlocking { values.map { result -> result.getOrNull() }.first() }

        val expectedResult = actualValues?.sortedWith(compareBy { it.title })

        assertEquals(expectedResult, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(true) { it.title })

        val expectedResultDescending = actualValues?.sortedWith(
            compareByDescending { it.title }
        )

        assertEquals(expectedResultDescending, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(false) { it.title })

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

        val alphabeticFilter = composeTestRule.onNodeWithTag("filterChip_ALPHABETICAL")

        alphabeticFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        assertEquals(
            SortOption.ALPHABETICAL,
            selected
        )

        composeTestRule.onNodeWithText("A").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("F").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("S").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("#").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortByPopularity() {
        val values = if (type == WatchTabItem.WATCHED) fakeRepo.watchedFlow else fakeRepo.watchlistFlow
        val actualValues = runBlocking { values.map { result -> result.getOrNull() }.first() }

        val expectedResult = actualValues?.sortedWith(compareBy { it.popularity })

        assertEquals(expectedResult, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(true) { it.popularity })

        val expectedResultDescending = actualValues?.sortedWith(
            compareByDescending { it.popularity }
        )

        assertEquals(expectedResultDescending, actualValues?.filter { it.id.type == mediaType }?.sortedByDirection(false) { it.popularity })

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

        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_POPULARITY")

        popularityFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        assertEquals(
            SortOption.POPULARITY,
            selected
        )

        composeTestRule.onNodeWithText("Trending Worldwide").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Very Popular").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Well Known").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Niche").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("#").assertExists().assertIsDisplayed()
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