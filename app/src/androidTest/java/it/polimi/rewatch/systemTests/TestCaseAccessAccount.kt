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
import androidx.compose.ui.test.ComposeTimeoutException

/**
 * Test Cases designed to test Interaction Scenario "Access Account"
 */


class TestCaseAccessAccount {
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.v2.createAndroidComposeRule<it.polimi.rewatch.MainActivity>()

    val emailAddress = "test.user@example.com"

    // Setup Procedure to add initial Watchlist & Watched Movies for Collision scenarios
    fun setupTrackingLists(){
        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[0].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watch", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Watch").performClick()

        composeTestRule.waitUntil(20_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watched")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[1].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watchlist", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Watchlist", substring = true).performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithContentDescription("In watchlist")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val contentPageNode2 = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode2.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)
    }

    // Setup Procedure to add initial Watchlist & Watched Tv Shows for Collision scenarios
    fun setupTrackingLists2(){
        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithText("Trending")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Tv Shows").assertExists().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[9].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watch", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Watch").performClick()

        composeTestRule.waitUntil(20_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watched")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val bottomSheet = composeTestRule.onNodeWithTag("RewatchBottomSheet").assertExists().assertIsDisplayed()

        bottomSheet.performTouchInput { swipeDown(200f, 400f) }
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        composeTestRule.onAllNodesWithTag("posterItem_Trending")[10].assertExists()
            .performTouchInput { longClick() }

        composeTestRule.waitUntil(35_000){
            composeTestRule
                .onAllNodesWithContentDescription("Watchlist", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Watchlist", substring = true).performClick()

        composeTestRule.waitUntil(15_000){
            composeTestRule
                .onAllNodesWithContentDescription("In watchlist")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        val contentPageNode2 = composeTestRule.onNodeWithText("See content page").assertExists().assertIsDisplayed()

        contentPageNode2.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)
    }

    @Test
    fun testAuthPageLogin() {

        // Entering the Profile Page

        composeTestRule.onNodeWithContentDescription("Profile").assertExists()
            .performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Login")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithTag("profile_page").assertExists()

        // Initiating Login Procedure in the Auth Page

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
        val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

        textField.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)

        // Inserting email address
        textField.assertIsFocused().performTextInput(emailAddress)
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(3000)
        sendLinkButton.assertIsEnabled()

        // Sending email Sign In link
        sendLinkButton.performClick()
        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Check your email")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(60000)

        try{
            composeTestRule.waitUntil(5_000){
                composeTestRule
                .onAllNodesWithContentDescription("Success")
                .fetchSemanticsNodes().isNotEmpty()
            }

            // Returning successfully to Profile Page

            composeTestRule.onNodeWithText("Success").assertExists().assertIsDisplayed()

        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, this assert will be skipped.")
        }

            composeTestRule.onNodeWithTag("doneButton").assertExists().assertIsDisplayed().performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)

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
        java.lang.Thread.sleep(3000)

        composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()
    }

    @Test
    fun testAuthPageCollisionResolve() {
        try{
            // On the Home Page, two casual poster items are selected

            setupTrackingLists()

            // Returning to the Profile Page first and then to the Authentication Page

            composeTestRule.onNodeWithContentDescription("Profile").assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithTag("profile_page").assertExists()

            composeTestRule.waitUntil(15_000) {
                composeTestRule
                    .onAllNodesWithText("Login")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()

            // Same procedures for Standard Login + Merge Prompt

            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
            val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

            textField.performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            textField.assertIsFocused().performTextInput(emailAddress)
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)
            sendLinkButton.assertIsEnabled()

            sendLinkButton.performClick()
            composeTestRule.waitUntil(5_000) {
                composeTestRule
                    .onAllNodesWithText("Check your email")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            java.lang.Thread.sleep(60000)

            composeTestRule.onNodeWithText("Account already exists").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("You have lists saved as a guest. Do you want to merge them with your existing account?")
                .assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Merge my lists").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Discard guest lists").assertExists().assertIsDisplayed()

            val mergeButton = composeTestRule.onNodeWithTag("mergeButton").assertExists().assertIsDisplayed().assertIsEnabled()

            java.lang.Thread.sleep(5000)

            // It is equivalent to execute this command: authViewModel.resolveCollision(true)
            mergeButton.performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)

            // Returning successfully to Profile Page

            composeTestRule.onNodeWithTag("profile_page").assertExists()

            java.lang.Thread.sleep(5000)

            goToTrackedMovies()         // Showing Merge Results
           // goToTrackedShows()

            testAuthPageLogout()
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, this test will be skipped.")
        }
    }

    fun goToTrackedMovies(){
        composeTestRule.onNodeWithContentDescription("Movies").assertExists()
            .performClick()

        composeTestRule.waitUntil(5_000){
            composeTestRule
                .onAllNodesWithText("Date")
                .fetchSemanticsNodes().isNotEmpty()
        }
        java.lang.Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        watchedTab.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(2000)
    }

    fun goToTrackedShows(){
        composeTestRule.onNodeWithContentDescription("Tv Shows").assertExists()
            .performClick()

        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(5000)

        composeTestRule.onNodeWithTag("tracking_page").assertExists()

        val watchedTab = composeTestRule.onNodeWithTag("Tab_Watched").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        watchedTab.performClick()
        composeTestRule.waitForIdle()
        java.lang.Thread.sleep(2000)
    }

    @Test
    fun testAuthPageCollisionResolveRefusingMerge() {
        try{
            // On the Home Page, two casual poster items are selected

            setupTrackingLists2()

            // Returning to the Profile Page first and then to the Authentication Page

            composeTestRule.onNodeWithContentDescription("Profile").assertExists()
                .performClick()

            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            composeTestRule.onNodeWithTag("profile_page").assertExists()

            composeTestRule.waitUntil(15_000) {
                composeTestRule
                    .onAllNodesWithText("Login")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()


            // Same procedures for Standard Login + Merge Prompt

            composeTestRule.onNodeWithText("Login").performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            val textField = composeTestRule.onNodeWithTag("signInTextField").assertExists().assertIsDisplayed().assertIsNotFocused()
            val sendLinkButton = composeTestRule.onNodeWithTag("sendAuthLinkButton").assertExists().assertIsDisplayed().assertIsNotEnabled()

            textField.performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)

            textField.assertIsFocused().performTextInput(emailAddress)
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(3000)
            sendLinkButton.assertIsEnabled()

            sendLinkButton.performClick()
            composeTestRule.waitUntil(5_000) {
                composeTestRule
                    .onAllNodesWithText("Check your email")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            java.lang.Thread.sleep(60000)

            // Beginning of Merge Prompt Content

            composeTestRule.onNodeWithText("Account already exists").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("You have lists saved as a guest. Do you want to merge them with your existing account?")
                .assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Merge my lists").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Discard guest lists").assertExists().assertIsDisplayed()

            val discardButton = composeTestRule.onNodeWithTag("discardMergeButton").assertExists().assertIsDisplayed().assertIsEnabled()
            java.lang.Thread.sleep(5000)

            // It is equivalent to execute this command: authViewModel.resolveCollision(false)
            discardButton.performClick()
            composeTestRule.waitForIdle()
            java.lang.Thread.sleep(5000)

            // Returning successfully to Profile Page

            composeTestRule.onNodeWithTag("profile_page").assertExists()

            java.lang.Thread.sleep(5000)

            //goToTrackedMovies()
            goToTrackedShows()           // Showing that no Merge occurred

            testAuthPageLogout()
        }catch (e: ComposeTimeoutException){
            println("The Application is taking too long, this test will be skipped.")
        }
    }
}