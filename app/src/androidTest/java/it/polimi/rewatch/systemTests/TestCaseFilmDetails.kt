package it.polimi.rewatch.systemTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import it.polimi.rewatch.MainActivity
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestCaseFilmDetails {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        /*composeTestRule.setContent {
            MainActivity()
        }*/
    }
    @Test
    fun test() {

    }
}