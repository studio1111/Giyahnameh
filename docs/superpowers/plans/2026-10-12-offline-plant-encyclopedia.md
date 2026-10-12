# Offline Plant Encyclopedia Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or implement the tasks sequentially with the native session workflow. Steps use checkbox syntax for tracking.

**Goal:** Turn Giyahnameh's 500-record catalogue into a Persian offline-first encyclopedia with rich, source-grounded articles, offline plant images, alphabetic and popularity ordering, and an installable debug APK.

**Architecture:** Preserve Room for the existing catalogue and user favourites. Store long-form articles and their source/image metadata as versioned bundled assets keyed by stable plant ID, so existing installations receive improved content without overwriting favourites. Add a pure Kotlin ordering module and render structured articles in Compose. Package legally reusable, correctly identified images locally; networking remains optional.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Kotlin Flow, WorkManager, Coil, JUnit 4, GitHub Actions.

**Spec:** docs/superpowers/specs/2026-10-12-offline-plant-encyclopedia-design.md

## Global Constraints

- Exactly 500 distinct plants must remain in the catalogue.
- UI copy is fluent Persian and right-to-left; use accepted scientific Latin names where needed.
- Do not assert treatment efficacy, dose, contraindication, or interaction without species- and preparation-relevant evidence.
- Every published article must have direct references and a review date.
- The main image for every published plant must be bundled locally with verified reuse terms and attribution.
- Preserve existing Room favourites and existing catalogue records; do not rely on network availability for article text or main images.
- No paid cloud service, external secret, or API key may be required for baseline use.
- Do not claim final release readiness until full tests, content validation, offline checks, and CI APK build succeed.

## Review Focus

- Arabic/Persian yeh and kaf variants, diacritics, and Persian alphabet ordering → covered by PlantOrderingTest.
- Missing/unknown popularity score and equal scores → covered by PlantOrderingTest.
- Existing Room rows created before the feature → avoid schema change for article assets; existing database data remains untouched.
- Missing article asset or broken image metadata → covered by article asset validation and tested UI fallback.
- Distinguishing Mentha spicata from Mentha × piperita and food leaf from concentrated oil → covered by the first researched article and source-specific safety review.
- Empty search, category changes, favourites, and sort toggle → covered by ViewModel/UI logic tests or regression review.

---

### Task 1: Define and test Persian ordering before implementation

**Files:**
- Create: app/src/test/java/com/studio1111/giyahnameh/data/PlantOrderingTest.kt
- Create later: app/src/main/java/com/studio1111/giyahnameh/data/PlantOrdering.kt

**Interfaces:**
- Produces: PlantOrdering.sort(plants: List<Plant>, mode: PlantSortMode): List<Plant>
- Produces: PlantSortMode.ALPHABETICAL and PlantSortMode.POPULARITY

- [ ] Write tests for Persian alphabet ordering, Arabic ي/ك normalization, stable tie-breaking, and popularity ranking of explicitly curated common plants.
- [ ] Run gradle --no-daemon :app:testDebugUnitTest in CI and confirm the tests fail for the missing ordering implementation.
- [ ] Implement the minimum pure Kotlin ordering logic.
- [ ] Re-run the focused and complete unit-test suite and verify both pass.

### Task 2: Add an explicit popularity model without fabricating search counts

**Files:**
- Modify: app/src/main/java/com/studio1111/giyahnameh/data/PlantOrdering.kt
- Create: app/src/main/java/com/studio1111/giyahnameh/data/PlantPopularity.kt
- Test: app/src/test/java/com/studio1111/giyahnameh/data/PlantOrderingTest.kt

**Interfaces:**
- Produces: PlantPopularity.estimatedScore(plant: Plant): Int
- Produces: PlantPopularity.label explaining that the relative ranking is an estimate unless comparable search data are explicitly documented.

- [ ] Add a curated list of high-recognition/common-use plants, verify each ID against the existing 500 records, and use a deterministic category-informed fallback for plants not yet scored.
- [ ] Test that popularity mode sorts highest scores first, ties alphabetically, and remains deterministic.
- [ ] Clearly label the mode as estimated popularity; never show invented query-volume numbers.

### Task 3: Define the structured offline article model and source links

**Files:**
- Create: app/src/main/java/com/studio1111/giyahnameh/data/PlantArticle.kt
- Create: app/src/main/java/com/studio1111/giyahnameh/data/PlantArticleCatalog.kt
- Create: app/src/test/java/com/studio1111/giyahnameh/PlantArticleCatalogTest.kt

**Interfaces:**
- Produces: PlantArticle, PlantReference, and PlantQuestion.
- Produces: PlantArticleCatalog.find(plantId: String): PlantArticle? and PlantArticleCatalog.curatedArticles.

- [ ] Define fields for identification, morphology, varieties, origin/distribution, practical uses, traditional use, evidence level, preparations, evidence-backed dosage when available, adverse effects, contraindications, medicine interactions, special populations, cultivation/propagation/harvest/storage, FAQs, references, review date, and local image metadata.
- [ ] Write validation tests that reject blank required sections, non-HTTPS references, missing review dates, and unverified image/license metadata.
- [ ] Add an in-depth Persian article for Mentha spicata using Kew for taxonomy, RHS for cultivation, and suitable Persian academic sources where relevant. Use NCCIH/EMA only to distinguish evidence and precautions for Mentha × piperita; never misattribute peppermint-oil trial results to spearmint.
- [ ] Keep legacy plant records renderable while unresearched articles are being filled; do not pad them with generic medical claims.

### Task 4: Add ordering controls and render complete structured articles

**Files:**
- Modify: app/src/main/java/com/studio1111/giyahnameh/MainActivity.kt
- Test: app/src/test/java/com/studio1111/giyahnameh/data/PlantOrderingTest.kt

- [ ] Add a two-mode Persian selector for «الفبایی» and «محبوبیت تخمینی» without losing the current query, selected category, favourites, or selected plant state.
- [ ] Sort after applying the active search/category/favourites source; keep the current default alphabetic order.
- [ ] Add dedicated article sections for preparation/use, evidence, contraindications, side effects, special populations, cultivation and FAQs, with medical warnings visually distinct from ordinary information.
- [ ] Display direct reference links and reviewed date; retain legacy sections as fallback for entries not yet migrated to the detailed article catalogue.
- [ ] Review RTL layout, long paragraphs, small-screen scrolling, and missing data fallbacks.

### Task 5: Grow article coverage in researchable batches

**Files:**
- Create: app/src/main/java/com/studio1111/giyahnameh/data/articles/PlantArticlesBatch01.kt through additional numbered batch files.
- Test: app/src/test/java/com/studio1111/giyahnameh/PlantArticleCatalogTest.kt

- [ ] Organize the 500 records into ten reviewable batches of 50 plant IDs, preserving stable IDs and preventing duplicates.
- [ ] For every plant, research botanical identity and horticultural needs from specialist sources; search Persian academic, university, pharmacology and botanical resources in parallel.
- [ ] For medically used plants, build a source-to-claim trail and separately document evidence from human studies, animal studies, laboratory studies, and traditional use. If a reliable therapeutic dose is unavailable, explicitly say so.
- [ ] Write natural, plant-specific Persian articles. A plant not used medicinally must clearly say that no well-established therapeutic use is established rather than acquiring generic filler.
- [ ] After each batch, run article coverage and content-quality tests; do not mark the catalogue complete until all 500 records pass.

### Task 6: Add licensed local image assets and offline image resolution

**Files:**
- Create: app/src/main/assets/plants/ and image manifest metadata.
- Modify: app/src/main/java/com/studio1111/giyahnameh/data/PlantArticle.kt
- Modify: app/src/main/java/com/studio1111/giyahnameh/MainActivity.kt
- Test: offline image manifest validation.

- [ ] Select at least one image of the correct species for every plant. Add more images for important forms, varieties, or plant parts when they materially aid identification.
- [ ] Verify original source page, scientific identification, creator/rights holder, license and license conditions for each image; exclude reuse-restricted imagery.
- [ ] Store attribution/license/source and Persian alt text alongside the local WebP asset.
- [ ] Load bundled assets first and keep remote enrichment as optional fallback only.
- [ ] Test that all 500 manifest paths exist and a plant's main image can load without network access.

### Task 7: Complete regression, catalogue QA, and build validation

**Files:**
- Modify: app/src/test/java/com/studio1111/giyahnameh/PlantSeedsTest.kt
- Modify: .github/workflows/android-ci.yml only if necessary.
- Modify: README.md to reflect the actual state, not planned features.

- [ ] Test exactly 500 distinct IDs and scientific names after deduplication rules.
- [ ] Add coverage checks for all 500 articles, direct references, required safety sections, reviewed dates, and local image files.
- [ ] Add UI/data regression checks for search, categories, favourites, and stable ties.
- [ ] Run gradle --no-daemon :app:testDebugUnitTest and gradle --no-daemon :app:assembleDebug through GitHub Actions.
- [ ] Inspect CI logs and the uploaded APK artifact. Only report an installable APK after a successful artifact build; note that physical-device behavior still needs device testing.

### Task 8: Final review and delivery

- [ ] Review the branch diff for unrelated changes, hard-coded network requirements, medical overclaims, incomplete sources, missing attribution, and regressions.
- [ ] Verify every acceptance criterion in the design spec.
- [ ] Produce a PR and use CI results as the gate; do not merge or label a release-ready build while article/image coverage is incomplete.
- [ ] Provide the user with the PR/CI APK link and a plain-language status separating completed work from remaining tasks.
