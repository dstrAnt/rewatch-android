package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseAccessProfilePage {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    @Test
    fun testProfilePage() {

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("profile_page").assertExists()

    }
}