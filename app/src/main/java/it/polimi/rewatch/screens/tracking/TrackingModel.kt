package it.polimi.rewatch.screens.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TrackingData(val watchlist: List<WatchlistMedia>, val watched: List<WatchedMedia>)

@HiltViewModel
class TrackingViewModel @Inject constructor(private val tmdbRepo: TmdbRepository, private val persistRepo: PersistRepository) :
    ViewModel() {

    val watchlist = persistRepo.watchlistFlow
        .map { result ->
            result.fold(
                onSuccess = { list ->
                    if (persistRepo.allowEnrichment)
                        UiState.Success(list.map { tmdbRepo.enrichWatchlistItem(it) })
                    else
                        UiState.Success(list)
                },
                onFailure = { error ->
                    UiState.Error(error.message ?: "Unknown error")
                },
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading, // Initial value is Loading, not an empty list
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val watchedList = persistRepo.watchedFlow
        .map { result ->
            result.fold(
                onSuccess = { list ->
                    if (persistRepo.allowEnrichment)
                        UiState.Success(list.map { tmdbRepo.enrichWatchedItem(it) })
                    else
                        UiState.Success(list)
                },
                onFailure = { error ->
                    UiState.Error(error.message ?: "Unknown error")
                },
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading, // Initial value is Loading, not an empty list
        )
}
