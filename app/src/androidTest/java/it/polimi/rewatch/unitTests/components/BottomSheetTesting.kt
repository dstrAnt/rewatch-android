package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.runtime.mutableStateOf
import org.junit.Before
import org.junit.Rule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import org.junit.Test

class BottomSheetTesting {

    lateinit var actions : NavigationActions

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        actions = NavigationActions(onNavigateToPerson = { id -> },

        onNavigateToMedia = { id ->  id.parentId + 1},
        onNavigateBack = {

        },
        onFullscreenImage = { url ->

        },
        onBottomSheet = { info ->  },
        onNavigateToLogin = {

        },
        onBackToHome = { tab ->

        })

        composeTestRule.setContent { BottomSheet(PosterInfo(MovieId(0)), actions = actions) { } }
    }

    @Test
    fun testBottomSheetHeader(){

    }

    @Test
    fun testBottomSheetActions(){

    }

    @Test
    fun testBottomSheetRating(){

    }
    @Test
    fun testBottomSheetWatchStatus(){

    }
    @Test
    fun testBottomSheetFooterAction(){

    }

}