package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbShow
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.FakeRepository.Companion.empty
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.common.fakeWatchlist
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.TrackingMedia
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import it.polimi.rewatch.screens.home.HomeTabItem
import it.polimi.rewatch.screens.tracking.SortFilterChips
import it.polimi.rewatch.screens.tracking.SortOption
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.screens.tracking.TrackingViewModel
import it.polimi.rewatch.screens.tracking.WatchTabItem
import it.polimi.rewatch.screens.tracking.sortedByDirection
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.collections.emptyList

class TrackingPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    var type: WatchTabItem = WatchTabItem.WATCHED
    val defaultMediaType: MediaType = MediaType.MOVIE

    lateinit var expectedPageTitle : String
    lateinit var expectedLabel : String

    var fakeRepo = FakeRepository()

    var destination: HomeTabItem? = null

    //@Before
    fun setup(mediaType : MediaType) {
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
                 destination = tab
             })

        val outerPadding = PaddingValues();

        fakeRepo.allowEnrichment = false

        val trackingViewModel = TrackingViewModel(tmdbRepo = TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")), persistRepo = fakeRepo)

        composeTestRule.setContent {
            TrackingPage(actions = actions, outerPadding = outerPadding, type = mediaType, viewModel = trackingViewModel)
        }
        composeTestRule.waitForIdle()
        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        //val watchlistTab = composeTestRule.onNodeWithTag("Tab_Watchlist").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        if(type == WatchTabItem.WATCHED){
            watchedTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
        }

        expectedPageTitle = if(mediaType == MediaType.MOVIE) "Movies" else "Tv Shows"
        expectedLabel = if(mediaType == MediaType.MOVIE) "Movies" else "Shows"
    }

    fun justScrollDown(grid: SemanticsNodeInteraction, times: Int){
        for (i in List(times) { it }){
            grid.performTouchInput { swipeUp(500f, 0f) }
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
        }
    }

    @Test
    fun testSortByDate() {
        setup(defaultMediaType)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_Date", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(3000)

        val dateFilter = composeTestRule.onNodeWithTag("filterChip_Date").assertExists().assertIsDisplayed().assertIsSelectable()

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        dateFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        if (type == WatchTabItem.WATCHED) {
            composeTestRule.onNodeWithText("April").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("March").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("2024").assertExists().assertIsDisplayed()
            justScrollDown(lazyGrid, 1)
            composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
        }
        else{
            composeTestRule.onNodeWithText("May").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("2025").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("February").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun testSortByDateTvShows() {
        setup(MediaType.TV_SHOW)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_Date", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(3000)

        val dateFilter = composeTestRule.onNodeWithTag("filterChip_Date").assertExists().assertIsDisplayed().assertIsSelectable()

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        dateFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        if (type == WatchTabItem.WATCHED) {
            composeTestRule.onNodeWithText("May").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("March").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("2024").assertExists().assertIsDisplayed()
            justScrollDown(lazyGrid, 1)
            composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
        }
        else{
            composeTestRule.onNodeWithText("May").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("This Month").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("2025").assertExists().assertIsDisplayed()
            justScrollDown(lazyGrid, 1)
            composeTestRule.onNodeWithText("February").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun testSortByRating() {
        setup(defaultMediaType)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_Rating", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(3000)

        val ratingFilter = composeTestRule.onNodeWithTag("filterChip_Rating").assertIsDisplayed().assertIsSelectable()

        ratingFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        ratingFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onNodeWithText("Masterpiece").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Great").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Average").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Below Average").assertExists().assertIsDisplayed()
        justScrollDown(lazyGrid, 1)
        composeTestRule.onNodeWithText("Unrated").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortByRatingTvShows() {
        setup(MediaType.TV_SHOW)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_Rating", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(3000)

        val ratingFilter = composeTestRule.onNodeWithTag("filterChip_Rating").assertIsDisplayed().assertIsSelectable()

        ratingFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        ratingFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onNodeWithText("Masterpiece").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Great").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Average").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Below Average").assertExists().assertIsDisplayed()
        justScrollDown(lazyGrid, 1)
        composeTestRule.onNodeWithText("Unrated").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortAlphabetically() {
        setup(defaultMediaType)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_A-Z", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(3000)

        val alphabeticFilter = composeTestRule.onNodeWithTag("filterChip_A-Z").assertIsDisplayed().assertIsSelectable()

        alphabeticFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        alphabeticFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onNodeWithText("#").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("B").assertExists().assertIsDisplayed()
        justScrollDown(lazyGrid, 1)
        composeTestRule.onNodeWithText("S").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("T").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortAlphabeticallyTvShows() {
        setup(MediaType.TV_SHOW)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_A-Z", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(3000)

        val alphabeticFilter = composeTestRule.onNodeWithTag("filterChip_A-Z").assertIsDisplayed().assertIsSelectable()

        alphabeticFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        alphabeticFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onNodeWithText("#").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("C").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("F").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("N").assertExists().assertIsDisplayed()
        justScrollDown(lazyGrid, 1)
        composeTestRule.onNodeWithText("S").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortByPopularity() {
        type = WatchTabItem.WATCHLIST
        setup(defaultMediaType)


        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_Popularity", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_Popularity").assertIsDisplayed().assertIsSelectable()

        popularityFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        popularityFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onNodeWithText("Trending Worldwide").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Well Known").assertExists().assertIsDisplayed()
        justScrollDown(lazyGrid, 1)
        composeTestRule.onNodeWithText("Niche").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Very Popular").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSortByPopularityTvShows() {
        type = WatchTabItem.WATCHLIST
        setup(MediaType.TV_SHOW)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("filterChip_Popularity", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_Popularity").assertIsDisplayed().assertIsSelectable()

        popularityFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        popularityFilter.assertIsSelected()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onNodeWithText("Trending Worldwide").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Very Popular").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Well Known").assertExists().assertIsDisplayed()
        justScrollDown(lazyGrid, 1)
        composeTestRule.onNodeWithText("Niche").assertExists().assertIsDisplayed()
    }

    @Test
    fun testOrdinaryContent(){
        setup(defaultMediaType)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText(expectedPageTitle).assertExists().assertIsDisplayed()

        composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())
        Thread.sleep(3000)
    }

    @Test
    fun testEmptyListCase(){
        fakeRepo = empty()
        setup(defaultMediaType)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Explore").assertExists().assertIsDisplayed()

        val mediaText = expectedLabel
        val typeText = if (type == WatchTabItem.WATCHLIST) "in Watchlist" else "Watched"

        composeTestRule.onNodeWithText("No $mediaText $typeText").assertExists().assertIsDisplayed()
        Thread.sleep(3000)
    }

    @Test
    fun testOnBackToHome(){
        fakeRepo = empty()
        setup(defaultMediaType)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Explore").assertExists().assertIsDisplayed().assertHasClickAction().performClick()

        assertEquals(HomeTabItem.MOVIES, destination)
        Thread.sleep(3000)
    }

    @Test
    fun testOrdinaryContentTvShows(){
        setup(MediaType.TV_SHOW)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText(expectedPageTitle).assertExists().assertIsDisplayed()

        composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())
        Thread.sleep(3000)
    }

    @Test
    fun testEmptyListCaseTvShows(){
        fakeRepo = empty()
        setup(MediaType.TV_SHOW)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Explore").assertExists().assertIsDisplayed()

        val mediaText = expectedLabel
        val typeText = if (type == WatchTabItem.WATCHLIST) "in Watchlist" else "Watched"

        composeTestRule.onNodeWithText("No $mediaText $typeText").assertExists().assertIsDisplayed()
        Thread.sleep(3000)
    }

    @Test
    fun testOnBackToHomeTvShows(){
        fakeRepo = empty()
        setup(MediaType.TV_SHOW)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Explore").assertExists().assertIsDisplayed().assertHasClickAction().performClick()

        assertEquals(HomeTabItem.SHOWS, destination)
        Thread.sleep(3000)
    }
}