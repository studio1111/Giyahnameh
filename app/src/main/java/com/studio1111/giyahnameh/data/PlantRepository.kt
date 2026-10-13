package com.studio1111.giyahnameh.data

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PlantRepository(private val dao: PlantDao, private val context: Context) {
    private companion object {
        const val CATALOG_VERSION = 69
        val catalogSyncMutex = Mutex()
    }

    fun plants(query: String): Flow<List<Plant>> = if (query.isBlank()) dao.observeAll() else dao.search(query)
    fun plant(id: String): Flow<Plant?> = dao.observeById(id)
    fun favorites(): Flow<List<Plant>> = dao.observeFavorites()
    fun cachedImageCount(): Flow<Int> = dao.observeCachedImageCount()
    fun totalPlantCount(): Flow<Int> = dao.observeTotalPlantCount()
    suspend fun favorite(id: String, enabled: Boolean) = dao.setFavorite(id, enabled)

    // Seed both the original examples and the curated regional catalogue without overwriting user data.
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        catalogSyncMutex.withLock {
            val seeds = (PlantSeeds.all + PlantCatalogAsia.plants + PlantCatalogAsia2.plants + PlantCatalogAsia3Clean.plants + PlantCatalogAsia4.plants + PlantCatalogAsia5.plants + PlantCatalogAsia6.plants + PlantCatalogAsia7.plants + PlantCatalogAsia8.plants + PlantCatalogAsia9.plants + PlantCatalogAsia10.plants + PlantCatalogAsia11.plants.filterNot { it.id == "cajanus-cajan-var" }.map { if (it.id == "tal inum-fruticosum") it.copy(id = "talinum-fruticosum") else it } + PlantCatalogAsia12.plants + PlantCatalogAsia13.plants + PlantCatalogAsia14.plants.filterNot { it.id in setOf("citrullus-lanatus", "cucurbita-pepo", "dioscorea-alata", "alpinia-galanga", "eleusine-coracana", "setaria-italica", "panicum-miliaceum") } + PlantCatalogAsia15.plants).map(PlantMonographOverrides::apply)
            dao.insertIfMissing(seeds)

            // Bump this value when bundled editorial content changes. User favorites and cached image metadata are preserved.
            val preferences = context.getSharedPreferences("giyahnameh_catalog", Context.MODE_PRIVATE)
            val installedCatalogVersion = preferences.getInt("catalog_version", 0)
            if (installedCatalogVersion < CATALOG_VERSION) {
                seeds.forEach { plant ->
                    dao.updateEditorialFields(
                        id = plant.id,
                        persianName = plant.persianName,
                        scientificName = plant.scientificName,
                        family = plant.family,
                        category = plant.category,
                        description = plant.description,
                        habitat = plant.habitat,
                        care = plant.care,
                        traditionalUses = plant.traditionalUses,
                        evidence = plant.evidence,
                        cautions = plant.cautions,
                        interactions = plant.interactions,
                        sourceUrl = plant.sourceUrl,
                        partsUsed = plant.partsUsed,
                        preparation = plant.preparation,
                        specialPopulations = plant.specialPopulations,
                        faq = plant.faq,
                        bibliography = plant.bibliography
                    )
                }
                preferences.edit().putInt("catalog_version", CATALOG_VERSION).apply()
            }
        }
    }

    suspend fun refreshMissingImages() {
        dao.missingImages().take(20).forEach { plant -> refreshImage(plant) }
    }

    suspend fun refreshImage(plant: Plant): String? = withContext(Dispatchers.IO) {
        if (plant.imageUrl.isNotBlank() && !dao.imageUrlUsedByOtherPlant(plant.imageUrl, plant.id)) {
            val cached = downloadImage(plant.imageUrl, plant.id)
            if (cached != null) {
                dao.updateImage(plant.id, plant.imageUrl, cached, plant.imageAttribution, plant.imageLicense, plant.imageSourceUrl)
                return@withContext plant.imageUrl
            }
            // A saved URL may have expired. Continue to fresh sources.
        }
        var connection: HttpURLConnection? = null
        try {
            val encoded = java.net.URLEncoder.encode(plant.scientificName.substringBefore(" ("), "UTF-8")
            connection = (URL("https://api.gbif.org/v1/occurrence/search?scientificName=$encoded&mediaType=StillImage&limit=5").openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode !in 200..299) return@withContext findAndCacheWikimediaImage(plant)
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val results = json.optJSONArray("results") ?: return@withContext findAndCacheWikimediaImage(plant)
            for (i in 0 until results.length()) {
                val media = results.optJSONObject(i)?.optJSONArray("media") ?: continue
                for (j in 0 until media.length()) {
                    val item = media.optJSONObject(j) ?: continue
                    val image = item.optString("identifier").orEmpty()
                    val license = item.optString("license").orEmpty()
                    val holder = item.optString("rightsHolder").orEmpty()
                    val source = item.optString("references").ifBlank { image }
                    val licenseLower = license.lowercase()
                    val reuseAllowed = licenseLower.contains("cc0") ||
                        licenseLower.contains("creativecommons.org/licenses/by/") ||
                        licenseLower.contains("creativecommons.org/licenses/by-sa/") ||
                        licenseLower.contains("public domain")
                    val nonCommercialOnly = licenseLower.contains("-nc") || licenseLower.contains("noncommercial")
                    if (image.startsWith("https://") && license.isNotBlank() && source.startsWith("https://") && reuseAllowed && !nonCommercialOnly) {
                        if (dao.imageUrlUsedByOtherPlant(image, plant.id)) continue
                        val localPath = downloadImage(image, plant.id) ?: continue
                        dao.updateImage(plant.id, image, localPath, holder.ifBlank { "دارنده حقوق در رکورد GBIF مشخص نشده" }, license, source)
                        return@withContext image
                    }
                }
            }
            findAndCacheWikimediaImage(plant)
        } catch (e: Exception) {
            Log.w("Giyahnameh", "GBIF image search unavailable; trying Wikimedia Commons: " + e.javaClass.simpleName)
            findAndCacheWikimediaImage(plant)
        } finally { connection?.disconnect() }
    }

    /** Search Commons as a second source when GBIF has no usable, reusable plant photo. */
    private suspend fun findAndCacheWikimediaImage(plant: Plant): String? {
        var connection: HttpURLConnection? = null
        return try {
            val botanicalName = plant.scientificName.substringBefore(" (")
            val search = java.net.URLEncoder.encode("filetype:bitmap \"" + botanicalName + "\"", "UTF-8")
            val api = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=" + search + "&gsrnamespace=6&gsrlimit=10&prop=imageinfo&iiprop=url%7Cextmetadata&iiurlwidth=1400&format=json"
            connection = (URL(api).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "GiyahnamehAndroid/1.0")
            }
            if (connection.responseCode !in 200..299) return null
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return null
            val keys = pages.keys()
            while (keys.hasNext()) {
                val page = pages.optJSONObject(keys.next()) ?: continue
                val imageInfo = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                val imageUrl = imageInfo.optString("thumburl").ifBlank { imageInfo.optString("url") }
                val descriptionUrl = imageInfo.optString("descriptionurl")
                val metadata = imageInfo.optJSONObject("extmetadata")
                val license = metadata?.optJSONObject("LicenseShortName")?.optString("value").orEmpty()
                val artist = metadata?.optJSONObject("Artist")?.optString("value").orEmpty()
                val licenseLower = license.lowercase()
                val reusable = licenseLower == "cc0" || licenseLower.contains("public domain") || licenseLower.contains("cc by") || licenseLower.contains("cc-by")
                val restrictive = licenseLower.contains("by-nc") || licenseLower.contains("by nd") || licenseLower.contains("by-nd") || licenseLower.contains("noncommercial") || licenseLower.contains("no derivatives")
                if (imageUrl.startsWith("https://") && descriptionUrl.startsWith("https://") && license.isNotBlank() && reusable && !restrictive) {
                    if (dao.imageUrlUsedByOtherPlant(imageUrl, plant.id)) continue
                    val localPath = downloadImage(imageUrl, plant.id) ?: continue
                    dao.updateImage(plant.id, imageUrl, localPath, artist.ifBlank { "نام عکاس در فراداده مشخص نشده" }, license, descriptionUrl)
                    return imageUrl
                }
            }
            null
        } catch (e: Exception) {
            Log.w("Giyahnameh", "Wikimedia image search unavailable: " + e.javaClass.simpleName)
            null
        } finally {
            connection?.disconnect()
        }
    }
    private fun downloadImage(imageUrl: String, plantId: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 10000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }
            if (connection.responseCode !in 200..299) return null
            val contentType = connection.contentType.orEmpty().substringBefore(';').lowercase()
            if (!contentType.startsWith("image/")) return null
            if (connection.contentLengthLong > 8L * 1024 * 1024) return null
            val extension = when {
                contentType.contains("jpeg") -> "jpg"
                contentType.contains("png") -> "png"
                contentType.contains("webp") -> "webp"
                contentType.contains("gif") -> "gif"
                else -> "img"
            }
            val folder = File(context.filesDir, "plant-images").apply { mkdirs() }
            val target = File(folder, "$plantId.$extension")
            val temporary = File(folder, "$plantId.tmp")
            connection.inputStream.use { input ->
                FileOutputStream(temporary).use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > 8L * 1024 * 1024) {
                            temporary.delete()
                            return null
                        }
                        output.write(buffer, 0, count)
                    }
                }
            }
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temporary.absolutePath, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                temporary.delete()
                return null
            }
            if (target.exists()) target.delete()
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
            }
            target.absolutePath
        } catch (e: Exception) {
            Log.w("Giyahnameh", "Offline image cache unavailable: ${e.javaClass.simpleName}")
            null
        } finally {
            connection?.disconnect()
        }
    }
}
