package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbDepartment
import app.moviebase.tmdb.model.TmdbFileImage
import app.moviebase.tmdb.model.TmdbPersonCredit
import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonImages
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import it.polimi.rewatch.BuildConfig
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.PersonId
import it.polimi.rewatch.screens.person.PersonContent
import it.polimi.rewatch.screens.person.PersonDetailsData
import it.polimi.rewatch.screens.person.PersonPage
import it.polimi.rewatch.screens.person.PersonViewModel
import it.polimi.rewatch.theme.RewatchTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

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

    val defaultPersonViewModel = PersonViewModel(personId = defaultPersonId, TmdbRepository(Tmdb3(BuildConfig.TMDB_API_KEY)))

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
            details = TmdbPersonDetail(
                birthday = LocalDate.now().toKotlinLocalDate(),
                biography = "A little cute mouse who loves to help people",
                name = "Stuart Little",
                profilePath = "https://upload.wikimedia.org/wikipedia/en/9/96/Stuart_Little.jpg"
            ),
            movieCredits = TmdbPersonMovieCredits(),
            showCredits = TmdbPersonShowCredits(),
        )

        val currentTab = runBlocking { defaultPersonViewModel.currentTab.first() }
        val sortOrder = runBlocking { defaultPersonViewModel.sortOrder.first() }
        val isAscending = runBlocking { defaultPersonViewModel.isAscending.first() }
        val availableTabs = runBlocking { defaultPersonViewModel.availableTabs.first() }
        val displayList = runBlocking { defaultPersonViewModel.displayList.first() }

        composeTestRule.setContent {
            RewatchTheme()
            {
                PersonContent(
                    data = personData,
                    outerPadding = outerPadding,
                    actions = actions,
                    currentTab = currentTab ?: "",
                    availableTabs = availableTabs,
                    sortOrder = sortOrder,
                    isAscending = isAscending,
                    displayList = displayList,
                    onTabSelected = defaultPersonViewModel::onTabSelected,
                    onSortSelected = defaultPersonViewModel::onSortSelected,
                )
            }
        }

        Thread.sleep(4000)

        composeTestRule.onNodeWithTag("birthInfo").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Age").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithTag("bioParagraph").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("profilePicture").assertExists().assertIsNotDisplayed()
    }

    @Test
    fun testDeadPersonInfo(){
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val birthDay = LocalDateTime(1971, 6, 12, 0,0).toJavaLocalDateTime().toLocalDate().toKotlinLocalDate()

        val personData = PersonDetailsData(
            details = TmdbPersonDetail(
                birthday = birthDay,
                deathday = LocalDate.now().toKotlinLocalDate(),
                biography = "An English Murderer",
                name = "Jack the Headchopper",
                profilePath = "https://upload.wikimedia.org/wikipedia/en/9/96/Stuart_Little.jpg"
            ),
            movieCredits = TmdbPersonMovieCredits(),
            showCredits = TmdbPersonShowCredits(),
        )

        val currentTab = runBlocking { defaultPersonViewModel.currentTab.first() }
        val sortOrder = runBlocking { defaultPersonViewModel.sortOrder.first() }
        val isAscending = runBlocking { defaultPersonViewModel.isAscending.first() }
        val availableTabs = runBlocking { defaultPersonViewModel.availableTabs.first() }
        val displayList = runBlocking { defaultPersonViewModel.displayList.first() }

        composeTestRule.setContent {
            RewatchTheme()
            {
                PersonContent(
                    data = personData,
                    outerPadding = outerPadding,
                    actions = actions,
                    currentTab = currentTab ?: "",
                    availableTabs = availableTabs,
                    sortOrder = sortOrder,
                    isAscending = isAscending,
                    displayList = displayList,
                    onTabSelected = defaultPersonViewModel::onTabSelected,
                    onSortSelected = defaultPersonViewModel::onSortSelected,
                )
            }
        }

        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("birthInfo").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Died").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSectionTabRow(){
        defaultSetup()

        Thread.sleep(5000)

        val tabs = composeTestRule.onAllNodesWithTag("customTabs").assertAll(isSelectable())

        tabs.assertAll(hasClickAction()).assertAll(isSelectable())
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