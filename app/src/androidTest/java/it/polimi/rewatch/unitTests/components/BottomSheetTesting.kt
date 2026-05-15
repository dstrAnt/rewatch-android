package it.polimi.rewatch.unitTests.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performTouchInput
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.model.TmdbMovieDetail
import org.junit.Before
import org.junit.Rule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.junit.Test
import java.time.LocalDateTime

class BottomSheetTesting {

    lateinit var actions : NavigationActions

    @get:Rule
    val composeTestRule = createComposeRule()

    val media = MovieId(0);

    /*@OptIn(ExperimentalMaterial3Api::class)
    var sheetState : SheetState? = null*/

    val String.Companion.Empty: String get() = ""
    var bottomSheetViewModel : BottomSheetViewModel? = null


    //@OptIn(ExperimentalMaterial3Api::class)
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

            /*val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            this.sheetState = sheetState*/

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
        val bottomSheetHeader = composeTestRule.onNodeWithTag("bottomSheetHeader").assertExists()

        var firstTextNode = bottomSheetHeader.onChildren().onFirst()
        var secondTextNode = bottomSheetHeader.onChildren().onLast()

        firstTextNode.assertTextEquals(String.Empty)
        secondTextNode.assertDoesNotExist()

        val releaseDate : LocalDate? = LocalDateTime.of(2026, 5, 13, 0, 0).toLocalDate() as LocalDate?
        val movie = MediaDetail(TmdbMovieDetail(title = "My Title", releaseDate = releaseDate))

        composeTestRule.setContent { BottomSheet(PosterInfo(movie, media), actions = actions) { } }

        firstTextNode = bottomSheetHeader.onChildren().onFirst()
        secondTextNode = bottomSheetHeader.onChildren().onLast()

        firstTextNode.assertTextEquals("My Title")
        secondTextNode.assertExists().assertTextEquals(releaseDate.toString().formatDate("MMM YYYY"))
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