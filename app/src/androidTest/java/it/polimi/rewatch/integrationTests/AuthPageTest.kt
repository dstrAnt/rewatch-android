package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.settings.AuthPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {

        val actions = NavigationActions(onNavigateToPerson = { id -> },

            onNavigateToMedia = { id -> },
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
            AuthPage(actions = actions, outerPadding = outerPadding)
        }
    }

    @Test
    fun testIdleContent() {
        composeTestRule.onNodeWithText("Sign in to synchronize your lists").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Send link").assertExists().assertIsDisplayed()

        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("matteo.")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsNotEnabled()

        textField.assertIsFocused().performTextReplacement("matteo.bianchi")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsNotEnabled()

        textField.assertIsFocused().performTextReplacement("matteo.bianchi@")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsNotEnabled()

        textField.assertIsFocused().performTextReplacement("matteo.bianchi@tutorial.")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsNotEnabled()

        textField.assertIsFocused().performTextReplacement("matteo.bianchi@tutorial.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        sendLinkButton.assertIsEnabled()
    }

    @Test
    fun testErrorContent() {

    }

    @Test
    fun testLinkSentContent() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        textField.performTextInput("matteo.bianchi@tutorial.com")
        composeTestRule.waitForIdle()

        sendLinkButton.assertIsEnabled().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Check your email").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("We sent you a sign-in link. Tap it to continue.").assertExists().assertIsDisplayed()
    }

    @Test
    fun testMergePromptContent() {

    }
    @Test
    fun testLoadingContent() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("matteo.bianchi@tutorial.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.onNodeWithText("Sending link...").assertExists().assertIsDisplayed()
    }

    @Test
    fun testLogIn() {



    }

}