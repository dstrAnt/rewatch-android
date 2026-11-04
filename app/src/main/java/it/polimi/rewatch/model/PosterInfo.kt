package it.polimi.rewatch.model

import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.model.TmdbKeywordDetail
import app.moviebase.tmdb.model.TmdbMediaListItem
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbPerson
import app.moviebase.tmdb.model.TmdbPersonCredit
import app.moviebase.tmdb.model.TmdbPersonCredit.Movie
import app.moviebase.tmdb.model.TmdbPersonCredit.Show
import app.moviebase.tmdb.model.TmdbSearchableListItem
import app.moviebase.tmdb.model.TmdbShow
import app.moviebase.tmdb.model.TmdbShowDetail
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

// Used to show results of search, movie, tv, person
@Serializable
data class PosterInfo(
    val id: ItemId,
    @Contextual val posterImage: TmdbImage?,
    val posterPath: String?,
    val releaseDate: LocalDate?,
    val title: String?,
    val popularity: Float,
    val rating: Float,
) {
    constructor(movie: TmdbSearchableListItem) : this(
        id = MovieId(movie.id),
        posterImage = null,
        posterPath = (movie as TmdbKeywordDetail).posterPath,
        releaseDate = null,
        title = movie.name,
        popularity = 0f,
        rating = 0f,
    )

    // Constructor for Movie Data
    constructor(movie: TmdbMediaListItem, releaseDate: LocalDate?, title: String?) : this(
        id = MovieId(movie.id),
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = releaseDate,
        title = title,
        popularity = movie.popularity,
        rating = movie.voteAverage ?: 0.0f,
    )

    constructor(movie: TmdbMovie) : this(
        movie,
        releaseDate = movie.releaseDate,
        title = movie.title,
    )

    constructor(movie: TmdbShow) : this(
        id = TvId(movie.id),
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = movie.firstAirDate,
        title = movie.name,
        popularity = movie.popularity,
        rating = movie.voteAverage,
    )

    constructor(credit: TmdbPersonCredit) : this(
        id = when (credit) {
            is Movie -> MovieId(credit.id)
            is Show -> TvId(credit.id)
        },
        posterImage = null,
        posterPath = credit.posterPath,
        releaseDate = when (credit) {
            is Movie -> credit.releaseDate
            is Show -> credit.firstAirDate
        },
        title = when (credit) {
            is Movie -> credit.title
            is Show -> credit.name
        },
        popularity = credit.popularity ?: 0f,
        rating = credit.voteAverage ?: 0f,
    )

    constructor(person: TmdbPerson) : this(
        id = PersonId(person.id),
        posterImage = person.profileImage,
        posterPath = person.profilePath,
        releaseDate = null,
        title = person.name,
        popularity = person.popularity,
        rating = 0.0f,
    )

    constructor(movie: TmdbMovieDetail) : this(
        id = MovieId(movie.id),
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = movie.releaseDate,
        title = movie.title,
        popularity = movie.popularity,
        rating = movie.voteAverage,
    )

    constructor(movie: TmdbShowDetail) : this(
        id = TvId(movie.id),
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = movie.firstAirDate,
        title = movie.name,
        popularity = movie.popularity,
        rating = movie.voteAverage ?: 0f,
    )

    constructor(movie: MediaDetail, id: MediaId) : this(
        id = id,
        posterImage = movie.posterImage,
        posterPath = movie.posterPath,
        releaseDate = movie.releaseDate,
        title = movie.title,
        popularity = 0f,
        rating = movie.voteAverage ?: 0f,
    )

    // empty media
    constructor(id: MediaId) : this(
        id = id,
        posterImage = null,
        posterPath = "",
        releaseDate = null,
        title = "",
        popularity = 0f,
        rating = 0f,
    )
}
