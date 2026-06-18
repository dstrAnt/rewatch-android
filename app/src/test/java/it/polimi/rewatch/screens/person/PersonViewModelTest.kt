package it.polimi.rewatch.screens.person

import app.moviebase.tmdb.model.TmdbPersonDetail
import app.moviebase.tmdb.model.TmdbPersonMovieCredits
import app.moviebase.tmdb.model.TmdbPersonShowCredits
import io.mockk.coEvery
import io.mockk.mockk
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonViewModelTest {

    private val repository: TmdbRepository = mockk()
    private lateinit var viewModel: PersonViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should fetch person data`() = runTest(testDispatcher) {
        val details = mockk<TmdbPersonDetail>(relaxed = true)
        val movieCredits = TmdbPersonMovieCredits(cast = emptyList(), crew = emptyList())
        val showCredits = TmdbPersonShowCredits(cast = emptyList(), crew = emptyList())

        coEvery { repository.getPersonData(any()) } returns PersonDetailsData(details, movieCredits, showCredits)

        viewModel = PersonViewModel(1, repository)

        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Success state but got $state", state is UiState.Success)
    }
}