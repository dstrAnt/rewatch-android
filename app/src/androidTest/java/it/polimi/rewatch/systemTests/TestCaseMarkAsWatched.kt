package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseMarkAsWatched {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        /*composeTestRule.setContent {
            MainActivity()
        }*/
    }
    @Test
    fun test() {
        composeTestRule.onAllNodesWithTag("posterItem")[0].assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("media_page").assertExists()
    }
}