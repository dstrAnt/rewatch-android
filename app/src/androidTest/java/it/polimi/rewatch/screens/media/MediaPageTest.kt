package it.polimi.rewatch.screens.media

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbCrew
import app.moviebase.tmdb.model.TmdbProviders
import app.moviebase.tmdb.model.TmdbReview
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaSpecificDetails
import it.polimi.rewatch.model.MovieId
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class MediaPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun renderMediaPageWithCustomData() {
        // 1. Prepare your custom data object
        val mockMovie = MediaDetail(
            title = "Custom Test Movie",
            posterPath = null,
            overview = "This is a custom description for testing purposes. It is long enough to test the expandable text component and see how it behaves on a real device.",
            director = "Test Director",
            certification = "PG-13",
            watchProviders = emptyMap<String, TmdbProviders>(),
            rating = 4.5f,
            reviews = emptyList<TmdbReview>(),
            cast = emptyList<TmdbCast>(),
            crew = emptyList<TmdbCrew>(),
            voteAverage = 8.5f,
            voteCount = 1234,
            runtime = "120",
            tagline = "Testing is fun!",
            releaseDate = LocalDate(2023, 10, 27),
            posterImage = null,
            backdropImage = null,
            specificDetails = MediaSpecificDetails.Movie(
                genres = emptyList(),
                budget = 1000000,
                productionCompanies = emptyList(),
                productionCountries = emptyList(),
                releaseDate = null,
                revenue = 5000000,
                runtime = 120,
            ),
        )
        val mockData = MovieData(movie = mockMovie, similar = emptyList())
        // 2. Render the component
        composeTestRule.setContent {
            MediaContent(
                data = mockData,
                watchlistItem = null,
                watchedListItem = null,
                padding = PaddingValues(0.dp),
                mediaId = MovieId(123),
                actions = NavigationActions(
                    onNavigateToPerson = {},
                    onNavigateToMedia = {},
                    onNavigateBack = {},
                    onFullscreenImage = {},
                    onBottomSheet = {},
                    onNavigateToLogin = {},
                    //onNavigateToProfile = {},
                    //onClearAndNavigateToSettings = {},
                    onBackToHome = {},
                ),
            )
        }
        // Keep the test alive for a while so you can see it on the device
        Thread.sleep(20000)
    }
}
