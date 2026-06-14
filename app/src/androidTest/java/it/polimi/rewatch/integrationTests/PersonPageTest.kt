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
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.performClick
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbFileImage
import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonImages
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
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

    /*  Junk Code:
     * images = TmdbPersonImages(List<TmdbFileImage>(1, { _ -> TmdbFileImage(
                    filePath = "https://www.google.com/url?sa=t&source=web&rct=j&url=https%3A%2F%2Fopen.spotify.com%2Fintl-it%2Fartist%2F19UGUoQUK2qqbtl5vYkAi2&ved=0CBYQjRxqFwoTCIjV4pHUhJUDFQAAAAAdAAAAABBU&opi=89978449",
                    aspectRatio = 16f/9,
                    height = 200,
                    width = 200
                )})),
    */

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
        val actions = defaultActions
        val outerPadding = defaultOuterPadding

        val personData = PersonDetailsData(
            details = TmdbPersonDetail(
                birthday = LocalDate.now().toKotlinLocalDate(),
                biography = "She participated in the cast of Avengers Endgame",
                name = "Scarlett Johansson",
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
        val tabs = composeTestRule.onNodeWithTag("Tab_Row").assertExists().assertIsDisplayed()
            .onChildren()

        tabs.assertAll(hasClickAction()).assertAll(isSelectable())

        val firstTab = tabs.onFirst().assertExists().assertIsDisplayed().assertIsSelected()
        val lastTab = tabs.onLast().assertExists().assertIsDisplayed().assertIsNotSelected()

        var myCurrentTab = firstTab

        do{
            myCurrentTab = myCurrentTab.onSibling()

            myCurrentTab.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(3000)

            myCurrentTab.assertIsSelected()
        } while (myCurrentTab != lastTab)
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