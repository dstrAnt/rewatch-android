package it.polimi.rewatch.systemTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseFilmRating {
    @get:Rule
    val composeTestRule = createComposeRule()

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
            MediaPage(MovieId(0), actions = actions, outerPadding = outerPadding)
        }
    }
    @Test
    fun test() {

    }
}