package com.studio1111.giyahnameh.data

import java.text.Normalizer
import java.util.Locale

enum class PlantSortMode {
    ALPHABETICAL,
    POPULARITY
}

/** Shared deterministic ordering for the plant list and filtered views. */
object PlantOrdering {
    private val persianAlphabet = listOf(
        'ا', 'ب', 'پ', 'ت', 'ث', 'ج', 'چ', 'ح', 'خ', 'د', 'ذ', 'ر', 'ز', 'ژ',
        'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف', 'ق', 'ک', 'گ', 'ل', 'م',
        'ن', 'و', 'ه', 'ی'
    )

    fun sort(plants: List<Plant>, mode: PlantSortMode): List<Plant> = when (mode) {
        PlantSortMode.ALPHABETICAL -> plants.sortedWith(Comparator(::compareAlphabetically))
        PlantSortMode.POPULARITY -> plants.sortedWith { left, right ->
            val score = PlantPopularity.estimatedScore(right).compareTo(PlantPopularity.estimatedScore(left))
            if (score != 0) score else compareAlphabetically(left, right)
        }
    }

    private fun compareAlphabetically(left: Plant, right: Plant): Int {
        val nameComparison = comparePersianText(left.persianName, right.persianName)
        if (nameComparison != 0) return nameComparison

        val scientificComparison = normalize(left.scientificName).compareTo(normalize(right.scientificName))
        if (scientificComparison != 0) return scientificComparison

        return left.id.compareTo(right.id)
    }

    private fun comparePersianText(left: String, right: String): Int {
        val a = normalize(left)
        val b = normalize(right)
        val limit = minOf(a.length, b.length)

        for (index in 0 until limit) {
            val difference = characterWeight(a[index]).compareTo(characterWeight(b[index]))
            if (difference != 0) return difference
        }
        return a.length.compareTo(b.length)
    }

    private fun characterWeight(character: Char): Int {
        if (character == ' ') return -1
        val index = persianAlphabet.indexOf(character)
        return if (index >= 0) index else persianAlphabet.size + character.code
    }

    private fun normalize(value: String): String {
        val canonical = Normalizer.normalize(value, Normalizer.Form.NFC)
            .lowercase(Locale.ROOT)
            .replace('ي', 'ی')
            .replace('ى', 'ی')
            .replace('ك', 'ک')
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('\u200c', ' ')
            .replace('\u200d', ' ')
        return buildString(canonical.length) {
            for (character in canonical) {
                val type = Character.getType(character)
                if (type != Character.NON_SPACING_MARK.toInt() &&
                    type != Character.COMBINING_SPACING_MARK.toInt() &&
                    type != Character.ENCLOSING_MARK.toInt()
                ) {
                    append(character)
                }
            }
        }.trim().replace(Regex("\\s+"), " ")
    }
}
