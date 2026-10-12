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
import androidx.compose.ui.layout.ContentScale
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
            PlantPhoto(plant = plant, modifier = Modifier.size(104.dp).clip(RoundedCornerShape(17.dp)), emoji = "🌿")
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
        PlantPhoto(plant = plant, modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)), emoji = "🌱", large = true)
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
        InfoSection("۱. معرفی و ویژگی‌های گیاه", plant.description)
        InfoSection("۲. نام علمی، خانواده و شناسایی درست", "نام علمی ثبت‌شده برای این گیاه «${plant.scientificName}» و خانوادهٔ گیاهی آن «${plant.family}» است. نام‌های محلی گاهی برای چند گیاه متفاوت به کار می‌روند؛ بنابراین برای شناسایی مطمئن، تنها به نام فارسی یا عکس اکتفا نکنید. شکل برگ، ساقه، گل، میوه، بوی گیاه و محل رویش باید در کنار هم بررسی شوند. اگر قرار است گیاه برای خوردن یا مصرف دارویی استفاده شود، در صورت تردید از کارشناس گیاه‌شناسی یا داروساز کمک بگیرید.")
        InfoSection("۳. زیستگاه و پراکندگی", plant.habitat)
        InfoSection("۴. شرایط رشد و نگهداری", plant.care + "\n\nبرای نگهداری بهتر، تغییرات گیاه را در طول زمان بررسی کنید: وضعیت برگ‌ها، رشد تازه، زهکشی خاک و نشانه‌های آفت یا پوسیدگی را ببینید. نیاز واقعی گیاه به نور و آب به اقلیم، فصل، گلدان و بافت خاک هم وابسته است؛ برنامهٔ آبیاری ثابت برای همهٔ گیاهان مناسب نیست. از ماندن آب در زیرگلدانی و تغییر ناگهانی شرایط محیطی پرهیز کنید.")
        InfoSection("۵. بخش‌های مورد استفاده و کاربردهای سنتی", plant.traditionalUses + "\n\nدر منابع گیاهان دارویی، «کاربرد سنتی» به معنای استفاده‌ای است که در یک فرهنگ یا نظام درمانی رواج داشته؛ این عبارت به‌تنهایی ثابت نمی‌کند که آن کاربرد مؤثر یا بی‌خطر است. برگ، گل، ریشه، پوست، دانه و اسانس یک گیاه می‌توانند ترکیب و خطرهای متفاوتی داشته باشند. اطلاعات مربوط به یک بخش گیاه یا یک فرآورده را نباید خودکار به بخش‌ها و فرآورده‌های دیگر تعمیم داد.")
        InfoSection("۶. روش استفاده و آماده‌سازی", "روش مصرف باید دقیقاً با گونه، بخش گیاه و فرآوردهٔ مورد بررسی هماهنگ باشد. در پروندهٔ فعلی، اطلاعات مستند و اختصاصی دربارهٔ مقدار مصرف، مدت مصرف و روش آماده‌سازی برای همهٔ کاربردها ثبت نشده است؛ به همین دلیل مقدار یا دستور ساخت خانگی پیشنهاد نمی‌شود. دمنوش، پودر، عصاره، کپسول، ژل موضعی و اسانس غلیظ از نظر قدرت و ایمنی یکسان نیستند. برای مصرف دارویی، دستور فرآوردهٔ معتبر و نظر پزشک یا داروساز را ملاک قرار دهید.")
        InfoSection("۷. شواهد علمی و میزان اطمینان", plant.evidence + "\n\nوجود ترکیبات شیمیایی یا نتیجهٔ یک آزمایش آزمایشگاهی، به‌تنهایی اثربخشی در انسان را ثابت نمی‌کند. ارزیابی علمی به نوع مطالعه، تعداد شرکت‌کنندگان، کیفیت فرآورده، مقدار مصرف و تکرار نتایج وابسته است. نتیجهٔ مربوط به یک گونه یا محصول را نمی‌توان بدون بررسی به گونهٔ مشابه یا محصول دیگر تعمیم داد.")
        InfoSection("۸. عوارض، هشدارها و زمان مراجعه", plant.cautions + "\n\nدر صورت بروز علائم شدید یا سریع مانند دشواری تنفس، تورم زبان یا گلو، غش، تشنج، استفراغ مداوم یا واکنش حساسیتی گسترده، مصرف را متوقف کنید و فوراً کمک پزشکی بگیرید. اگر پس از تماس یا مصرف گیاه علائم غیرعادی ایجاد شد، نام گیاه و شکل فرآورده را نگه دارید تا بررسی آن آسان‌تر شود.", warning = true)
        InfoSection("۹. تداخلات دارویی", plant.interactions + "\n\nتداخل فقط به قرص‌ها محدود نیست و می‌تواند با مکمل‌ها، گیاهان دیگر و داروهای بیهوشی یا آرام‌بخش هم مطرح شود. پیش از مصرف دارویی، فهرست داروها و مکمل‌های خود را با پزشک یا داروساز مرور کنید؛ به‌ویژه اگر داروی منظم مصرف می‌کنید یا قرار است جراحی انجام دهید.", warning = true)
        InfoSection("۱۰. کودکان، بارداری، شیردهی و سالمندان", "ایمنی این گیاه برای همهٔ گروه‌های حساس از روی اطلاعات عمومی قابل نتیجه‌گیری نیست. کودکان ممکن است نسبت به مقدار یکسان از فرآورده حساس‌تر باشند؛ در بارداری و شیردهی نیز داده‌های ایمنی بسیاری از گیاهان و مکمل‌ها ناکافی است. سالمندان و افراد دارای بیماری کبدی، کلیوی، قلبی یا چند داروی هم‌زمان نیز باید احتیاط بیشتری داشته باشند. تا زمانی که منبع معتبر و اختصاصی، ایمنی و روش مصرف را تأیید نکرده است، مصرف دارویی یا اسانس غلیظ را خودسرانه آغاز نکنید.", warning = true)
        InfoSection("۱۱. نگهداری و کیفیت فرآورده", "برای گیاه خوراکی یا دارویی، هویت گونه و کیفیت محصول اهمیت زیادی دارد. از گیاه ناشناس، کپک‌زده، آلوده یا جمع‌آوری‌شده از کنار جاده و محل‌های آلوده استفاده نکنید. فرآورده را طبق برچسب و دور از دسترس کودکان نگه دارید. خشک‌کردن یا نگهداری نامناسب می‌تواند کیفیت گیاه را تغییر دهد و ظاهر سالم به‌تنهایی تضمین‌کنندهٔ ایمنی نیست.")
        InfoSection("۱۲. پرسش‌های پرتکرار", "آیا این گیاه درمان قطعی بیماری است؟ خیر؛ در این پرونده فقط سطح شواهد ذکرشده ملاک است و ادعای درمان قطعی مطرح نمی‌شود.\n\nآیا مصرف طبیعی همیشه بی‌خطر است؟ خیر؛ گیاهان نیز می‌توانند عارضه، حساسیت یا تداخل دارویی داشته باشند.\n\nآیا می‌توان مقدار مصرف را از روی تجربهٔ دیگران تعیین کرد؟ خیر؛ گونه، بخش گیاه، غلظت فرآورده، سن و وضعیت سلامت افراد متفاوت است.\n\nچطور از درستی شناسایی مطمئن شوم؟ نام علمی، ویژگی‌های ظاهری و منبع معتبر را با هم بررسی کنید و در صورت تردید از متخصص کمک بگیرید.")
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
private fun PlantPhoto(plant: Plant, modifier: Modifier, emoji: String, large: Boolean = false) {
    var useRemoteFallback by remember(plant.id, plant.imageUrl, plant.imageLocalPath) { mutableStateOf(false) }
    val localFile = plant.imageLocalPath.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.isFile && it.length() > 0L }
    val remoteUrl = plant.imageUrl.takeIf { it.startsWith("https://") }
    val imageModel: Any? = if (!useRemoteFallback && localFile != null) localFile else remoteUrl
    Box(modifier.background(Leaf), contentAlignment = Alignment.Center) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = "تصویر ${plant.persianName}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    if (!useRemoteFallback && localFile != null && remoteUrl != null) useRemoteFallback = true
                    else useRemoteFallback = true
                },
                onSuccess = { }
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(emoji, style = if (large) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displaySmall)
                if (large) Text("برای این گیاه هنوز عکس قابل‌نمایشی پیدا نشده است؛ با اتصال اینترنت دوباره تلاش می‌شود.", color = ForestDeep, style = MaterialTheme.typography.bodySmall)
            }
        }
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
