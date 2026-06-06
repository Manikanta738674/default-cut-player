package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesSearchService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 30
    ): iTunesSearchResponse
}

data class iTunesSearchResponse(
    val resultCount: Int,
    val results: List<iTunesSongResult>
)

data class iTunesSongResult(
    val trackId: Long?,
    val trackName: String?,
    val artistName: String?,
    val collectionName: String?,
    val previewUrl: String?,
    val artworkUrl100: String?,
    val trackTimeMillis: Long?
)

fun iTunesSongResult.toTrack(): Track {
    val safeId = "itunes_${trackId}"
    return Track(
        id = safeId,
        title = trackName ?: "Unknown Title",
        artist = artistName ?: "Unknown Artist",
        album = collectionName ?: "Unknown Album",
        durationMs = trackTimeMillis ?: 180000L,
        mediaUrl = previewUrl ?: "",
        coverUrl = artworkUrl100 ?: "",
        lyrics = "[00:01.00] Enjoy streaming preview or live cloud track of ${trackName ?: "this song"}!\n[00:06.00] Artist: ${artistName ?: "Unknown Artist"}\n[00:12.00] Album: ${collectionName ?: "Unknown Album"}\n[00:18.00] Delivered in real-time over the high-speed cyber-web.",
        isOffline = false,
        isFavorite = false,
        isRadio = false,
        isDownloaded = false
    )
}

object iTunesSearchClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: iTunesSearchService = retrofit.create(iTunesSearchService::class.java)
}
