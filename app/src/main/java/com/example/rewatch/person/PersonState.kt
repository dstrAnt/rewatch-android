package com.example.rewatch.person

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import com.example.rewatch.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PersonDetailsData(
    val details: TmdbPersonDetail,
    val credits: TmdbPersonMovieCredits,
)

sealed class PersonUiState {
    object Loading : PersonUiState()
    data class Success(val data: PersonDetailsData) : PersonUiState()
    data class Error(val msg: String) : PersonUiState()
}

class PersonViewModel(
    private val personId: Int,
) : ViewModel() {

    private val tmdb = Tmdb3(BuildConfig.TMDB_API_KEY)

    var uiState by mutableStateOf<PersonUiState>(PersonUiState.Loading)
        private set

    init {
        fetchPersonDetails()
    }

    fun fetchPersonDetails() = viewModelScope.launch {
        uiState = PersonUiState.Loading
        uiState = try {
            val data = getPersonDataFromApi(personId)
            PersonUiState.Success(data)
        } catch (e: Exception) {
            PersonUiState.Error(e.localizedMessage ?: "Errore nel caricamento dei dettagli")
        }
    }

    private suspend fun getPersonDataFromApi(id: Int): PersonDetailsData = withContext(Dispatchers.IO) {
        // Scarichiamo i dettagli anagrafici (bio, data di nascita, foto)
        val detailsDeferred = async { tmdb.people.getDetails(id) }

        // Scarichiamo i crediti cinematografici (film in cui ha recitato o lavorato)
        val creditsDeferred = async { tmdb.people.getCombinedCredits(id) }

        PersonDetailsData(
            details = detailsDeferred.await(),
            credits = creditsDeferred.await(),
        )
    }
}