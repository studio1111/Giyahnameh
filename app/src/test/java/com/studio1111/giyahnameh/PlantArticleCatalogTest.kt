package com.studio1111.giyahnameh

import com.studio1111.giyahnameh.data.PlantArticleCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantArticleCatalogTest {
    @Test
    fun spearmintHasPlantSpecificLongFormSectionsAndDirectSources() {
        val article = PlantArticleCatalog.find("mentha-spicata")
        assertNotNull("The spearmint article must be bundled in the app", article)
        article!!

        assertTrue(article.missingRequiredSections().toString(), article.missingRequiredSections().isEmpty())
        assertTrue("At least three relevant references are required", article.references.size >= 3)
        assertTrue(
            "Every reference must be a direct HTTPS page",
            article.references.all { it.url.startsWith("https://") && it.relevanceNote.isNotBlank() }
        )
        assertTrue("The dosage section must not invent a medicinal dose", article.dosage.contains("دوز درمانی"))
        assertTrue("Evidence must distinguish spearmint and peppermint", article.evidence.contains("نعناع فلفلی"))
        assertTrue("Special populations must be discussed", article.specialPopulations.contains("بارداری"))
        assertTrue("Questions and answers should be present", article.faqs.size >= 3)
    }

    @Test
    fun noReferenceMayBeAPlaceholderOrAHomePageWithoutClaimContext() {
        val articles = PlantArticleCatalog.curatedArticles
        assertFalse("The initial curated catalogue must not be empty", articles.isEmpty())

        articles.forEach { article ->
            assertTrue(article.plantId.isNotBlank())
            assertFalse(article.references.any { it.url.endsWith("://") || it.url.contains("example.com") })
            assertTrue(article.references.all { it.relevanceNote.isNotBlank() })
            assertTrue(article.reviewDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }
    }
}
