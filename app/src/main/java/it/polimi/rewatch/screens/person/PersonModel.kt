package it.polimi.rewatch.screens.person

import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiFetchViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PersonDetailsData(
    val details: TmdbPersonDetail,
    val movieCredits: TmdbPersonMovieCredits,
    val showCredits: TmdbPersonShowCredits,
)

@HiltViewModel(assistedFactory = PersonViewModel.Factory::class)
class PersonViewModel @AssistedInject constructor(
    @Assisted("personId") private val personId: Int,
    private val repository: TmdbRepository,
) : UiFetchViewModel<PersonDetailsData>() {
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



}
