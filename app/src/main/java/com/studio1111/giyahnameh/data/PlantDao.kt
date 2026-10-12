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

    @Query("SELECT * FROM plants WHERE imageUrl = '' OR imageUrl IS NULL")
    suspend fun missingImages(): List<Plant>

    @Query("UPDATE plants SET imageUrl = :imageUrl, imageAttribution = :attribution, imageLicense = :license, imageSourceUrl = :sourceUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateImage(id: String, imageUrl: String, attribution: String, license: String, sourceUrl: String, updatedAt: Long = System.currentTimeMillis())
}
