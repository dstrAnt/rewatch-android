package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.search.SearchPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

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

            SearchPage(actions = actions, outerPadding = outerPadding, pagerState = pagerState )
        }
    }

    @Test
    fun testSearchInputField(){
        val startSearchButton = composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        startSearchButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        searchField.assertIsFocused()

        val inputText = "Avengers"

        searchField.performTextInput(inputText)
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Start Searching").assertDoesNotExist()

        try {
            composeTestRule.onNodeWithContentDescription(inputText, substring = true).assertExists().assertIsDisplayed()
        }catch (e: AssertionError){
            composeTestRule.onNodeWithText("No results").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
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

        movieTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        movieTab.assertIsSelected()

    }

    @Test
    fun testSearchTopBarTvShows() {
        val showsTab = composeTestRule.onNodeWithTag("Tab_Tv Shows").assertExists().assertIsDisplayed().assertHasClickAction()

        showsTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        showsTab.assertIsSelected()
    }

    @Test
    fun testSearchTopBarPerson() {
        val peopleTab = composeTestRule.onNodeWithTag("Tab_People").assertExists().assertIsDisplayed().assertHasClickAction()

        peopleTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        peopleTab.assertIsSelected()
    }

    @Test
    fun testEmptySearchState(){
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed()

        searchField.performTextInput("hjkl")
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("No results").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed()
    }
}