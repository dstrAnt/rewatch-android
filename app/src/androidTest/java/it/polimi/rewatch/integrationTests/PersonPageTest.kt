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
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbPersonCredits
import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.screens.media.MediaContent
import it.polimi.rewatch.screens.media.MovieData
import it.polimi.rewatch.screens.person.PersonContent
import it.polimi.rewatch.screens.person.PersonDetailsData
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

    val defaultActions = NavigationActions(onNavigateToPerson = { id -> },
        onNavigateToMedia = { id -> },
        onNavigateBack = {},
        onFullscreenImage = { url -> },
        onBottomSheet = { info ->  },
        onNavigateToLogin = {},
        onBackToHome = { tab -> })

    val defaultOuterPadding = PaddingValues();

    val defaultPersonViewModel = PersonViewModel(personId = defaultPersonId, TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")))

    fun defaultSetup() {
        composeTestRule.setContent {
            RewatchTheme(darkTheme = true) {
                PersonPage(actions = defaultActions, outerPadding = defaultOuterPadding, personId = defaultPersonId, viewModel = defaultPersonViewModel)
            }
        }
    }

    @Test
    fun testEmptyPageInfo() {
        defaultSetup()
        Thread.sleep(4000)

        composeTestRule.onNodeWithText("Born").assertDoesNotExist()
        composeTestRule.onNodeWithText("Age").assertDoesNotExist()
        composeTestRule.onNodeWithText("Died").assertDoesNotExist()
        composeTestRule.onNodeWithTag("bioParagraph").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profilePicture").assertDoesNotExist()
    }

    @Test
    fun testNonEmptyPageInfo(){
        val actions = defaultActions
        val outerPadding = defaultOuterPadding

        val personData = PersonDetailsData(
            details = TmdbPersonDetail(),
            movieCredits = TmdbPersonMovieCredits(),
            showCredits = TmdbPersonShowCredits(),
        )

        composeTestRule.setContent {
            PersonContent(
                data = personData,
                outerPadding = outerPadding,
                actions = actions,
                currentTab = TODO(),
                availableTabs = TODO(),
                sortOrder = TODO(),
                isAscending = TODO(),
                displayList = TODO(),
                onTabSelected = TODO(),
                onSortSelected = TODO(),
            )
        }

        composeTestRule.onNodeWithTag("birthInfo").assertExists().assertIsDisplayed()
        try {
            composeTestRule.onNodeWithText("Age").assertExists().assertIsDisplayed()
        }catch (e: AssertionError){
            composeTestRule.onNodeWithText("Died").assertExists().assertIsDisplayed()
        }

        composeTestRule.onNodeWithTag("bioParagraph").assertExists().assertIsDisplayed()
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