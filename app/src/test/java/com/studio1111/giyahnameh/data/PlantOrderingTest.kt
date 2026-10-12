package com.studio1111.giyahnameh.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PlantOrderingTest {
    @Test
    fun alphabeticModeUsesPersianLetterOrder() {
        val input = listOf(
            plant("g", "گلابی"),
            plant("c", "چای"),
            plant("t", "تاج خروس"),
            plant("p", "پونه"),
            plant("b", "بادرنجبویه"),
            plant("k", "کاکتوس")
        )

        val sorted = PlantOrdering.sort(input, PlantSortMode.ALPHABETICAL)

        assertEquals(
            listOf("بادرنجبویه", "پونه", "تاج خروس", "چای", "کاکتوس", "گلابی"),
            sorted.map { it.persianName }
        )
    }

    @Test
    fun alphabeticModeNormalizesArabicKafAndBreaksEquivalentNameTiesById() {
        val input = listOf(
            plant("z", "كاکتوس"),
            plant("a", "کاکتوس")
        )

        val sorted = PlantOrdering.sort(input, PlantSortMode.ALPHABETICAL)

        assertEquals(listOf("a", "z"), sorted.map { it.id })
    }

    @Test
    fun popularityModePlacesCuratedCommonPlantsBeforeUnknownPlants() {
        val input = listOf(
            plant("ornamental-unknown", "گل ناشناخته", category = "زینتی"),
            plant("aloe-vera", "آلوئه‌ورا", category = "کاربردی"),
            plant("mentha-spicata", "نعناع سبز", category = "خوراکی و دارویی")
        )

        val sorted = PlantOrdering.sort(input, PlantSortMode.POPULARITY)

        assertEquals(
            listOf("mentha-spicata", "aloe-vera", "ornamental-unknown"),
            sorted.map { it.id }
        )
    }

    @Test
    fun popularityModeIsDeterministicWhenScoresAreEqual() {
        val input = listOf(
            plant("unknown-z", "زرشک ناشناخته", category = "گیاه خاص"),
            plant("unknown-b", "بنفشه ناشناخته", category = "گیاه خاص")
        )

        val first = PlantOrdering.sort(input, PlantSortMode.POPULARITY).map { it.id }
        val second = PlantOrdering.sort(input.reversed(), PlantSortMode.POPULARITY).map { it.id }

        assertEquals(first, second)
    }

    private fun plant(
        id: String,
        persianName: String,
        category: String = "گیاه",
        scientificName: String = id
    ) = Plant(
        id = id,
        persianName = persianName,
        scientificName = scientificName,
        family = "آزمایشی",
        category = category,
        description = "توضیح نمونه",
        habitat = "زیستگاه نمونه",
        care = "نگهداری نمونه",
        traditionalUses = "کاربرد سنتی نمونه",
        evidence = "شواهد نمونه",
        cautions = "هشدار نمونه",
        interactions = "تداخلات نمونه"
    )
}
