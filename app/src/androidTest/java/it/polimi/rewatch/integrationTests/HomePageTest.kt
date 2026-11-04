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
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.espresso.action.ViewActions.swipeUp
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.TmdbClientConfig
import it.polimi.rewatch.BuildConfig
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.HomeTabItem
import it.polimi.rewatch.screens.home.HomeViewModel
import it.polimi.rewatch.theme.RewatchTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomePageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val defaultStartTab = HomeTabItem.MOVIES

    val tmdb = Tmdb3(BuildConfig.TMDB_API_KEY)

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

        composeTestRule.setContent {
            RewatchTheme(darkTheme = true) {
                HomePage(actions = actions, outerPadding = outerPadding, startTab = startTab, viewModel = homeViewModel)
            }
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
        setup(defaultStartTab, {media -> println(media.toString())})
        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        composeTestRule.onAllNodesWithTag("posterItem").assertAll(hasClickAction())

        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Trending").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Top Rated").assertExists().assertIsDisplayed()
        val trendingButton = composeTestRule.onNodeWithTag("button_Trending").assertExists().assertIsDisplayed().assertHasClickAction()
        testHomeButton(trendingButton, "Trending")

        justScrollDown(lazyGrid, 2)

        val ratedButton = composeTestRule.onNodeWithTag("button_Top Rated").assertExists().assertIsDisplayed().assertHasClickAction()
        testHomeButton(ratedButton, "Top Rated")

        composeTestRule.onNodeWithText("Newest").assertExists().assertIsDisplayed()
        val newestButton = composeTestRule.onNodeWithTag("button_Newest").assertExists().assertIsDisplayed().assertHasClickAction()
        testHomeButton(newestButton, "Newest")
    }

    fun testHomeButton(button: SemanticsNodeInteraction, title: String){

        composeTestRule.onAllNodesWithTag("posterItem$title").assertAll(hasClickAction())

        composeTestRule.onNodeWithText("Show Less").assertDoesNotExist()

        button.performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("More_$title").assertDoesNotExist()

        val lazyGrid = composeTestRule.onNodeWithTag("lazyGrid")

        justScrollDown(lazyGrid, 3)

        button.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Show Less").assertDoesNotExist()
    }

    fun justScrollDown(grid: SemanticsNodeInteraction, times: Int){
        for (i in List(times) { it }){
            grid.performTouchInput { swipeUp(500f, 0f) }
            composeTestRule.waitForIdle()
            Thread.sleep(2000)
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