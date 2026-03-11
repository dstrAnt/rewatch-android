package it.polimi.rewatch.common

import android.os.Build
import androidx.annotation.RequiresApi
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.AppendResponse
import app.moviebase.tmdb.model.TmdbDiscover
import app.moviebase.tmdb.model.TmdbDiscoverMovieSortBy
import app.moviebase.tmdb.model.TmdbDiscoverTimeRange
import app.moviebase.tmdb.model.TmdbTimeWindow
import it.polimi.rewatch.screens.home.HomeData
import it.polimi.rewatch.screens.media.Media
import it.polimi.rewatch.screens.media.MediaDetail
import it.polimi.rewatch.screens.media.MediaType
import it.polimi.rewatch.screens.media.MovieData
import it.polimi.rewatch.screens.person.PersonDetailsData
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
        val similar = tmdb.movies.getSimilar(movieId = movieId).results.map(::Media)

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
        val similar = tmdb.show.getSimilar(showId = showId).results.map(::Media)

        MovieData(
            MediaDetail(details),
            similar,
        )
    }

    suspend fun getPersonData(personId: Int): PersonDetailsData = withContext(Dispatchers.IO) {
        val detailsDeferred = async { tmdb.people.getDetails(personId) }
        val movieCreditsDeferred = async { tmdb.people.getCombinedCredits(personId) }
        val showCreditsDeferred = async { tmdb.people.getShowCredits(personId) }

        PersonDetailsData(
            details = detailsDeferred.await(),
            movieCredits = movieCreditsDeferred.await(),
            showCredits = showCreditsDeferred.await(),
        )
    }

    suspend fun searchMedia(query: String, mediaType: MediaType): List<Media> {
        if (query.isBlank()) {
            return emptyList()
        }

        return try {
            when (mediaType) {
                MediaType.MOVIE -> {
                    tmdb.search.findMovies(query = query, page = 1)
                        .results.sortedByDescending { it.popularity }
                        .map(::Media) // Maps TmdbMovie to your Media class
                }

                MediaType.TV_SHOW -> {
                    // Note: The exact method name depends on your TMDB library wrapper,
                    // it might be findTv(), searchTv(), or findTvShows()
                    tmdb.search.findShows(query = query, page = 1)
                        .results.sortedByDescending { it.popularity }
                        .map(::Media) // Maps TmdbTvShow to your Media class
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}