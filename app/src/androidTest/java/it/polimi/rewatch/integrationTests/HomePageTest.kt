package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.HomeTabItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomePageTest {
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
            HomePage(actions = actions, outerPadding = outerPadding, startTab = HomeTabItem.MOVIES)
        }
    }

    @Test
    fun testBrowseFilms() {

    }

    @Test
    fun testHomeButton() {

    }

    @Test
    fun testMovieSectionLayout() {

    }

}