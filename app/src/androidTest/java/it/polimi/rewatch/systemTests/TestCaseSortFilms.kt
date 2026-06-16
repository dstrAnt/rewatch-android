package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.ComposeTimeoutException
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
import kotlin.collections.get
import kotlin.math.floor
import kotlin.random.Random

class TestCaseSortFilms {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    val mediaType = it.polimi.rewatch.model.MediaType.MOVIE
    var trackingInfo = it.polimi.rewatch.screens.tracking.WatchTabItem.WATCHED


    fun setupWatchlist(){
        try{
            composeTestRule.waitUntil(35_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            if (mediaType == it.polimi.rewatch.model.MediaType.TV_SHOW) composeTestRule.onNodeWithText("Tv Shows").assertExists()
                .performClick()
            composeTestRule.waitForIdle()

            trackItem(0, "Watchlist", true)
            trackItem(1, "Watchlist", true)
            trackItem(2, "Watchlist", true)
            trackItem(3, "Watchlist", true)
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    fun setupWatched(){
        try{
            composeTestRule.waitUntil(35_000) {
                composeTestRule
                    .onAllNodesWithText("Trending")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            if (mediaType == it.polimi.rewatch.model.MediaType.TV_SHOW) composeTestRule.onNodeWithText("Tv Shows").assertExists()
                .performClick()
            composeTestRule.waitForIdle()

            trackItem(4, "Watch")
            trackItem(5, "Watch")
            trackItem(6, "Watch")
            trackItem(7, "Watch")
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    fun trackItem(index: Int, option: String, substring : Boolean = false){
        composeTestRule.onAllNodesWithTag("posterItem_Trending")[index].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(10_000){
            composeTestRule
                .onAllNodesWithContentDescription(option, substring = substring)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(1000)

        composeTestRule.onNodeWithContentDescription(option, substring = substring).performClick()

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(2000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)
    }

    fun chooseSortOption(numOptions: Int): it.polimi.rewatch.screens.tracking.SortOption {
        val randNumber = kotlin.random.Random.Default.nextInt(0, numOptions)

        return when (randNumber){
            0 -> it.polimi.rewatch.screens.tracking.SortOption.DATE
            1 -> it.polimi.rewatch.screens.tracking.SortOption.RATING
            2 -> it.polimi.rewatch.screens.tracking.SortOption.ALPHABETICAL
            else -> it.polimi.rewatch.screens.tracking.SortOption.POPULARITY
        }
    }

    fun getSortTabTag(filter: it.polimi.rewatch.screens.tracking.SortOption): String{
        return when (filter){
            it.polimi.rewatch.screens.tracking.SortOption.DATE -> "filterChip_Date"
            it.polimi.rewatch.screens.tracking.SortOption.RATING -> "filterChip_Rating"
            it.polimi.rewatch.screens.tracking.SortOption.ALPHABETICAL -> "filterChip_A-Z"
            it.polimi.rewatch.screens.tracking.SortOption.POPULARITY -> "filterChip_Popularity"
        }
    }

    fun accessTrackingPage(){
        val rightTab = if (mediaType == it.polimi.rewatch.model.MediaType.MOVIE) "Movies" else "Tv Shows"

        composeTestRule.onNodeWithContentDescription(rightTab).assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        if(trackingInfo == it.polimi.rewatch.screens.tracking.WatchTabItem.WATCHED){
            watchedTab.performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(2000)
        }
    }

    @Test
    fun testSortWatched() {
        setupWatched()

        trackingInfo = it.polimi.rewatch.screens.tracking.WatchTabItem.WATCHED
        accessTrackingPage()

        val sortChoice = chooseSortOption(3)
        val sortFilterTag = getSortTabTag(sortChoice)

        val sortFilter = composeTestRule.onNodeWithTag(sortFilterTag).assertExists().assertIsDisplayed().assertIsSelectable()

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)
    }

    @Test
    fun testSortWatchlist() {
        setupWatchlist()

        trackingInfo = it.polimi.rewatch.screens.tracking.WatchTabItem.WATCHLIST
        accessTrackingPage()

        val sortChoice = chooseSortOption(4)
        val sortFilterTag = getSortTabTag(sortChoice)

        val sortFilter = composeTestRule.onNodeWithTag(sortFilterTag).assertExists().assertIsDisplayed().assertIsSelectable()

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        sortFilter.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)
    }
}