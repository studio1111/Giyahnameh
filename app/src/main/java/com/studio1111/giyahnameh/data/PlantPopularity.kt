package com.studio1111.giyahnameh.data

/**
 * A transparent editorial estimate, not a claim about measured Google search volume.
 * Scores are deliberately separated from plant facts so that the ranking can be reviewed and updated.
 */
object PlantPopularity {
    const val label = "محبوبیت تخمینی"
    const val explanation =
        "برآورد نسبی بر پایهٔ رواج عمومی، کاربرد خوراکی/روزمره و شناخته‌شدن گیاه است؛ آمار جست‌وجوی واقعی برای همهٔ ۵۰۰ گیاه در دسترس و قابل‌مقایسه نیست."

    private val curatedScores = mapOf(
        "mentha-spicata" to 100,
        "aloe-vera" to 99,
        "crocus-sativus" to 98,
        "zingiber-officinale" to 97,
        "curcuma-longa" to 96,
        "rosa-damascena" to 95,
        "nigella-sativa" to 94,
        "matricaria-chamomilla" to 93,
        "mentha-piperita" to 92,
        "allium-sativum" to 91,
        "citrus-limon" to 90,
        "ocimum-basilicum" to 89,
        "camellia-sinensis" to 88,
        "thymus-vulgaris" to 87,
        "salvia-rosmarinus" to 86,
        "punica-granatum" to 85,
        "foeniculum-vulgare" to 84,
        "coriandrum-sativum" to 83,
        "cuminum-cyminum" to 82,
        "panax-ginseng" to 81,
        "glycyrrhiza-glabra" to 80,
        "plantago-ovata" to 79,
        "senna-alexandrina" to 78,
        "allium-cepa" to 77,
        "cinnamomum-verum" to 76,
        "syzygium-aromaticum" to 75,
        "piper-nigrum" to 74,
        "elettaria-cardamomum" to 73,
        "citrus-sinensis" to 72,
        "phoenix-dactylifera" to 71,
        "vitis-vinifera" to 70,
        "mangifera-indica" to 69,
        "malus-domestica" to 68,
        "olea-europaea" to 67,
        "coffea-arabica" to 66,
        "theobroma-cacao" to 65,
        "psidium-guajava" to 64,
        "moringa-oleifera" to 63,
        "withania-somnifera" to 62,
        "trigonella-foenum-graecum" to 61,
        "eucalyptus-globulus" to 60,
        "pistacia-vera" to 59,
        "juglans-regia" to 58,
        "ficus-carica" to 57,
        "cocos-nucifera" to 56,
        "musa-acuminata" to 55,
        "cicer-arietinum" to 54,
        "sesamum-indicum" to 53,
        "olea-europaea" to 52,
        "cymbopogon-citratus" to 51
    )

    fun estimatedScore(plant: Plant): Int = curatedScores[plant.id] ?: categoryEstimate(plant.category)

    private fun categoryEstimate(category: String): Int {
        val value = category.lowercase()
        return when {
            listOf("خوراک", "غذایی", "ادویه", "سبزی", "میوه", "غله", "حبوبات").any(value::contains) -> 48
            listOf("دارویی", "گیاه دارو", "درمانی").any(value::contains) -> 42
            listOf("عطری", "اسانس", "معطر").any(value::contains) -> 34
            listOf("زینتی", "آپارتمانی", "باغچه").any(value::contains) -> 25
            else -> 10
        }
    }
}
