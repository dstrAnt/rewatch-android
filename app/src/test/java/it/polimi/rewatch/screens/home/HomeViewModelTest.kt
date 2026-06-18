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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository: TmdbRepository = mockk()
    private lateinit var viewModel: HomeViewModel
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
    fun `init should fetch home data successfully`() = runTest(testDispatcher) {
        val movieData = MediaData(emptyList(), emptyList(), emptyList())
        val showData = MediaData(emptyList(), emptyList(), emptyList())

        coEvery { repository.getHomeMovieData() } returns movieData
        coEvery { repository.getHomeShowData() } returns showData

        viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Success state but got $state", state is UiState.Success)
        assertEquals(movieData, (state as UiState.Success).data.movieData)
        assertEquals(showData, state.data.showData)
    }

    @Test
    fun `init should handle error during fetch`() = runTest(testDispatcher) {
        coEvery { repository.getHomeMovieData() } throws RuntimeException("Network error")
        coEvery { repository.getHomeShowData() } returns MediaData(emptyList(), emptyList(), emptyList())

        viewModel = HomeViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState
        assertTrue("Expected Error state but got $state", state is UiState.Error)
        assertEquals("Network error", (state as UiState.Error).msg)
    }
}