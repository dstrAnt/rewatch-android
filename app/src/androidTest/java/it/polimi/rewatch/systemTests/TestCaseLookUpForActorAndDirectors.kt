package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.collections.get

/**
 * Test Cases designed to test Interaction Scenario "Look Up For Actor And Directors"
 */

class TestCaseLookUpForActorAndDirectors {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testActorAndDirectorsSearch(){

        // Tap on SEARCH Bottom Bar
        composeTestRule.onNodeWithContentDescription("Search").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        // Assertion verifying that the Search Page is loaded correctly
        composeTestRule.onNodeWithTag("search_page").assertExists()

        val peopleTab = composeTestRule.onNodeWithTag("Tab_People").assertExists().assertIsDisplayed().assertHasClickAction()

        //Selecting "Find People" Filter
        peopleTab.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        val inputText = "Emily Blunt"
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        // Inserting search query for "Emily Blunt"
        searchField.performTextInput(inputText)
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithContentDescription(inputText, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

        // Tapping on the first search result
        searchResult.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        // Assertion verifying that the Person Page is loaded correctly
        composeTestRule.onNodeWithTag("person_page").assertExists()

        val backButton = composeTestRule.onNodeWithTag("backButton").assertExists().assertIsDisplayed().assertHasClickAction()

        // Tapping on the back button
        backButton.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("search_page").assertExists()

        val inputText2 = "Cillian Murphy"
        val searchField2 = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        // Inserting search query for "Cillian Murphy"
        searchField2.performTextReplacement(inputText2)
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithContentDescription(inputText2, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val searchResult2 = composeTestRule.onAllNodesWithContentDescription(inputText2, substring = true)[0]

        // Tapping on the first search result
        searchResult2.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("person_page").assertExists()
    }

    @Test
    fun testActorContent(){
        composeTestRule.onNodeWithContentDescription("Search").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        // Assertion verifying that the Search Page is loaded correctly
        composeTestRule.onNodeWithTag("search_page").assertExists()

        val peopleTab = composeTestRule.onNodeWithTag("Tab_People").assertExists().assertIsDisplayed().assertHasClickAction()

        peopleTab.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        val inputText = "Scarlett Johansson"
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        searchField.performTextInput(inputText)
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithContentDescription(inputText, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

        searchResult.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("person_page").assertExists()

        composeTestRule.onNodeWithText("Directing").assertHasClickAction().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

    }
}