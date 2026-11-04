package it.polimi.rewatch.screens.person

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiFetchViewModel
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.tracking.sortedByDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PersonDetailsData(
    val details: TmdbPersonDetail,
    val movieCredits: TmdbPersonMovieCredits,
    val showCredits: TmdbPersonShowCredits,
)

enum class PersonSortOrder(val label: String) {
    POPULARITY("Popularity"),
    DATE("Date"),
}

@HiltViewModel(assistedFactory = PersonViewModel.Factory::class)
class PersonViewModel @AssistedInject constructor(
    @Assisted("personId") private val personId: Int,
    private val repository: TmdbRepository,
) : UiFetchViewModel<PersonDetailsData>() {
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("personId") personId: Int): PersonViewModel
    }

    private val _currentTab = MutableStateFlow<String?>(null)
    val currentTab: StateFlow<String?> = _currentTab
    private val _sortOrder = MutableStateFlow(PersonSortOrder.POPULARITY)
    val sortOrder: StateFlow<PersonSortOrder> = _sortOrder
    private val _isAscending = MutableStateFlow(false)
    val isAscending: StateFlow<Boolean> = _isAscending
    private val uiStateFlow: StateFlow<UiState<PersonDetailsData>> = snapshotFlow { uiState }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)
    val availableTabs: StateFlow<List<String>> = uiStateFlow
        .map { state ->
            if (state is UiState.Success) computeAvailableTabs(state.data) else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val displayList: StateFlow<List<PosterInfo>> = combine(
        uiStateFlow,
        currentTab,
        sortOrder,
        isAscending,
    ) { state, tab, sort, ascending ->
        if (state !is UiState.Success || tab == null) return@combine emptyList()
        val data = state.data
        val knownFor = data.details.knownForDepartment?.toString()?.replace("_", " ") ?: "Acting"
        val isActingPrimary = knownFor.equals("Acting", ignoreCase = true)
        val baseList = when (tab) {
            "Movies" -> {
                if (isActingPrimary) data.movieCredits.cast.asSequence()
                else data.movieCredits.crew.asSequence().filter { it.department?.toString().equals(knownFor, ignoreCase = true) }
            }

            "Tv Shows" -> {
                if (isActingPrimary) data.showCredits.cast.asSequence()
                else data.showCredits.crew.asSequence().filter { it.department?.toString().equals(knownFor, ignoreCase = true) }
            }

            "Acting" -> {
                data.movieCredits.cast.asSequence() + data.showCredits.cast.asSequence()
            }

            else -> {
                (data.movieCredits.crew.asSequence() + data.showCredits.crew.asSequence())
                    .filter { it.department?.toString().equals(tab, ignoreCase = true) }
            }
        }.map { PosterInfo(it) }.distinctBy { it.id }.toList()

        when (sort) {
            PersonSortOrder.DATE -> baseList.sortedByDirection(ascending) { it.releaseDate }
            PersonSortOrder.POPULARITY -> baseList.sortedByDirection(ascending) { it.popularity }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        executeFetch()
    }

    override suspend fun fetchData(): PersonDetailsData = repository.getPersonData(personId)

    fun onTabSelected(tab: String) {
        _currentTab.value = tab
    }

    fun onSortSelected(order: PersonSortOrder) {
        if (_sortOrder.value == order) {
            _isAscending.value = !_isAscending.value
        } else {
            _sortOrder.value = order
            _isAscending.value = false
        }
    }

    private fun computeAvailableTabs(data: PersonDetailsData): List<String> {
        val knownFor = data.details.knownForDepartment?.toString()?.replace("_", " ") ?: "Acting"
        val isActingPrimary = knownFor.equals("Acting", ignoreCase = true)
        val tabs = buildSet {
            val hasPrimaryMovies = if (isActingPrimary) {
                data.movieCredits.cast.isNotEmpty()
            } else {
                data.movieCredits.crew.any { it.department?.toString().equals(knownFor, ignoreCase = true) }
            }
            if (hasPrimaryMovies) add("Movies")
            val hasPrimaryShows = if (isActingPrimary) {
                data.showCredits.cast.isNotEmpty()
            } else {
                data.showCredits.crew.any { it.department?.toString().equals(knownFor, ignoreCase = true) }
            }
            if (hasPrimaryShows) add("Tv Shows")

            if (!isActingPrimary && (data.movieCredits.cast.isNotEmpty() || data.showCredits.cast.isNotEmpty())) {
                add("Acting")
            }
            sequenceOf(data.movieCredits.crew, data.showCredits.crew)
                .flatten()
                .filter { !it.department?.toString().equals(knownFor, ignoreCase = true) }
                .mapTo(this) {
                    it.department?.toString()?.replace("_", " ")
                        ?.split(" ")?.joinToString(" ") { word ->
                            word.lowercase().replaceFirstChar { it.uppercase() }
                        } ?: ""
                }
        }.filter { it.isNotEmpty() }.sortedWith(
            compareBy<String> { tabName ->
                when (tabName) {
                    "Movies" -> 0
                    "Tv Shows" -> 1
                    "Acting" -> 2
                    else -> 3
                }
            }.thenBy { it },
        )
        if (_currentTab.value == null && tabs.isNotEmpty()) {
            _currentTab.value = tabs.first()
        }
        return tabs
    }
}
