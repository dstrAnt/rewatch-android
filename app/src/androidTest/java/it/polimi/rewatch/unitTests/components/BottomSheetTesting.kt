package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performTouchInput
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.junit.Before
import org.junit.Rule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import kotlinx.coroutines.flow.map
import org.junit.Test

class BottomSheetTesting {

    lateinit var actions : NavigationActions

    @get:Rule
    val composeTestRule = createComposeRule()

    val media = MovieId(0);
    var bottomSheetViewModel : BottomSheetViewModel? = null

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


        composeTestRule.setContent {
            val viewModel = hiltViewModel<BottomSheetViewModel, BottomSheetViewModel.Factory>(
                key = "sheet_${media.parentId}",
                creationCallback = { it.create(media) },
            )

            bottomSheetViewModel  = viewModel

            BottomSheet(PosterInfo(media), actions = actions) { }
        }
    }

    @Test
    fun testBottomSheetWatchDate(){

        val datePickerDialog = composeTestRule.onNodeWithText("Select").onParent()

        // Select a date in the picker...
        datePickerDialog.performTouchInput { click(center) }

        val watchedItemDate = bottomSheetViewModel?.persistRepo?.watchedFlow
            ?.map { watched -> watched.getOrNull()?.find { it.id == media } }
            ?.map { result -> result?.date}

        assert(watchedItemDate != null)

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