package com.studio1111.giyahnameh.data

import java.time.LocalDate

data class PlantArticle(
    val plantId: String,
    val overview: String,
    val identification: String,
    val varieties: String,
    val origin: String,
    val practicalUses: String,
    val traditionalUses: String,
    val evidence: String,
    val preparation: String,
    val dosage: String,
    val sideEffects: String,
    val contraindications: String,
    val interactions: String,
    val specialPopulations: String,
    val cultivation: String,
    val propagationHarvestStorage: String,
    val faqs: List<PlantQuestion>,
    val references: List<PlantReference>,
    val reviewDate: String,
    val images: List<PlantImage> = emptyList()
) {
    val wordCount: Int
        get() = listOf(
            overview, identification, varieties, origin, practicalUses, traditionalUses,
            evidence, preparation, dosage, sideEffects, contraindications, interactions,
            specialPopulations, cultivation, propagationHarvestStorage
        ).joinToString(" ").trim().split(Regex("\\s+")).count { it.isNotBlank() } +
            faqs.sumOf { (it.question + " " + it.answer).trim().split(Regex("\\s+")).count(String::isNotBlank) }

    fun missingRequiredSections(): List<String> = buildList {
        val sections = listOf(
            "معرفی" to overview,
            "شناسایی ظاهری" to identification,
            "گونه‌ها و شباهت‌ها" to varieties,
            "خاستگاه و زیستگاه" to origin,
            "کاربردهای روزمره" to practicalUses,
            "کاربردهای سنتی" to traditionalUses,
            "شواهد علمی" to evidence,
            "روش استفاده" to preparation,
            "دوز و حدود دانسته‌ها" to dosage,
            "عوارض" to sideEffects,
            "موارد احتیاط یا منع مصرف" to contraindications,
            "تداخلات" to interactions,
            "گروه‌های خاص" to specialPopulations,
            "کاشت و نگهداری" to cultivation,
            "تکثیر، برداشت و نگهداری" to propagationHarvestStorage
        )
        sections.filter { it.second.isBlank() }.forEach { add(it.first) }
        if (faqs.size < 3 || faqs.any { it.question.isBlank() || it.answer.isBlank() }) {
            add("پرسش‌های متداول")
        }
        if (references.size < 3) add("حداقل سه منبع مستقیم")
        if (references.any { it.title.isBlank() || it.relevanceNote.isBlank() || !it.url.startsWith("https://") }) {
            add("منابع مستقیم با توضیح ارتباط")
        }
        if (runCatching { LocalDate.parse(reviewDate) }.isFailure) add("تاریخ بازبینی معتبر")
        if (wordCount < 350) add("مقالهٔ اختصاصی با جزئیات کافی")
    }

    fun imageMetadataIssues(): List<String> = buildList {
        val mainImages = images.filter { it.role == PlantImageRole.MAIN }
        if (mainImages.isEmpty()) {
            add("تصویر اصلی آفلاین")
        } else {
            mainImages.forEach { image ->
                if (!image.assetPath.startsWith("plants/") || image.assetPath.contains("..")) add("مسیر محلی امن تصویر")
                if (image.altTextFa.isBlank()) add("متن جایگزین فارسی تصویر")
                if (image.creator.isBlank()) add("نام صاحب اثر")
                if (image.license.isBlank() || !image.licenseVerified) add("مجوز تأییدشدهٔ تصویر")
                if (!image.sourcePageUrl.startsWith("https://")) add("صفحهٔ منبع تصویر")
            }
        }
    }

    fun publicationIssues(): List<String> =
        (missingRequiredSections() + imageMetadataIssues()).distinct()
}

data class PlantQuestion(
    val question: String,
    val answer: String
)

data class PlantReference(
    val title: String,
    val url: String,
    val sourceType: String,
    val relevanceNote: String
)

enum class PlantImageRole {
    MAIN,
    LEAF,
    FLOWER,
    FRUIT,
    ROOT,
    VARIETY,
    HABITAT
}

data class PlantImage(
    val assetPath: String,
    val altTextFa: String,
    val creator: String,
    val license: String,
    val sourcePageUrl: String,
    val licenseVerified: Boolean,
    val role: PlantImageRole = PlantImageRole.MAIN
)
