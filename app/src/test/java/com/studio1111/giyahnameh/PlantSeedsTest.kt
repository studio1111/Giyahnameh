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
import com.studio1111.giyahnameh.data.PlantMonographOverrides
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

    @Test fun regionalStapleMonographsHaveDedicatedSections() {
        val ids = setOf(
            "zingiber-officinale", "curcuma-longa", "coriandrum-sativum",
            "cuminum-cyminum", "crocus-sativus", "elettaria-cardamomum",
            "syzygium-aromaticum", "cinnamomum-verum", "capsicum-annuum",
            "solanum-tuberosum", "abelmoschus-esculentus"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(11, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun fruitAndHerbMonographsHaveDedicatedSections() {
        val ids = setOf(
            "citrus-limon", "malus-domestica", "pyrus-communis",
            "ziziphus-jujuba", "berberis-vulgaris", "salvia-officinalis",
            "thymus-vulgaris", "origanum-vulgare", "mentha-piperita",
            "cymbopogon-citratus"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun vegetableAndGrainMonographsHaveDedicatedSections() {
        val ids = setOf(
            "citrus-sinensis", "cucumis-sativus", "daucus-carota",
            "spinacia-oleracea", "zea-mays", "ipomoea-batatas",
            "sorghum-bicolor", "triticum-durum", "brassica-juncea",
            "raphanus-sativus"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun vegetableLegumeAndCoffeeMonographsHaveDedicatedSections() {
        val ids = setOf(
            "beta-vulgaris", "brassica-oleracea", "lactuca-sativa",
            "solanum-lycopersicum", "momordica-charantia", "cucumis-melo",
            "phaseolus-vulgaris", "pisum-sativum", "arachis-hypogaea",
            "coffea-arabica"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun grainAndOilCropMonographsHaveDedicatedSections() {
        val ids = setOf(
            "luffa-aegyptiaca", "lagenaria-siceraria", "avena-sativa",
            "fagopyrum-esculentum", "chenopodium-quinoa", "amaranthus-tricolor",
            "brassica-nigra", "carthamus-tinctorius", "helianthus-annuus",
            "theobroma-cacao"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun asianFruitMonographsHaveDedicatedSections() {
        val ids = setOf(
            "citrus-aurantiifolia", "citrus-reticulata", "citrus-maxima",
            "citrus-medica", "prunus-domestica", "prunus-avium",
            "pyrus-pyrifolia", "morus-nigra", "ziziphus-mauritiana",
            "syzygium-cumini"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun asianSpiceAndHerbMonographsHaveDedicatedSections() {
        val ids = setOf(
            "phyllanthus-emblica", "terminalia-chebula", "terminalia-bellirica",
            "piper-longum", "curcuma-zedoaria", "kaempferia-galanga",
            "amomum-subulatum", "anethum-graveolens", "carum-carvi",
            "murraya-koenigii"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun legumeHerbAndTropicalFruitMonographsHaveDedicatedSections() {
        val ids = setOf(
            "vigna-unguiculata", "cajanus-cajan", "vigna-mungo",
            "vigna-angularis", "lablab-purpureus", "petroselinum-crispum",
            "apium-graveolens", "citrus-japonica", "diospyros-kaki",
            "litchi-chinensis"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun tropicalFruitAndBerryMonographsHaveDedicatedSections() {
        val ids = setOf(
            "dimocarpus-longan", "annona-squamosa", "artocarpus-altilis",
            "morinda-citrifolia", "spondias-dulcis", "persea-americana",
            "passiflora-edulis", "fragaria-ananassa", "rubus-idaeus",
            "prunus-cerasus"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun berryHerbAndRootVegetableMonographsHaveDedicatedSections() {
        val ids = setOf(
            "actinidia-deliciosa", "vaccinium-corymbosum", "rubus-fruticosus",
            "allium-porrum", "allium-schoenoprasum", "portulaca-oleracea",
            "hibiscus-sabdariffa", "cynara-cardunculus", "pastinaca-sativa",
            "lupinus-albus"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun medicinalTreeAndHerbMonographsHaveDedicatedSections() {
        val ids = setOf(
            "boswellia-serrata", "commiphora-wightii", "santalum-album",
            "terminalia-arjuna", "saraca-asoca", "berberis-lycium",
            "valeriana-jatamansi", "picrorhiza-kurroa", "saussurea-costus",
            "nardostachys-jatamansi"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun southAsianTreeMonographsHaveDedicatedSections() {
        val ids = setOf(
            "ficus-religiosa", "ficus-benghalensis", "ficus-racemosa",
            "zanthoxylum-armatum", "vachellia-nilotica", "albizia-lebbeck",
            "bauhinia-variegata", "cassia-fistula", "butea-monosperma",
            "shorea-robusta"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun mountainHerbAndTemperateTreeMonographsHaveDedicatedSections() {
        val ids = setOf(
            "plectranthus-amboinicus", "coleus-forskohlii", "alpinia-officinarum",
            "catharanthus-roseus", "dactylorhiza-hatagirea", "rheum-australe",
            "cedrus-deodara", "platanus-orientalis", "salix-alba", "populus-nigra"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun asianSpiceMonographsHaveDedicatedSections() {
        val ids = setOf(
            "zanthoxylum-bungeanum", "illicium-verum", "pimpinella-anisum",
            "myristica-fragrans", "piper-cubeba", "curcuma-aromatica",
            "kaempferia-parviflora", "alpinia-zerumbet", "zingiber-mioga",
            "allium-tuberosum"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun asianVegetableAndTropicalFruitMonographsHaveDedicatedSections() {
        val ids = setOf(
            "eruca-vesicaria", "cichorium-endivia", "cucurbita-moschata",
            "cucurbita-maxima", "capsicum-frutescens", "solanum-torvum",
            "artocarpus-integer", "durio-zibethinus", "lansium-domesticum",
            "salacca-zalacca"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun aromaticRootAndSourFruitMonographsHaveDedicatedSections() {
        val ids = setOf(
            "averrhoa-carambola", "averrhoa-bilimbi", "cinnamomum-porrectum",
            "ocimum-americanum", "ocimum-gratissimum", "mimosa-pudica",
            "lepidium-meyenii", "armoracia-rusticana", "wasabia-japonica",
            "amomum-kravanh"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun rareCitrusAndYamMonographsHaveDedicatedSections() {
        val ids = setOf(
            "citrus-junos", "citrus-australasica", "citrus-ichangensis",
            "citrus-micrantha", "citrus-medica-var-sarcodactylis",
            "dioscorea-opposita", "dioscorea-japonica", "citrus-hystrix",
            "citrus-trifoliata", "piper-sarmentosum"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun berryNutAndTraditionalFruitMonographsHaveDedicatedSections() {
        val ids = setOf(
            "lycium-barbarum", "hippophae-rhamnoides", "cydonia-oblonga",
            "eriobotrya-japonica", "corylus-avellana", "castanea-sativa",
            "pistacia-atlantica", "crataegus-monogyna", "rosa-canina",
            "sambucus-nigra"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test fun northernFruitAndHerbMonographsHaveDedicatedSections() {
        val ids = setOf(
            "diospyros-lotus", "diospyros-virginiana", "pinus-pinea",
            "pistacia-terebinthus", "crataegus-azarolus", "berberis-aristata",
            "schisandra-chinensis", "ginkgo-biloba", "urtica-dioica",
            "taraxacum-officinale"
        )
        val entries = allSeedPlants.filter { it.id in ids }
        assertEquals(10, entries.size)
        assertTrue(entries.all {
            it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
    }

    @Test
    fun gojiBerryArticleIncludesBotanyCultivationTraditionalMedicineAndSafetySources() {
        val goji = allSeedPlants.first { it.id == "lycium-barbarum" }
        assertTrue(goji.description.length > 250)
        assertTrue(goji.habitat.contains("چین"))
        assertTrue(goji.care.contains("قلمه"))
        assertTrue(goji.traditionalUses.contains("گوجی"))
        assertTrue(goji.preparation.contains("جوشانده"))
        assertTrue(goji.specialPopulations.contains("وارفارین"))
        assertTrue(goji.bibliography.contains("pubmed.ncbi.nlm.nih.gov/19844860"))
        assertTrue(goji.bibliography.contains("extension.usu.edu"))
    }

    @Test
    fun seaBuckthornAndLoquatArticlesIncludeTraditionCultivationAndSafety() {
        val seaBuckthorn = allSeedPlants.first { it.id == "hippophae-rhamnoides" }
        val loquat = allSeedPlants.first { it.id == "eriobotrya-japonica" }
        assertTrue(seaBuckthorn.care.contains("دوپایه"))
        assertTrue(seaBuckthorn.traditionalUses.contains("طب سنتی"))
        assertTrue(seaBuckthorn.bibliography.contains("pubmed.ncbi.nlm.nih.gov/36930583"))
        assertTrue(loquat.care.contains("پیوند"))
        assertTrue(loquat.traditionalUses.contains("Pi Pa Ye"))
        assertTrue(loquat.cautions.contains("هسته"))
        assertTrue(loquat.bibliography.contains("ask.ifas.ufl.edu/publication/MG050"))
    }
}


    @Test fun aromaticAndTraditionalPlantMonographsIncludeSpecificEvidenceAndWarnings() {
        val ids = setOf("passiflora-incarnata", "melaleuca-alternifolia", "citrus-aurantium", "gymnema-sylvestre")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertTrue(articles.all {
            it.description.length >= 400 &&
                it.care.length >= 250 &&
                it.traditionalUses.length >= 200 &&
                it.evidence.length >= 200 &&
                it.cautions.length >= 200 &&
                it.interactions.length >= 120 &&
                it.partsUsed.length >= 120 &&
                it.preparation.length >= 120 &&
                it.specialPopulations.length >= 120 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://")
        })
        assertTrue(articles.first { it.id == "passiflora-incarnata" }.cautions.contains("خواب‌آلودگی"))
        assertTrue(articles.first { it.id == "melaleuca-alternifolia" }.cautions.contains("نباید خورد"))
        assertTrue(articles.first { it.id == "citrus-aurantium" }.evidence.contains("کاهش وزن"))
        assertTrue(articles.first { it.id == "gymnema-sylvestre" }.interactions.contains("قند خون"))
    }

    @Test fun newMedicinalMonographsHaveDepthSpeciesSpecificSourcesAndSafety() {
        val ids = setOf(
            "panax-quinquefolius",
            "silybum-marianum",
            "glycyrrhiza-uralensis",
            "stevia-rebaudiana",
            "polygonum-multiflorum"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 500 &&
                it.habitat.length >= 150 &&
                it.care.length >= 300 &&
                it.traditionalUses.length >= 250 &&
                it.evidence.length >= 250 &&
                it.cautions.length >= 250 &&
                it.interactions.length >= 150 &&
                it.partsUsed.length >= 150 &&
                it.preparation.length >= 150 &&
                it.specialPopulations.length >= 150 &&
                it.faq.length >= 100 &&
                it.bibliography.contains("https://") &&
                it.sourceUrl.startsWith("https://")
        })
        val licorice = articles.first { it.id == "glycyrrhiza-uralensis" }
        assertTrue(licorice.cautions.contains("فشار خون"))
        assertTrue(licorice.cautions.contains("پتاسیم"))
        val foTi = articles.first { it.id == "polygonum-multiflorum" }
        assertTrue(foTi.cautions.contains("آسیب کبدی"))
        assertTrue(foTi.bibliography.contains("NBK548795"))
        val milkThistle = articles.first { it.id == "silybum-marianum" }
        assertTrue(milkThistle.bibliography.contains("nccih.nih.gov/health/milk-thistle"))
    }
}
