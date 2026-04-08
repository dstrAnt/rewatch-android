package it.polimi.rewatch.common

import android.util.Log
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.AppendResponse
import app.moviebase.tmdb.model.TmdbDiscover
import app.moviebase.tmdb.model.TmdbDiscoverMovieSortBy
import app.moviebase.tmdb.model.TmdbDiscoverShowSortBy
import app.moviebase.tmdb.model.TmdbDiscoverTimeRange
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbPerson
import app.moviebase.tmdb.model.TmdbShow
import app.moviebase.tmdb.model.TmdbTimeWindow
import it.polimi.rewatch.model.EpisodeId
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.model.SeasonId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.home.MediaData
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.media.MovieData
import it.polimi.rewatch.screens.person.PersonDetailsData
import it.polimi.rewatch.screens.person.PersonPage
import it.polimi.rewatch.screens.search.SearchType
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate

suspend fun <T> fetchMultiplePages(count: Int, block: suspend (Int) -> List<T>): List<T> = coroutineScope {
    (1..count).map { page ->
        async { block(page) }
    }.awaitAll().flatten()
}

/**
 * Repository to retrieve details about the Movies, TV Shows, Seasons, Episodes and People.
 */
@Singleton // Tells Hilt to only make one of these for the whole app
class TmdbRepository @Inject constructor(
    private val tmdb: Tmdb3, // Hilt delivers your API client here!
) {
    /**
     * This function retrieves 2 pages of discoverable movies to be shown in the [HomePage]
     */
    suspend fun getHomeMovieData(): MediaData = withContext(Dispatchers.IO) {
        val threeMonthsAgo = LocalDate.now().minusMonths(3).toString()


        val trendingDeferred = async {
            fetchMultiplePages(2) { page ->
                tmdb.trending.getTrendingMovies(TmdbTimeWindow.DAY, page).results
            }.map(::PosterInfo)
        }

        val topDeferred = async {
            fetchMultiplePages(2) { page ->
                tmdb.movies.getTopRated(page = page).results
            }.map(::PosterInfo)
        }

        val recentDeferred = async {
            val discoverParams = TmdbDiscover.Movie(
                sortBy = TmdbDiscoverMovieSortBy.POPULARITY,
                primaryReleaseDate = TmdbDiscoverTimeRange.Custom(threeMonthsAgo),
                voteCountGte = 50,
            )
            fetchMultiplePages(2) { page ->
                tmdb.discover.discoverMovie(page = page, discover = discoverParams).results
            }.map(::PosterInfo)
        }

        MediaData(
            trending = trendingDeferred.await(),
            popular = topDeferred.await(),
            recent = recentDeferred.await(),
        )
    }

    /**
     * This function retrieves 2 pages of discoverable TV shows to be shown in the [HomePage]
     */
    suspend fun getHomeShowData(): MediaData = withContext(Dispatchers.IO) {
        val threeMonthsAgo = LocalDate.now().minusMonths(3).toString()

        val trendingDeferred = async {
            fetchMultiplePages(2) { page ->
                tmdb.trending.getTrendingShows(TmdbTimeWindow.DAY, page).results
            }.map(::PosterInfo)
        }
        val topDeferred = async {
            fetchMultiplePages(2) { page ->
                tmdb.show.getTopRated(page = page).results
            }.map(::PosterInfo)
        }

        val recentDeferred = async {
            val discoverParams = TmdbDiscover.Show(
                sortBy = TmdbDiscoverShowSortBy.POPULARITY,
                firstAirDate = TmdbDiscoverTimeRange.Custom(threeMonthsAgo),
                voteCountGte = 50,
            )
            fetchMultiplePages(2) { page ->
                tmdb.discover.discoverShow(page = page, discover = discoverParams).results
            }.map(::PosterInfo)
        }

        MediaData(
            trending = trendingDeferred.await(),
            popular = topDeferred.await(),
            recent = recentDeferred.await(),
        )
    }

    /**
     * This function retrieves data to be displayed in the movie instance of [MediaPage]
     */
    suspend fun movieData(movieId: Int): MovieData = withContext(Dispatchers.IO) {
//        tmdb.show.getDetails()
        val details = tmdb.movies.getDetails(
            movieId = movieId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
                AppendResponse.REVIEWS,
                AppendResponse.RELEASES_DATES,
                AppendResponse.WATCH_PROVIDERS,
            ),
        )
        val similar = tmdb.movies.getSimilar(movieId = movieId).results.map(::PosterInfo)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    /**
     * This function retrieves data to be displayed in the TV show instance of [MediaPage]
     */
    suspend fun tvData(showId: Int): MovieData = withContext(Dispatchers.IO) {
//        tmdb.show.getDetails()
        val details = tmdb.show.getDetails(
            showId = showId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
                AppendResponse.REVIEWS,
                AppendResponse.RELEASES_DATES,
                AppendResponse.WATCH_PROVIDERS,
            ),
        )
        val similar = tmdb.show.getSimilar(showId = showId).results.map(::PosterInfo)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    /**
     * This function retrieves data to be displayed in the TV show season instance of [MediaPage]
     */
    suspend fun seasonData(id: SeasonId): MovieData = withContext(Dispatchers.IO) {
        val details = tmdb.showSeasons.getDetails(
            showId = id.parentId,
            seasonNumber = id.seasonId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
            ),
        )
        val similar = tmdb.show.getSimilar(showId = id.parentId).results.map(::PosterInfo)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    /**
     * This function retrieves data to be displayed in the TV show episode instance of [MediaPage]
     */
    suspend fun episodeData(id: EpisodeId): MovieData = withContext(Dispatchers.IO) {
        val details = tmdb.showEpisodes.getDetails(
            showId = id.parentId,
            seasonNumber = id.seasonId,
            episodeNumber = id.episodeId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
            ),
        )
        val similar = tmdb.show.getSimilar(showId = id.parentId).results.map(::PosterInfo)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    /**
     * This function retrieves data to be displayed in [PersonPage]
     */
    suspend fun getPersonData(personId: Int): PersonDetailsData = withContext(Dispatchers.IO) {
        val detailsDeferred = async { tmdb.people.getDetails(personId) }
        val movieCreditsDeferred = async { tmdb.people.getMovieCredits(personId) }
        val showCreditsDeferred = async { tmdb.people.getShowCredits(personId) }

        PersonDetailsData(
            details = detailsDeferred.await(),
            movieCredits = movieCreditsDeferred.await(),
            showCredits = showCreditsDeferred.await(),
        )
    }

    suspend fun movieDetails(id: Int): PosterInfo = tmdb.movies.getDetails(id).let(::PosterInfo)
    suspend fun showDetails(id: Int): PosterInfo = tmdb.show.getDetails(id).let(::PosterInfo)

//    suspend fun getTrackingData(fakeRepo: FakeRepository): TrackingData = coroutineScope {
//        // 1. Recuperiamo i dati "grezzi" (simulando Firebase)
//        // 2. Lanciamo le chiamate API in parallelo per la Watchlist
//        val watchlistDeferred = fakeRepo.watchlistFlow.map { result ->
//            result.mapCatching { list ->
//                coroutineScope {
//                    list.map { item ->
//                        async(Dispatchers.IO) {
//                            val details = item.id.getDetails(this@TmdbRepository)
//                            item.copy(mediaData = details!!)
//                        }
//                    }.awaitAll()
//                }
//            }
//        }
//        // 3. Facciamo lo stesso per i Watched
//        val watchedDeferred = fakeRepo.watchedFlow.map { result ->
//            result.mapCatching { list ->
//                coroutineScope {
//                    list.map { item ->
//                        async(Dispatchers.IO) {
//                            val details = item.id.getDetails(this@TmdbRepository)
//                            item.copy(mediaData = details!!)
//                        }
//                    }.awaitAll()
//                }
//            }
//        }
//        // 4. Attendiamo che tutte le chiamate finiscano e restituiamo il risultato
//        TrackingData(
//            watchlist = watchlistDeferred.awaitAll(),
//            watched = watchedDeferred.awaitAll(),
//        )
//    }
    /**
     * Adds [PosterInfo] information to an empty [WatchlistMedia]
     */
    suspend fun enrichWatchlistItem(item: WatchlistMedia): WatchlistMedia {
        return try {
            val details = item.id.getDetails(this@TmdbRepository)
            item.copy(mediaData = details!!)
        } catch (e: Exception) {
            Log.w("TmdbRepo", "Failed to enrich ${item.id}, using cached data", e)
            item // return un-enriched item instead of crashing
        }
        // Creiamo una COPIA dell'oggetto originale con i nuovi mediaData
    }

    /**
     * Adds [PosterInfo] information to an empty [WatchedMedia]
     */
    suspend fun enrichWatchedItem(item: WatchedMedia) = withContext(Dispatchers.IO) {
        val details = item.id.getDetails(this@TmdbRepository)
        // Creiamo una COPIA dell'oggetto originale con i nuovi mediaData
        item.copy(mediaData = details!!) // copy mantiene gli altri campi
    }

    /**
     * Queries the API with a given query and returns a list containing the resulting content
     * @param query String to search for
     * @param searchType Type of content to search for
     * @return List of [PosterInfo] representing the results of the searching operation; [emptyList] if no results were found
     */
    suspend fun searchMedia(
        query: String,
        searchType: SearchType,
    ): List<PosterInfo> {
        if (query.isBlank()) {
            return emptyList()
        }

        return try {
            when (searchType) {
                SearchType.MOVIES -> {
                    tmdb.search.findMovies(query = query, page = 1)
                        .results.sortedByDescending { it.popularity }
                        .map(::PosterInfo) // Maps TmdbMovie to your Media class
                }

                SearchType.TV -> {
                    // Note: The exact method name depends on your TMDB library wrapper,
                    // it might be findTv(), searchTv(), or findTvShows()
                    tmdb.search.findShows(query = query, page = 1)
                        .results.sortedByDescending { it.popularity }
                        .map(::PosterInfo) // Maps TmdbTvShow to your Media class
                }
//
                SearchType.CAST -> {
                    tmdb.search.findPeople(query = query, page = 1).results.map(::PosterInfo) // Maps TmdbTvShow to your Media class
                }

                SearchType.ALL -> {
                    tmdb.search.findMulti(query = query, page = 1).results.map {
                        when (it) {
                            is TmdbShow -> PosterInfo(it)
                            is TmdbMovie -> PosterInfo(it)
                            is TmdbPerson -> PosterInfo(it)
                            else -> PosterInfo(it)
                        }
                    } // Maps TmdbTvShow to your Media class
                }

//                else -> emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

