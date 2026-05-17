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
    fun testBottomSheetWatchDateSelect(){

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo( media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Select").assertDoesNotExist()
        composeTestRule.onNodeWithText("Content watched on ").assertDoesNotExist()

        composeTestRule.onNodeWithTag("bottomSheetActions").onChildren().onFirst().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Content watched on ").assertExists()
        val watchDateTrigger = composeTestRule.onNodeWithTag("watchDateTrigger").assertExists()
        watchDateTrigger.assertIsDisplayed()
        watchDateTrigger.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val datePickerDialogConfirmButton = composeTestRule.onNodeWithText("Select").assertExists()
        val datePickerDialog = datePickerDialogConfirmButton.onParent().assertExists()

        datePickerDialogConfirmButton.assertIsDisplayed()

        datePickerDialog.assertIsDisplayed()

        // Select a date in the picker...
        datePickerDialog.performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        datePickerDialogConfirmButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()
    }

    @Test
    fun testBottomSheetWatchDateDismiss(){

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo( media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("bottomSheetActions").onChildren().onFirst().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Content watched on ").assertExists()
        val watchDateTrigger = composeTestRule.onNodeWithTag("watchDateTrigger").assertExists()
        watchDateTrigger.assertIsDisplayed()
        watchDateTrigger.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val datePickerDialogDismissButton = composeTestRule.onNodeWithText("Dismiss").assertExists().assertIsDisplayed()
        val datePickerDialog = datePickerDialogDismissButton.onParent().assertExists()

        datePickerDialogDismissButton.assertIsDisplayed()

        datePickerDialog.assertIsDisplayed()

        // Dismissing the BottomSheet
        datePickerDialogDismissButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()
    }

    @Test
    fun testBottomSheetHeader1(){

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo( media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val bottomSheetHeader = composeTestRule.onNodeWithTag("bottomSheetHeader").assertExists()

        val firstTextNode = bottomSheetHeader.onChildren().onFirst()
        val secondTextNode = bottomSheetHeader.onChildren().onLast()

        firstTextNode.assertTextEquals(String.Empty)
        secondTextNode.assertExists().assertIsNotDisplayed()

    }

    @Test
    fun testBottomSheetHeader2(){

        val releaseDate : LocalDate? = LocalDate(2026, 5, 13) as LocalDate?
        val movie = MediaDetail(TmdbMovieDetail(title = "My Title", releaseDate = releaseDate))

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo(movie, media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val bottomSheetHeader = composeTestRule.onNodeWithTag("bottomSheetHeader").assertExists()

        val firstTextNode = bottomSheetHeader.onChildren().onFirst()
        val secondTextNode = bottomSheetHeader.onChildren().onLast()

        firstTextNode.assertTextEquals("My Title")
        secondTextNode.assertExists().assertTextEquals(releaseDate.toString().formatDate("MMM YYYY"))

        // Verifying that clicking on the header dismisses the BottomSheet
        bottomSheetHeader.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        bottomSheetHeader.assertIsNotDisplayed()
    }

    @Test
    fun testBottomSheetActions1() {

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo(media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        /*
         * An initial Test to check existence
         */

        val bottomSheetActions = composeTestRule.onNodeWithTag("bottomSheetActions").assertExists()

        var firstIconButton = composeTestRule.onNodeWithContentDescription("Watch").assertExists().assertIsDisplayed()
        var secondIconButton = composeTestRule.onNodeWithContentDescription("Watchlist", substring = true).assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("  Watchlist  ").assertExists().assertIsDisplayed()

        /*
         * Testing all the basic functionalities
         */

        // Verifying that the Film is added correctly to the Watched list/ Watchlist

        firstIconButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Watched").assertExists().assertIsDisplayed()

        secondIconButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("In watchlist").assertExists().assertIsDisplayed()

        // Verifying that the Film is removed correctly from the Watched list/ Watchlist

        firstIconButton = composeTestRule.onNodeWithContentDescription("Watched").assertExists().assertIsDisplayed()
        secondIconButton = composeTestRule.onNodeWithContentDescription("In watchlist").assertExists().assertIsDisplayed()

        firstIconButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()

        secondIconButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Watchlist", substring = true).assertExists().assertIsDisplayed()
    }

    @Test
    fun testBottomSheetActions2(){

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo(media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        /*
         * An initial Test to check existence
         */

        val bottomSheetActions = composeTestRule.onNodeWithTag("bottomSheetActions").assertExists()

        val firstIconButton = bottomSheetActions.onChildren().onFirst().assertIsDisplayed()

        // Verifying that the Rating is added automatically when the Film becomes Watched

        composeTestRule.onNodeWithTag("bottomSheetStarRating").performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Watched").assertExists().assertIsDisplayed()

        /*
         * Testing to check behavior with rating
         */

        // Verifying that the user cannot remove from Watched list until the rating is back to 0 stars

        firstIconButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val snackbarShower = composeTestRule.onNodeWithText("Please remove your rating before unwatching.").assertExists().assertIsDisplayed()
        Thread.sleep(5000)

        snackbarShower.performTouchInput { swipeRight(30f, 200f) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        snackbarShower.assertIsNotDisplayed()

        // Verifying that the user can finally remove the film from Watched list
        composeTestRule.onNodeWithTag("bottomSheetStarRating").performTouchInput { click(Offset(left.toLong())) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        firstIconButton.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed()
        snackbarShower.assertIsNotDisplayed()
    }

    @Test
    fun testBottomSheetRating(){

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

        composeTestRule.setContent { BottomSheet(PosterInfo( media), actions = actions, viewModel = bottomSheetViewModel) { } }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        val bottomSheetStarRating = composeTestRule.onNodeWithTag("bottomSheetStarRating").assertExists()

        composeTestRule.onNodeWithText("Rate now").assertExists().assertIsDisplayed()

        bottomSheetStarRating.performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        Thread.sleep(5000)

        composeTestRule.onNodeWithText("Rate now").assertDoesNotExist()

        composeTestRule.onNodeWithText("Average").assertExists().assertIsDisplayed()
    }

    @Test
    fun testBottomSheetWatchStatus(){

        val bottomSheetViewModel = BottomSheetViewModel(id = media, persistRepo = FakeRepository())

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