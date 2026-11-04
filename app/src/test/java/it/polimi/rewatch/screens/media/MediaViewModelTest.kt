package it.polimi.rewatch.screens.media

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.model.MovieId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
 * Test suite for [MediaViewModel].
 * Verifies the initialization and data fetching logic for individual media items (e.g., Movies or TV Shows),
 * ensuring that the UI state correctly transitions based on repository responses and user lists (watched/watchlist).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest {

    private val repository: TmdbRepository = mockk()
    private val persistRepo: PersistRepository = mockk()
    private lateinit var viewModel: MediaViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val watchlistFlow = MutableSharedFlow<Result<List<it.polimi.rewatch.model.WatchlistMedia>>>()
    private val watchedFlow = MutableSharedFlow<Result<List<it.polimi.rewatch.model.WatchedMedia>>>()

    /**
     * Sets up the testing environment before each test.
     * Replaces the main coroutine dispatcher with a [StandardTestDispatcher] for predictable execution
     * and mocks the default persistence flows (watchlist and watched media) required by the ViewModel.
     */
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { persistRepo.watchlistFlow } returns watchlistFlow
        every { persistRepo.watchedFlow } returns watchedFlow
    }

    /**
     * Cleans up the testing environment after each test by resetting the main coroutine dispatcher.
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifies the successful data fetching flow for a Movie.
     * Ensures that when the ViewModel is instantiated with a [MovieId], it correctly delegates
     * the fetch operation to the [TmdbRepository] and updates its internal state to [UiState.Success]
     * containing the expected [MovieData].
     */
    @Test
    fun `should fetch movie data successfully`() = runTest(testDispatcher) {
        val mediaId = MovieId(123)
        val movieData = MovieData(mockk(relaxed = true), emptyList())

        // In MediaViewModel, fetchData calls mediaId.getData(repository)
        // MovieId.getData calls repository.movieData(parentId)
        coEvery { repository.movieData(any()) } returns movieData

        viewModel = MediaViewModel(mediaId, repository, persistRepo)

        // Advance coroutine execution until all pending asynchronous tasks (like data fetching) complete
        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Success state but got $state", state is UiState.Success)
        assertEquals(movieData, (state as UiState.Success).data)
    }
}
