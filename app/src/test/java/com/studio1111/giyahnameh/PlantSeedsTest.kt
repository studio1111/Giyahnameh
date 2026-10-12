package com.studio1111.giyahnameh

import com.studio1111.giyahnameh.data.PlantCatalogAsia
import com.studio1111.giyahnameh.data.PlantSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantSeedsTest {
    private val allSeedPlants = PlantSeeds.all + PlantCatalogAsia.plants

    @Test fun seedIdsAreUniqueAcrossAllOfflineBatches() {
        assertEquals(allSeedPlants.size, allSeedPlants.map { it.id }.toSet().size)
    }

    @Test fun everySeedHasSafetyAndSourceFields() {
        assertTrue(allSeedPlants.all {
            it.cautions.isNotBlank() &&
                it.interactions.isNotBlank() &&
                it.sourceUrl.startsWith("https://")
        })
    }

    @Test fun regionalBatchContainsExpectedStarterCoverage() {
        assertTrue(PlantCatalogAsia.plants.size >= 20)
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "زعفران" })
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "سیاه‌دانه" })
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "زنجبیل" })
    }
}
