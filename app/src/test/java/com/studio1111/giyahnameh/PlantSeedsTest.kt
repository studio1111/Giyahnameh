package com.studio1111.giyahnameh

import com.studio1111.giyahnameh.data.PlantCatalogAsia
import com.studio1111.giyahnameh.data.PlantCatalogAsia2
import com.studio1111.giyahnameh.data.PlantSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantSeedsTest {
    private val allSeedPlants = PlantSeeds.all + PlantCatalogAsia.plants + PlantCatalogAsia2.plants

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

    @Test fun secondBatchHasBroadRegionalCoverageWithoutDuplicateSpecies() {
        assertTrue(PlantCatalogAsia2.plants.size >= 25)
        assertTrue(allSeedPlants.size >= 50)
        assertEquals(allSeedPlants.size, allSeedPlants.map { it.scientificName.lowercase() }.toSet().size)
        assertTrue(PlantCatalogAsia2.plants.any { it.persianName == "نخود" })
        assertTrue(PlantCatalogAsia2.plants.any { it.persianName == "گل محمدی" })
        assertTrue(PlantCatalogAsia2.plants.any { it.persianName == "اسفرزه" })
    }

    @Test fun regionalBatchContainsExpectedStarterCoverage() {
        assertTrue(PlantCatalogAsia.plants.size >= 20)
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "زعفران" })
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "سیاه‌دانه" })
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "زنجبیل" })
    }
}
