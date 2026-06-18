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

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val repository: TmdbRepository = mockk()
    private lateinit var viewModel: SearchViewModel
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
    fun `search should debounce and return results`() = runTest {
        coEvery { repository.searchMedia("Inception", any()) } returns emptyList()

        viewModel = SearchViewModel(repository)

        viewModel.searchResults.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial is UiState.Success)
            assertEquals("", (initial as UiState.Success).data.query)

            viewModel.onQueryChange("Inception")

            // Should emit Loading after debouncing? 
            // Actually SearchViewModel transformLatest emits Loading immediately, then delays.

            val loading = awaitItem()
            assertTrue(loading is UiState.Loading)

            // Wait for debounce delay (200ms)
            advanceTimeBy(201)

            val result = awaitItem()
            assertTrue(result is UiState.Success)
            assertEquals("Inception", (result as UiState.Success).data.query)
        }
    }

    @Test
    fun `search should handle error`() = runTest {
        coEvery { repository.searchMedia(any(), any()) } throws Exception("Search error")

        viewModel = SearchViewModel(repository)

        viewModel.searchResults.test {
            awaitItem() // Initial

            viewModel.onQueryChange("Error")

            assertTrue(awaitItem() is UiState.Loading)

            advanceTimeBy(201)

            val error = awaitItem()
            assertTrue(error is UiState.Error)
            assertEquals("Search error", (error as UiState.Error).msg)
        }
    }
}