package com.studio1111.giyahnameh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY persianName COLLATE NOCASE")
    fun observeAll(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE persianName LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY persianName")
    fun search(query: String): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<Plant?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(plants: List<Plant>)

    @Query("UPDATE plants SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("SELECT * FROM plants WHERE isFavorite = 1 ORDER BY persianName")
    fun observeFavorites(): Flow<List<Plant>>

    @Query("SELECT COUNT(*) FROM plants WHERE imageLocalPath != ''")
    fun observeCachedImageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM plants")
    fun observeTotalPlantCount(): Flow<Int>

    @Query("SELECT * FROM plants WHERE imageLocalPath = '' OR imageLocalPath IS NULL ORDER BY RANDOM()")
    suspend fun missingImages(): List<Plant>

    @Query("SELECT EXISTS(SELECT 1 FROM plants WHERE imageUrl = :imageUrl AND id != :plantId)")
    suspend fun imageUrlUsedByOtherPlant(imageUrl: String, plantId: String): Boolean

    @Query("""
        UPDATE plants SET
            persianName = :persianName,
            scientificName = :scientificName,
            family = :family,
            category = :category,
            description = :description,
            habitat = :habitat,
            care = :care,
            traditionalUses = :traditionalUses,
            evidence = :evidence,
            cautions = :cautions,
            interactions = :interactions,
            sourceUrl = :sourceUrl,
            partsUsed = :partsUsed,
            preparation = :preparation,
            specialPopulations = :specialPopulations,
            faq = :faq,
            bibliography = :bibliography
        WHERE id = :id
    """)
    suspend fun updateEditorialFields(
        id: String,
        persianName: String,
        scientificName: String,
        family: String,
        category: String,
        description: String,
        habitat: String,
        care: String,
        traditionalUses: String,
        evidence: String,
        cautions: String,
        interactions: String,
        sourceUrl: String,
        partsUsed: String,
        preparation: String,
        specialPopulations: String,
        faq: String,
        bibliography: String
    )

    @Query("UPDATE plants SET imageUrl = :imageUrl, imageLocalPath = :localPath, imageAttribution = :attribution, imageLicense = :license, imageSourceUrl = :sourceUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateImage(id: String, imageUrl: String, localPath: String, attribution: String, license: String, sourceUrl: String, updatedAt: Long = System.currentTimeMillis())
}
