package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AnonymousComment
import com.example.data.LiveStock
import com.example.data.PortfolioStock
import com.example.data.WatchlistItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Colors for premium financial look (Bento Grid Theme)
val DeepDarkBlue = Color(0xFF0F1115)
val CardDarkBlue = Color(0xFF1B1B1F)
val BrightCyan = Color(0xFF0D9488) // Beautiful brand teal
val GainGreen = Color(0xFF10B981)   // Modern emerald green
val LossRed = Color(0xFFEF4444)    // Modern rose red
val SecondaryTextGray = Color(0xFF94A3B8) // Slate 400

val PureWhite = Color(0xFFE2E2E6) // Off-white for sleek dark mode high contrast
val PureBlack = Color(0xFF000000)
val LightBg = Color(0xFFF1F5F9)
val LightCard = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiniyogApp(viewModel: BiniyogViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val liveStocks by viewModel.liveStocks.collectAsState()
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val portfolioStocks by viewModel.portfolioStocks.collectAsState()
    val comments by viewModel.allComments.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()

    // Calculate current portfolio value dynamically for the header
    val totalCurrentValue = portfolioStocks.fold(0.0) { acc, item ->
        val liveItem = liveStocks.firstOrNull { it.symbol == item.symbol }
        val livePrice = liveItem?.currentPrice ?: item.buyPrice
        acc + (livePrice * item.shares)
    }
    val formattedAssetValue = String.format(Locale.US, "%,.2f", totalCurrentValue)

    // UI Styles based on Theme
    val backgroundColor = if (isDark) DeepDarkBlue else LightBg
    val cardColor = if (isDark) CardDarkBlue else LightCard
    val textColor = if (isDark) PureWhite else PureBlack
    val dividerColor = if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0)

    var currentTab by remember { mutableStateOf("market") }
    var showAddPortfolioDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrightCyan.copy(alpha = 0.15f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Asset wallet icon",
                                tint = BrightCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "আমার সম্পদ (পোর্টফোলিও)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryTextGray
                            )
                            Text(
                                text = "৳ $formattedAssetValue",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                        }
                    }
                },
                actions = {
                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme Switcher",
                            tint = if (isDark) Color(0xFFFFD54F) else Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) CardDarkBlue else LightCard,
                    titleContentColor = textColor
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) CardDarkBlue else LightCard,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                val tabs = listOf(
                    Triple("market", "বাজার", Icons.Default.Storefront),
                    Triple("portfolio", "পোর্টফোলিও", Icons.Default.AccountBalanceWallet),
                    Triple("compare", "তুলনা", Icons.Default.Compare),
                    Triple("guide", "শিক্ষা গাইড", Icons.Default.School),
                    Triple("guru", "এআই গুরু", Icons.Default.SmartToy)
                )
                tabs.forEach { (tabId, label, icon) ->
                    NavigationBarItem(
                        selected = currentTab == tabId,
                        onClick = { currentTab = tabId },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrightCyan,
                            selectedTextColor = BrightCyan,
                            indicatorColor = BrightCyan.copy(alpha = 0.15f),
                            unselectedIconColor = SecondaryTextGray,
                            unselectedTextColor = SecondaryTextGray
                        ),
                        modifier = Modifier.testTag("nav_tab_$tabId")
                    )
                }
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            // Main content based on selected tab
            when (currentTab) {
                "market" -> MarketTab(
                    viewModel = viewModel,
                    liveStocks = liveStocks,
                    watchlistItems = watchlistItems,
                    alerts = alerts,
                    textColor = textColor,
                    cardColor = cardColor,
                    dividerColor = dividerColor,
                    isDark = isDark
                )
                "portfolio" -> PortfolioTab(
                    viewModel = viewModel,
                    portfolioStocks = portfolioStocks,
                    liveStocks = liveStocks,
                    textColor = textColor,
                    cardColor = cardColor,
                    isDark = isDark,
                    onAddAssetClick = { showAddPortfolioDialog = true }
                )
                "compare" -> ComparisonTab(
                    viewModel = viewModel,
                    liveStocks = liveStocks,
                    textColor = textColor,
                    cardColor = cardColor,
                    dividerColor = dividerColor,
                    isDark = isDark
                )
                "guide" -> GuideTab(
                    textColor = textColor,
                    cardColor = cardColor,
                    isDark = isDark
                )
                "guru" -> AIAdvisorTab(
                    viewModel = viewModel,
                    textColor = textColor,
                    cardColor = cardColor,
                    isDark = isDark
                )
            }


            // No longer using the top-level floating NotificationAlertsOverlay to avoid disturbing the user.
            // Active alerts are now integrated into their own dedicated section inside the Market Tab.


            // Bottom drawer or detail dialog for selected stock
            selectedStock?.let { stock ->
                StockDetailBottomSheet(
                    stock = stock,
                    comments = comments.filter { it.companySymbol == stock.symbol },
                    isFav = watchlistItems.any { it.symbol == stock.symbol },
                    textColor = textColor,
                    cardColor = cardColor,
                    dividerColor = dividerColor,
                    isDark = isDark,
                    onDismiss = { viewModel.selectStock(null) },
                    onFavToggle = { viewModel.toggleWatchlist(stock) },
                    onAddComment = { text, rating, alias ->
                        viewModel.addAnonymousComment(stock.symbol, text, rating, alias)
                    }
                )
            }

            // Dialog for adding stock to portfolio
            if (showAddPortfolioDialog) {
                AddPortfolioStockDialog(
                    availableStocks = liveStocks,
                    onDismiss = { showAddPortfolioDialog = false },
                    onAdd = { symbol, shares, buyPrice, exchange ->
                        viewModel.addPortfolioStock(symbol, shares, buyPrice, exchange)
                        showAddPortfolioDialog = false
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. MARKET TAB (বাজার)
// ==========================================
@Composable
fun MarketTab(
    viewModel: BiniyogViewModel,
    liveStocks: List<LiveStock>,
    watchlistItems: List<WatchlistItem>,
    alerts: List<PriceAlert>,
    textColor: Color,
    cardColor: Color,
    dividerColor: Color,
    isDark: Boolean
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSector by viewModel.selectedSector.collectAsState()

    val sectors = listOf("All", "Telecommunication", "Pharmaceuticals", "Food & Allied", "Cement", "Banking", "Mutual Funds")
    val sectorsBangla = mapOf(
        "All" to "সব খাত",
        "Telecommunication" to "টেলিযোগাযোগ",
        "Pharmaceuticals" to "ওষুধ ও রসায়ন",
        "Food & Allied" to "খাদ্য ও আনুষঙ্গিক",
        "Cement" to "সিমেন্ট শিল্প",
        "Banking" to "ব্যাংক",
        "Mutual Funds" to "মিউচুয়াল ফান্ড"
    )

    val filteredStocks = liveStocks.filter { stock ->
        val matchesSearch = stock.symbol.contains(searchQuery, ignoreCase = true) ||
                stock.companyNameBangla.contains(searchQuery, ignoreCase = true) ||
                stock.companyName.contains(searchQuery, ignoreCase = true)
        val matchesSector = selectedSector == "All" || stock.sector == selectedSector
        matchesSearch && matchesSector
    }

    val indexChangePercent = liveStocks.map { it.changePercent }.average().takeIf { !it.isNaN() } ?: 1.2
    val indexValue = 6324.50 + (indexChangePercent * 10)
    val formattedIndexValue = String.format(Locale.US, "%,.2f", indexValue)
    val formattedIndexChange = String.format(Locale.US, "%+,.2f%%", indexChangePercent)
    val isIndexGain = indexChangePercent >= 0
    val trendColor = if (isIndexGain) GainGreen else LossRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Bento Ticker Widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardDarkBlue.copy(alpha = 0.6f) else LightCard),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(GainGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "লাইভ মার্কেট সূচক (DSE)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTextGray,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(GainGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "খোলা আছে",
                            fontSize = 10.sp,
                            color = GainGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = formattedIndexValue,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "$formattedIndexChange (লাইভ)",
                            fontSize = 13.sp,
                            color = trendColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(32.dp)
                    ) {
                        listOf(0.4f, 0.6f, 0.8f, 1.0f).forEach { heightFactor ->
                            val opacity = 0.3f + (0.7f * heightFactor)
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(28.dp * heightFactor)
                                    .background(
                                        color = trendColor.copy(alpha = opacity),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Inline Bento Alerts Section (Dedicated Non-disturbing section for live notifications)
        if (alerts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) CardDarkBlue.copy(alpha = 0.4f) else LightCard),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(LossRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "লাইভ অ্যালার্ট ও বাজার সংকেত (${alerts.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) BrightCyan else Color(0xFF0097A7)
                            )
                        }
                        
                        Text(
                            text = "সব মুছুন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTextGray,
                            modifier = Modifier
                                .clickable {
                                    alerts.forEach { viewModel.dismissAlert(it.id) }
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(alerts.take(5)) { alert ->
                            val alertColor = if (alert.type == AlertType.HIGH) GainGreen else LossRed
                            val bgAlertColor = if (alert.type == AlertType.HIGH) GainGreen.copy(alpha = 0.08f) else LossRed.copy(alpha = 0.08f)
                            
                            Card(
                                modifier = Modifier
                                    .width(260.dp)
                                    .border(1.dp, alertColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = bgAlertColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (alert.type == AlertType.HIGH) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = "Alert Trend",
                                            tint = alertColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = alert.message,
                                            color = textColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { viewModel.dismissAlert(alert.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss Alert",
                                            tint = SecondaryTextGray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val focusManager = LocalFocusManager.current

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("শেয়ার কোড বা কোম্পানির নাম খুঁজুন...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("stock_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = BrightCyan,
                unfocusedBorderColor = dividerColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Sector tabs list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filters",
                tint = SecondaryTextGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(sectors) { sector ->
                    val isSelected = sector == selectedSector
                    val sectorLabel = sectorsBangla[sector] ?: sector
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) BrightCyan else if (isDark) Color(0xFF252836) else Color(
                                    0xFFE0E0E0
                                )
                            )
                            .clickable { viewModel.updateSelectedSector(sector) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sectorLabel,
                            color = if (isSelected) PureBlack else textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Watchlist Quick Carousel
        val favStocks = liveStocks.filter { stock ->
            watchlistItems.any { it.symbol == stock.symbol }
        }
        if (favStocks.isNotEmpty()) {
            Text(
                text = "আপনার নজরে (Watchlist)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(favStocks) { stock ->
                    WatchlistCard(
                        stock = stock,
                        textColor = textColor,
                        cardColor = cardColor,
                        isDark = isDark,
                        onClick = { viewModel.selectStock(stock) }
                    )
                }
            }
        }

        // Active Stock list header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "শেয়ার কোড / খাত",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryTextGray
            )
            Text(
                text = "বর্তমান দর (পরিবর্তন)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryTextGray
            )
        }

        // Main Stock List
        if (filteredStocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো তথ্য পাওয়া যায়নি!",
                    color = SecondaryTextGray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("stock_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStocks, key = { it.symbol + it.exchange }) { stock ->
                    StockListItem(
                        stock = stock,
                        isFav = watchlistItems.any { it.symbol == stock.symbol },
                        textColor = textColor,
                        cardColor = cardColor,
                        isDark = isDark,
                        onFavClick = { viewModel.toggleWatchlist(stock) },
                        onClick = { viewModel.selectStock(stock) }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistCard(
    stock: LiveStock,
    textColor: Color,
    cardColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val changeColor = if (stock.changePercent >= 0) GainGreen else LossRed
    val formattedPrice = String.format("%.1f", stock.currentPrice)
    val formattedChange = String.format("%+.2f%%", stock.changePercent)

    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.symbol,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (stock.exchange == "DSE") Color(0xFF1E3A8A) else Color(0xFF065F46),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stock.exchange,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$formattedPrice ৳",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedChange,
                color = changeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StockListItem(
    stock: LiveStock,
    isFav: Boolean,
    textColor: Color,
    cardColor: Color,
    isDark: Boolean,
    onFavClick: () -> Unit,
    onClick: () -> Unit
) {
    val changeColor = if (stock.changePercent >= 0) GainGreen else LossRed
    val formattedPrice = String.format("%.1f", stock.currentPrice)
    val formattedChange = String.format("%+.2f%%", stock.changePercent)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onFavClick) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Add to watchlist",
                        tint = if (isFav) Color(0xFFFFB300) else SecondaryTextGray
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (stock.exchange == "DSE") Color(0x333B82F6) else Color(0x3310B981),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stock.exchange,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stock.exchange == "DSE") Color(0xFF60A5FA) else Color(0xFF34D399)
                            )
                        }
                    }
                    Text(
                        text = stock.companyNameBangla,
                        fontSize = 12.sp,
                        color = SecondaryTextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$formattedPrice ৳",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (stock.changePercent >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Price Change Trend",
                        tint = changeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = formattedChange,
                        color = changeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // AI Signal Tag
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(
                            when (stock.signal) {
                                "BUY" -> GainGreen.copy(alpha = 0.15f)
                                "SELL" -> LossRed.copy(alpha = 0.15f)
                                else -> textColor.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = stock.signalBangla,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (stock.signal) {
                            "BUY" -> GainGreen
                            "SELL" -> LossRed
                            else -> SecondaryTextGray
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. PORTFOLIO TAB (পোর্টফোলিও ট্র্যাকিং)
// ==========================================
@Composable
fun PortfolioTab(
    viewModel: BiniyogViewModel,
    portfolioStocks: List<PortfolioStock>,
    liveStocks: List<LiveStock>,
    textColor: Color,
    cardColor: Color,
    isDark: Boolean,
    onAddAssetClick: () -> Unit
) {
    // Math logic for Portfolio Tracker
    var totalInvested = 0.0
    var totalCurrentValue = 0.0

    portfolioStocks.forEach { item ->
        val liveItem = liveStocks.firstOrNull { it.symbol == item.symbol }
        val livePrice = liveItem?.currentPrice ?: item.buyPrice
        totalInvested += (item.buyPrice * item.shares)
        totalCurrentValue += (livePrice * item.shares)
    }

    val totalProfitLoss = totalCurrentValue - totalInvested
    val profitLossPercent = if (totalInvested > 0) (totalProfitLoss / totalInvested) * 100 else 0.0
    val profitColor = if (totalProfitLoss >= 0) GainGreen else LossRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "আপনার বিনিয়োগ পোর্টফোলিও",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "আপনার অলস অর্থকে কাজে লাগিয়ে পোর্টফোলিও ট্র্যাক করুন",
            fontSize = 12.sp,
            color = SecondaryTextGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Grand Total Card (Bento Style)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, BrightCyan.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BrightCyan, Color(0xFF0F766E))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "মোট পোর্টফোলিও মূল্য",
                        fontSize = 12.sp,
                        color = Color(0xFFCCFBF1),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", totalCurrentValue)} ৳",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "মোট বিনিয়োগ",
                                fontSize = 11.sp,
                                color = Color(0xFFCCFBF1),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,.2f", totalInvested)} ৳",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "মোট লাভ/ক্ষতি",
                                fontSize = 11.sp,
                                color = Color(0xFFCCFBF1),
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (totalProfitLoss >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "gain trend",
                                    tint = if (totalProfitLoss >= 0) Color(0xFF2DD4BF) else Color(0xFFFECDD3),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%+,.2f", totalProfitLoss)} ৳ (${String.format(Locale.US, "%+.2f%%", profitLossPercent)})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalProfitLoss >= 0) Color(0xFF2DD4BF) else Color(0xFFFECDD3)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "বিনিয়োগকৃত শেয়ারসমূহ (${portfolioStocks.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Button(
                onClick = onAddAssetClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrightCyan, contentColor = PureBlack),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_asset_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Portfolio stocks list
        if (portfolioStocks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Empty Wallet",
                        modifier = Modifier.size(48.dp),
                        tint = SecondaryTextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনার পোর্টফোলিওতে কোনো শেয়ার নেই!",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "উপরের 'যোগ করুন' বাটনে ক্লিক করে শেয়ার এবং ক্রয়মূল্য যুক্ত করুন।",
                        color = SecondaryTextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            portfolioStocks.forEach { stock ->
                val liveItem = liveStocks.firstOrNull { it.symbol == stock.symbol }
                val currentLivePrice = liveItem?.currentPrice ?: stock.buyPrice
                val totalBuyCost = stock.buyPrice * stock.shares
                val totalCurrentVal = currentLivePrice * stock.shares
                val gainLoss = totalCurrentVal - totalBuyCost
                val gainPercent = (gainLoss / totalBuyCost) * 100.0
                val itemGainColor = if (gainLoss >= 0) GainGreen else LossRed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stock.symbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x3300E5FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stock.exchange,
                                            fontSize = 8.sp,
                                            color = BrightCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${stock.shares} শেয়ার @ ${stock.buyPrice} ৳",
                                    fontSize = 12.sp,
                                    color = SecondaryTextGray
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format("%,.1f", totalCurrentVal)} ৳",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${String.format("%+,.1f", gainLoss)} (${String.format("%+.2f%%", gainPercent)})",
                                        color = itemGainColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = { viewModel.sellPortfolioStock(stock.id) },
                                    modifier = Modifier.testTag("delete_asset_${stock.symbol}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Sell Stock",
                                        tint = LossRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. COMPARISON TAB (কোম্পানি তুলনা ও ফি)
// ==========================================
@Composable
fun ComparisonTab(
    viewModel: BiniyogViewModel,
    liveStocks: List<LiveStock>,
    textColor: Color,
    cardColor: Color,
    dividerColor: Color,
    isDark: Boolean
) {
    var stockA by remember { mutableStateOf<LiveStock?>(null) }
    var stockB by remember { mutableStateOf<LiveStock?>(null) }

    var showSelectADialog by remember { mutableStateOf(false) }
    var showSelectBDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "লাইভ কোম্পানি ও মিউচুয়াল ফান্ড তুলনা",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "২টি কোম্পানি নির্বাচন করে পি/ই রেশিও, ডিভিডেন্ড এবং লুকানো চার্জ তুলনা করুন",
            fontSize = 12.sp,
            color = SecondaryTextGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Selectors Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Company A Selection Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showSelectADialog = true },
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "কোম্পানি ১", fontSize = 11.sp, color = SecondaryTextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stockA?.symbol ?: "নির্বাচন করুন ➕",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (stockA != null) BrightCyan else SecondaryTextGray,
                        textAlign = TextAlign.Center
                    )
                    stockA?.let {
                        Text(
                            text = it.companyNameBangla,
                            fontSize = 10.sp,
                            color = SecondaryTextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Company B Selection Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showSelectBDialog = true },
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "কোম্পানি ২", fontSize = 11.sp, color = SecondaryTextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stockB?.symbol ?: "নির্বাচন করুন ➕",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (stockB != null) BrightCyan else SecondaryTextGray,
                        textAlign = TextAlign.Center
                    )
                    stockB?.let {
                        Text(
                            text = it.companyNameBangla,
                            fontSize = 10.sp,
                            color = SecondaryTextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comparison Table / Comparison Insights
        if (stockA == null || stockB == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = "Compare",
                        modifier = Modifier.size(48.dp),
                        tint = SecondaryTextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "তুলনা করার জন্য দুটি শেয়ার বা ফান্ড সিলেক্ট করুন",
                        color = SecondaryTextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            val sA = stockA!!
            val sB = stockB!!

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "স্বয়ংক্রিয় লাইভ তুলনা মেটাবক্স",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrightCyan,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Row Generator
                    ComparisonRow(label = "কোম্পানির নাম", valA = sA.companyNameBangla, valB = sB.companyNameBangla, textColor, dividerColor)
                    ComparisonRow(label = "বাজার এক্সচেঞ্জ", valA = sA.exchange, valB = sB.exchange, textColor, dividerColor)
                    ComparisonRow(label = "বর্তমান দাম", valA = "${sA.currentPrice} ৳", valB = "${sB.currentPrice} ৳", textColor, dividerColor)
                    ComparisonRow(label = "খাত (Sector)", valA = sA.sectorBangla, valB = sB.sectorBangla, textColor, dividerColor)
                    
                    // P/E Ratio (Lower is better generally)
                    val peAColor = if (sA.peRatio < sB.peRatio) GainGreen else textColor
                    val peBColor = if (sB.peRatio < sA.peRatio) GainGreen else textColor
                    ComparisonRow(
                        label = "P/E রেশিও (কম ভালো)",
                        valA = "${sA.peRatio}",
                        valB = "${sB.peRatio}",
                        textColor,
                        dividerColor,
                        colorA = peAColor,
                        colorB = peBColor
                    )

                    // Dividend Yield (Higher is better)
                    val divAColor = if (sA.dividendYield > sB.dividendYield) GainGreen else textColor
                    val divBColor = if (sB.dividendYield > sA.dividendYield) GainGreen else textColor
                    ComparisonRow(
                        label = "বার্ষিক ডিভিডেন্ড (%)",
                        valA = "${sA.dividendYield}%",
                        valB = "${sB.dividendYield}%",
                        textColor,
                        dividerColor,
                        colorA = divAColor,
                        colorB = divBColor
                    )

                    ComparisonRow(label = "সাধারণ ফি", valA = sA.regularFeesBangla, valB = sB.regularFeesBangla, textColor, dividerColor)
                    ComparisonRow(label = "লুকানো চার্জসমূহ", valA = sA.hiddenFeesBangla, valB = sB.hiddenFeesBangla, textColor, dividerColor)

                    // Is Shariah / Mutual fund
                    ComparisonRow(
                        label = "মিউচুয়াল ফান্ড?",
                        valA = if (sA.isMutualFund) "হ্যাঁ (ম্যানেজার: ${sA.fundManagerBangla})" else "না",
                        valB = if (sB.isMutualFund) "হ্যাঁ (ম্যানেজার: ${sB.fundManagerBangla})" else "না",
                        textColor,
                        dividerColor
                    )

                    // Recommendation logic based on basic metrics
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .background(BrightCyan.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Insights, contentDescription = "Insight icon", tint = BrightCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "তুলনামূলক বিশ্লেষণ", color = BrightCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val betterStock = if (sA.peRatio < sB.peRatio && sA.dividendYield > sB.dividendYield) sA else if (sB.peRatio < sA.peRatio && sB.dividendYield > sA.dividendYield) sB else null
                            val comment = if (betterStock != null) {
                                "বিশ্লেষণ অনুযায়ী, ${betterStock.companyNameBangla} (${betterStock.symbol}) তুলনামূলকভাবে কম পি/ই রেশিও এবং বেশি ডিভিডেন্ড ইল্ড অফার করছে, যা দীর্ঘমেয়াদী বিনিয়োগকারীদের জন্য ইতিবাচক সংকেত।"
                            } else {
                                "উভয় শেয়ারের নিজস্ব বৈশিষ্ট্য রয়েছে। যেমন: ${sA.symbol} এর ${if (sA.peRatio < sB.peRatio) "মূল্য যৌক্তিক (কম পি/ই)" else "ডিভিডেন্ড বেশি"}, অন্যদিকে ${sB.symbol} এর ${if (sB.peRatio < sA.peRatio) "মূল্য আকর্ষণীয়" else "ডিভিডেন্ড আকর্ষণীয়"}।"
                            }
                            Text(
                                text = comment,
                                color = textColor,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Search dialog helper overlays
        if (showSelectADialog) {
            CompanySelectDialog(
                stocks = liveStocks,
                onDismiss = { showSelectADialog = false },
                onSelect = {
                    stockA = it
                    showSelectADialog = false
                }
            )
        }

        if (showSelectBDialog) {
            CompanySelectDialog(
                stocks = liveStocks,
                onDismiss = { showSelectBDialog = false },
                onSelect = {
                    stockB = it
                    showSelectBDialog = false
                }
            )
        }
    }
}

@Composable
fun ComparisonRow(
    label: String,
    valA: String,
    valB: String,
    textColor: Color,
    dividerColor: Color,
    colorA: Color = textColor,
    colorB: Color = textColor
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryTextGray,
                modifier = Modifier.width(90.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = valA,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorA,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "vs",
                    fontSize = 10.sp,
                    color = SecondaryTextGray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = valB,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorB,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Divider(color = dividerColor, thickness = 0.5.dp)
    }
}

// ==========================================
// 4. EDUCATION GUIDE TAB (শিক্ষা গাইড)
// ==========================================
@Composable
fun GuideTab(
    textColor: Color,
    cardColor: Color,
    isDark: Boolean
) {
    val steps = listOf(
        Triple(
            "ধাপ ১: বিও (BO) অ্যাকাউন্ট তৈরি",
            "শেয়ার বাজারে বিনিয়োগ শুরু করার প্রথম ধাপ হলো একটি 'Beneficiary Owner' বা বিও অ্যাকাউন্ট খোলা। এটি ঠিক একটি ব্যাংক অ্যাকাউন্টের মতো, যেখানে আপনার শেয়ারগুলো ডিজিটালভাবে জমা থাকে।",
            "যা যা লাগবে: আপনার জাতীয় পরিচয়পত্র (NID), পাসপোর্ট সাইজ ছবি, ব্যাংক অ্যাকাউন্ট নম্বর এবং চেকের পাতা, নমিনির তথ্য।"
        ),
        Triple(
            "ধাপ ২: ব্রোকারেজ হাউজ নির্বাচন",
            "আপনি সরাসরি স্টক এক্সচেঞ্জ থেকে শেয়ার কিনতে পারবেন না। আপনাকে একটি অনুমোদিত ব্রোকারেজ হাউজ বা মেম্বার সিকিউরিটির মাধ্যমে লেনদেন করতে হবে। যেমন লংকাবাংলা সিকিউরিটিজ, আইসিবি ক্যাপিটাল ইত্যাদি।",
            "লক্ষ্য রাখুন: লেনদেনের ব্রোকারেজ ফি সাধারণত ০.২৫% থেকে ০.৪৫% পর্যন্ত হয়। কম ফি চার্জ করা হাউজগুলো বেছে নেওয়া ভালো।"
        ),
        Triple(
            "ধাপ ৩: টাকা জমা (Fund Deposit)",
            "আপনার বিও অ্যাকাউন্ট তৈরি হয়ে গেলে, ব্রোকারেজ হাউজের ব্যাংক অ্যাকাউন্টে ফান্ড ট্রান্সফার, ব্যাংক চেক বা মোবাইল ব্যাংকিংয়ের মাধ্যমে বিনিয়োগের টাকা জমা করতে হবে।",
            "টিপস: ব্রোকার অ্যাপ বা পোর্টালে আপনার ব্যালেন্স যুক্ত হতে সাধারণত কয়েক ঘণ্টা সময় লাগতে পারে।"
        ),
        Triple(
            "ধাপ ৪: প্রথম শেয়ার ক্রয়",
            "এখন আপনি লেনদেনের জন্য প্রস্তুত! ব্রোকারের দেওয়া মোবাইল অ্যাপ, ওয়েবসাইট অথবা সরাসরি ট্রেডারকে ফোন করে আপনার পছন্দের শেয়ারটি কিনতে পারেন।",
            "পরামর্শ: প্রথম বিনিয়োগে সব টাকা একটি শেয়ারে না খাটিয়ে কয়েকটি শক্তিশালী ক্যাটাগরির শেয়ার বা মিউচুয়াল ফান্ডে বিভক্ত করে বিনিয়োগ (Diversification) করুন।"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "নতুনদের জন্য বিনিয়োগ শিক্ষা গাইড",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "জটিল অর্থনৈতিক টার্মগুলো সহজে বুঝুন এবং মাত্র ৪টি ধাপে শেয়ার বাজারে প্রবেশ করুন",
            fontSize = 12.sp,
            color = SecondaryTextGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Terminology simplifications cards
        Text(
            text = "সহজ কথায় শেয়ার বাজারের কিছু গুরুত্বপূর্ণ পরিভাষা",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        val jargons = listOf(
            Triple("P/E Ratio (পি/ই রেশিও)", "এটি দেখায় একটি কোম্পানির ১ টাকা লাভ করার জন্য আপনি কত টাকা দিতে প্রস্তুত। পিই রেশিও ১৫-এর নিচে হলে সাধারণত শেয়ারটি সস্তা বা ভালো মনে করা হয়।", "উদাহরণ: একটি শেয়ারের দাম ১৫০ টাকা এবং আয় ১০ টাকা হলে, পিই রেশিও হবে ১৫।"),
            Triple("Dividend Yield (ডিভিডেন্ড ইল্ড)", "ডিভিডেন্ড মানে হচ্ছে কোম্পানির অর্জিত মুনাফার অংশ যা বিনিয়োগকারীদের দেওয়া হয়। ডিভিডেন্ড ইল্ড যত বেশি, শেয়ার থেকে আপনার বার্ষিক প্যাসিভ ইনকাম তত বেশি।", "উদাহরণ: ১০০ টাকা শেয়ার দাম হলে বার্ষিক ১০ টাকা লভ্যাংশ পেলে ইল্ড ১০%।"),
            Triple("BO Account (বিও অ্যাকাউন্ট)", "শেয়ার কেনাবেচা করার ডিজিটাল লকার। বিও অ্যাকাউন্টের বার্ষিক সিডিবিএল চার্জ সাধারণত ৪৫০ টাকা হয়। এটি একটি পরোক্ষ বা লুকানো খরচ।", "জরুরি তথ্য: প্রতি বছর এই ফি কেটে নেওয়া হয়, তা না দিলে লকার ফ্রিজ হতে পারে।")
        )

        jargons.forEach { jargon ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = jargon.first,
                        fontWeight = FontWeight.Bold,
                        color = BrightCyan,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = jargon.second,
                        color = textColor,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(textColor.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = jargon.third,
                            color = SecondaryTextGray,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step-by-Step Guide Progress Tracker
        Text(
            text = "৪ ধাপে বিনিয়োগের সহজ রোডম্যাপ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        steps.forEachIndexed { idx, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrightCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        fontWeight = FontWeight.Bold,
                        color = PureBlack,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = step.first,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = step.second,
                        color = SecondaryTextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.third,
                        color = if (isDark) Color(0xFFA5F3FC) else Color(0xFF0369A1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = textColor.copy(alpha = 0.1f))
                }
            }
        }
    }
}

// ==========================================
// 5. AI INVESTMENT ADVISOR (এআই গুরু)
// ==========================================
@Composable
fun AIAdvisorTab(
    viewModel: BiniyogViewModel,
    textColor: Color,
    cardColor: Color,
    isDark: Boolean
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()

    var userMessage by remember { mutableStateOf("") }

    val suggestedPrompts = listOf(
        "মিউচুয়াল ফান্ড কি ও এর সুবিধা কি?",
        "ডিএসই এবং সিএসই এর পার্থক্য কি?",
        "পেশাদার পোর্টফোলিও ডাইভার্সিফিকেশন কিভাবে করব?",
        "শেয়ার বাজারে লুকানো খরচ ও ফিস কি কি?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "বিনিয়োগ গুরু (AI Advisor)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "গুগল জেমিনি এআই চালিত রিয়েল-টাইম বিনিয়োগ সহকারী",
                    fontSize = 11.sp,
                    color = SecondaryTextGray
                )
            }
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear conversation logs", tint = BrightCyan)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat message history scroll panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(cardColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .border(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .padding(8.dp)
        ) {
            if (chatHistory.size <= 1) {
                // Render suggestions if empty
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot icon",
                        tint = BrightCyan,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "বিনিয়োগ সম্পর্কিত যেকোনো প্রশ্ন বাংলায় জিজ্ঞেস করুন",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    suggestedPrompts.forEach { prompt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.askBiniyogGuru(prompt) },
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF23252F) else Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = prompt,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = BrightCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatHistory) { (msg, isUser) ->
                        ChatBubble(message = msg, isUser = isUser, textColor = textColor, isDark = isDark)
                    }
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = BrightCyan, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val keyboardController = LocalSoftwareKeyboardController.current

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                placeholder = { Text("শেয়ার বাজার সম্পর্কে প্রশ্ন করুন...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("chat_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = BrightCyan,
                    unfocusedBorderColor = textColor.copy(alpha = 0.2f)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (userMessage.trim().isNotEmpty()) {
                            viewModel.askBiniyogGuru(userMessage)
                            userMessage = ""
                            keyboardController?.hide()
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userMessage.trim().isNotEmpty()) {
                        viewModel.askBiniyogGuru(userMessage)
                        userMessage = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(BrightCyan, CircleShape)
                    .testTag("send_chat_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send message", tint = if (isDark) Color(0xFF0F1115) else Color.White)
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: String,
    isUser: Boolean,
    textColor: Color,
    isDark: Boolean
) {
    val bubbleColor = if (isUser) {
        if (isDark) Color(0xFF0D9488) else Color(0xFF0F766E)
    } else {
        if (isDark) Color(0xFF252836) else Color(0xFFE2E8F0)
    }

    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                color = if (isUser || isDark) PureWhite else PureBlack,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ==========================================
// OVERLAYS, BOTTOM DRAWERS, & DIALOGS
// ==========================================

@Composable
fun NotificationAlertsOverlay(
    alerts: List<PriceAlert>,
    onDismiss: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Render maximum of latest 3 alerts concurrently as floating banners
            alerts.take(3).forEach { alert ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (alert.type == AlertType.HIGH) GainGreen else LossRed,
                            RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.type == AlertType.HIGH) Color(0xFF0F1E19) else Color(0xFF241013)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (alert.type == AlertType.HIGH) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = "Alert type icon",
                                tint = if (alert.type == AlertType.HIGH) GainGreen else LossRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alert.message,
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { onDismiss(alert.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompanySelectDialog(
    stocks: List<LiveStock>,
    onDismiss: () -> Unit,
    onSelect: (LiveStock) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDarkBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "কোম্পানি নির্বাচন করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(stocks) { stock ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(stock) }
                                .padding(12.dp)
                                .border(0.5.dp, SecondaryTextGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = stock.symbol, fontWeight = FontWeight.Bold, color = BrightCyan)
                                Text(text = stock.companyNameBangla, fontSize = 11.sp, color = SecondaryTextGray)
                            }
                            Text(text = "${stock.currentPrice} ৳", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPortfolioStockDialog(
    availableStocks: List<LiveStock>,
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double, String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf(availableStocks.firstOrNull()?.symbol ?: "") }
    var shares by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var exchange by remember { mutableStateOf("DSE") }
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDarkBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "নতুন বিনিয়োগ যোগ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stock Symbol dropdown or selector
                Text(text = "কোম্পানি সিলেক্ট করুন", fontSize = 11.sp, color = SecondaryTextGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableStocks.forEach { stock ->
                        val isSel = stock.symbol == selectedSymbol
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) BrightCyan else Color(0xFF252836))
                                .clickable { selectedSymbol = stock.symbol }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stock.symbol,
                                color = if (isSel) PureBlack else PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exchange Selector
                Text(text = "এক্সচেঞ্জ", fontSize = 11.sp, color = SecondaryTextGray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DSE", "CSE").forEach { ex ->
                        val isSel = exchange == ex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BrightCyan else Color(0xFF252836))
                                .clickable { exchange = ex }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ex,
                                color = if (isSel) PureBlack else PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shares Input
                OutlinedTextField(
                    value = shares,
                    onValueChange = { shares = it },
                    label = { Text("শেয়ার সংখ্যা") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("shares_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = BrightCyan,
                        unfocusedBorderColor = SecondaryTextGray
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Buy Price Input
                OutlinedTextField(
                    value = buyPrice,
                    onValueChange = { buyPrice = it },
                    label = { Text("শেয়ার প্রতি ক্রয়মূল্য (৳)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("buy_price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = BrightCyan,
                        unfocusedBorderColor = SecondaryTextGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "বাতিল", color = SecondaryTextGray)
                    }

                    Button(
                        onClick = {
                            val sh = shares.toDoubleOrNull() ?: 0.0
                            val pr = buyPrice.toDoubleOrNull() ?: 0.0
                            if (selectedSymbol.isNotEmpty() && sh > 0 && pr > 0) {
                                onAdd(selectedSymbol, sh, pr, exchange)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightCyan),
                        modifier = Modifier.weight(1f).testTag("save_asset_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "সংরক্ষণ", color = PureBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StockDetailBottomSheet(
    stock: LiveStock,
    comments: List<AnonymousComment>,
    isFav: Boolean,
    textColor: Color,
    cardColor: Color,
    dividerColor: Color,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onFavToggle: () -> Unit,
    onAddComment: (String, Int, String) -> Unit
) {
    var userCommentText by remember { mutableStateOf("") }
    var userRating by remember { mutableStateOf(5) }
    var userAlias by remember { mutableStateOf("") }

    val changeColor = if (stock.changePercent >= 0) GainGreen else LossRed
    val formattedPrice = String.format("%.1f", stock.currentPrice)
    val formattedChange = String.format("%+.2f%%", stock.changePercent)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) DeepDarkBlue else LightBg),
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .border(1.dp, dividerColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stock.symbol,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1E3A8A), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stock.exchange,
                                    fontSize = 10.sp,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = stock.companyNameBangla,
                            fontSize = 13.sp,
                            color = SecondaryTextGray
                        )
                    }

                    Row {
                        IconButton(onClick = onFavToggle) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Favorite Toggle",
                                tint = if (isFav) Color(0xFFFFB300) else SecondaryTextGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close overlay",
                                tint = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Fluctuations panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "সর্বশেষ বাজার দর", fontSize = 11.sp, color = SecondaryTextGray)
                            Text(
                                text = "$formattedPrice ৳",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "দৈনিক উঠানামা", fontSize = 11.sp, color = SecondaryTextGray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (stock.changePercent >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "Price Trend Indicator",
                                    tint = changeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = formattedChange,
                                    color = changeColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simple mini technical analysis canvas indicator
                Text(
                    text = "স্মার্ট টেকনিক্যাল অ্যানালাইসিস (এআই চালিত)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "আরএসআই (RSI - 14)", fontSize = 11.sp, color = SecondaryTextGray)
                                Text(text = String.format("%.1f", stock.rsi), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                            Column {
                                Text(text = "মুভিং অ্যাভারেজ (SMA 50)", fontSize = 11.sp, color = SecondaryTextGray)
                                Text(text = "${String.format("%.1f", stock.sma50)} ৳", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "অ্যাকশন সিগন্যাল", fontSize = 11.sp, color = SecondaryTextGray)
                                Text(
                                    text = stock.signalBangla,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (stock.signal) {
                                        "BUY" -> GainGreen
                                        "SELL" -> LossRed
                                        else -> textColor
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Small Canvas Candle-Stick representation
                        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                            // draw background line
                            drawLine(
                                color = SecondaryTextGray.copy(alpha = 0.3f),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 3f
                            )
                            // draw low point
                            drawCircle(
                                color = LossRed,
                                radius = 8f,
                                center = Offset(size.width * 0.1f, size.height / 2)
                            )
                            // draw high point
                            drawCircle(
                                color = GainGreen,
                                radius = 8f,
                                center = Offset(size.width * 0.9f, size.height / 2)
                            )
                            // draw current point
                            val currentPos = size.width * 0.1f + (size.width * 0.8f * (stock.rsi / 100f).toFloat())
                            drawCircle(
                                color = BrightCyan,
                                radius = 12f,
                                center = Offset(currentPos, size.height / 2)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "অতিরিক্ত বিক্রয় (RSI < 30)", fontSize = 9.sp, color = SecondaryTextGray)
                            Text(text = "নিরাপদ অঞ্চল (RSI 50)", fontSize = 9.sp, color = SecondaryTextGray)
                            Text(text = "অতিরিক্ত ক্রয় (RSI > 70)", fontSize = 9.sp, color = SecondaryTextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Company Description (Simplified for Beginners)
                Text(
                    text = "কোম্পানি পরিচিতি ও ফান্ডামেন্টাল তথ্য",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stock.descriptionBangla,
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fees Table
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ফি এবং পরোক্ষ/লুকানো খরচ স্ট্রাকচার",
                            fontWeight = FontWeight.Bold,
                            color = BrightCyan,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "১. লেনদেন কমিশন (Brokerage Fee):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(text = stock.regularFeesBangla, fontSize = 11.sp, color = SecondaryTextGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "২. বিও অ্যাকাউন্ট ও অন্যান্য চার্জ (Hidden Fees):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(text = stock.hiddenFeesBangla, fontSize = 11.sp, color = SecondaryTextGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "গুরুত্বপূর্ণ শিক্ষা: মিউচুয়াল ফান্ড এবং স্টক ডিভিডেন্ড আয়ের প্রথম ২৫,০০০ টাকা সম্পূর্ণ করমুক্ত!",
                            fontSize = 10.sp,
                            color = GainGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Anonymous comments / reviews
                Text(
                    text = "বিনিয়োগকারীদের বেনামী মন্তব্য (${comments.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    text = "এখানে ব্যবহারকারীরা সম্পূর্ণ বেনামে তাদের বাস্তব অভিজ্ঞতা শেয়ার করতে পারেন",
                    fontSize = 11.sp,
                    color = SecondaryTextGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Add comment form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "বেনামী মন্তব্য লিখুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        OutlinedTextField(
                            value = userAlias,
                            onValueChange = { userAlias = it },
                            placeholder = { Text("ছদ্মনাম (ফাঁকা রাখলে 'নামহীন বিনিয়োগকারী' দেখাবে)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = BrightCyan,
                                unfocusedBorderColor = dividerColor
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = userCommentText,
                            onValueChange = { userCommentText = it },
                            placeholder = { Text("শেয়ারটি সম্পর্কে আপনার সততা ও বেনামী মতামত দিন...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = BrightCyan,
                                unfocusedBorderColor = dividerColor
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Star selector
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "রেটিং:", fontSize = 11.sp, color = textColor)
                                Spacer(modifier = Modifier.width(4.dp))
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = "Star rating",
                                        tint = if (star <= userRating) Color(0xFFFFB300) else SecondaryTextGray,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { userRating = star }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (userCommentText.trim().isNotEmpty()) {
                                        onAddComment(userCommentText, userRating, userAlias)
                                        userCommentText = ""
                                        userAlias = ""
                                        userRating = 5
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightCyan, contentColor = PureBlack),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("submit_comment_button")
                            ) {
                                Text(text = "মন্তব্য করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Render previous reviews
                if (comments.isEmpty()) {
                    Text(
                        text = "এখনও কোনো মন্তব্য করা হয়নি। প্রথম মন্তব্যকারী হোন!",
                        color = SecondaryTextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    comments.forEach { comment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = comment.alias,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = BrightCyan
                                    )
                                    Row {
                                        (1..5).forEach { rStar ->
                                            Icon(
                                                imageVector = if (rStar <= comment.rating) Icons.Default.Star else Icons.Default.StarOutline,
                                                contentDescription = "rating show",
                                                tint = if (rStar <= comment.rating) Color(0xFFFFB300) else SecondaryTextGray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.commentText,
                                    fontSize = 12.sp,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                Text(
                                    text = df.format(Date(comment.timestamp)),
                                    fontSize = 8.sp,
                                    color = SecondaryTextGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
