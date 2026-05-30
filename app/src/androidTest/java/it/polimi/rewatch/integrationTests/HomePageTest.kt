package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.HomeTabItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomePageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val startTab = HomeTabItem.MOVIES


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
            HomePage(actions = actions, outerPadding = outerPadding, startTab = startTab)
        }
    }

    @Test
    fun testBrowseFilms() {
        setup()

        composeTestRule.onNodeWithText("Movies").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tv Shows").assertExists().assertIsDisplayed()

        val movieTab = composeTestRule.onNodeWithTag("homeTab_Movies").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()
        val showsTab = composeTestRule.onNodeWithTag("homeTab_Tv Shows").assertExists().assertIsDisplayed().assertHasClickAction().assertIsSelectable()


        if(startTab == HomeTabItem.MOVIES) {
            showsTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)

            movieTab.assertIsNotSelected()
            showsTab.assertIsSelected()

            movieTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)

            movieTab.assertIsSelected()
            showsTab.assertIsNotSelected()
        }
        else{
            movieTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)

            movieTab.assertIsSelected()
            showsTab.assertIsNotSelected()

            showsTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)

            movieTab.assertIsNotSelected()
            showsTab.assertIsSelected()
        }
    }

    @Test
    fun testHomeButtons() {
        setup()

        val trendingButton = composeTestRule.onNodeWithText("button_Trending").assertExists().assertIsDisplayed().assertHasClickAction()
        val ratedButton = composeTestRule.onNodeWithText("button_Top Rated").assertExists().assertIsDisplayed().assertHasClickAction()
        val newestButton = composeTestRule.onNodeWithText("button_Newest").assertExists().assertIsDisplayed().assertHasClickAction()

        testHomeButton(trendingButton, "Trending")
        testHomeButton(ratedButton, "Top Rated")
        testHomeButton(newestButton, "Newest")
    }

    fun testHomeButton(button: SemanticsNodeInteraction, title: String){
        val expectedCompressedSizeForPortrait = 12
        val expectedCompressedSizeForLandscape = 27
        val expectedExpandedSizeForPortrait = 40
        val expectedExpandedSizeForLandscape = 36

        try {
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForPortrait)
        }
        catch (e: AssertionError){
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForLandscape)
        }

        button.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        try {
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedExpandedSizeForPortrait)
        }
        catch (e: AssertionError){
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedExpandedSizeForLandscape)
        }

        button.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        try {
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForPortrait)
        }
        catch (e: AssertionError){
            composeTestRule.onAllNodesWithTag("posterItem_$title").assertCountEquals(expectedCompressedSizeForLandscape)
        }
    }

    @Test
    fun testMovieSectionLandscapeLayout() {

        val actions = NavigationActions(
            onNavigateToPerson = { id -> },

            onNavigateToMedia = { id -> },
            onNavigateBack = {

            },
            onFullscreenImage = { url ->

            },
            onBottomSheet = { info -> },
            onNavigateToLogin = {

            },
            onBackToHome = { tab ->

            }
        )

        val outerPadding = PaddingValues();

        composeTestRule.setContent {
            Box(
                Modifier.size(width = 800.dp, height = 400.dp)
            ){
                HomePage(actions = actions, outerPadding = outerPadding, startTab = startTab)
            }
        }
    }
}