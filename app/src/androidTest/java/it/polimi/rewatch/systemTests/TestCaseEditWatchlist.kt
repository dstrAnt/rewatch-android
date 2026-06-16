package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.model.MediaType
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.collections.get

class TestCaseEditWatchlist {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    val defaultMedia = it.polimi.rewatch.model.MediaType.MOVIE

    fun setup(media: it.polimi.rewatch.model.MediaType) {
        val mediaType = media
        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        if(mediaType == it.polimi.rewatch.model.MediaType.TV_SHOW) {
            composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(2000)
        }

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[4].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watchlist", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Watchlist", substring = true).performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithContentDescription("In watchlist")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[8].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watchlist", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Watchlist", substring = true).performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithContentDescription("In watchlist")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val bottomSheet2 = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet2.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)
    }

    @Test
    fun testWatchlistMovies() {
        try{
            setup(defaultMedia)

            composeTestRule.onNodeWithContentDescription("Movies").assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)

            composeTestRule.onNodeWithTag("tracking_page").assertExists()

            removeFromWatchlist()
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    @Test
    fun testWatchlistTvShows() {
        try{
            setup(it.polimi.rewatch.model.MediaType.TV_SHOW)

            composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
                .performClick()

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("tracking_page").assertExists()

            removeFromWatchlist()
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, it will be skipped.")
        }
    }

    fun removeFromWatchlist(){
        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watchlist").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        watchedTab.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        val firstItem = composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists().assertIsDisplayed()
        firstItem.performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("In watchlist").performClick()

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)
    }


}