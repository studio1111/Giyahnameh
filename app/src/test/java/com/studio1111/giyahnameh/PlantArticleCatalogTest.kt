package com.studio1111.giyahnameh

import com.studio1111.giyahnameh.data.PlantArticleCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantArticleCatalogTest {
    @Test
    fun allSixCuratedArticlesHaveCompleteLongFormSectionsAndDirectReferences() {
        val articles = PlantArticleCatalog.curatedArticles
        assertEquals(
            setOf(
                "mentha-spicata",
                "zingiber-officinale",
                "curcuma-longa",
                "allium-sativum",
                "matricaria-chamomilla",
                "senna-alexandrina"
            ),
            articles.map { it.plantId }.toSet()
        )

        articles.forEach { article ->
            assertTrue("${article.plantId}: ${article.missingRequiredSections()}", article.missingRequiredSections().isEmpty())
            assertTrue("${article.plantId}: too few references", article.references.size >= 3)
            assertTrue("${article.plantId}: article too short (${article.wordCount} words)", article.wordCount >= 350)
            assertTrue("${article.plantId}: invalid review date", article.reviewDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            assertTrue(
                "${article.plantId}: source links need a relevance note",
                article.references.all { it.url.startsWith("https://") && it.relevanceNote.isNotBlank() && it.sourceType.isNotBlank() }
            )
            assertFalse(
                "${article.plantId}: placeholder source found",
                article.references.any { it.url.contains("example.com") || it.url.endsWith("://") }
            )
        }
    }

    @Test
    fun spearmintSeparatesSpearmintEvidenceFromPeppermintOilTrials() {
        val article = PlantArticleCatalog.find("mentha-spicata")
        assertNotNull(article)
        article!!
        assertTrue(article.dosage.contains("دوز درمانی"))
        assertTrue(article.evidence.contains("نعناع فلفلی"))
        assertTrue(article.specialPopulations.contains("بارداری"))
        assertTrue(article.faqs.size >= 3)
        assertTrue(article.references.any { it.url.contains("jm.um.ac.ir") })
        assertTrue(article.references.any { it.url.contains("journal.bums.ac.ir") })
    }

    @Test
    fun clinicallySensitivePlantsHaveSpeciesSpecificRiskGuidance() {
        val senna = PlantArticleCatalog.find("senna-alexandrina")!!
        assertTrue(senna.dosage.contains("۱۲ سال"))
        assertTrue(senna.dosage.contains("یک هفته"))
        assertTrue(senna.interactions.contains("پتاسیم"))

        val garlic = PlantArticleCatalog.find("allium-sativum")!!
        assertTrue(garlic.interactions.contains("ضدانعقاد") || garlic.interactions.contains("ضدانعقاد"))
        assertTrue(garlic.specialPopulations.contains("بارداری"))

        val turmeric = PlantArticleCatalog.find("curcuma-longa")!!
        assertTrue(turmeric.sideEffects.contains("کبد"))
        assertTrue(turmeric.specialPopulations.contains("بارداری"))
    }
}
