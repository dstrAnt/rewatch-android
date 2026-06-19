package it.polimi.rewatch.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test suite for [TrackingMediaDto] mapping and conversion functions.
 * Verifies that data is correctly preserved when converting between data transfer objects (DTOs)
 * and domain models like [WatchedMedia] and [WatchlistMedia].
 */
class TrackingMediaTest {

    /**
     * Tests the instantiation of [TrackingMediaDto] under different scenarios,
     * delegating to helper methods to verify default values and mapping from existing domain objects.
     */
    @Test
    fun testAppTrackingMediaConstructor() {
        testEmptyTrackingMedia()
        testTrivialWatchedMedia()
        testTrivialWatchedMedia2()
    }

    /**
     * Verifies the conversion from [TrackingMediaDto] to [WatchedMedia] for Movies.
     * Ensures that properties like the parent ID, rating, and date are accurately mapped
     * to a domain model using a [MovieId].
     */
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

    /**
     * Verifies the conversion from [TrackingMediaDto] to [WatchedMedia] for TV Shows.
     * Ensures that the generated domain object correctly uses a [TvId] instead of a [MovieId]
     * and that all specific properties are maintained.
     */
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

    /**
     * Verifies the conversion from [TrackingMediaDto] to [WatchlistMedia] for Movies.
     * Ensures that the movie ID and added date are correctly preserved,
     * without including properties that don't belong to a watchlist (like ratings).
     */
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

    /**
     * Verifies the conversion from [TrackingMediaDto] to [WatchlistMedia] for TV Shows.
     * Ensures that the generated domain object uses a [TvId] and properly preserves the date.
     */
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

    /**
     * Helper method to verify that an empty [TrackingMediaDto] is initialized
     * with the correct default values (empty strings, zero integers/floats, null dates).
     */
    private fun testEmptyTrackingMedia() {
        val trackingMedia = TrackingMediaDto()

        assertEquals("", trackingMedia.documentId)
        assertEquals(0, trackingMedia.parentId)
        assertEquals("", trackingMedia.mediaType)
        assertEquals(0f, trackingMedia.rating)
        assertNull(trackingMedia.date)
    }

    /**
     * Helper method to verify the mapping of a basic [WatchedMedia] (e.g., a Movie with just an ID)
     * into a [TrackingMediaDto].
     */
    private fun testTrivialWatchedMedia() {
        val watchedMedia = WatchedMedia(id = MovieId(0))
        val trackingMedia = TrackingMediaDto(watchedMedia)

        assertEquals(watchedMedia.id.getUid(), trackingMedia.documentId)
        assertEquals(watchedMedia.id.parentId, trackingMedia.parentId)
        assertEquals(watchedMedia.id.type.name, trackingMedia.mediaType)
        assertEquals(watchedMedia.rating, trackingMedia.rating)
        assertNotNull(trackingMedia.date)
    }

    /**
     * Helper method to verify the mapping of a fully populated [WatchedMedia]
     * (e.g., a TV Show with a specific ID, rating, and date) into a [TrackingMediaDto],
     * checking specifically for proper time zone and milliseconds conversion.
     */
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