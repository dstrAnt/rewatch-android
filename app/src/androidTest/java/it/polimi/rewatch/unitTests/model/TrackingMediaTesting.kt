package it.polimi.rewatch.unitTests.model

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import it.polimi.rewatch.model.*;
import it.polimi.rewatch.theme.RewatchTheme
import org.junit.Rule
import java.time.LocalDateTime

class TrackingMediaTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppTrackingMedia() {

        testEmptyTrackingMedia();
        testWatchedMedia();
    }

    fun testEmptyTrackingMedia(){

        val trackingMedia = TrackingMediaDto();

        assert(trackingMedia.documentId == "");
        assert(trackingMedia.parentId == 0);
        assert(trackingMedia.mediaType == "");
        assert(trackingMedia.rating == 0f);
        assert(trackingMedia.date == null);

    }

    fun testWatchedMedia(){
        val watchedMedia = WatchedMedia(id = MovieId(0));
        val trackingMedia = TrackingMediaDto(watchedMedia);

        assert(trackingMedia.documentId == watchedMedia.id.type.name + "_" + watchedMedia.id.parentId);
        assert(trackingMedia.parentId == watchedMedia.id.parentId);
        assert(trackingMedia.mediaType == watchedMedia.id.type.name,);
        assert(trackingMedia.rating == watchedMedia.rating);
        assert(trackingMedia.date != null);
    }

    fun testWatchedMedia2(){
        val watchedMedia = WatchedMedia(id = TvId(1390),
            rating = 8.5f,
            date = LocalDateTime.of(2026, 3, 23, 0, 0),);
    }

}