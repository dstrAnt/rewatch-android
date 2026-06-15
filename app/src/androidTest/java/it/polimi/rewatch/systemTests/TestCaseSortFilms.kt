package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.tracking.SortOption
import it.polimi.rewatch.screens.tracking.WatchTabItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.floor
import kotlin.random.Random

class TestCaseSortFilms {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<MainActivity>()

    val mediaType = MediaType.MOVIE
    var trackingInfo = WatchTabItem.WATCHED


    fun setupWatchlist(){
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        if(mediaType == MediaType.TV_SHOW) composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
        composeTestRule.waitForIdle()

        trackItem(0, "Watchlist", true)
        trackItem(1, "Watchlist", true)
        trackItem(2, "Watchlist", true)
        trackItem(3, "Watchlist", true)
    }

    fun setupWatched(){
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        if(mediaType == MediaType.TV_SHOW) composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
        composeTestRule.waitForIdle()

        trackItem(4, "Watch")
        trackItem(5, "Watch")
        trackItem(6, "Watch")
        trackItem(7, "Watch")
    }

    fun trackItem(index: Int, option: String, substring : Boolean = false){
        composeTestRule.onAllNodesWithTag("posterItem_Trending")[index].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(10_000){
            composeTestRule
                .onAllNodesWithContentDescription(option, substring = substring)
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(1000)

        composeTestRule.onNodeWithContentDescription(option, substring = substring).performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
    }

    fun chooseSortOption(numOptions: Int): SortOption{
        val randNumber = Random.nextInt(0, numOptions)

        return when (randNumber){
            0 -> SortOption.DATE
            1 -> SortOption.RATING
            2 -> SortOption.ALPHABETICAL
            else -> SortOption.POPULARITY
        }
    }

    fun getSortTabTag(filter: SortOption): String{
        return when (filter){
            SortOption.DATE -> "filterChip_Date"
            SortOption.RATING -> "filterChip_Rating"
            SortOption.ALPHABETICAL -> "filterChip_A-Z"
            SortOption.POPULARITY -> "filterChip_Popularity"
        }
    }

    fun accessTrackingPage(){
        val rightTab = if (mediaType == MediaType.MOVIE) "Movies" else "Tv Shows"

        composeTestRule.onNodeWithContentDescription(rightTab).assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        if(trackingInfo == WatchTabItem.WATCHED){
            watchedTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
        }
    }

    @Test
    fun testSortWatched() {
        setupWatched()

        trackingInfo = WatchTabItem.WATCHED
        accessTrackingPage()

        val sortChoice = chooseSortOption(3)
        val sortFilterTag = getSortTabTag(sortChoice)

        val sortFilter = composeTestRule.onNodeWithTag(sortFilterTag).assertExists().assertIsDisplayed().assertIsSelectable()

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testSortWatchlist() {
        setupWatchlist()

        trackingInfo = WatchTabItem.WATCHLIST
        accessTrackingPage()

        val sortChoice = chooseSortOption(4)
        val sortFilterTag = getSortTabTag(sortChoice)

        val sortFilter = composeTestRule.onNodeWithTag(sortFilterTag).assertExists().assertIsDisplayed().assertIsSelectable()

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }
}