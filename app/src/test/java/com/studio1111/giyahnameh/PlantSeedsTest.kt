package com.studio1111.giyahnameh

import com.studio1111.giyahnameh.data.PlantSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantSeedsTest {
    @Test fun seedIdsAreUnique() {
        assertEquals(PlantSeeds.all.size, PlantSeeds.all.map { it.id }.toSet().size)
    }
    @Test fun everySeedHasSafetyAndSourceFields() {
        assertTrue(PlantSeeds.all.all { it.cautions.isNotBlank() && it.interactions.isNotBlank() && it.sourceUrl.startsWith("https://") })
    }
}
