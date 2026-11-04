package it.polimi.rewatch.screens.search

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [SearchViewModel].
 * Verifies the search functionality, including query debouncing, reactive state transitions
 * (Loading, Success, Error), and interactions with the repository using Turbine for Flow testing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val repository: TmdbRepository = mockk()
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    /**
     * Sets up the testing environment before each test.
     * Replaces the main coroutine dispatcher with a [StandardTestDispatcher]. This is crucial
     * for testing time-based operations (like debouncing) as it allows virtual time manipulation.
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
     * Verifies the standard search flow, including the debounce mechanism.
     * Ensures that entering a query immediately transitions the Flow to a [UiState.Loading] state,
     * and after the designated debounce delay (200ms), fetches the data and transitions
     * to a [UiState.Success] state with the updated query results.
     */
    @Test
    fun `search should debounce and return results`() = runTest {
        coEvery { repository.searchMedia("Inception", any()) } returns emptyList()

        viewModel = SearchViewModel(repository)

        // Use Turbine to test the StateFlow emissions sequentially
        viewModel.searchResults.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial is UiState.Success)
            assertEquals("", (initial as UiState.Success).data.query)

            viewModel.onQueryChange("Inception")

            // transformLatest emits Loading immediately, then delays

            val loading = awaitItem()
            assertTrue(loading is UiState.Loading)

            // Wait for debounce delay (200ms) to trigger the actual repository fetch
            advanceTimeBy(201)

            val result = awaitItem()
            assertTrue(result is UiState.Success)
            assertEquals("Inception", (result as UiState.Success).data.query)
        }
    }

    /**
     * Verifies the error handling logic during a search operation.
     * Ensures that if the repository throws an exception (e.g., due to a network failure),
     * the ViewModel gracefully catches it after the debounce period and emits a [UiState.Error]
     * containing the appropriate error message.
     */
    @Test
    fun `search should handle error`() = runTest {
        coEvery { repository.searchMedia(any(), any()) } throws Exception("Search error")

        viewModel = SearchViewModel(repository)

        viewModel.searchResults.test {
            awaitItem() // Consume the initial state

            viewModel.onQueryChange("Error")

            assertTrue(awaitItem() is UiState.Loading)

            // Wait for the debounce delay to trigger the failing fetch
            advanceTimeBy(201)

            val error = awaitItem()
            assertTrue(error is UiState.Error)
            assertEquals("Search error", (error as UiState.Error).msg)
        }
    }
}
