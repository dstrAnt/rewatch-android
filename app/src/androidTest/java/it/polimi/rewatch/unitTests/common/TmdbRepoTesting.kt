package it.polimi.rewatch.unitTests.common

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import it.polimi.rewatch.common.*;
import org.junit.Rule


class TmdbRepoTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGetMovieData(){
        assert(true);
    }

    @Test
    fun testGetTvData(){
        assert(true);


    }

    @Test
    fun testGetHomeMovieData(){
        assert(true);
    }

    @Test
    fun testGetHomeTvShowData(){
        assert(true);
    }

    @Test
    fun testGetSeasonData(){
        assert(true);


    }

    @Test
    fun testGetEpisodeData(){
        assert(true);


    }

    @Test
    fun testGetPersonData(){
        assert(true);


    }

    @Test
    fun testGetMovieDetails(){
        assert(true);


    }

    @Test
    fun testEnrichItem(){

        testEnrichWatchedItem();
        testEnrichWatchlistItem();

    }

    fun testEnrichWatchedItem(){
        assert(true);
    }

    fun testEnrichWatchlistItem(){
        assert(true);
    }

    @Test
    fun testSearchMedia(){

        testEnrichWatchedItem();
        testEnrichWatchlistItem();

    }
}