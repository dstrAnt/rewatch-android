package it.polimi.rewatch.widgetTests.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import app.moviebase.tmdb.Tmdb3
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.media.MediaViewModel
import it.polimi.rewatch.theme.RewatchTheme
import org.junit.Rule
import org.junit.Test

/**
 * Test Cases designed to test Class Theme, which defines Composable RewatchTheme
 */

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

    // Setup for Dark/Light Mode
    fun setup(dark: Boolean) {

        mediaViewModel = MediaViewModel(mediaId = defaultMedia, persistRepo = fakeRepo, repository = TmdbRepository(Tmdb3("***REMOVED_TMDB_KEY***")))

        composeTestRule.setContent {
            RewatchTheme(darkTheme = dark) {
                MediaPage(defaultMedia, actions = defaultActions, outerPadding = defaultOuterPadding, viewModel = mediaViewModel)
            }
        }
    }

    // Setup for Dark/Light Mode without Dynamic Coloring
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

        // Obliging Test Compose to wait for Content
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