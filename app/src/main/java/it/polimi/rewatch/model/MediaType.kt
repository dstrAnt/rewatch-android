package it.polimi.rewatch.model

import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.screens.media.MovieData
import kotlinx.serialization.Serializable

enum class MediaType {
    MOVIE,
    TV_SHOW,
    SEASON,
    EPISODE,
}

// Common parent between PersonId and MediaId, used in PosterMedia
interface ItemId {
    fun onClick(actions: NavigationActions)
}

data class PersonId(private val id: Int) : ItemId {
    override fun onClick(actions: NavigationActions) = actions.onNavigateToPerson(id)
}

@Serializable
sealed interface MediaId : ItemId {
    val parentId: Int
    val type: MediaType
    suspend fun getData(repo: TmdbRepository): MovieData
    fun getUid(): String = "${type.name}_$parentId"
    suspend fun getDetails(repo: TmdbRepository): PosterInfo?
}

@Serializable
data class MovieId(override val parentId: Int, override val type: MediaType = MediaType.MOVIE) : MediaId {
    override suspend fun getData(repo: TmdbRepository) = repo.movieData(parentId)
    override suspend fun getDetails(repo: TmdbRepository) = repo.movieDetails(parentId)
    override fun onClick(actions: NavigationActions) = actions.onNavigateToMedia(this)
    override fun toString(): String {
        return parentId.toString()
    }
}

@Serializable
data class TvId(override val parentId: Int, override val type: MediaType = MediaType.TV_SHOW) : MediaId {
    override suspend fun getData(repo: TmdbRepository) = repo.tvData(parentId)
    override suspend fun getDetails(repo: TmdbRepository) = repo.showDetails(parentId)
    override fun onClick(actions: NavigationActions) = actions.onNavigateToMedia(this)
}

@Serializable
data class EpisodeId(override val parentId: Int, val seasonId: Int, val episodeId: Int, override val type: MediaType = MediaType.EPISODE) :
    MediaId {
    override suspend fun getData(repo: TmdbRepository) = repo.episodeData(this)
    override suspend fun getDetails(repo: TmdbRepository) = null
    override fun onClick(actions: NavigationActions) = actions.onNavigateToMedia(this)
}

@Serializable
data class SeasonId(override val parentId: Int, val seasonId: Int, override val type: MediaType = MediaType.SEASON) : MediaId {
    override suspend fun getData(repo: TmdbRepository) = repo.seasonData(this)
    override suspend fun getDetails(repo: TmdbRepository) = null
    override fun onClick(actions: NavigationActions) = actions.onNavigateToMedia(this)
}
