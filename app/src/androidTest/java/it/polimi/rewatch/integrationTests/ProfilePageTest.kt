package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.polimi.rewatch.common.AuthState
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.screens.settings.ProfilePage
import it.polimi.rewatch.screens.settings.ProfileViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class ProfilePageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val profileViewModel = ProfileViewModel(repository = FakeRepository())

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
            ProfilePage(actions = actions, outerPadding = outerPadding, vm = profileViewModel)
        }
    }

    @Test
    fun testProfileBox() {
        val email = profileViewModel.email

        if(!email.equals("")){
            composeTestRule.onNodeWithText("$email").assertExists().assertIsDisplayed()
        }
        else{
            composeTestRule.onNodeWithText("Guest").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun testProfileBoxRows() {

        var watchedMovies = profileViewModel.watchedMovies
        var watchedShows = profileViewModel.watchedShows
        var watchlistShows = profileViewModel.watchlistShows
        var watchlistMovies = profileViewModel.watchlistMovies

        composeTestRule.onNodeWithText("Movies in watchlist").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("$watchlistMovies").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Movies watched").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("$watchedMovies").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Tv Shows in watchlist").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("$watchlistShows").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Tv Shows watched").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("$watchedShows").assertExists().assertIsDisplayed()

        profileViewModel.repository.addToWatchlist(MovieId(0))
        profileViewModel.repository.addToWatchedList(MovieId(0), 0.0f, LocalDateTime.now())
        profileViewModel.repository.addToWatchedList(TvId(0), 0.0f, LocalDateTime.now())
        profileViewModel.repository.addToWatchlist(TvId(0))

        composeTestRule.onNodeWithTag("profile_page").assertExists()
        composeTestRule.waitForIdle()


        watchedMovies = profileViewModel.watchedMovies
         watchedShows = profileViewModel.watchedShows
         watchlistShows = profileViewModel.watchlistShows
         watchlistMovies = profileViewModel.watchlistMovies
        composeTestRule.onNodeWithText("${watchlistMovies}").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("${watchedMovies}").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("${watchlistShows}").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("${watchedShows}").assertExists().assertIsDisplayed()
    }

    @Test
    fun testLogOut() {
        val authState = profileViewModel.state

        val logButton = composeTestRule.onNodeWithTag("logButton").assertExists().assertIsDisplayed()

        if (authState is AuthState.Authenticated){
            logButton.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)

            assert(profileViewModel.state is AuthState.SignedOut)
        }
        else{
            println("User is not logged! This test must halt.")
            assert(false)
        }
    }
}