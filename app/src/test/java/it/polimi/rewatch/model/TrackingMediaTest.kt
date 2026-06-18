package it.polimi.rewatch.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class TrackingMediaTest {

    @Test
    fun testAppTrackingMediaConstructor() {
        testEmptyTrackingMedia()
        testTrivialWatchedMedia()
        testTrivialWatchedMedia2()
    }

    @Test
    fun testAppTrackingMediaToWatchedMovies() {
        val trackingMedia = TrackingMediaDto()
        val watched = trackingMedia.toWatchedMedia()

        assertEquals(0, (watched.id as MovieId).parentId)
        assertEquals(0f, watched.rating)
        assertNotNull(watched.date)

        val genericWatchedMedia = WatchedMedia(
            id = MovieId(0),
            date = LocalDateTime.of(2026, 4, 25, 0, 0),
        )

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia)
        val watched2 = trackingMedia2.toWatchedMedia()

        assertEquals(0, (watched2.id as MovieId).parentId)
        assertEquals(0f, watched2.rating)
        assertEquals(genericWatchedMedia.date, watched2.date)

        val trackingMedia3 = TrackingMediaDto(
            WatchedMedia(
                id = MovieId(3),
                rating = 4.3f,
                date = LocalDateTime.of(2026, 4, 25, 0, 0),
            ),
        )
        val watched3 = trackingMedia3.toWatchedMedia()

        assertEquals(3, (watched3.id as MovieId).parentId)
        assertEquals(4.3f, watched3.rating)
        assertEquals(trackingMedia3.date, watched3.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

    @Test
    fun testAppTrackingMediaToWatchedTVShows() {
        val trackingMedia = TrackingMediaDto(mediaType = MediaType.TV_SHOW.name)
        val watched = trackingMedia.toWatchedMedia()

        assertTrue(watched.id is TvId)
        assertEquals(0, (watched.id as TvId).parentId)
        assertEquals(0f, watched.rating)
        assertNotNull(watched.date)

        val genericWatchedMedia = WatchedMedia(
            id = TvId(0),
            date = LocalDateTime.of(2026, 4, 25, 0, 0),
        )

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia)
        val watched2 = trackingMedia2.toWatchedMedia()

        assertTrue(watched2.id is TvId)
        assertEquals(0, (watched2.id as TvId).parentId)
        assertEquals(0f, watched2.rating)
        assertEquals(genericWatchedMedia.date, watched2.date)

        val trackingMedia3 = TrackingMediaDto(
            WatchedMedia(
                id = TvId(3),
                rating = 4.3f,
                date = LocalDateTime.of(2026, 4, 25, 0, 0),
            ),
        )
        val watched3 = trackingMedia3.toWatchedMedia()

        assertTrue(watched3.id is TvId)
        assertEquals(3, (watched3.id as TvId).parentId)
        assertEquals(4.3f, watched3.rating)
        assertEquals(trackingMedia3.date, watched3.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

    @Test
    fun testAppTrackingMediaToWatchlistMovies() {
        val trackingMedia = TrackingMediaDto()
        val watchlist = trackingMedia.toWatchlistMedia()

        assertEquals(0, (watchlist.id as MovieId).parentId)
        assertNotNull(watchlist.date)

        val genericWatchedMedia = WatchedMedia(
            id = MovieId(0),
            date = LocalDateTime.of(2026, 5, 3, 0, 0),
        )

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia)
        val watchlist2 = trackingMedia2.toWatchlistMedia()

        assertEquals(0, (watchlist2.id as MovieId).parentId)
        assertEquals(genericWatchedMedia.date, watchlist2.date)

        val trackingMedia3 = TrackingMediaDto(WatchlistMedia(id = MovieId(3)))
        val watchlist3 = trackingMedia3.toWatchlistMedia()

        assertEquals(3, (watchlist3.id as MovieId).parentId)
        assertEquals(trackingMedia3.date, watchlist3.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

    @Test
    fun testAppTrackingMediaToWatchlistTVShows() {
        val trackingMedia = TrackingMediaDto(mediaType = MediaType.TV_SHOW.name)
        val watchlist = trackingMedia.toWatchlistMedia()

        assertTrue(watchlist.id is TvId)
        assertEquals(0, (watchlist.id as TvId).parentId)
        assertNotNull(watchlist.date)

        val genericWatchedMedia = WatchedMedia(
            id = TvId(0),
            date = LocalDateTime.of(2026, 5, 3, 0, 0),
        )

        val trackingMedia2 = TrackingMediaDto(genericWatchedMedia)
        val watchlist2 = trackingMedia2.toWatchlistMedia()

        assertTrue(watchlist2.id is TvId)
        assertEquals(0, (watchlist2.id as TvId).parentId)
        assertEquals(genericWatchedMedia.date, watchlist2.date)

        val trackingMedia3 = TrackingMediaDto(WatchlistMedia(id = TvId(3)))
        val watchlist3 = trackingMedia3.toWatchlistMedia()

        assertTrue(watchlist3.id is TvId)
        assertEquals(3, (watchlist3.id as TvId).parentId)
        assertEquals(trackingMedia3.date, watchlist3.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

    private fun testEmptyTrackingMedia() {
        val trackingMedia = TrackingMediaDto()

        assertEquals("", trackingMedia.documentId)
        assertEquals(0, trackingMedia.parentId)
        assertEquals("", trackingMedia.mediaType)
        assertEquals(0f, trackingMedia.rating)
        assertNull(trackingMedia.date)
    }

    private fun testTrivialWatchedMedia() {
        val watchedMedia = WatchedMedia(id = MovieId(0))
        val trackingMedia = TrackingMediaDto(watchedMedia)

        assertEquals(watchedMedia.id.getUid(), trackingMedia.documentId)
        assertEquals(watchedMedia.id.parentId, trackingMedia.parentId)
        assertEquals(watchedMedia.id.type.name, trackingMedia.mediaType)
        assertEquals(watchedMedia.rating, trackingMedia.rating)
        assertNotNull(trackingMedia.date)
    }

    private fun testTrivialWatchedMedia2() {
        val watchedMedia = WatchedMedia(
            id = TvId(1390),
            rating = 8.5f,
            date = LocalDateTime.of(2026, 3, 23, 0, 0),
        )

        val trackingMedia = TrackingMediaDto(watchedMedia)

        assertEquals(watchedMedia.id.getUid(), trackingMedia.documentId)
        assertEquals(watchedMedia.id.parentId, trackingMedia.parentId)
        assertEquals(watchedMedia.id.type.name, trackingMedia.mediaType)
        assertEquals(watchedMedia.rating, trackingMedia.rating)
        assertEquals(watchedMedia.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), trackingMedia.date)
    }
}