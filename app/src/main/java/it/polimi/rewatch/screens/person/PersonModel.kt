package it.polimi.rewatch.screens.person

import androidx.lifecycle.viewModelScope
import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.screens.media.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

data class PersonDetailsData(
    val details: TmdbPersonDetail,
    val movieCredits: TmdbPersonMovieCredits,
    val showCredits: TmdbPersonShowCredits,
)

@HiltViewModel(assistedFactory = PersonViewModel.Factory::class)
class PersonViewModel @AssistedInject constructor(
    @Assisted("personId") private val personId: Int,
    private val repository: TmdbRepository,
) : BaseViewModel<PersonDetailsData>() {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("personId") personId: Int,
        ): PersonViewModel
    }

    init {
        executeFetch()
    }

    override suspend fun fetchData(): PersonDetailsData = repository.getPersonData(personId)

    // 1. Inputs: Hold the current state of your tab and your data
    private val _currentTab = MutableStateFlow("")
    val currentTab: StateFlow<String> = _currentTab

    // (Replace 'YourDataType' with your actual data class name)
    private val _data = MutableStateFlow<PersonDetailsData?>(null)
    val data: StateFlow<PersonDetailsData?> = _data

    // 2. Output: The processed list exposed to the UI
    val baseList: StateFlow<List<Media>> = combine(_currentTab, _data) { tab, currentData ->
        // Return early if data isn't ready or tab is blank
        if (tab.isBlank() || currentData == null) return@combine emptyList()
        val isActing = tab.equals("Acting", ignoreCase = true)
        val combinedSequence = if (isActing) {
            currentData.movieCredits.cast.asSequence() + currentData.showCredits.cast.asSequence()
        } else {
            (currentData.movieCredits.crew.asSequence() + currentData.showCredits.crew.asSequence())
                .filter { it.department.toString().equals(tab, ignoreCase = true) }
        }
        // Apply transformations sequentially
        combinedSequence
            .map(::Media)
            .distinctBy { it.id to it.type }
            .sortedWith(
                compareBy<Media> { it.stringDate.isNullOrBlank() }
                    .thenByDescending { it.stringDate },
            )
            .toList()
    }
        .flowOn(Dispatchers.Default) // <-- CRITICAL: Does the heavy lifting on a background thread
        .stateIn(
            scope = viewModelScope, // Ties the work to the ViewModel's lifecycle
            started = SharingStarted.WhileSubscribed(5000), // Pauses work if UI is completely hidden
            initialValue = emptyList(), // The default state before calculation finishes
        )

    // 3. Events: Functions for the UI to call when things change
    fun selectTab(newTab: String) {
        _currentTab.value = newTab
    }

    fun updateData(newData: PersonDetailsData) {
        _data.value = newData
    }

}
