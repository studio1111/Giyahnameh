package com.studio1111.giyahnameh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.studio1111.giyahnameh.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Forest = Color(0xFF214C38)
private val ForestDeep = Color(0xFF153A2B)
private val Leaf = Color(0xFFDDE9D6)
private val Paper = Color(0xFFF6F5EE)
private val Muted = Color(0xFF66776B)
private val WarningBg = Color(0xFFFFF0E8)
private val WarningInk = Color(0xFF8A351D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PlantRepository(PlantDatabase.get(applicationContext).plantDao())
        ImageEnrichmentWorker.schedule(applicationContext)
        setContent {
            val vm: PlantViewModel = viewModel(factory = PlantViewModel.Factory(repository))
            GiyahTheme { GiyahApp(vm) }
        }
    }
}

class PlantViewModel(private val repo: PlantRepository) : ViewModel() {
    var query by mutableStateOf("")
        private set
    var selectedId by mutableStateOf<String?>(null)
        private set
    var showFavorites by mutableStateOf(false)
        private set
    var selectedCategory by mutableStateOf("همه")
        private set
    var sortMode by mutableStateOf(PlantSortMode.ALPHABETICAL)
        private set
    private val queryFlow = kotlinx.coroutines.flow.MutableStateFlow("")
    val plants = queryFlow.flatMapLatest { repo.plants(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites = repo.favorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { viewModelScope.launch { repo.seedIfNeeded() } }

    fun search(value: String) {
        query = value
        queryFlow.value = value
        showFavorites = false
        selectedCategory = "همه"
    }
    fun category(value: String) {
        selectedCategory = value
        query = ""
        queryFlow.value = ""
        showFavorites = false
    }
    fun changeSortMode(value: PlantSortMode) {
        sortMode = value
    }
    fun open(id: String) { selectedId = id }
    fun back() { selectedId = null }
    fun toggleFavorites() {
        showFavorites = !showFavorites
        query = ""
        queryFlow.value = ""
        selectedCategory = "همه"
    }
    fun plant(id: String) = repo.plant(id)
    fun toggleFavorite(plant: Plant) { viewModelScope.launch { repo.favorite(plant.id, !plant.isFavorite) } }

    class Factory(private val repo: PlantRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlantViewModel(repo) as T
    }
}

@Composable
private fun GiyahTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Forest,
                onPrimary = Color.White,
                secondary = Color(0xFF587B50),
                background = Paper,
                surface = Color.White,
                onSurface = Color(0xFF202820),
                surfaceVariant = Leaf
            ),
            typography = Typography(),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GiyahApp(vm: PlantViewModel) {
    val context = LocalContext.current
    val imageCredits = remember(context) { PlantImageAssetCatalog.loadVerifiedCredits(context) }
    val baseList by (if (vm.showFavorites) vm.favorites else vm.plants).collectAsState()
    val list = remember(baseList, vm.selectedCategory, vm.sortMode) {
        val filtered = if (vm.selectedCategory == "همه") baseList
        else baseList.filter { it.category.contains(vm.selectedCategory, ignoreCase = true) }
        PlantOrdering.sort(filtered, vm.sortMode)
    }
    val selected = vm.selectedId
    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(38.dp).clip(CircleShape).background(Forest), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(if (selected == null) "گیاه‌نامه" else "پرونده گیاه", fontWeight = FontWeight.Bold)
                            if (selected == null) Text("دانشنامه گیاهان جهان", style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                    }
                },
                navigationIcon = {
                    if (selected != null) IconButton(onClick = vm::back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (selected == null) IconButton(onClick = vm::toggleFavorites) {
                        Icon(if (vm.showFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "علاقه‌مندی‌ها", tint = Forest)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        }
    ) { padding ->
        if (selected != null) {
            val plant by remember(selected) { vm.plant(selected) }.collectAsState(initial = null)
            if (plant != null) PlantDetails(
                plant = plant!!,
                localImageAvailable = plant!!.id in imageCredits,
                imageCredit = imageCredits[plant!!.id],
                modifier = Modifier.padding(padding),
                onFavorite = vm::toggleFavorite
            )
            else Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (!vm.showFavorites && vm.query.isBlank() && vm.selectedCategory == "همه") {
                    WelcomePanel()
                }
                OutlinedTextField(
                    value = vm.query,
                    onValueChange = vm::search,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    placeholder = { Text("جست‌وجوی نام فارسی یا علمی") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = Color(0xFFD8DED5),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("همه", "دارویی", "خوراکی", "زینتی").forEach { category ->
                        CategoryChip(category, vm.selectedCategory == category) { vm.category(category) }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ترتیب نمایش", color = Muted, style = MaterialTheme.typography.labelMedium)
                    FilterChip(
                        selected = vm.sortMode == PlantSortMode.ALPHABETICAL,
                        onClick = { vm.changeSortMode(PlantSortMode.ALPHABETICAL) },
                        label = { Text("الفبایی") }
                    )
                    FilterChip(
                        selected = vm.sortMode == PlantSortMode.POPULARITY,
                        onClick = { vm.changeSortMode(PlantSortMode.POPULARITY) },
                        label = { Text(PlantPopularity.label) }
                    )
                }
                if (vm.sortMode == PlantSortMode.POPULARITY) {
                    Text(
                        PlantPopularity.explanation,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted
                    )
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 18.dp, end = 16.dp, top = 18.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (vm.showFavorites) "گیاهان ذخیره‌شده" else if (vm.query.isNotBlank()) "نتایج جست‌وجو" else "گیاهان را کشف کن",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestDeep
                        )
                        Text("${list.size} پرونده گیاهی", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Text("آفلاین‌خوان", style = MaterialTheme.typography.labelSmall, color = Forest,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Leaf).padding(horizontal = 10.dp, vertical = 6.dp))
                }
                if (list.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🌱", style = MaterialTheme.typography.displayMedium)
                            Text("هنوز گیاهی پیدا نشد", fontWeight = FontWeight.Bold, color = Forest)
                            Text("نام دیگری را جست‌وجو کنید یا دسته‌بندی را تغییر دهید.", color = Muted)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list, key = { it.id }) { plant ->
                            PlantCard(
                                plant = plant,
                                localImageAvailable = plant.id in imageCredits,
                                onClick = { vm.open(plant.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePanel() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(ForestDeep).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("از ریشه تا شناخت", color = Color(0xFFCFE2C4), style = MaterialTheme.typography.labelMedium)
            Text("جهان سبز را\nورق بزن", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("شناخت علمی، روش نگهداری و نکات ایمنی", color = Color(0xFFE0E8DD), style = MaterialTheme.typography.bodySmall)
        }
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFF315C43)), contentAlignment = Alignment.Center) {
            Text("🌿", style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Forest else Color.White,
        contentColor = if (selected) Color.White else Forest,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2D8))
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PlantCard(plant: Plant, localImageAvailable: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(104.dp).clip(RoundedCornerShape(17.dp)).background(Leaf), contentAlignment = Alignment.Center) {
                val imageModel = if (localImageAvailable) "file:///android_asset/plants/${plant.id}.webp" else plant.imageUrl
                if (imageModel.isNotBlank()) {
                    AsyncImage(model = imageModel, contentDescription = plant.persianName, modifier = Modifier.fillMaxSize())
                } else {
                    Text("🌿", style = MaterialTheme.typography.displaySmall)
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(plant.persianName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestDeep)
                Text(plant.scientificName, style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    plant.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Forest,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Leaf).padding(horizontal = 9.dp, vertical = 4.dp)
                )
                Text(plant.description, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = Color(0xFF46544A))
            }
        }
    }
}

@Composable
private fun PlantDetails(
    plant: Plant,
    localImageAvailable: Boolean,
    imageCredit: PlantImageCredit?,
    modifier: Modifier,
    onFavorite: (Plant) -> Unit
) {
    val article = PlantArticleCatalog.find(plant.id)
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)).background(Leaf), contentAlignment = Alignment.Center) {
            val imageModel = if (localImageAvailable) "file:///android_asset/plants/${plant.id}.webp" else plant.imageUrl
            if (imageModel.isNotBlank()) {
                AsyncImage(model = imageModel, contentDescription = plant.persianName, modifier = Modifier.fillMaxSize())
            } else {
                Text("🌱", style = MaterialTheme.typography.displayLarge)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(plant.persianName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ForestDeep)
                Text(plant.scientificName, style = MaterialTheme.typography.titleMedium, color = Forest)
                Text("خانواده: ${plant.family}", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(plant.category, style = MaterialTheme.typography.labelMedium, color = Forest)
            }
            IconButton(onClick = { onFavorite(plant) }) {
                Icon(if (plant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "ذخیره در علاقه‌مندی‌ها", tint = Forest)
            }
        }
        if (article != null) {
            InfoSection("معرفی کوتاه", article.overview)
            InfoSection("شناسایی و ویژگی‌های گیاه", article.identification)
            InfoSection("گونه‌ها و تفاوت با گیاهان مشابه", article.varieties)
            InfoSection("خاستگاه و پراکندگی", article.origin)
            InfoSection("کاربردهای روزمره", article.practicalUses)
            InfoSection("کاربردهای سنتی", article.traditionalUses)
            InfoSection("شواهد علمی و محدودیت دانسته‌ها", article.evidence)
            InfoSection("روش‌های استفاده", article.preparation)
            InfoSection("مقدار مصرف و حدود اطلاعات معتبر", article.dosage)
            InfoSection("عوارض احتمالی", article.sideEffects, warning = true)
            InfoSection("موارد احتیاط و منع مصرف", article.contraindications, warning = true)
            InfoSection("تداخلات دارویی", article.interactions, warning = true)
            InfoSection("توصیه برای بارداری، شیردهی، کودکان و بیماران", article.specialPopulations, warning = true)
            InfoSection("کاشت و نگهداری", article.cultivation)
            InfoSection("تکثیر، برداشت و نگهداری", article.propagationHarvestStorage)
            article.faqs.forEach { question ->
                InfoSection(question.question, question.answer)
            }
            Text("آخرین بازبینی محتوای این مقاله: ${article.reviewDate}", color = Muted, style = MaterialTheme.typography.labelSmall)
            Text("منابع بررسی‌شده", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestDeep)
            article.references.forEach { reference ->
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(reference.title, color = Forest, fontWeight = FontWeight.SemiBold)
                    Text(reference.sourceType, color = Muted, style = MaterialTheme.typography.labelSmall)
                    Text(reference.relevanceNote, color = Color(0xFF46544A), style = MaterialTheme.typography.bodySmall)
                    Text(reference.url, color = Forest, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (article.images.isEmpty() && !localImageAvailable) {
                Text(
                    "تصویر اختصاصی آفلاین این مقاله هنوز تأیید و بسته‌بندی نشده است.",
                    color = WarningInk,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            InfoSection("معرفی و ویژگی‌ها", plant.description)
            InfoSection("زیستگاه و پراکندگی", plant.habitat)
            InfoSection("پرورش و نگهداری", plant.care)
            InfoSection("کاربردهای سنتی و شکل‌های رایج", plant.traditionalUses)
            InfoSection("شواهد علمی و محدودیت دانسته‌ها", plant.evidence)
            InfoSection("عوارض و هشدارهای ایمنی", plant.cautions, warning = true)
            InfoSection("تداخلات دارویی و احتیاط‌ها", plant.interactions, warning = true)
        }
        if (imageCredit != null) {
            InfoSection(
                "اعتبار تصویر آفلاین",
                "پدیدآورنده/دارنده حقوق: ${imageCredit.creator}\nمجوز: ${imageCredit.license}\nمنبع: ${imageCredit.source}\nصفحهٔ اصلی: ${imageCredit.sourcePageUrl}"
            )
        } else if (plant.imageUrl.isNotBlank()) {
            InfoSection("اعتبار تصویر آنلاین", "دارنده حقوق: ${plant.imageAttribution}\nمجوز: ${plant.imageLicense}\nمنبع تصویر: ${plant.imageSourceUrl}")
        }
        if (plant.sourceUrl.isNotBlank()) {
            Text("منبع پیشنهادی برای مطالعه بیشتر", fontWeight = FontWeight.Bold, color = ForestDeep)
            Text(plant.sourceUrl, color = Forest, style = MaterialTheme.typography.bodySmall)
        }
        Card(colors = CardDefaults.cardColors(containerColor = WarningBg), shape = RoundedCornerShape(18.dp)) {
            Text(
                "اطلاعات این پرونده آموزشی است. گیاهان می‌توانند با داروها تداخل داشته باشند و جایگزین تشخیص یا درمان پزشکی نیستند.",
                modifier = Modifier.padding(16.dp), color = WarningInk, style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoSection(title: String, body: String, warning: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (warning) WarningBg else Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (warning) WarningInk else Forest)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E382F))
        }
    }
}
