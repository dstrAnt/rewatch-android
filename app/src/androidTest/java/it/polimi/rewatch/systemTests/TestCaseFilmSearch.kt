package it.polimi.rewatch.systemTests

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import it.polimi.rewatch.BottomTabs
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.screens.search.SearchPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseFilmSearch {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {
            MainActivity()
        }
    }

    @Test
    fun testSearchTopBarFilm() {

    }

    @Test
    fun testSearchInputFieldFilm(){

    }

    @Test
    fun testSearchResultsListFilm(){

    }

    @Test
    fun testEmptySearchState(){

    }
}