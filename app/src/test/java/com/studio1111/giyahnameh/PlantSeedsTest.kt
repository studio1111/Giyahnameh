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

    @Test fun aromaticAndTraditionalPlantMonographsIncludeSpecificEvidenceAndWarnings() {
        val ids = setOf("passiflora-incarnata", "melaleuca-alternifolia", "citrus-aurantium", "gymnema-sylvestre")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertTrue(articles.all {
            it.description.length >= 300 &&
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


    @Test fun coldClimateAndTraditionalRootMonographsHaveSpecificSafetySections() {
        val ids = setOf("rhodiola-rosea", "angelica-sinensis", "eleutherococcus-senticosus", "equisetum-arvense")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertTrue(articles.all {
            it.description.length >= 300 &&
                it.habitat.length >= 150 &&
                it.care.length >= 250 &&
                it.traditionalUses.length >= 200 &&
                it.evidence.length >= 200 &&
                it.cautions.length >= 200 &&
                it.interactions.length >= 120 &&
                it.partsUsed.length >= 120 &&
                it.preparation.length >= 120 &&
                it.specialPopulations.length >= 120 &&
                it.bibliography.contains("https://")
        })
        assertTrue(articles.first { it.id == "rhodiola-rosea" }.bibliography.contains("nccih.nih.gov/health/rhodiola"))
        assertTrue(articles.first { it.id == "angelica-sinensis" }.interactions.contains("وارفارین"))
        assertTrue(articles.first { it.id == "equisetum-arvense" }.cautions.contains("کلیه"))
    }


    @Test fun additionalTraditionalChineseMonographsDocumentInteractionsAndLimits() {
        val ids = setOf("salvia-miltiorrhiza", "cinnamomum-camphora", "scutellaria-baicalensis", "coptis-chinensis")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertTrue(articles.all {
            it.description.length >= 300 &&
                it.care.length >= 250 &&
                it.traditionalUses.length >= 200 &&
                it.evidence.length >= 200 &&
                it.cautions.length >= 200 &&
                it.interactions.length >= 120 &&
                it.partsUsed.length >= 120 &&
                it.preparation.length >= 120 &&
                it.specialPopulations.length >= 120 &&
                it.bibliography.contains("https://")
        })
        assertTrue(articles.first { it.id == "salvia-miltiorrhiza" }.interactions.contains("وارفارین"))
        assertTrue(articles.first { it.id == "cinnamomum-camphora" }.cautions.contains("مسمومیت"))
        assertTrue(articles.first { it.id == "coptis-chinensis" }.interactions.contains("تداخل"))
    }


    @Test fun gingerTurmericGarlicAndSaffronArticlesSeparateFoodFromSupplements() {
        val ids = setOf("zingiber-officinale", "curcuma-longa", "allium-sativum", "crocus-sativus")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map { PlantMonographOverrides.apply(it) }
        assertTrue(articles.all { it.description.length >= 300 && it.care.length >= 250 && it.traditionalUses.length >= 150 && it.evidence.length >= 150 && it.cautions.length >= 150 && it.interactions.length >= 100 && it.partsUsed.length >= 100 && it.preparation.length >= 100 && it.specialPopulations.length >= 100 && it.faq.length >= 80 && it.bibliography.contains("https://") })
        assertTrue(articles.first { it.id == "allium-sativum" }.interactions.contains("ضدانعقاد"))
        assertTrue(articles.first { it.id == "curcuma-longa" }.cautions.contains("آسیب کبدی"))
        assertTrue(articles.first { it.id == "crocus-sativus" }.cautions.contains("مسمومیت"))
    }

    @Test fun coreHerbMonographsHaveAllRequiredArticleSections() {
        val ids = setOf("lavandula-angustifolia", "mentha-spicata", "aloe-vera", "matricaria-chamomilla")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map { PlantMonographOverrides.apply(it) }
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 && it.care.length >= 250 &&
                it.traditionalUses.length >= 150 && it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 && it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 && it.faq.length >= 80 && it.bibliography.contains("https://")
        })
        assertTrue(articles.first { it.id == "aloe-vera" }.cautions.contains("لاتکس"))
        assertTrue(articles.first { it.id == "matricaria-chamomilla" }.cautions.contains("کاسنیان"))
    }

    @Test fun ashwagandhaGinsengSennaAndPsylliumMonographsMeetSafetyChecks() {
        val ids = setOf("withania-somnifera", "panax-ginseng", "senna-alexandrina", "plantago-ovata")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map { PlantMonographOverrides.apply(it) }
        assertTrue(articles.all {
            it.description.length >= 300 &&
                it.habitat.length >= 150 &&
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
        assertTrue(articles.first { it.id == "withania-somnifera" }.cautions.contains("آسیب کبدی"))
        assertTrue(articles.first { it.id == "senna-alexandrina" }.cautions.contains("پتاسیم"))
        assertTrue(articles.first { it.id == "plantago-ovata" }.cautions.contains("خفگی"))
        assertTrue(articles.first { it.id == "panax-ginseng" }.bibliography.contains("nccih.nih.gov/health/asian-ginseng"))
    }


    @Test fun commonHerbMonographsHaveFullSectionsAndSpecificSafetySources() {
        val ids = setOf("lavandula-angustifolia", "matricaria-chamomilla", "aloe-vera", "salvia-rosmarinus", "zingiber-officinale")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map { PlantMonographOverrides.apply(it) }
        assertTrue(articles.all {
            it.description.length >= 400 &&
                it.habitat.length >= 150 &&
                it.care.length >= 300 &&
                it.traditionalUses.length >= 200 &&
                it.evidence.length >= 200 &&
                it.cautions.length >= 200 &&
                it.interactions.length >= 120 &&
                it.partsUsed.length >= 150 &&
                it.preparation.length >= 150 &&
                it.specialPopulations.length >= 120 &&
                it.faq.length >= 120 &&
                it.bibliography.contains("https://")
        })
        assertTrue(articles.first { it.id == "aloe-vera" }.cautions.contains("لاتکس"))
        assertTrue(articles.first { it.id == "matricaria-chamomilla" }.interactions.contains("وارفارین"))
        assertTrue(articles.first { it.id == "zingiber-officinale" }.bibliography.contains("nccih.nih.gov/health/ginger"))
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
            it.description.length >= 300 &&
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

    @Test fun eightCulinaryHerbMonographsHaveSpeciesSourcesAndSafetySections() {
        val ids = setOf(
            "salvia-officinalis", "thymus-vulgaris", "origanum-vulgare",
            "mentha-piperita", "cymbopogon-citratus", "ocimum-basilicum",
            "coriandrum-sativum", "cuminum-cyminum"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 &&
                it.habitat.length >= 150 &&
                it.care.length >= 250 &&
                it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 &&
                it.cautions.length >= 150 &&
                it.interactions.length >= 100 &&
                it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 &&
                it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "mentha-piperita" }.bibliography.contains("nccih.nih.gov"))
        assertTrue(articles.first { it.id == "salvia-officinalis" }.cautions.contains("توجون"))
        assertTrue(articles.first { it.id == "cuminum-cyminum" }.partsUsed.contains("میوه خشک"))
    }

    @Test fun seedOilAndBeveragePlantMonographsHaveSpecificSourcesAndSafety() {
        val ids = setOf(
            "foeniculum-vulgare", "trigonella-foenum-graecum", "nigella-sativa",
            "allium-cepa", "piper-nigrum", "camellia-sinensis",
            "sesamum-indicum", "linum-usitatissimum"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 &&
                it.habitat.length >= 150 &&
                it.care.length >= 250 &&
                it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 &&
                it.cautions.length >= 150 &&
                it.interactions.length >= 100 &&
                it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 &&
                it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "trigonella-foenum-graecum" }.cautions.contains("قند خون"))
        assertTrue(articles.first { it.id == "piper-nigrum" }.interactions.contains("پیپرین"))
        assertTrue(articles.first { it.id == "camellia-sinensis" }.cautions.contains("کافئین"))
        assertTrue(articles.first { it.id == "linum-usitatissimum" }.cautions.contains("بلع"))
    }

    @Test fun stapleFoodAndTropicalMedicinalPlantMonographsHaveSafetySections() {
        val ids = setOf(
            "oryza-sativa", "triticum-aestivum", "mangifera-indica", "psidium-guajava",
            "punica-granatum", "moringa-oleifera", "eucalyptus-globulus", "azadirachta-indica"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 &&
                it.habitat.length >= 150 &&
                it.care.length >= 250 &&
                it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 &&
                it.cautions.length >= 150 &&
                it.interactions.length >= 100 &&
                it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 &&
                it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 &&
                it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "eucalyptus-globulus" }.cautions.contains("مسمومیت"))
        assertTrue(articles.first { it.id == "azadirachta-indica" }.cautions.contains("نخورید"))
        assertTrue(articles.first { it.id == "triticum-aestivum" }.cautions.contains("گلوتن"))
        assertTrue(articles.first { it.id == "oryza-sativa" }.cautions.contains("یخچال"))
    }

    @Test fun additionalEuropeanHerbsHaveDetailedMonographsAndPlantSpecificSafety() {
        val ids = setOf(
            "melissa-officinalis", "artemisia-dracunculus", "tanacetum-parthenium",
            "origanum-majorana", "satureja-hortensis", "hyssopus-officinalis",
            "nepeta-cataria", "borago-officinalis"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "hyssopus-officinalis" }.cautions.contains("تشنج"))
        assertTrue(articles.first { it.id == "borago-officinalis" }.cautions.contains("کبدی"))
        assertTrue(articles.first { it.id == "tanacetum-parthenium" }.cautions.contains("خون‌ریزی"))
    }

    @Test fun eightAdditionalMedicinalAndTraditionalPlantsHaveSafetyAndSources() {
        val ids = setOf(
            "tilia-cordata", "humulus-lupulus", "oenothera-biennis",
            "primula-veris", "filipendula-ulmaria", "arnica-montana",
            "gentiana-lutea", "lawsonia-inermis"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "arnica-montana" }.cautions.contains("سمی"))
        assertTrue(articles.first { it.id == "lawsonia-inermis" }.cautions.contains("حساسیت"))
        assertTrue(articles.first { it.id == "filipendula-ulmaria" }.interactions.contains("ضدانعقاد"))
    }

    @Test fun eightOrnamentalTreeMonographsExplainEcologyAndSafeUse() {
        val ids = setOf(
            "quercus-robur", "betula-pendula", "acer-palmatum", "magnolia-grandiflora",
            "osmanthus-fragrans", "pinus-wallichiana", "picea-abies", "abies-alba"
        )
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "acer-palmatum" }.traditionalUses.contains("زینتی"))
        assertTrue(articles.first { it.id == "quercus-robur" }.cautions.contains("تانن"))
        assertTrue(articles.first { it.id == "abies-alba" }.description.contains("مخروط"))
    }

    @Test fun citrusFruitMonographsHaveDetailedSectionsAndWikipediaSources() {
        val ids = setOf("citrus-paradisi", "citrus-clementina")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "citrus-paradisi" }.interactions.contains("دارو"))
        assertTrue(articles.first { it.id == "citrus-clementina" }.description.contains("کلمنتین"))
    }

    @Test fun oilAndSugarCropMonographsHaveDetailedSectionsAndWikipediaSources() {
        val ids = setOf("camellia-oleifera", "saccharum-officinarum")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "saccharum-officinarum" }.cautions.contains("قند"))
        assertTrue(articles.first { it.id == "camellia-oleifera" }.partsUsed.contains("بذر"))
    }

    @Test fun wildPepperAndBananaMonographsHaveSpecificIdentityAndSafety() {
        val ids = setOf("piper-chaba", "musa-balbisiana")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "piper-chaba" }.cautions.contains("اسانس"))
        assertTrue(articles.first { it.id == "musa-balbisiana" }.description.contains("دانه"))
    }

    @Test fun fourTraditionalRootHerbsHaveSafetyAndSourceSections() {
        val ids = setOf("rehmannia-glutinosa", "paeonia-lactiflora", "bupleurum-chinense", "codonopsis-pilosula")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "bupleurum-chinense" }.cautions.contains("کبدی"))
    }

    @Test fun fourAsianMedicinalPlantsHaveDetailedSafetyAndSources() {
        val ids = setOf("tribulus-terrestris", "gynostemma-pentaphyllum", "sophora-flavescens", "astragalus-membranaceus")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "sophora-flavescens" }.cautions.contains("آلکالوئید"))
        assertTrue(articles.first { it.id == "astragalus-membranaceus" }.cautions.contains("حساسیتی"))
    }

    @Test fun fourFoodAndOrnamentalAsianPlantsHaveDetailedMonographs() {
        val ids = setOf("houttuynia-cordata", "pueraria-montana-var-lobata", "camellia-japonica", "sesbania-grandiflora")
        val originals = allSeedPlants.filter { it.id in ids }
        assertEquals(ids, originals.map { it.id }.toSet())
        val articles = originals.map(PlantMonographOverrides::apply)
        assertEquals(ids, PlantMonographOverrides.coveredSpecies().intersect(ids))
        assertTrue(articles.all {
            it.description.length >= 300 && it.habitat.length >= 150 &&
                it.care.length >= 250 && it.traditionalUses.length >= 150 &&
                it.evidence.length >= 150 && it.cautions.length >= 150 &&
                it.interactions.length >= 100 && it.partsUsed.length >= 100 &&
                it.preparation.length >= 100 && it.specialPopulations.length >= 100 &&
                it.faq.length >= 80 && it.bibliography.contains("wikipedia.org") &&
                it.sourceUrl.startsWith("https://")
        })
        assertTrue(articles.first { it.id == "pueraria-montana-var-lobata" }.cautions.contains("مهاجم"))
        assertTrue(articles.first { it.id == "camellia-japonica" }.description.contains("زینتی"))
    }
}
