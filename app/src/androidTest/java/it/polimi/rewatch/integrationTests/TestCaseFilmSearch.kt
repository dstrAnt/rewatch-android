package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.HomeTabItem
import it.polimi.rewatch.screens.search.SearchPage
import okhttp3.internal.checkOffsetAndCount
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseFilmSearch {
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
            //SearchPage(actions = actions, outerPadding = outerPadding, pagerState = )
        }
    }

    @Test
    fun test() {
        //composeTestRule.onNodeWithText("Title").assertIsDisplayed()
    }
}