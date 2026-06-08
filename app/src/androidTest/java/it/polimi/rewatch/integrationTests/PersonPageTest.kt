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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PersonPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val personViewModel = PersonViewModel(personId = 0, TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")))

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
            PersonPage(actions = actions, outerPadding = outerPadding, personId = 0, viewModel = personViewModel)
        }
    }

    @Test
    fun testBirthInfo() {
        composeTestRule.onNodeWithText("Born").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("").assertExists().assertIsDisplayed()
        try {
            composeTestRule.onNodeWithText("Age").assertExists().assertIsDisplayed()
        }catch (e: AssertionError){
            composeTestRule.onNodeWithText("Died").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun testBio() {
        composeTestRule.onNodeWithTag("bioParagraph").assertExists().assertIsDisplayed()
    }

    @Test
    fun testProfilePicture() {
        composeTestRule.onNodeWithTag("profilePicture").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSectionTabRow(){
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
        val dateFilter = composeTestRule.onNodeWithTag("filterChip_DATE").assertExists().assertIsSelectable()

        dateFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        dateFilter.assertIsSelected()
    }

    @Test
    fun testSortTabPopularity() {
        val popularityFilter = composeTestRule.onNodeWithTag("filterChip_POPULARITY").assertIsSelectable()

        popularityFilter.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        popularityFilter.assertIsSelected()
    }
}