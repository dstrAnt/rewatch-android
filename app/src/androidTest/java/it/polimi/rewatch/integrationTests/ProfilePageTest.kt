package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class ProfilePageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val repo = FakeRepository()
    val profileViewModel = ProfileViewModel(repository = repo)

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
        composeTestRule.onNodeWithText("Guest").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()
        Thread.sleep(3000)

        val email = "mark.zuck@meta.com"
        runBlocking {  repo.verifySignInLink(email,"") }
        Thread.sleep(5000)


        composeTestRule.onNodeWithText("Guest").assertDoesNotExist()
        composeTestRule.onNodeWithText("Logout").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(email).assertExists().assertIsDisplayed()
        Thread.sleep(5000)
    }

    @Test
    fun testProfileBoxRows() {

        // Calculating the number of tracked Films
        var watchedMovies = profileViewModel.watchedMovies; var nWatchedMovies = runBlocking { watchedMovies.first() }
        var watchedShows = profileViewModel.watchedShows; var nWatchedShows = runBlocking { watchedShows.first() }
        var watchlistShows = profileViewModel.watchlistShows; var nWatchlistShows = runBlocking { watchlistShows.first() }
        var watchlistMovies = profileViewModel.watchlistMovies; var nWatchlistMovies = runBlocking { watchlistMovies.first() }

        composeTestRule.onNodeWithText("Movies in watchlist").assertExists().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("$nWatchedMovies")[0].assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Movies watched").assertExists().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("$nWatchedShows")[0].assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Tv Shows in watchlist").assertExists().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("$nWatchlistShows")[0].assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Tv Shows watched").assertExists().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("$nWatchlistMovies")[0].assertExists().assertIsDisplayed()
        Thread.sleep(5000)

        // Adding two Films for the Watchlist and the Watched List
        profileViewModel.repository.addToWatchlist(MovieId(0))
        profileViewModel.repository.addToWatchedList(MovieId(0), 0.0f, LocalDateTime.now())
        profileViewModel.repository.addToWatchedList(TvId(0), 0.0f, LocalDateTime.now())
        profileViewModel.repository.addToWatchlist(TvId(0))

        // Calculating the number of tracked Films
        composeTestRule.onNodeWithTag("profile_page").assertExists()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        // Realculating the number of tracked Films
        watchedMovies = profileViewModel.watchedMovies
         watchedShows = profileViewModel.watchedShows
         watchlistShows = profileViewModel.watchlistShows
         watchlistMovies = profileViewModel.watchlistMovies

        nWatchedMovies = runBlocking { watchedMovies.first() }
        nWatchedShows = runBlocking { watchedShows.first() }
        nWatchlistShows = runBlocking { watchlistShows.first() }
        nWatchlistMovies = runBlocking { watchlistMovies.first() }

        composeTestRule.onAllNodesWithText("${nWatchedMovies}")[0].assertExists().assertIsDisplayed()

        composeTestRule.onAllNodesWithText("${nWatchedShows}")[0].assertExists().assertIsDisplayed()

        composeTestRule.onAllNodesWithText("${nWatchlistShows}")[0].assertExists().assertIsDisplayed()

        composeTestRule.onAllNodesWithText("${nWatchlistMovies}")[0].assertExists().assertIsDisplayed()
    }

    @Test
    fun testLogOut() {

        // Simulate a correct Login
        val email = "mark.zuck@meta.com"
        runBlocking {  repo.verifySignInLink(email,"") }
        Thread.sleep(5000)

        // Find and click on the Logout Button
        val logButton = composeTestRule.onNodeWithTag("logButton").assertExists().assertIsDisplayed()
        Thread.sleep(5000)
        try {
            composeTestRule.onNodeWithText("Logout").assertExists().assertIsDisplayed()
            logButton.performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(5000)
        }
        catch (e: AssertionError){
            println("User is not logged! This test must halt.")
            assert(false)
        }
        composeTestRule.onNodeWithText("Login").assertExists().assertIsDisplayed()
    }
}