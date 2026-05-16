package it.polimi.rewatch.unitTests.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import app.moviebase.tmdb.model.TmdbMovieDetail
import it.polimi.rewatch.common.FakeRepository
import org.junit.Before
import org.junit.Rule
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.utility.FakeBottomSheetViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Test
import java.time.LocalDateTime

class BottomSheetTesting {

    lateinit var actions : NavigationActions

    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.createComposeRule()

    val media = MovieId(0);

    val String.Companion.Empty: String get() = ""

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
    }

    @Test
    fun debug() {
        composeTestRule.setContent {
            androidx.compose.material3.Text("HELLO")
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testBottomSheetWatchDate(){

        composeTestRule.setContent {

            BottomSheet(PosterInfo(media), actions = actions) { }
        }

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

        // Dismissing the BottomSheet
        composeTestRule.onNodeWithText("Dismiss").assertExists().assertIsDisplayed()
        datePickerDialog.assertIsNotDisplayed()
    }

    @Test
    fun testBottomSheetHeader1(){

        composeTestRule.setContent {

            BottomSheet(PosterInfo(media), actions = actions) { }
        }

        val bottomSheetHeader = composeTestRule.onNodeWithTag("bottomSheetHeader").assertExists()

        val firstTextNode = bottomSheetHeader.onChildren().onFirst()
        val secondTextNode = bottomSheetHeader.onChildren().onLast()

        firstTextNode.assertTextEquals(String.Empty)
        secondTextNode.assertDoesNotExist()

    }

    @Test
    fun testBottomSheetHeader2(){

        val releaseDate : LocalDate? = LocalDate(2026, 5, 13) as LocalDate?
        val movie = MediaDetail(TmdbMovieDetail(title = "My Title", releaseDate = releaseDate))

        composeTestRule.setContent { BottomSheet(PosterInfo(movie, media), actions = actions) { } }

        val bottomSheetHeader = composeTestRule.onNodeWithTag("bottomSheetHeader").assertExists()

        val firstTextNode = bottomSheetHeader.onChildren().onFirst()
        val secondTextNode = bottomSheetHeader.onChildren().onLast()

        firstTextNode.assertTextEquals("My Title")
        secondTextNode.assertExists().assertTextEquals(releaseDate.toString().formatDate("MMM YYYY"))

        // Verifying that clicking on the header dismisses the BottomSheet
        bottomSheetHeader.performClick()
        bottomSheetHeader.assertIsNotDisplayed()
    }

    @Test
    fun testBottomSheetActions(){

        composeTestRule.setContent { BottomSheet(PosterInfo(media), actions = actions) { } }

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

        secondIconButton.performClick()
        composeTestRule.onNodeWithText("In watchlist").assertExists().assertIsDisplayed()

        // Verifying that the Film is removed correctly from the Watched list/ Watchlist

        firstIconButton.performClick()
        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()

        secondIconButton.performClick()
        composeTestRule.onNodeWithText("Watchlist").assertExists().assertIsDisplayed()


        // Verifying that the Rating is added automatically when the Film becomes Watched

        composeTestRule.onNodeWithTag("bottomSheetStarRating").performTouchInput { click(center) }
        composeTestRule.onNodeWithText("Watched").assertExists().assertIsDisplayed()

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
    }

    @Test
    fun testBottomSheetRating(){

        composeTestRule.setContent { BottomSheet(PosterInfo( media), actions = actions) { } }

        val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

        composeTestRule.onNodeWithText("Rate now").assertExists().assertIsDisplayed()

        bottomSheetStarRating.performTouchInput { click(center) }
        composeTestRule.onNodeWithText("Rate now").assertExists().assertIsNotDisplayed()

        composeTestRule.onNodeWithText("Below Average").assertExists().assertIsDisplayed()
    }

    @Test
    fun testBottomSheetWatchStatus(){

        val bottomSheetViewModel = //FakeBottomSheetViewModel(media, FakeRepository())
            BottomSheetViewModel(
                id = media,
                persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo( media), actions = actions, viewModel = bottomSheetViewModel) { } }

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("You haven't watched this content yet").assertExists().assertIsDisplayed()

        val bottomSheetActions = composeTestRule.onNodeWithTag("bottomSheetActions").assertExists()
        bottomSheetActions.onChildren().onFirst().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Content watched on ").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(LocalDateTime.now()?.toString()?.formatDate("d MMM YYYY").orEmpty())
            .assertExists().assertIsDisplayed()

        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        bottomSheetActions.onChildren().onFirst().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)
        composeTestRule.onNodeWithText("You haven't watched this content yet").assertExists().assertIsDisplayed()
    }
}