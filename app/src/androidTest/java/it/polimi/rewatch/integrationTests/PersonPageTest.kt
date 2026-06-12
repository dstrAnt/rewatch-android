package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.performClick
import app.moviebase.tmdb.Tmdb3
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.screens.person.PersonPage
import it.polimi.rewatch.screens.person.PersonViewModel
import it.polimi.rewatch.theme.RewatchTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val defaultPersonId = 33200

    val defaultPersonViewModel = PersonViewModel(personId = defaultPersonId, TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")))

    fun defaultSetup() {

        val actions = NavigationActions(onNavigateToPerson = { id -> },
            onNavigateToMedia = { id -> },
            onNavigateBack = {},
            onFullscreenImage = { url -> },
            onBottomSheet = { info ->  },
            onNavigateToLogin = {},
            onBackToHome = { tab -> })

        val outerPadding = PaddingValues();

        composeTestRule.setContent {
            RewatchTheme(darkTheme = true) {
                PersonPage(actions = actions, outerPadding = outerPadding, personId = defaultPersonId, viewModel = defaultPersonViewModel)
            }
        }
    }

    @Test
    fun testBirthInfo() {
        /*Thread.sleep(4000)
        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithText("Born")
                .fetchSemanticsNodes().isNotEmpty()
        }*/
        Thread.sleep(4000)

        try {
            composeTestRule.onNodeWithTag("birthInfo").assertExists().assertIsDisplayed()
            try {
                composeTestRule.onNodeWithText("Age").assertExists().assertIsDisplayed()
            }catch (e: AssertionError){
                composeTestRule.onNodeWithText("Died").assertExists().assertIsDisplayed()
            }
        }
        catch (e: AssertionError){
            composeTestRule.onNodeWithText("Born").assertDoesNotExist()
        }
    }

    @Test
    fun testBio() {
        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("bioParagraph").assertExists().assertIsDisplayed()
    }

    @Test
    fun testProfilePicture() {
        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("profilePicture").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSectionTabRow(){
        Thread.sleep(4000)
        val tabs = composeTestRule.onNodeWithTag("Tab_Row").assertExists().assertIsDisplayed()
            .onChildren()

        tabs.assertAll(hasClickAction()).assertAll(isSelectable())

        val firstTab = tabs.onFirst().assertExists().assertIsDisplayed().assertIsSelected()
        val lastTab = tabs.onLast().assertExists().assertIsDisplayed().assertIsNotSelected()

        var currentTab = firstTab

        do{
            currentTab = currentTab.onSibling()

            currentTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            currentTab.assertIsSelected()
        } while (currentTab != lastTab)
    }

    @Test
    fun testSortTabDate() {
        defaultSetup()

        Thread.sleep(4000)
        val dateFilter = composeTestRule.onNodeWithTag("filterChip_Date").assertExists().assertIsSelectable()

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        dateFilter.assertIsSelected()

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
        dateFilter.assertIsSelected()
    }

    @Test
    fun testSortTabPopularity() {
        defaultSetup()

        Thread.sleep(4000)
        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_Popularity").assertIsSelectable()

        popularityFilter.assertIsSelected().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        popularityFilter.assertIsSelected()
    }
}