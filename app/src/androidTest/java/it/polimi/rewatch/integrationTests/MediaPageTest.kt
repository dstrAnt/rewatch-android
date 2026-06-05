package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.media.MediaViewModel
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbMovieDetail
import it.polimi.rewatch.common.fakeWatch
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import it.polimi.rewatch.screens.media.MediaContent
import it.polimi.rewatch.screens.media.MovieData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.collections.emptyList

class MediaPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val defaultMedia = MovieId(0)
    val fakeRepo = FakeRepository()
    lateinit var mediaViewModel: MediaViewModel

    //@Before
    fun setup(media: MediaId) {

        mediaViewModel = MediaViewModel(mediaId = media, persistRepo = fakeRepo, repository = TmdbRepository(Tmdb3{}))

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
            MediaPage(media, actions = actions, outerPadding = outerPadding, viewModel = mediaViewModel)
        }
    }

    @Test
    fun testMediaHeaderItems(){
        setup(defaultMedia)

        composeTestRule.onNodeWithTag("header_backdrop").assertExists().assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag("header_title").assertExists().assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag("header_tagline").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_ratings").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_tracking").assertExists().assertIsDisplayed()
            .onChild().assertExists().assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun testTrackingInfoWithoutTracking() {

        setup(defaultMedia)

        val watchedMedia = mediaViewModel.watchedListItem.value
        val watchlistMedia = mediaViewModel.watchlistItem.value

        assert(watchedMedia == null)
        assert(watchlistMedia == null)

        composeTestRule.onNodeWithText("Add to your lists").assertExists().assertIsDisplayed()
    }

    @Test
    fun testTrackingInfoWithOnlyWatched() {

        setup(MovieId(83541))

        val watchedMedia = mediaViewModel.watchedListItem.value
        val watchlistMedia = mediaViewModel.watchlistItem.value

        assert(watchedMedia != null)
        assert(watchlistMedia == null)

        composeTestRule.onNodeWithText("You watched this").assertExists().assertIsDisplayed()
    }

    @Test
    fun testTrackingInfoWithOnlyWatchlist() {

        setup(MovieId(550))

        val watchedMedia = mediaViewModel.watchedListItem.value
        val watchlistMedia = mediaViewModel.watchlistItem.value

        assert(watchedMedia == null)
        assert(watchlistMedia != null)

        composeTestRule.onNodeWithText("In your watchlist").assertExists().assertIsDisplayed()
    }

    @Test
    fun testTrackingInfoWithWatchedAndWatchlist() {

        setup(TvId(1394))

        val watchedMedia = mediaViewModel.watchedListItem.value
        val watchlistMedia = mediaViewModel.watchlistItem.value

        assert(watchedMedia != null)
        assert(watchlistMedia != null)

        composeTestRule.onNodeWithText("You watched this").assertExists().assertIsDisplayed()
    }

    @Test
    fun testTrackingInfoWithWatchedAndWatchlistRated() {

        setup(TvId(1390))

        val watchedMedia = mediaViewModel.watchedListItem.value
        val watchlistMedia = mediaViewModel.watchlistItem.value

        assert(watchedMedia != null)
        assert(watchlistMedia != null)
        watchedMedia?.rating?.let { assert(it > 0f) }

        composeTestRule.onNodeWithText("Your rating").assertExists().assertIsDisplayed()
    }

    @Test
    fun testBuildTabsForMovies() {
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
        val movieData = MovieData(MediaDetail(TmdbMovieDetail()), similar = emptyList())

        composeTestRule.setContent {
            MediaContent(
                data = movieData,
                watchlistItem = null,
                watchedListItem = null,
                padding = outerPadding,
                mediaId = defaultMedia,
                actions = actions,
            )
        }

        composeTestRule.onNodeWithText("Cast").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Crew").assertExists().assertIsDisplayed().assertIsToggleable()

        composeTestRule.onNodeWithText("Details").assertDoesNotExist()
        composeTestRule.onNodeWithText("Watch").assertDoesNotExist()
    }

    @Test
    fun testBuildTabsForShows() {

    }

    @Test
    fun testBuildTabsForTvShowSeasons() {

    }

    @Test
    fun testOptionalTitledSection() {

    }

    @Test
    fun testTabbedSection() {

    }

    @Test
    fun testTaglineAndOverview() {

    }

    @Test
    fun testTmdbRatings() {

    }

    @Test
    fun testBackdropImage() {

    }

    @Test
    fun testTitleAndInfoSection() {

    }

    @Test
    fun testWatchTab() {

    }

    @Test
    fun testDetailsTab() {

    }

    @Test
    fun testDetailRow() {

    }

    @Test
    fun testCompanyItem() {

    }

    @Test
    fun testSimilarSection() {

    }

    @Test
    fun testPersonItem() {

    }

    @Test
    fun testReviewItem() {

    }

    @Test
    fun testWatchProviders() {

    }

    @Test
    fun testEpisodeItem() {

    }
}