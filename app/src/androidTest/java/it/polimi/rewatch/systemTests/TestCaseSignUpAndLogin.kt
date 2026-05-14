package it.polimi.rewatch.systemTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.settings.AuthPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseSignUpAndLogin {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {

        composeTestRule.setContent {
            MainActivity()
        }
    }

    @Test
    fun testAuthPageLogin() {

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        composeTestRule.onNodeWithText("Login").performClick()

        //...

    }

    @Test
    fun testAuthPageLogout() {

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        composeTestRule.onNodeWithText("Logout").performClick()

        //...

    }

}