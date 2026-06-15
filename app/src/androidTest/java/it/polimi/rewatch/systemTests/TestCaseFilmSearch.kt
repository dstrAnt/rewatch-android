package it.polimi.rewatch.systemTests

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.search.SearchPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.collections.get

class TestCaseFilmSearch {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testFilmSearch() {
        composeTestRule.onNodeWithContentDescription("Search").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_page").assertExists()

        composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        val inputText = "The Devil Wears Prada"
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        searchField.performTextInput(inputText)
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

        searchResult.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("media_page").assertExists()

        val backButton = composeTestRule.onNodeWithTag("backButton").assertExists().assertIsDisplayed().assertHasClickAction()

        backButton.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("search_page").assertExists()

        val inputText2 = "Oppenheimer"
        val searchField2 = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        searchField2.performTextReplacement(inputText2)
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithContentDescription(inputText2, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val searchResult2 = composeTestRule.onAllNodesWithContentDescription(inputText2, substring = true)[0]

        searchResult2.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("media_page").assertExists()
    }
}