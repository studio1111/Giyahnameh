package com.studio1111.giyahnameh.data

import android.content.Context
import org.json.JSONObject

data class PlantImageCredit(
    val creator: String,
    val license: String,
    val source: String,
    val sourcePageUrl: String
)

/**
 * Reads the image manifest generated during the Android build. An image is considered
 * offline-ready only when the packaged asset exists and the manifest records an accepted
 * reusable license and an HTTPS source page.
 */
object PlantImageAssetCatalog {
    fun loadVerifiedCredits(context: Context): Map<String, PlantImageCredit> {
        val packagedFiles = context.assets.list("plants").orEmpty().toSet()
        if (packagedFiles.isEmpty()) return emptyMap()

        val manifest = runCatching {
            context.assets.open("plant-image-manifest.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return emptyMap()

        val root = runCatching { JSONObject(manifest) }.getOrNull() ?: return emptyMap()
        val credits = mutableMapOf<String, PlantImageCredit>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val id = keys.next()
            val record = root.optJSONObject(id) ?: continue
            val assetPath = record.optString("assetPath")
            val sourcePageUrl = record.optString("sourcePageUrl")
            if (record.optBoolean("licenseVerified") != true) continue
            if (assetPath != "plants/$id.webp" || "$id.webp" !in packagedFiles) continue
            if (!sourcePageUrl.startsWith("https://")) continue

            val license = record.optString("license")
            val creator = record.optString("creator")
            val source = record.optString("source")
            if (license.isBlank() || creator.isBlank() || source.isBlank()) continue

            credits[id] = PlantImageCredit(
                creator = creator,
                license = license,
                source = source,
                sourcePageUrl = sourcePageUrl
            )
        }
        return credits
    }
}
