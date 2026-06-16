package it.polimi.rewatch.unitTests.model

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import it.polimi.rewatch.model.*;
import it.polimi.rewatch.theme.RewatchTheme
import org.junit.Rule
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test Case designed to test Class TrackingMedia
 */

class TrackingMediaTesting {

    @get:Rule
    val composeTestRule = createComposeRule()

    /* Testing how class TrackingMedia is constructed */

    @Test
    fun testAppTrackingMediaConstructor() {

        testEmptyTrackingMedia();
        testTrivialWatchedMedia();
        testTrivialWatchedMedia2();
    }

    @Test
    fun testAppTrackingMediaToWatchedMovies() {

       val trackingMedia = TrackingMediaDto();
       val watched = trackingMedia.toWatchedMedia()

        assert(watched.id.parentId == 0)
        assert(watched.rating == 0f)
        assert(watched.date != null)

        val genericWatchedMedia = WatchedMedia(id = MovieId(0),
            date = LocalDateTime.of(2026, 4, 25, 0, 0))

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia);
        val watched2 = trackingMedia2.toWatchedMedia()

        assert(watched2.id.parentId == 0)
        assert(watched2.rating == 0f)
        assert(watched2.date == genericWatchedMedia.date)

        val trackingMedia3 = TrackingMediaDto(WatchedMedia(id = MovieId(3), rating = 4.3f,
            date = LocalDateTime.of(2026, 4, 25, 0, 0)),);
        val watched3 = trackingMedia3.toWatchedMedia()

        assert(watched3.id.parentId == 3)
        assert(watched3.rating == 4.3f)
        assert(trackingMedia3.date?.equals(watched3.date.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()) == true);
    }

    @Test
    fun testAppTrackingMediaToWatchedTVShows() {

        val trackingMedia = TrackingMediaDto();
        val watched = trackingMedia.toWatchedMedia()

        assert(watched.id.parentId == 0)
        assert(watched.rating == 0f)
        assert(watched.date != null)

        val genericWatchedMedia = WatchedMedia(id = TvId(0),
            date = LocalDateTime.of(2026, 4, 25, 0, 0))

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia);
        val watched2 = trackingMedia2.toWatchedMedia()

        assert(watched2.id.parentId == 0)
        assert(watched2.rating == 0f)
        assert(watched2.date == genericWatchedMedia.date)

        val trackingMedia3 = TrackingMediaDto(WatchedMedia(id = TvId(3), rating = 4.3f,
            date = LocalDateTime.of(2026, 4, 25, 0, 0)));
        val watched3 = trackingMedia3.toWatchedMedia()

        assert(watched3.id.parentId == 3)
        assert(watched3.rating == 4.3f)
        assert(trackingMedia3.date?.equals(watched3.date.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()) == true);
    }

    @Test
    fun testAppTrackingMediaToWatchlistMovies() {

        val trackingMedia = TrackingMediaDto();
        val watchlist = trackingMedia.toWatchlistMedia()

        assert(watchlist.id.parentId == 0)
        assert(watchlist.date != null)

        val genericWatchedMedia = WatchedMedia(id = MovieId(0),
            date = LocalDateTime.of(2026, 5, 3, 0, 0))

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia);
        val watchlist2 = trackingMedia2.toWatchlistMedia()

        assert(watchlist2.id.parentId == 0)
        assert(watchlist2.date.equals(genericWatchedMedia.date))

        val trackingMedia3 = TrackingMediaDto(WatchlistMedia(id = MovieId(3)));
        val watchlist3 = trackingMedia3.toWatchlistMedia()

        assert(watchlist3.id.parentId == 3)
        assert(trackingMedia3.date?.equals(watchlist3.date.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()) == true);
    }

    @Test
    fun testAppTrackingMediaToWatchlistTVShows() {

        val trackingMedia = TrackingMediaDto();
        val watchlist = trackingMedia.toWatchlistMedia()

        assert(watchlist.id.parentId == 0)
        assert(watchlist.date != null)

        val genericWatchedMedia = WatchedMedia(id = TvId(0),
            date = LocalDateTime.of(2026, 5, 3, 0, 0))

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia);
        val watchlist2 = trackingMedia2.toWatchlistMedia()

        assert(watchlist2.id.parentId == 0)
        assert(watchlist2.date.equals(genericWatchedMedia.date))

        val trackingMedia3 = TrackingMediaDto(WatchlistMedia(id = TvId(3)));
        val watchlist3 = trackingMedia3.toWatchlistMedia()

        assert(watchlist3.id.parentId == 3)
        assert(trackingMedia3.date?.equals(watchlist3.date.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()) == true);
    }

    fun testEmptyTrackingMedia(){

        val trackingMedia = TrackingMediaDto();

        assert(trackingMedia.documentId == "");
        assert(trackingMedia.parentId == 0);
        assert(trackingMedia.mediaType == "");
        assert(trackingMedia.rating == 0f);
        assert(trackingMedia.date == null);

    }

    fun testTrivialWatchedMedia(){
        val watchedMedia = WatchedMedia(id = MovieId(0));
        val trackingMedia = TrackingMediaDto(watchedMedia);

        assert(trackingMedia.documentId == watchedMedia.id.type.name + "_" + watchedMedia.id.parentId);
        assert(trackingMedia.parentId == watchedMedia.id.parentId);
        assert(trackingMedia.mediaType == watchedMedia.id.type.name,);
        assert(trackingMedia.rating == watchedMedia.rating);
        assert(trackingMedia.date != null);
    }

    fun testTrivialWatchedMedia2(){
        val watchedMedia = WatchedMedia(id = TvId(1390),
            rating = 8.5f,
            date = LocalDateTime.of(2026, 3, 23, 0, 0),);

        val trackingMedia = TrackingMediaDto(watchedMedia);

        assert(trackingMedia.documentId == watchedMedia.id.type.name + "_" + watchedMedia.id.parentId);
        assert(trackingMedia.parentId == watchedMedia.id.parentId);
        assert(trackingMedia.mediaType == watchedMedia.id.type.name,);
        assert(trackingMedia.rating == watchedMedia.rating);
        assert(trackingMedia.date?.equals(watchedMedia.date.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()) == true);
    }

}