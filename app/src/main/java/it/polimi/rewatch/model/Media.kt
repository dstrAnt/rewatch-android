package it.polimi.rewatch.model

import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbPerson
import app.moviebase.tmdb.model.TmdbPersonCredit
import app.moviebase.tmdb.model.TmdbPersonCredit.Movie
import app.moviebase.tmdb.model.TmdbPersonCredit.Show
import app.moviebase.tmdb.model.TmdbShow
import kotlinx.datetime.LocalDate

// Used to show results of search, person
data class Media(
    val id: Int,
    val posterImage: TmdbImage?,
    val posterPath: String?,
    val releaseDate: LocalDate?,
    val title: String?,
    val type: MediaType,
) {
    // Constructor for Movie Data
    constructor(movie: TmdbMovie) : this(
        id = movie.id,
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = movie.releaseDate,
        title = movie.title,
        type = MediaType.MOVIE,
    )

    constructor(movie: TmdbShow) : this(
        id = movie.id,
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = movie.firstAirDate,
        title = movie.name,
        type = MediaType.TV_SHOW,
    )

    constructor(credit: TmdbPersonCredit) : this(
        // If 'id' and 'posterPath' are shared properties on the base TmdbPersonCredit class,
        // you can call them directly like this:
        id = credit.id,
        posterImage = null,
        posterPath = credit.posterPath,
        // For properties that differ, use inline 'when' statements:
        releaseDate = when (credit) {
            is Movie -> credit.releaseDate
            is Show -> credit.firstAirDate
        },
        title = when (credit) {
            is Movie -> credit.title
            is Show -> credit.name
        },
        type = when (credit) {
            is Movie -> MediaType.MOVIE
            is Show -> MediaType.TV_SHOW
        },
    )

    constructor(movie: TmdbPerson) : this(
        id = movie.id,
        posterImage = movie.profileImage,
        posterPath = movie.profilePath,
        releaseDate = null,
        title = movie.name,
        type = MediaType.TV_SHOW,
    )
}