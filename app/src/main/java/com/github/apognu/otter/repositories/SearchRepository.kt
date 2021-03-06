package com.github.apognu.otter.repositories

import android.content.Context
import com.github.apognu.otter.utils.FunkwhaleResponse
import com.github.apognu.otter.utils.Track
import com.github.apognu.otter.utils.TracksCache
import com.github.apognu.otter.utils.TracksResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader

class SearchRepository(override val context: Context?, query: String) : Repository<Track, TracksCache>() {
  override val cacheId: String? = null
  override val upstream = HttpUpstream<Track, FunkwhaleResponse<Track>>(HttpUpstream.Behavior.AtOnce, "/api/v1/tracks/?playable=true&q=$query", object : TypeToken<TracksResponse>() {}.type)

  override fun cache(data: List<Track>) = TracksCache(data)
  override fun uncache(reader: BufferedReader) = gsonDeserializerOf(TracksCache::class.java).deserialize(reader)

  override fun onDataFetched(data: List<Track>): List<Track> = runBlocking {
    val favorites = FavoritedRepository(context).fetch(Origin.Network.origin)
      .map { it.data }
      .toList()
      .flatten()

    data.map { track ->
      track.favorite = favorites.contains(track.id)
      track
    }
  }
}