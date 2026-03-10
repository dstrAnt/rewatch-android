package it.polimi.rewatch.screens.person

import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.TmdbRepository

data class PersonDetailsData(
    val details: TmdbPersonDetail,
    val credits: TmdbPersonMovieCredits,
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
}
