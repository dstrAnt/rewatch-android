package it.polimi.rewatch.systemTests

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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.floor

class TestCaseSortFilms {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<MainActivity>()

    val mediaType = MediaType.MOVIE

    @Before
    fun setup(){
        trackItem(0, "Watchlist", true)
        trackItem(1, "Watchlist", true)
        trackItem(2, "Watchlist", true)
        trackItem(3, "Watchlist", true)

        trackItem(4, "Watch")
        trackItem(5, "Watch")
        trackItem(6, "Watch")
        trackItem(7, "Watch")
    }

    fun trackItem(index: Int, option: String, substring : Boolean = false){
        composeTestRule.onAllNodesWithTag("posterItem")[index].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription(option, substring).performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    fun chooseSortOption(numOptions: Int): SortOption{
        val randNumber = floor(Math.random()).toInt() % numOptions

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
    }

    @Test
    fun testSortWatched() {
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
    fun testSortTrackedTvShows() {
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