package com.studio1111.giyahnameh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

private val Forest = Color(0xFF1F4D3A)
private val Leaf = Color(0xFFDAE8D0)
private val Paper = Color(0xFFF7F5ED)

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
    private val queryFlow = kotlinx.coroutines.flow.MutableStateFlow("")
    val plants = queryFlow.flatMapLatest { repo.plants(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites = repo.favorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { viewModelScope.launch { repo.seedIfNeeded() } }
    fun search(value: String) { query = value; queryFlow.value = value; showFavorites = false }
    fun open(id: String) { selectedId = id }
    fun back() { selectedId = null }
    fun toggleFavorites() { showFavorites = !showFavorites; query = ""; queryFlow.value = "" }
    fun plant(id: String) = repo.plant(id)
    fun toggleFavorite(plant: Plant) { viewModelScope.launch { repo.favorite(plant.id, !plant.isFavorite) } }

    class Factory(private val repo: PlantRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlantViewModel(repo) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GiyahTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Forest, onPrimary = Color.White, secondary = Color(0xFF587B50), background = Paper, surface = Color.White, onSurface = Color(0xFF202820)), content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GiyahApp(vm: PlantViewModel) {
    val list by (if (vm.showFavorites) vm.favorites else vm.plants).collectAsState()
    val selected = vm.selectedId
    Scaffold(containerColor = Paper, topBar = {
        TopAppBar(
            title = { Column { Text(if (selected == null) "گیاه‌نامه" else "پرونده گیاه", fontWeight = FontWeight.Bold); if (selected == null) Text("دانشنامه گیاهان جهان", style = MaterialTheme.typography.labelSmall) } },
            navigationIcon = { if (selected != null) IconButton(onClick = vm::back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت") } },
            actions = { if (selected == null) IconButton(onClick = vm::toggleFavorites) { Icon(Icons.Filled.Favorite, contentDescription = "علاقه‌مندی‌ها") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
        )
    }) { padding ->
        if (selected != null) {
            val plant by remember(selected) { vm.plant(selected) }.collectAsState(initial = null)
            if (plant != null) PlantDetails(plant!!, Modifier.padding(padding), vm::toggleFavorite)
            else Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(value = vm.query, onValueChange = vm::search, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("نام فارسی، نام علمی یا دسته‌بندی") }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, singleLine = true, shape = RoundedCornerShape(18.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip("همه گیاهان") { vm.search("") }
                    CategoryChip("دارویی") { vm.search("دارویی") }
                    CategoryChip("خوراکی") { vm.search("خوراکی") }
                    CategoryChip("زینتی") { vm.search("زینتی") }
                }
                Text(if (vm.showFavorites) "گیاهان ذخیره‌شده" else "کشف دنیای گیاهان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                if (list.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("هنوز موردی پیدا نشد. عبارت دیگری را جست‌وجو کنید.") }
                else LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list, key = { it.id }) { plant -> PlantCard(plant) { vm.open(plant.id) } }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = Leaf, contentColor = Forest) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (plant.imageUrl.isNotBlank()) AsyncImage(model = plant.imageUrl, contentDescription = plant.persianName, modifier = Modifier.size(94.dp).background(Leaf, RoundedCornerShape(14.dp)))
            else Box(Modifier.size(94.dp).background(Leaf, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("🌿", style = MaterialTheme.typography.headlineLarge) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(plant.persianName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(plant.scientificName, style = MaterialTheme.typography.bodySmall, color = Forest)
                Text(plant.category, style = MaterialTheme.typography.labelSmall, color = Color(0xFF617363))
                Text(plant.description, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PlantDetails(plant: Plant, modifier: Modifier, onFavorite: (Plant) -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (plant.imageUrl.isNotBlank()) AsyncImage(model = plant.imageUrl, contentDescription = plant.persianName, modifier = Modifier.fillMaxWidth().height(230.dp).background(Leaf, RoundedCornerShape(22.dp)))
        else Box(Modifier.fillMaxWidth().height(150.dp).background(Leaf, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) { Text("🌱", style = MaterialTheme.typography.displayLarge) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plant.persianName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(plant.scientificName, style = MaterialTheme.typography.titleMedium, color = Forest)
                Text("خانواده: ${plant.family}  •  ${plant.category}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onFavorite(plant) }) { Icon(if (plant.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "ذخیره در علاقه‌مندی‌ها", tint = Forest) }
        }
        InfoSection("معرفی و ویژگی‌ها", plant.description)
        InfoSection("زیستگاه و پراکندگی", plant.habitat)
        InfoSection("پرورش و نگهداری", plant.care)
        InfoSection("کاربردهای سنتی", plant.traditionalUses)
        InfoSection("شواهد علمی", plant.evidence)
        InfoSection("عوارض و هشدارها", plant.cautions, warning = true)
        InfoSection("تداخلات و احتیاط‌ها", plant.interactions, warning = true)
        if (plant.sourceUrl.isNotBlank()) { Text("منبع پیشنهادی برای مطالعه بیشتر", fontWeight = FontWeight.Bold); Text(plant.sourceUrl, color = Forest, style = MaterialTheme.typography.bodySmall) }
        Text("یادآوری: محتوای آموزشی است و جایگزین نظر پزشک یا داروساز نیست.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF76512A), modifier = Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun InfoSection(title: String, body: String, warning: Boolean = false) {
    Card(colors = CardDefaults.cardColors(containerColor = if (warning) Color(0xFFFFF0E8) else Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = if (warning) Color(0xFF8A351D) else Forest)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
