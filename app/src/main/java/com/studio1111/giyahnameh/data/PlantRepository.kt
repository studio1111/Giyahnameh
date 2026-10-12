package com.studio1111.giyahnameh.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PlantRepository(private val dao: PlantDao) {
    fun plants(query: String): Flow<List<Plant>> = if (query.isBlank()) dao.observeAll() else dao.search(query)
    fun plant(id: String): Flow<Plant?> = dao.observeById(id)
    fun favorites(): Flow<List<Plant>> = dao.observeFavorites()
    suspend fun favorite(id: String, enabled: Boolean) = dao.setFavorite(id, enabled)

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) { dao.insertIfMissing(PlantSeeds.all) }

    suspend fun refreshImage(plant: Plant): String? = withContext(Dispatchers.IO) {
        if (plant.imageUrl.isNotBlank()) return@withContext plant.imageUrl
        var connection: HttpURLConnection? = null
        try {
            val encoded = java.net.URLEncoder.encode(plant.scientificName.substringBefore(" ("), "UTF-8")
            connection = (URL("https://api.gbif.org/v1/occurrence/search?scientificName=$encoded&mediaType=StillImage&limit=5").openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode !in 200..299) return@withContext null
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val results = json.optJSONArray("results") ?: return@withContext null
            for (i in 0 until results.length()) {
                val media = results.optJSONObject(i)?.optJSONArray("media") ?: continue
                for (j in 0 until media.length()) {
                    val image = media.optJSONObject(j)?.optString("identifier").orEmpty()
                    if (image.startsWith("https://")) {
                        dao.updateImage(plant.id, image)
                        return@withContext image
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.w("Giyahnameh", "Image enrichment unavailable: ${e.javaClass.simpleName}")
            null
        } finally { connection?.disconnect() }
    }
}
