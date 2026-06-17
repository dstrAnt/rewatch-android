package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test Cases designed to test Interaction Scenario "Access Profile Page"
 */

class TestCaseAccessProfilePage {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testProfilePage() {

        // Tap on PROFILE Bottom Bar
        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        // Assertion verifying that the Profile Page is loaded correctly
        composeTestRule.onNodeWithTag("profile_page").assertExists()

    }
}