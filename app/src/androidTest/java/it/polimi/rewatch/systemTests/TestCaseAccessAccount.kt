package it.polimi.rewatch.systemTests

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import it.polimi.rewatch.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseAccessAccount {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val emailAddress = "test.user@example.com"

    fun setupTrackingLists(){
        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Watch").performClick()

        composeTestRule.waitUntil(10_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watched")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[1].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Watchlist", substring = true).performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithContentDescription("In watchlist")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(3000)

        val contentPageNode2 = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode2.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
    }

    @Test
    fun testAuthPageLogin() {

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput(emailAddress)
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Check your email")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(60000)

        composeTestRule.onNodeWithText("Success").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithTag("doneButton").assertExists().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        testAuthPageLogout()
    }

    fun testAuthPageLogout() {

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        composeTestRule.onNodeWithText("Logout").performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()
    }

    @Test
    fun testAuthPageCollisionResolve() {
        // On the Home Page, two casual poster items are selected

        setupTrackingLists()

        // Returning to the Profile Page first and then to the Authentication Page

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()


        // Same procedures for Standard Login + Merge Prompt

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        textField.assertIsFocused().performTextInput(emailAddress)
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        sendLinkButton.performClick()
        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Check your email")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(60000)

        composeTestRule.onNodeWithText("Account already exists").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("You have lists saved as a guest. Do you want to merge them with your existing account?").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Merge my lists").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Discard guest lists").assertExists().assertIsDisplayed()

        val mergeButton = composeTestRule.onNodeWithTag("mergeButton").assertExists().assertIsDisplayed().assertIsEnabled()

        Thread.sleep(5000)

        // It is equivalent to execute this command: authViewModel.resolveCollision(true)
        mergeButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        Thread.sleep(5000)

        goToTrackedMovies()
        goToTrackedShows()
    }

    fun goToTrackedMovies(){
        composeTestRule.onNodeWithContentDescription("Movies").assertExists()
            .performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Date")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        watchedTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    fun goToTrackedShows(){
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        watchedTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }
}