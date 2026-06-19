package it.polimi.rewatch.screens.home

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [HomeViewModel].
 * Verifies the initialization logic, specifically how it fetches home screen data
 * (movies and TV shows) concurrently and updates the UI state, handling both
 * success and error scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository: TmdbRepository = mockk()
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    /**
     * Sets up the coroutine testing environment by replacing the main dispatcher
     * with a [StandardTestDispatcher]. This allows for predictable control over
     * coroutine execution during tests.
     */
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Cleans up the coroutine testing environment by resetting the main dispatcher
     * back to its original state after each test.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifies the successful data fetching flow during ViewModel initialization.
     * Ensures that when the repository successfully returns both movie and TV show data,
     * the ViewModel correctly aggregates them and transitions to a [UiState.Success] state.
     */
    @Test
    fun `init should fetch home data successfully`() = runTest(testDispatcher) {
        val movieData = MediaData(emptyList(), emptyList(), emptyList())
        val showData = MediaData(emptyList(), emptyList(), emptyList())

        // Mock successful repository responses
        coEvery { repository.getHomeMovieData() } returns movieData
        coEvery { repository.getHomeShowData() } returns showData

        viewModel = HomeViewModel(repository)

        // Advance coroutine execution until all pending tasks (the init block fetch) complete
        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Success state but got $state", state is UiState.Success)
        assertEquals(movieData, (state as UiState.Success).data.movieData)
        assertEquals(showData, state.data.showData)
    }

    /**
     * Verifies the error handling flow during ViewModel initialization.
     * Ensures that if the repository throws an exception (e.g., a network error) while fetching data,
     * the ViewModel catches it and transitions to a [UiState.Error] state with the correct message.
     */
    @Test
    fun `init should handle error during fetch`() = runTest(testDispatcher) {
        // Mock a failure for the movie data fetch
        coEvery { repository.getHomeMovieData() } throws RuntimeException("Network error")
        coEvery { repository.getHomeShowData() } returns MediaData(emptyList(), emptyList(), emptyList())

        viewModel = HomeViewModel(repository)

        // Advance coroutine execution until the error is caught
        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Error state but got $state", state is UiState.Error)
        assertEquals("Network error", (state as UiState.Error).msg)
    }
}