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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import java.io.File
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
        val repository = PlantRepository(PlantDatabase.get(applicationContext).plantDao(), applicationContext)
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
    val baseList by (if (vm.showFavorites) vm.favorites else vm.plants).collectAsState()
    val list = remember(baseList, vm.selectedCategory) {
        if (vm.selectedCategory == "همه") baseList
        else baseList.filter { it.category.contains(vm.selectedCategory, ignoreCase = true) }
    }
    val selected = vm.selectedId
    Scaffold(
        containerColor = Paper,
        topBar = {
            if (selected != null) {
                TopAppBar(
                    title = { Text("پروندهٔ گیاه", fontWeight = FontWeight.Bold, color = Color(0xFF202820)) },
                    navigationIcon = {
                        IconButton(onClick = vm::back) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = ForestDeep)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper, titleContentColor = Color(0xFF202820))
                )
            }
        }
    ) { padding ->
        if (selected != null) {
            val plant by remember(selected) { vm.plant(selected) }.collectAsState(initial = null)
            if (plant != null) PlantDetails(plant!!, Modifier.padding(padding), vm::toggleFavorite)
            else Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(17.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8DED5)),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.height(58.dp).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(34.dp).clip(CircleShape).background(Forest), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Spa, contentDescription = "لوگوی گیاه‌نامه", tint = Color(0xFFDDE9D6), modifier = Modifier.size(22.dp))
                            }
                            Text("گیاه‌نامه", fontWeight = FontWeight.ExtraBold, color = ForestDeep, style = MaterialTheme.typography.titleSmall)
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = vm.query,
                                onValueChange = vm::search,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF202820),
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                ),
                                decorationBox = { inner ->
                                    Box {
                                        if (vm.query.isEmpty()) Text("جست‌وجوی گیاه...", color = Color(0xFF667067), fontWeight = FontWeight.Medium)
                                        inner()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Icon(Icons.Default.Search, contentDescription = "جست‌وجو", tint = ForestDeep, modifier = Modifier.size(23.dp))
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf("همه", "دارویی", "خوراکی", "زینتی").forEach { category ->
                        Box(Modifier.weight(1f)) {
                            CategoryChip(category, vm.selectedCategory == category) { vm.category(category) }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 18.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (vm.showFavorites) "گیاهان ذخیره‌شده" else if (vm.query.isNotBlank()) "نتایج جست‌وجو" else "همهٔ گیاهان",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF202820)
                    )
                    Spacer(Modifier.weight(1f))
                    Text("${list.size} گونه", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF465047))
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
                        items(list, key = { it.id }) { plant -> PlantCard(plant) { vm.open(plant.id) } }
                    }
                }
            }
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
        Text(label, modifier = Modifier.padding(horizontal = 7.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
    }
}

@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(104.dp).clip(RoundedCornerShape(17.dp)).background(Leaf), contentAlignment = Alignment.Center) {
                if (plant.imageUrl.isNotBlank()) {
                    AsyncImage(model = plant.imageLocalPath.takeIf { it.isNotBlank() }?.let(::File) ?: plant.imageUrl, contentDescription = plant.persianName, modifier = Modifier.fillMaxSize())
                } else {
                    Text("🌿", style = MaterialTheme.typography.displaySmall)
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(plant.persianName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF202820))
                Text(plant.scientificName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF465047), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    plant.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Forest,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Leaf).padding(horizontal = 9.dp, vertical = 4.dp)
                )
                Text(plant.description, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color(0xFF303830))
            }
        }
    }
}

@Composable
private fun PlantDetails(plant: Plant, modifier: Modifier, onFavorite: (Plant) -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)).background(Leaf), contentAlignment = Alignment.Center) {
            if (plant.imageUrl.isNotBlank()) {
                AsyncImage(model = plant.imageLocalPath.takeIf { it.isNotBlank() }?.let(::File) ?: plant.imageUrl, contentDescription = plant.persianName, modifier = Modifier.fillMaxSize())
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
        InfoSection("معرفی و ویژگی‌ها", plant.description)
        InfoSection("زیستگاه و پراکندگی", plant.habitat)
        InfoSection("پرورش و نگهداری", plant.care)
        InfoSection("کاربردهای سنتی و شکل‌های رایج", plant.traditionalUses)
        InfoSection("شواهد علمی و محدودیت دانسته‌ها", plant.evidence)
        InfoSection("عوارض و هشدارهای ایمنی", plant.cautions, warning = true)
        InfoSection("تداخلات دارویی و احتیاط‌ها", plant.interactions, warning = true)
        if (plant.imageUrl.isNotBlank()) {
            InfoSection("اعتبار تصویر", "دارنده حقوق: ${plant.imageAttribution}\nمجوز: ${plant.imageLicense}\nمنبع تصویر: ${plant.imageSourceUrl}")
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
            Text(title, fontWeight = FontWeight.Bold, color = if (warning) WarningInk else Color(0xFF202820))
            Text(body, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = Color(0xFF202820))
        }
    }
}
