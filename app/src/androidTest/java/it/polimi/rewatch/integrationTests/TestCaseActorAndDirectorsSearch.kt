package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.search.SearchPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseActorAndDirectorsSearch {
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
            val pagerState = rememberPagerState(pageCount = { BottomTabs.entries.size })
            SearchPage(actions = actions, outerPadding = outerPadding, pagerState = pagerState )
        }
    }

    @Test
    fun testSearchTopBarPerson() {

    }

    @Test
    fun testSearchInputFieldPerson(){

    }

    @Test
    fun testSearchResultsListPerson(){

    }

    @Test
    fun testEmptySearchStatePerson(){

    }
}