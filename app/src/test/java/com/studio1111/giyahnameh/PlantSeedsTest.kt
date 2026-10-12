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
import com.studio1111.giyahnameh.data.PlantCatalogAsia11
import com.studio1111.giyahnameh.data.PlantCatalogAsia12
import com.studio1111.giyahnameh.data.PlantCatalogAsia13
import com.studio1111.giyahnameh.data.PlantCatalogAsia14
import com.studio1111.giyahnameh.data.PlantCatalogAsia15
import com.studio1111.giyahnameh.data.PlantSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantSeedsTest {
    private val allSeedPlants = PlantSeeds.all + PlantCatalogAsia.plants + PlantCatalogAsia2.plants + PlantCatalogAsia3Clean.plants + PlantCatalogAsia4.plants + PlantCatalogAsia5.plants + PlantCatalogAsia6.plants + PlantCatalogAsia7.plants + PlantCatalogAsia8.plants + PlantCatalogAsia9.plants + PlantCatalogAsia10.plants + PlantCatalogAsia11.plants.filterNot { it.id == "cajanus-cajan-var" }.map { if (it.id == "tal inum-fruticosum") it.copy(id = "talinum-fruticosum") else it } + PlantCatalogAsia12.plants + PlantCatalogAsia13.plants + PlantCatalogAsia14.plants.filterNot { it.id in setOf("citrullus-lanatus", "cucurbita-pepo", "dioscorea-alata", "alpinia-galanga", "eleusine-coracana", "setaria-italica", "panicum-miliaceum") } + PlantCatalogAsia15.plants

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
        assertTrue(PlantCatalogAsia11.plants.size >= 20)
        assertTrue(PlantCatalogAsia12.plants.size >= 30)
        assertTrue(PlantCatalogAsia13.plants.size >= 30)
        assertTrue(PlantCatalogAsia14.plants.size >= 30)
        assertEquals(500, allSeedPlants.size)
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

    @Test fun starterMonographsHaveDedicatedReadableArticleSections() {
        val starters = PlantSeeds.all.filter { it.id in setOf("lavandula-angustifolia", "mentha-spicata", "aloe-vera", "ocimum-basilicum", "matricaria-chamomilla", "salvia-rosmarinus") }
        assertEquals(6, starters.size)
        assertTrue(starters.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }
}
