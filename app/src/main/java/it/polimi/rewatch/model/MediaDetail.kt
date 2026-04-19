package it.polimi.rewatch.model

import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbCompany
import app.moviebase.tmdb.model.TmdbCountry
import app.moviebase.tmdb.model.TmdbCrew
import app.moviebase.tmdb.model.TmdbEpisode
import app.moviebase.tmdb.model.TmdbEpisodeDetail
import app.moviebase.tmdb.model.TmdbExternalIds
import app.moviebase.tmdb.model.TmdbGenre
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbProviders
import app.moviebase.tmdb.model.TmdbReview
import app.moviebase.tmdb.model.TmdbSeason
import app.moviebase.tmdb.model.TmdbSeasonDetail
import app.moviebase.tmdb.model.TmdbShowDetail
import app.moviebase.tmdb.model.TmdbShowStatus
import app.moviebase.tmdb.model.TmdbVideo
import app.moviebase.tmdb.model.TmdbVideoSite
import app.moviebase.tmdb.model.TmdbVideoType
import kotlinx.datetime.LocalDate

// Data object for MediaPage data of Movie, Tv, Season, Episode
data class MediaDetail(
    val title: String?,
    val posterPath: String?,
    val overview: String?,
    val director: String?,
    val certification: String?,
    val watchProviders: Map<String, TmdbProviders>,
    val rating: Float?,
    val reviews: List<TmdbReview>, // Consider replacing 'Any' with a specific Review type
    val cast: List<TmdbCast>,
    val crew: List<TmdbCrew>,
    val voteAverage: Float?,
    val voteCount: Int?,
    val runtime: String,
    val externalIds: Map<String, String> = emptyMap(),
    val tagline: String?,
    val releaseDate: LocalDate?,
    val posterImage: TmdbImage?,
    val backdropImage: TmdbImage?,
    val specificDetails: MediaSpecificDetails,
) {
    val isDetailsAvailable: Boolean
        get() {
            if (specificDetails !is MediaSpecificDetails.Movie) return false
            return specificDetails.genres.isNotEmpty() ||
                    specificDetails.budget > 0 ||
                    specificDetails.releaseDate != null ||
                    !specificDetails.productionCompanies.isNullOrEmpty()
        }

    // Constructor for Movie Data
    constructor(movie: TmdbMovieDetail) : this(
        title = movie.title,
        posterPath = movie.posterPath,
        overview = movie.overview,
        director = movie.credits?.crew?.find { it.job == "Director" }?.name,
        certification = movie.releaseDates?.results?.find { it.iso3166 == "US" }?.releaseDates?.find { it.certification != "" }?.certification,
        watchProviders = movie.watchProviders?.results.orEmpty(),
        rating = movie.voteAverage,
        reviews = movie.reviews?.results.orEmpty(),
        cast = movie.credits?.cast.orEmpty(),
        crew = movie.credits?.crew.orEmpty(),
        voteAverage = movie.voteAverage,
        voteCount = movie.voteCount,
        runtime = movie.runtime.toString(),
        externalIds = toExternalLinks(movie.externalIds, movie.videos?.results),
        tagline = movie.tagline,
        releaseDate = movie.releaseDate, // Assumes movie.releaseDate is LocalDate
        posterImage = movie.posterImage,
        backdropImage = movie.backdropImage,
        specificDetails = MediaSpecificDetails.Movie(
            budget = movie.budget,
            revenue = movie.revenue,
            runtime = movie.runtime,
            productionCompanies = movie.productionCompanies,
            productionCountries = movie.productionCountries,
            releaseDate = movie.releaseDate,
            genres = movie.genres,
        ),
    )

    // Constructor for Show Data
    constructor(show: TmdbShowDetail) : this(
        title = show.name, // TMDB uses 'name' for TV shows
        posterPath = show.posterPath,
        overview = show.overview,
        rating = show.voteAverage,
        director = show.credits?.crew?.find { it.job == "Director" }?.name,
        certification = null,
        watchProviders = show.watchProviders?.results.orEmpty(),
        runtime = show.status?.name.orEmpty(),
        cast = show.credits?.cast.orEmpty(),
        crew = show.credits?.crew.orEmpty(),
        reviews = show.reviews?.results.orEmpty(),
        voteAverage = show.voteAverage,
        voteCount = show.voteCount,
        externalIds = toExternalLinks(show.externalIds, show.videos?.results),
        tagline = show.tagline,
        releaseDate = show.firstAirDate, // TMDB uses 'firstAirDate' for TV shows
        posterImage = show.posterImage,
        backdropImage = show.backdropImage,
        specificDetails = MediaSpecificDetails.Show(
            seasons = show.seasons,
            status = show.status, // Shows often provide a list of runtimes
        ),
    )

    constructor(season: TmdbSeasonDetail) : this(
        title = season.name, // TMDB uses 'name' for TV shows
        posterPath = season.posterPath,
        overview = season.overview,
        rating = season.voteAverage,
        director = season.credits?.crew?.find { it.job == "Director" }?.name,
        certification = null,
        watchProviders = emptyMap(),
        runtime = "",
        cast = season.credits?.cast.orEmpty(),
        crew = season.credits?.crew.orEmpty(),
        reviews = emptyList(),
        voteAverage = season.voteAverage,
        voteCount = season.voteCount,
        externalIds = toExternalLinks(season.externalIds, season.videos?.results),
        tagline = null,
        releaseDate = season.airDate, // TMDB uses 'firstAirDate' for TV shows
        posterImage = season.posterImage,
        backdropImage = null,
        specificDetails = MediaSpecificDetails.Season(
            episodes = season.episodes,
        ),
    )

    constructor(episode: TmdbEpisodeDetail) : this(
        title = episode.name.orEmpty(), // TMDB uses 'name' for TV shows
        posterPath = null,
        overview = episode.overview.orEmpty(),
        rating = episode.voteAverage,
        director = episode.crew?.find { it.job == "Director" }?.name.orEmpty(),
        certification = null,
        watchProviders = emptyMap(),
        runtime = episode.runtime.toString(),
        cast = episode.guestStars.orEmpty(),
        crew = episode.crew.orEmpty(),
        reviews = emptyList(),
        voteAverage = episode.voteAverage,
        voteCount = episode.voteCount,
        externalIds = toExternalLinks(episode.externalIds, null),
        tagline = null,
        releaseDate = episode.airDate, // TMDB uses 'firstAirDate' for TV shows
        posterImage = null,
        backdropImage = episode.backdropImage, // se non funziona, prova stillPath
        specificDetails = MediaSpecificDetails.Episode(
            season = episode.seasonNumber,
            episodeNumber = episode.episodeNumber,
        ),
    )
}

// Sealed interface for the parts that differ
sealed interface MediaSpecificDetails {
    data class Movie(
        val budget: Long, val revenue: Long,
        val runtime: Int?,
        val productionCompanies: List<TmdbCompany>?,
        val productionCountries: List<TmdbCountry>?,
        val releaseDate: LocalDate?,
        val genres: List<TmdbGenre>,
    ) : MediaSpecificDetails

    data class Show(val status: TmdbShowStatus?, val seasons: List<TmdbSeason>) : MediaSpecificDetails

    data class Episode(
        val season: Int,
        val episodeNumber: Int,
    ) : MediaSpecificDetails

    data class Season(
        val episodes: List<TmdbEpisode>?,
    ) : MediaSpecificDetails
}

fun toExternalLinks(
    externalIds: TmdbExternalIds?,
    videos: List<TmdbVideo>?,
): Map<String, String> {
    return buildMap {
        externalIds?.imdbId?.let { put("IMDb", "https://www.imdb.com/title/$it/") }

        videos?.find { it.site == TmdbVideoSite.YOUTUBE && it.type == TmdbVideoType.TRAILER }?.key?.let {
            put("Trailer", "https://www.youtube.com/watch?v=$it")
        }
    }
}
