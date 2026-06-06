package it.polimi.rewatch.integrationTests

import android.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.TmdbClientConfig
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.HomeTabItem
import it.polimi.rewatch.screens.home.HomeViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomePageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val defaultStartTab = HomeTabItem.MOVIES

    /*var tmdbConfig = TmdbClientConfig()

    lateinit var tmdb: Tmdb3

    lateinit var repository : TmdbRepository

    lateinit var homeViewModel : HomeViewModel*/

    val tmdb = Tmdb3("***REMOVED_TMDB_KEY***")

    val repository = TmdbRepository(tmdb)

    val homeViewModel = HomeViewModel(repository)


    fun setup(startTab: HomeTabItem, onNavToMedia: (MediaId) -> Unit) {

        val actions = NavigationActions(onNavigateToPerson = { id -> },

            onNavigateToMedia = { onNavToMedia },
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
        /*tmdbConfig.tmdbApiKey = "***REMOVED_TMDB_KEY***"
        tmdb = Tmdb3("***REMOVED_TMDB_KEY***")
        repository = TmdbRepository(tmdb)
        homeViewModel = HomeViewModel(repository)*/

        composeTestRule.setContent {
            HomePage(actions = actions, outerPadding = outerPadding, startTab = startTab, viewModel = homeViewModel)
        }
    }

    @Test
    fun testBrowseFilmsForMovies() {
        setup(defaultStartTab, {})

        composeTestRule.onNodeWithText("Movies").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tv Shows").assertExists().assertIsDisplayed()

        val movieTab = composeTestRule.onNodeWithTag("Tab_Movies").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        val showsTab = composeTestRule.onNodeWithTag("Tab_Tv Shows").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        showsTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        movieTab.assertIsNotSelected()
        showsTab.assertIsSelected()

        movieTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        movieTab.assertIsSelected()
        showsTab.assertIsNotSelected()

        composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasNoClickAction())
    }

    @Test
    fun testBrowseFilmsForShows(){
        setup(HomeTabItem.SHOWS, {media -> println(media.toString())})

        composeTestRule.onNodeWithText("Movies").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tv Shows").assertExists().assertIsDisplayed()

        val movieTab = composeTestRule.onNodeWithTag("Tab_Movies").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        val showsTab = composeTestRule.onNodeWithTag("Tab_Tv Shows").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()

        movieTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        movieTab.assertIsSelected()
        showsTab.assertIsNotSelected()

        showsTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        movieTab.assertIsNotSelected()
        showsTab.assertIsSelected()

        composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())
    }

    @Test
    fun testHomeButtons() {
        setup(defaultStartTab, {})

        val trendingButton = composeTestRule.onNodeWithTag("button_Trending").assertExists().assertIsDisplayed().assertHasClickAction()
        val ratedButton = composeTestRule.onNodeWithTag("button_Top Rated").assertExists().assertIsDisplayed().assertHasClickAction()
        val newestButton = composeTestRule.onNodeWithTag("button_Newest").assertExists().assertIsDisplayed().assertHasClickAction()

        testHomeButton(trendingButton, "Trending")
        testHomeButton(ratedButton, "Top Rated")
        testHomeButton(newestButton, "Newest")
    }

    fun testHomeButton(button: SemanticsNodeInteraction, title: String){
        val expectedCompressedSizeForPortrait = 12
        val expectedCompressedSizeForLandscape = 27
        val expectedExpandedSizeForPortrait = 40
        val expectedExpandedSizeForLandscape = 36

        try {
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForPortrait)
        }
        catch (e: AssertionError){
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForLandscape)
        }

        button.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        try {
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedExpandedSizeForPortrait)
        }
        catch (e: AssertionError){
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedExpandedSizeForLandscape)
        }

        button.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        try {
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForPortrait)
        }
        catch (e: AssertionError){
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForLandscape)
        }
    }

    @Test
    fun testMovieSectionLandscapeLayout() {

        val actions = NavigationActions(
            onNavigateToPerson = { id -> },

            onNavigateToMedia = { id -> },
            onNavigateBack = {

            },
            onFullscreenImage = { url ->

            },
            onBottomSheet = { info -> },
            onNavigateToLogin = {

            },
            onBackToHome = { tab ->

            }
        )

        val outerPadding = PaddingValues();

        composeTestRule.setContent {
            Box(
                Modifier.size(width = 800.dp, height = 400.dp)
            ){
                HomePage(actions = actions, outerPadding = outerPadding, startTab = defaultStartTab, viewModel = homeViewModel)
            }
        }

        Thread.sleep(5000)
    }
}