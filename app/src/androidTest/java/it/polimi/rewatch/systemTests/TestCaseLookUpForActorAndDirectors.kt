package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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

class TestCaseLookUpForActorAndDirectors {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testActorAndDirectorsSearch(){
        composeTestRule.onNodeWithContentDescription("Search").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("search_page").assertExists()

        val peopleTab = composeTestRule.onNodeWithTag("Tab_People").assertExists().assertIsDisplayed().assertHasClickAction()

        peopleTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Start Searching").assertExists().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val inputText = "Emily Blunt"
        val searchField = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        searchField.performTextInput(inputText)
        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithContentDescription(inputText, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(3000)

        val searchResult = composeTestRule.onAllNodesWithContentDescription(inputText, substring = true)[0]

        searchResult.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("person_page").assertExists()

        val backButton = composeTestRule.onNodeWithTag("backButton").assertExists().assertIsDisplayed().assertHasClickAction()

        backButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("search_page").assertExists()

        val inputText2 = "Cillian Murphy"
        val searchField2 = composeTestRule.onNodeWithTag("textField").assertExists().assertIsDisplayed().assertHasClickAction()

        searchField2.performTextReplacement(inputText2)
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val searchResult2 = composeTestRule.onAllNodesWithContentDescription(inputText2, substring = true)[0]

        searchResult2.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("person_page").assertExists()
    }
}