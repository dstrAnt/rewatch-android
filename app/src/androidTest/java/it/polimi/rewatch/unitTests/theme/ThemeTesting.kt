package it.polimi.rewatch.unitTests.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DarkMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.printToString
import app.moviebase.tmdb.Tmdb3
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.media.MediaViewModel
import org.junit.Test
import it.polimi.rewatch.theme.*;
import org.junit.Before
import org.junit.Rule

internal class ThemeTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    val defaultMedia = MovieId(551)
    val fakeRepo = FakeRepository()
    lateinit var mediaViewModel: MediaViewModel

    val defaultActions = NavigationActions(onNavigateToPerson = { id -> },
        onNavigateToMedia = { id -> },
        onNavigateBack = {},
        onFullscreenImage = { url -> },
        onBottomSheet = { info ->  },
        onNavigateToLogin = {},
        onBackToHome = { tab -> })

    val defaultOuterPadding = PaddingValues();


    fun setup(dark: Boolean) {

        mediaViewModel = MediaViewModel(mediaId = defaultMedia, persistRepo = fakeRepo, repository = TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")))

        composeTestRule.setContent {
            RewatchTheme(darkTheme = dark) {
                MediaPage(defaultMedia, actions = defaultActions, outerPadding = defaultOuterPadding, viewModel = mediaViewModel)
            }
        }
    }

    fun setupNoDynamicColor(dark: Boolean) {

        mediaViewModel = MediaViewModel(mediaId = defaultMedia, persistRepo = fakeRepo, repository = TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")))

        composeTestRule.setContent {
            RewatchTheme(darkTheme = dark, dynamicColor = false) {
                MediaPage(defaultMedia, actions = defaultActions, outerPadding = defaultOuterPadding, viewModel = mediaViewModel)
            }
        }
    }

    @Test
    fun testAppDarkTheme() {
        setup(true)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithText("Cast")
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(10000);
    }

    @Test
    fun testAppLightTheme (){
        setup(false)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithText("Cast")
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(10000);
    }

    @Test
    fun testAppStaticDarkTheme() {
        setupNoDynamicColor(true)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithText("Cast")
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(10000);
    }

    @Test
    fun testAppStaticLightTheme (){
        setupNoDynamicColor(false)

        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithText("Cast")
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(10000);
    }

}