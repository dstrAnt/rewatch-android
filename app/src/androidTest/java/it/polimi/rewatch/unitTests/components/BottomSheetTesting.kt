package it.polimi.rewatch.unitTests.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.model.TmdbMovieDetail
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
import it.polimi.rewatch.screens.tracking.getRatingHeader
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Test
import java.time.LocalDateTime

@HiltAndroidTest
class BottomSheetTesting {

    lateinit var actions : NavigationActions

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    val media = MovieId(0);

    val String.Companion.Empty: String get() = ""
    lateinit var bottomSheetViewModel : BottomSheetViewModel


    //@OptIn(ExperimentalMaterial3Api::class)
    @Before
    fun setup() {
        actions = NavigationActions(onNavigateToPerson = { id -> },

        onNavigateToMedia = { id ->  },
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

            BottomSheet(PosterInfo(media), actions = actions, viewModel = viewModel) { }
        }
    }

    @Test
    fun testBottomSheetWatchDate(){

        val datePickerDialogConfirmButton = composeTestRule.onNodeWithText("Select").assertExists().assertIsNotDisplayed()
        val watchDateTrigger = composeTestRule.onNodeWithText("Content watched on ").onSibling().assertExists().assertIsNotDisplayed()

        val datePickerDialog = datePickerDialogConfirmButton.onParent().assertExists().assertIsNotDisplayed()

        composeTestRule.onNodeWithTag("bottomSheetActions").onChildren().onFirst().performClick()
        watchDateTrigger.assertIsDisplayed()
        watchDateTrigger.performClick()

        datePickerDialogConfirmButton.assertIsDisplayed()

        datePickerDialog.assertIsDisplayed()

        // Select a date in the picker...
        datePickerDialog.performTouchInput { click(center) }
        datePickerDialogConfirmButton.performClick()

        val watchedItemDate = bottomSheetViewModel.persistRepo.watchedFlow
            .map { watched -> watched.getOrNull()?.find { it.id == media } }
            .map { result -> result?.date}

        assert(watchedItemDate != null)

        // Dismissing the BottomSheet
        composeTestRule.onNodeWithText("Dismiss").assertExists().assertIsDisplayed()
        datePickerDialog.assertIsNotDisplayed()
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

        // Verifying that clicking on the header dismisses the BottomSheet
        bottomSheetHeader.performClick()
        bottomSheetHeader.assertIsNotDisplayed()
    }

    @Test
    fun testBottomSheetActions(){

        /*
         * An initial Test to check existence
         */

        val bottomSheetActions = composeTestRule.onNodeWithTag("bottomSheetActions").assertExists()

        val firstIconButton = bottomSheetActions.onChildren().onFirst().assertIsDisplayed()
        val secondIconButton = bottomSheetActions.onChildren().onLast().assertIsDisplayed()

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Watchlist").assertExists().assertIsDisplayed()

        /*
         * Testing all the basic functionalities
         */

        // Verifying that the Film is added correctly to the Watched list/ Watchlist

        firstIconButton.performClick()
        composeTestRule.onNodeWithText("Watched").assertExists().assertIsDisplayed()
        assert(bottomSheetViewModel.watchedItem != null)

        secondIconButton.performClick()
        composeTestRule.onNodeWithText("In watchlist").assertExists().assertIsDisplayed()
        assert(bottomSheetViewModel.watchlistItem != null)

        // Verifying that the Film is removed correctly from the Watched list/ Watchlist

        firstIconButton.performClick()
        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()
        assert(bottomSheetViewModel.watchedItem != null)

        secondIconButton.performClick()
        composeTestRule.onNodeWithText("Watchlist").assertExists().assertIsDisplayed()
        assert(bottomSheetViewModel.watchlistItem != null)


        // Verifying that the Rating is added automatically when the Film becomes Watched

        composeTestRule.onNodeWithTag("bottomSheetStarRating").performTouchInput { click(center) }
        composeTestRule.onNodeWithText("Watched").assertExists().assertIsDisplayed()
        assert(bottomSheetViewModel.watchedItem != null)

        /*
         * Testing to check behavior with rating
         */

        // Verifying that the user cannot remove from Watched list until the rating is back to 0 stars

        firstIconButton.performClick()

        val snackbarShower = composeTestRule.onNodeWithText("Please remove your rating before unwatching.").assertExists().assertIsDisplayed()
        Thread.sleep(5000)

        snackbarShower.performTouchInput { swipeRight(30f, 200f) }
        Thread.sleep(5000)

        snackbarShower.assertIsNotDisplayed()

        // Verifying that the user can finally remove the film from Watched list
        composeTestRule.onNodeWithTag("bottomSheetStarRating").performTouchInput { click(Offset(left.toLong())) }
        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()
        snackbarShower.assertIsNotDisplayed()
        assert(bottomSheetViewModel.watchedItem == null)

    }

    @Test
    fun testBottomSheetRating(){
        val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

        composeTestRule.onNodeWithText("Rate now").assertExists().assertIsDisplayed()

        bottomSheetStarRating.performTouchInput { click(center) }
        composeTestRule.onNodeWithText("Rate now").assertExists().assertIsNotDisplayed()
        assert(bottomSheetViewModel.watchedItem != null)

        /*val rating =
            runBlocking {
                bottomSheetViewModel?.watchedItem?.map { watchedItem -> watchedItem?.rating }?.single()
            }*/

        val rating = bottomSheetViewModel.watchedItem?.value?.rating
        composeTestRule.onNodeWithText(getRatingHeader(rating?.times(2))).assertExists().assertIsDisplayed()
    }

    @Test
    fun testBottomSheetWatchStatus(){
        composeTestRule.onNodeWithText("You haven't watched this content yet").assertExists().assertIsDisplayed()

        val bottomSheetActions = composeTestRule.onNodeWithTag("bottomSheetActions").assertExists()
        bottomSheetActions.onChildren().onFirst().assertIsDisplayed().performClick()

        composeTestRule.onNodeWithText("Content watched on ").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(bottomSheetViewModel.watchedItem?.value?.date?.toString()?.formatDate("d MMM YYYY").orEmpty())
            .assertExists().assertIsDisplayed()

        bottomSheetActions.onChildren().onFirst().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("You haven't watched this content yet").assertExists().assertIsDisplayed()
    }
}