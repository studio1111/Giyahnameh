package com.studio1111.giyahnameh

import com.studio1111.giyahnameh.data.PlantCatalogAsia
import com.studio1111.giyahnameh.data.PlantCatalogAsia2
import com.studio1111.giyahnameh.data.PlantCatalogAsia3Clean
import com.studio1111.giyahnameh.data.PlantCatalogAsia4
import com.studio1111.giyahnameh.data.PlantCatalogAsia5
import com.studio1111.giyahnameh.data.PlantCatalogAsia6
import com.studio1111.giyahnameh.data.PlantCatalogAsia7
import com.studio1111.giyahnameh.data.PlantCatalogAsia8
import com.studio1111.giyahnameh.data.PlantCatalogAsia9
import com.studio1111.giyahnameh.data.PlantCatalogAsia10
import com.studio1111.giyahnameh.data.PlantSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantSeedsTest {
    private val allSeedPlants = PlantSeeds.all + PlantCatalogAsia.plants + PlantCatalogAsia2.plants + PlantCatalogAsia3Clean.plants + PlantCatalogAsia4.plants + PlantCatalogAsia5.plants + PlantCatalogAsia6.plants + PlantCatalogAsia7.plants + PlantCatalogAsia8.plants + PlantCatalogAsia9.plants + PlantCatalogAsia10.plants

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
        assertTrue(PlantCatalogAsia3Clean.plants.size >= 30)
        assertTrue(PlantCatalogAsia4.plants.size >= 25)
        assertTrue(PlantCatalogAsia5.plants.size >= 30)
        assertTrue(PlantCatalogAsia6.plants.size >= 30)
        assertTrue(PlantCatalogAsia7.plants.size >= 35)
        assertTrue(PlantCatalogAsia8.plants.size >= 45)
        assertTrue(PlantCatalogAsia9.plants.size >= 35)
        assertTrue(PlantCatalogAsia10.plants.size >= 35)
        assertTrue(allSeedPlants.size >= 330)
        assertEquals(allSeedPlants.size, allSeedPlants.map { it.scientificName.lowercase() }.toSet().size)
        assertTrue(PlantCatalogAsia2.plants.any { it.persianName == "نخود" })
        assertTrue(PlantCatalogAsia2.plants.any { it.persianName == "گل محمدی" })
        assertTrue(PlantCatalogAsia2.plants.any { it.persianName == "اسفرزه" })
        assertTrue(PlantCatalogAsia3Clean.plants.any { it.persianName == "عناب" })
        assertTrue(PlantCatalogAsia3Clean.plants.any { it.persianName == "اسفناج" })
    }

    @Test fun regionalBatchContainsExpectedStarterCoverage() {
        assertTrue(PlantCatalogAsia.plants.size >= 20)
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "زعفران" })
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "سیاه‌دانه" })
        assertTrue(PlantCatalogAsia.plants.any { it.persianName == "زنجبیل" })
    }
}
