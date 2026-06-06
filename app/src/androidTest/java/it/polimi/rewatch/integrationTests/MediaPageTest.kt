package it.polimi.rewatch.integrationTests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.media.MediaViewModel
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbCompany
import app.moviebase.tmdb.model.TmdbCompanyId
import app.moviebase.tmdb.model.TmdbCountry
import app.moviebase.tmdb.model.TmdbCrew
import app.moviebase.tmdb.model.TmdbEpisode
import app.moviebase.tmdb.model.TmdbEpisodeDetail
import app.moviebase.tmdb.model.TmdbGenre
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbProvider
import app.moviebase.tmdb.model.TmdbProviders
import app.moviebase.tmdb.model.TmdbResult
import app.moviebase.tmdb.model.TmdbReview
import app.moviebase.tmdb.model.TmdbSeason
import app.moviebase.tmdb.model.TmdbSeasonDetail
import app.moviebase.tmdb.model.TmdbShowDetail
import app.moviebase.tmdb.model.TmdbWatchProviderResult
import it.polimi.rewatch.common.fakeWatch
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.model.EpisodeId
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.model.SeasonId
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import it.polimi.rewatch.screens.media.DetailsTab
import it.polimi.rewatch.screens.media.MediaContent
import it.polimi.rewatch.screens.media.MovieData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.collections.emptyList

class MediaPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val defaultMedia = MovieId(0)
    val fakeRepo = FakeRepository()
    lateinit var mediaViewModel: MediaViewModel

    val defaultActions = NavigationActions(onNavigateToPerson = { id -> },

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

    val defaultOuterPadding = PaddingValues();

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
        val actions = defaultActions

        val outerPadding = defaultOuterPadding
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
        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()
    }

    @Test
    fun testBuildTabsForMoviesWithDetails() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding

        val movieData = MovieData(MediaDetail(TmdbMovieDetail(budget = 1000)), similar = emptyList())

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

        composeTestRule.onNodeWithText("Details").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Watch").assertDoesNotExist()
        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()
    }

    @Test
    fun testBuildTabsForShows() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val seasons = List(2, {index -> TmdbSeason(id = index, airDate = LocalDate.now().toKotlinLocalDate(), name = "The Eyes $index", seasonNumber = index, episodeCount = index)})

        val showsData = MovieData(MediaDetail(TmdbShowDetail(seasons = seasons)), similar = emptyList())

        composeTestRule.setContent {
            MediaContent(
                data = showsData,
                watchlistItem = null,
                watchedListItem = null,
                padding = outerPadding,
                mediaId = TvId(1000),
                actions = actions,
            )
        }

        composeTestRule.onNodeWithText("Cast").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Crew").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Seasons").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()

        composeTestRule.onNodeWithText("Details").assertDoesNotExist()
        composeTestRule.onNodeWithText("Watch").assertDoesNotExist()

        composeTestRule.onNodeWithText("The Eyes 1").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("The Eyes 2").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText(LocalDate.now().toKotlinLocalDate().toString()).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("1 episodes").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("2 episodes").assertExists().assertIsDisplayed()
    }

    @Test
    fun testBuildTabsForTvShowSeasons() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val episodes = List(2, {index -> TmdbEpisode(id = index, airDate = LocalDate.now().toKotlinLocalDate(), name = "Scene $index", episodeNumber = index, runtime = 10 * index, seasonNumber = index)})

        val seasonsData = MovieData(MediaDetail(TmdbSeasonDetail(episodes = episodes)), similar = emptyList())

        composeTestRule.setContent {
            MediaContent(
                data = seasonsData,
                watchlistItem = null,
                watchedListItem = null,
                padding = outerPadding,
                mediaId = SeasonId(1000, 10),
                actions = actions,
            )
        }

        composeTestRule.onNodeWithText("Cast").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Crew").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Episodes").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()

        composeTestRule.onNodeWithText("Details").assertDoesNotExist()
        composeTestRule.onNodeWithText("Watch").assertDoesNotExist()

        composeTestRule.onNodeWithText("Scene 1").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Scene 2").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText(LocalDate.now().toKotlinLocalDate().toString()).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("10 min").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("20 min").assertExists().assertIsDisplayed()
    }

    @Test
    fun testBuildTabsForTvShowEpisode() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val cast = List(2, {index -> TmdbCast(name = "Bob Sullivan", character = "Albert von Wollenstein")})
        val crew = List(2, {index -> TmdbCrew(name = "Alex Wasowski", job = "Producer")})

        val episodeData = MovieData(MediaDetail(TmdbEpisodeDetail(guestStars = cast)), similar = emptyList())

        composeTestRule.setContent {
            MediaContent(
                data = episodeData,
                watchlistItem = null,
                watchedListItem = null,
                padding = outerPadding,
                mediaId = EpisodeId(100, 10, 1),
                actions = actions,
            )
        }

        composeTestRule.onNodeWithText("Cast").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Crew").assertExists().assertIsDisplayed().assertIsToggleable()

        composeTestRule.onNodeWithText("Bob Sullivan").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Albert von Wollenstein").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex Wasowski").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Producer").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Details").assertDoesNotExist()
        composeTestRule.onNodeWithText("Watch").assertDoesNotExist()
    }

    @Test
    fun testOptionalTitledSectionForReviews() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding

        val reviews = TmdbResult<TmdbReview>(
            results = List<TmdbReview>(
                5,
                { index ->
                    TmdbReview(
                        id = index.toString(),
                        author = "Giuseppe B. ($index)",
                        url = "",
                        content = "I don't like this movie ${if (index > 0) "sequel n° $index" else ""}, I'm ${if (index > 0) "still" else ""} disappointed"
                    )
                },
            ),
        )

        val movieData = MovieData(MediaDetail(TmdbMovieDetail(reviews = reviews)), similar = emptyList())

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

        composeTestRule.onNodeWithText("Reviews").assertExists().assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("review_listItem").assertAll(hasClickAction())
        composeTestRule.onNodeWithContentDescription("Avatar for", substring = true).assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Giuseppe B. (2)").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("I don't like this movie, I'm disappointed").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Star rating icon").assertExists().assertIsDisplayed()
    }

    @Test
    fun testOptionalTitledSectionForSimilars() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val similars = List<PosterInfo>(1, {index -> PosterInfo(MovieId(index)) })

        val movieData = MovieData(MediaDetail(TmdbMovieDetail()), similar = similars)

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

        composeTestRule.onNodeWithText("Similar").assertExists().assertIsDisplayed()
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
    fun testDetailsTab() {
        val companies = List<TmdbCompany>(4, { index -> TmdbCompany(name = companyDecider(index), id = index)})

        composeTestRule.setContent {
            DetailsTab(pad = 16.dp,
                MediaDetail(
                    TmdbMovieDetail(
                        genres = List<TmdbGenre>(4, { i -> genreDecider(i + 1)}),
                        budget = 500000,
                        releaseDate = LocalDate.now().toKotlinLocalDate(),
                        productionCountries = List<TmdbCountry>(3, { index -> TmdbCountry(name = "Country n° $index", iso3166 = index.toString())}),
                        productionCompanies = companies
                    )
                )
            )
        }

        composeTestRule.onNodeWithText("Genres:").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Horror").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Comedy").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Drama").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Adventure").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Budget: $ 500 thousand").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Countries:").assertExists().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Country n° ", substring = true).assertCountEquals(3)

        composeTestRule.onNodeWithText("Release: ${LocalDate.now().toKotlinLocalDate().toString().formatDate("d MMMM yyyy")}").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Production:").assertExists().assertIsDisplayed()

        for (company in companies){
            try {
                company.name?.let { composeTestRule.onNodeWithText(it).assertExists().assertIsDisplayed() }
                company.name?.let { composeTestRule.onNodeWithContentDescription(it).assertExists().assertIsDisplayed() }
            }catch (e: AssertionError){
                composeTestRule.onNodeWithText("Unknown Company").assertExists().assertIsDisplayed()
            }

        }
    }

    fun genreDecider(index: Int): TmdbGenre {
        when(index){
            1 -> return TmdbGenre(id = index, name = "Horror")
            2 -> return TmdbGenre(id = index, name = "Comedy")
            3 -> return TmdbGenre(id = index, name = "Drama")
            else -> return TmdbGenre(id = index, name = "Adventure")
        }
    }

    fun companyDecider(index: Int): String?{
        when(index){
            1 -> return "UNIVERSAL"
            2 -> return "NETFLIX_INTERNATIONAL_PICTURES"
            3 -> return "CENTURY_STUDIOS"
            else -> return null
        }
    }

    @Test
    fun testPersonItem() {
        setup(defaultMedia)

        composeTestRule.onAllNodesWithTag("person_listItem").assertAll(hasClickAction())
        composeTestRule.onNodeWithContentDescription("Dettagli attore").assertExists().assertIsDisplayed()
    }

    @Test
    fun testEpisodeItem() {
        setup(EpisodeId(0, 1, 10))

        composeTestRule.onAllNodesWithTag("episode_listItem").assertAll(hasClickAction())
    }

    @Test
    fun testWatchProvidersForBuy() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val watchMap =  HashMap<String, TmdbProviders>(1)
        val buy = List<TmdbProvider>(1, {index -> TmdbProvider(providerId = index)})
        watchMap["US"] = TmdbProviders(buy = buy)

        val movieData = MovieData(MediaDetail(TmdbMovieDetail(watchProviders = TmdbWatchProviderResult(results = watchMap))), similar = emptyList())

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

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()

        composeTestRule.onNodeWithText("Buy").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown Provider").assertExists().assertIsDisplayed()
    }

    @Test
    fun testWatchProvidersForFlatrate() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val watchMap =  HashMap<String, TmdbProviders>(1)
        val flatrate = List<TmdbProvider>(1, {index -> TmdbProvider(providerId = index)})
        watchMap["US"] = TmdbProviders(flatrate = flatrate)

        val movieData = MovieData(MediaDetail(TmdbMovieDetail(watchProviders = TmdbWatchProviderResult(results = watchMap))), similar = emptyList())

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

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed().assertIsToggleable()

        composeTestRule.onNodeWithText("Streaming").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown Provider").assertExists().assertIsDisplayed()
    }

    @Test
    fun testWatchProvidersForBuyAndFlatrate() {
        val actions = defaultActions
        val outerPadding = defaultOuterPadding
        val watchMap =  HashMap<String, TmdbProviders>(1)
        val buy = List<TmdbProvider>(1, {index -> TmdbProvider(providerId = index, providerName = "Mountain Prod.")})
        val flatrate = List<TmdbProvider>(1, {index -> TmdbProvider(providerId = index, providerName = "Another Netflix")})
        watchMap["US"] = TmdbProviders(buy = buy, flatrate = flatrate)

        val movieData = MovieData(MediaDetail(TmdbMovieDetail(watchProviders = TmdbWatchProviderResult(results = watchMap))), similar = emptyList())

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

        composeTestRule.onNodeWithText("Watch").assertExists().assertIsDisplayed().assertIsToggleable()
        composeTestRule.onNodeWithText("Similar").assertDoesNotExist()

        composeTestRule.onNodeWithText("Buy").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Mountain Prod.").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithText("Streaming").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Another Netflix").assertExists().assertIsDisplayed()
    }
}