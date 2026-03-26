package it.polimi.rewatch.common

import android.os.Build
import androidx.annotation.RequiresApi
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.AppendResponse
import app.moviebase.tmdb.model.TmdbDiscover
import app.moviebase.tmdb.model.TmdbDiscoverMovieSortBy
import app.moviebase.tmdb.model.TmdbDiscoverTimeRange
import app.moviebase.tmdb.model.TmdbTimeWindow
import it.polimi.rewatch.model.EpisodeId
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.PosterMedia
import it.polimi.rewatch.model.SeasonId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import it.polimi.rewatch.screens.home.HomeData
import it.polimi.rewatch.screens.media.MovieData
import it.polimi.rewatch.screens.person.PersonDetailsData
import it.polimi.rewatch.screens.search.SearchType
import it.polimi.rewatch.screens.tracking.TrackingData
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Singleton // Tells Hilt to only make one of these for the whole app
class TmdbRepository @Inject constructor(
    private val tmdb: Tmdb3, // Hilt delivers your API client here!
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getHomeData(): HomeData = withContext(Dispatchers.IO) {
        val threeMonthsAgo = LocalDate.now().minusMonths(3).toString()
        val trendingDeferred = async { tmdb.trending.getTrendingMovies(TmdbTimeWindow.DAY, 1).results }
        val topDeferred = async { tmdb.movies.getTopRated().results }
        val recentDeferred = async {
            val discoverParams = TmdbDiscover.Movie(
                sortBy = TmdbDiscoverMovieSortBy.POPULARITY,
                releaseDate = TmdbDiscoverTimeRange.Custom(threeMonthsAgo),
                voteCountGte = 50,
            )
            tmdb.discover.discoverMovie(page = 1, discover = discoverParams).results
        }

        HomeData(
            trending = trendingDeferred.await(),
            popular = topDeferred.await(),
            recent = recentDeferred.await(),
        )
    }

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
        val similar = tmdb.movies.getSimilar(movieId = movieId).results.map(::PosterMedia)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

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
        val similar = tmdb.show.getSimilar(showId = showId).results.map(::PosterMedia)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    suspend fun seasonData(id: SeasonId): MovieData = withContext(Dispatchers.IO) {
        val details = tmdb.showSeasons.getDetails(
            showId = id.parentId,
            seasonNumber = id.seasonId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
            ),
        )
        val similar = tmdb.show.getSimilar(showId = id.parentId).results.map(::PosterMedia)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    suspend fun episodeData(id: EpisodeId): MovieData = withContext(Dispatchers.IO) {
        val details = tmdb.showEpisodes.getDetails(
            showId = id.parentId,
            seasonNumber = id.seasonId,
            episodeNumber = id.episodeId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
            ),
        )
        val similar = tmdb.show.getSimilar(showId = id.parentId).results.map(::PosterMedia)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

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

    suspend fun movieDetails(id: Int): PosterMedia = tmdb.movies.getDetails(id).let(::PosterMedia)
    suspend fun showDetails(id: Int): PosterMedia = tmdb.show.getDetails(id).let(::PosterMedia)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTrackingData(fakeRepo: FakeRepository): TrackingData = coroutineScope {
        // 1. Recuperiamo i dati "grezzi" (simulando Firebase)
        val rawWatchlist = fakeRepo.watchlist.value
        val rawWatched = fakeRepo.watchedList.value
        // 2. Lanciamo le chiamate API in parallelo per la Watchlist
        val watchlistDeferred = rawWatchlist.map { item ->
            async(Dispatchers.IO) {
                val details = item.id.getDetails(this@TmdbRepository)
                // Creiamo una COPIA dell'oggetto originale con i nuovi mediaData
                item.copy(mediaData = details!!) // copy mantiene gli altri campi
            }
        }
        // 3. Facciamo lo stesso per i Watched
        val watchedDeferred = rawWatched.map { item ->
            async(Dispatchers.IO) {
                val details = item.id.getDetails(this@TmdbRepository)
                item.copy(mediaData = details!!) // copy mantiene gli altri campi
            }
        }
        // 4. Attendiamo che tutte le chiamate finiscano e restituiamo il risultato
        TrackingData(
            watchlist = watchlistDeferred.awaitAll(),
            watched = watchedDeferred.awaitAll(),
        )
    }

    suspend fun enrichWatched(item: WatchlistMedia) = withContext(Dispatchers.IO) {
        val details = item.id.getDetails(this@TmdbRepository)
        // Creiamo una COPIA dell'oggetto originale con i nuovi mediaData
        item.copy(mediaData = details!!) // copy mantiene gli altri campi
    }

    suspend fun enrichWatched(item: WatchedMedia) = withContext(Dispatchers.IO) {
        val details = item.id.getDetails(this@TmdbRepository)
        // Creiamo una COPIA dell'oggetto originale con i nuovi mediaData
        item.copy(mediaData = details!!) // copy mantiene gli altri campi
    }

    suspend fun searchMedia(
        query: String,
        searchType: SearchType,
    ): List<PosterMedia> {
        if (query.isBlank()) {
            return emptyList()
        }

        return try {
            when (searchType) {
                SearchType.MOVIES -> {
                    tmdb.search.findMovies(query = query, page = 1)
                        .results.sortedByDescending { it.popularity }
                        .map(::PosterMedia) // Maps TmdbMovie to your Media class
                }

                SearchType.TV -> {
                    // Note: The exact method name depends on your TMDB library wrapper,
                    // it might be findTv(), searchTv(), or findTvShows()
                    tmdb.search.findShows(query = query, page = 1)
                        .results.sortedByDescending { it.popularity }
                        .map(::PosterMedia) // Maps TmdbTvShow to your Media class
                }
//
                SearchType.CAST -> {
                    tmdb.search.findPeople(query = query, page = 1).results.map(::PosterMedia) // Maps TmdbTvShow to your Media class
                }

                else -> emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}