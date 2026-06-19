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

/**
 * Test suite for [PersonViewModel].
 * Verifies the initialization and data fetching logic for individuals (such as actors or crew members),
 * ensuring that the UI state transitions correctly based on the repository responses.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PersonViewModelTest {

    private val repository: TmdbRepository = mockk()
    private lateinit var viewModel: PersonViewModel
    private val testDispatcher = StandardTestDispatcher()

    /**
     * Sets up the testing environment before each test.
     * Replaces the main coroutine dispatcher with a [StandardTestDispatcher] to allow
     * for predictable and controllable execution of asynchronous operations.
     */
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Cleans up the testing environment after each test by resetting the main
     * coroutine dispatcher back to its default state.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifies the successful data fetching flow for a specific person.
     * Ensures that when the ViewModel is instantiated with a valid person ID, it requests
     * the combined data (details, movie credits, and show credits) from the [TmdbRepository],
     * and updates its internal UI state to [UiState.Success].
     */
    @Test
    fun `should fetch person data`() = runTest(testDispatcher) {
        val details = mockk<TmdbPersonDetail>(relaxed = true)
        val movieCredits = TmdbPersonMovieCredits(cast = emptyList(), crew = emptyList())
        val showCredits = TmdbPersonShowCredits(cast = emptyList(), crew = emptyList())

        // Mock the repository response for fetching a person's complete profile and filmography
        coEvery { repository.getPersonData(any()) } returns PersonDetailsData(details, movieCredits, showCredits)

        viewModel = PersonViewModel(1, repository)

        // Advance coroutine execution until all pending asynchronous tasks complete
        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Success state but got $state", state is UiState.Success)
    }
}