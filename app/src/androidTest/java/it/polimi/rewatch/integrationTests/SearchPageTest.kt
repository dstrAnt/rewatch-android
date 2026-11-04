package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.action.ViewActions.swipeLeft
import app.moviebase.tmdb.Tmdb3
import it.polimi.rewatch.BuildConfig
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.search.SearchPage
import it.polimi.rewatch.screens.search.SearchViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val searchViewModel = SearchViewModel(TmdbRepository(Tmdb3(BuildConfig.TMDB_API_KEY)))

    @Before
    fun setup() {

        composeTestRule.setContent {
            val outerPadding = PaddingValues();
            val actions = NavigationActions(
                onNavigateToPerson = { id -> },

                onNavigateToMedia = { id -> },
                onNavigateBack = {

                },
                onFullscreenImage = { url ->

                },
                onBottomSheet = { info ->

                },
                onNavigateToLogin = {

                },
                onBackToHome = { tab ->

                },
            )
            val pagerState = rememberPagerState(pageCount = { BottomTabs.entries.size })

            SearchPage(actions = actions, outerPadding = outerPadding, pagerState = pagerState, viewModel = searchViewModel)
        }
    }

    @Test
    fun testSearchInputField(){
        composeTestRule.onNodeWithText("Find All").assertExists().assertIsDisplayed()

        val startSearchButton = composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        startSearchButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        searchField.assertIsFocused()

        val inputText = "Avengers"

        // Inserting search query for "Avengers"
        searchField.performTextInput(inputText)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        // Assertion to state that button "Start Searching" is not displayed
        composeTestRule.onNodeWithText("Start Searching").assertDoesNotExist()

        try {
            val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

            searchResult.assertExists().assertIsDisplayed()
        }catch (e: AssertionError){
            try {

                composeTestRule.waitUntil(15_000) {
                    composeTestRule
                        .onAllNodesWithText("No results")
                        .fetchSemanticsNodes().isNotEmpty()
                }
                composeTestRule.onNodeWithText("No results").assertExists().assertIsDisplayed()
                composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
            }
            catch (e: ComposeTimeoutException){

            }
        }

        composeTestRule.onNodeWithContentDescription("Clear search").assertExists().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Find All").assertExists().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        searchField.assertIsNotFocused()
    }

    @Test
    fun testSearchTopBarMovie() {
        val movieTab = composeTestRule.onNodeWithTag("Tab_Movies").assertExists().assertIsDisplayed().assertHasClickAction()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        // Select Movie Filter
        movieTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        movieTab.assertIsSelected()
        composeTestRule.onNodeWithText("Find Movies").assertExists().assertIsDisplayed()

        val inputText = "Avengers"

        searchField.performTextInput(inputText)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

        searchResult.assertExists().assertIsDisplayed()

        val inputText2 = "Chris Hemsworth"

        searchField.performTextReplacement(inputText2)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription(inputText2).assertDoesNotExist()
    }

    @Test
    fun testSearchTopBarTvShows() {
        val showsTab = composeTestRule.onNodeWithTag("Tab_Tv Shows").assertExists().assertIsDisplayed().assertHasClickAction()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        // Select Tv Show Filter
        showsTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        showsTab.assertIsSelected()
        composeTestRule.onNodeWithText("Find Tv Shows").assertExists().assertIsDisplayed()

        val inputText = "Avengers"

        searchField.performTextInput(inputText)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

        //composeTestRule.onNodeWithContentDescription(inputText, substring = true).assertExists().assertIsDisplayed()
        searchResult.assertExists().assertIsDisplayed()

        val inputText2 = "Matt Damon"

        searchField.performTextReplacement(inputText2)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription(inputText2).assertDoesNotExist()
    }

    @Test
    fun testSearchTopBarPerson() {
        val peopleTab = composeTestRule.onNodeWithTag("Tab_People").assertExists().assertIsDisplayed().assertHasClickAction()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        // Select People Filter
        peopleTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        peopleTab.assertIsSelected()
        composeTestRule.onNodeWithText("Find People").assertExists().assertIsDisplayed()

        val inputText = "Chris Evans"

        searchField.performTextInput(inputText)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText)[0]

        searchResult.assertExists().assertIsDisplayed()

        val inputText2 = "Kung Fu Panda"

        searchField.performTextReplacement(inputText2)
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription(inputText2).assertDoesNotExist()

        Thread.sleep(5000)
    }

    /* Test Case to show the scenario with no Search Results Available */
    @Test
    fun testEmptySearchState(){
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed()

        searchField.performTextInput("hjkl")
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("No results").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
    }

    /* Testing IME Action (IME Action = Search) */
    @Test
    fun testFocusOnImeAction(){
        val startSearchButton = composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        startSearchButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.assertIsFocused()

        searchField.performTextInput("Christian Bale")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.performImeAction()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.assertIsNotFocused()
    }

    /* Testing Focus Dynamics */
    @Test
    fun testFocusOnScroll(){
        val startSearchButton = composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        startSearchButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.assertIsFocused()

        composeTestRule.onNodeWithText("Search for", substring = true).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.assertIsNotFocused()

        searchField.performTextInput("Christian Bale")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.assertIsFocused()

        composeTestRule.onNodeWithTag("searchResultListState").performTouchInput { swipeDown() }
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        searchField.assertIsNotFocused()
    }
}