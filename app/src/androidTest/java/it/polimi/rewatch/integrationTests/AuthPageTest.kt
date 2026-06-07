package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.settings.AuthPage
import it.polimi.rewatch.screens.settings.AuthViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val authViewModel = AuthViewModel(repo = FakeRepository())

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
            AuthPage(actions = actions, outerPadding = outerPadding, viewModel = authViewModel)
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
    fun testErrorContentLink() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("a@b.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Failed to send link").assertExists().assertIsDisplayed()
    }

    @Test
    fun testErrorContentVerifyLink() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("c@b.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        authViewModel.handleDeepLink(" ")
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
        composeTestRule.onNodeWithText("Sign-in failed").assertExists().assertIsDisplayed()
    }

    @Test
    fun testErrorContentMergeFailure() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("cannotMerge.me@b.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
        composeTestRule.onNodeWithText("Check your email").assertExists().assertIsDisplayed()

        authViewModel.handleDeepLink(" ")
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
        composeTestRule.onNodeWithText("Account already exists").assertExists().assertIsDisplayed()

        val discardButton = composeTestRule.onNodeWithTag("discardMergeButton").assertExists().assertIsDisplayed().assertIsEnabled()

        // It is equivalent to execute this command: authViewModel.resolveCollision(false)
        discardButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
        composeTestRule.onNodeWithText("Failed").assertExists().assertIsDisplayed()
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
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        composeTestRule.onNodeWithText("Sending link…").assertExists().assertIsDisplayed()
        Thread.sleep(3000)
        composeTestRule.waitForIdle()

        authViewModel.handleDeepLink(" ")
        Thread.sleep(3000)
        composeTestRule.onNodeWithText("Signing in…").assertExists().assertIsDisplayed()
    }

    @Test
    fun testLoadingContentWithMerge() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("merge.me@b.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.onNodeWithText("Sending link…").assertExists().assertIsDisplayed()
        composeTestRule.waitForIdle()

        authViewModel.handleDeepLink(" ")
        composeTestRule.onNodeWithText("Signing in…").assertExists().assertIsDisplayed()

        val mergeButton = composeTestRule.onNodeWithTag("mergeButton").assertExists().assertIsDisplayed().assertIsEnabled()

        // It is equivalent to execute this command: authViewModel.resolveCollision(true)
        mergeButton.performClick()
        composeTestRule.onNodeWithText("Merging lists…").assertExists().assertIsDisplayed()
    }

    @Test
    fun testMergePromptContent() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("merge.me@b.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        composeTestRule.onNodeWithText("Check your email").assertExists().assertIsDisplayed()

        authViewModel.handleDeepLink(" ")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Account already exists").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("You have lists saved as a guest. Do you want to merge them with your existing account?").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Merge my lists").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Discard guest lists").assertExists().assertIsDisplayed()

        val mergeButton = composeTestRule.onNodeWithTag("mergeButton").assertExists().assertIsDisplayed().assertIsEnabled()

        // It is equivalent to execute this command: authViewModel.resolveCollision(true)
        mergeButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Success").assertExists().assertIsDisplayed()
    }

    @Test
    fun testLogIn() {
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
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        authViewModel.handleDeepLink(" ")
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Success").assertExists().assertIsDisplayed()
    }

    @Test
    fun testLogInDiscardingMerge() {
        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput("merge.me@b.com")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        composeTestRule.onNodeWithText("Check your email").assertExists().assertIsDisplayed()

        authViewModel.handleDeepLink(" ")
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        composeTestRule.onNodeWithText("Account already exists").assertExists().assertIsDisplayed()

        val discardButton = composeTestRule.onNodeWithTag("discardMergeButton").assertExists().assertIsDisplayed().assertIsEnabled()

        // It is equivalent to execute this command: authViewModel.resolveCollision(false)
        discardButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Success").assertExists().assertIsDisplayed()
    }

}