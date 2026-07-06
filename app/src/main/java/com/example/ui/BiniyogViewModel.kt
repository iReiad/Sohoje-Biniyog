package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.api.GeminiClient
import com.example.data.AnonymousComment
import com.example.data.BiniyogDatabase
import com.example.data.BiniyogRepository
import com.example.data.LiveStock
import com.example.data.PortfolioStock
import com.example.data.StockService
import com.example.data.WatchlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Simple model representing in-app real-time price alerts
data class PriceAlert(
    val id: Long = System.currentTimeMillis() + (1..1000).random(),
    val symbol: String,
    val message: String,
    val type: AlertType, // HIGH, LOW
    val timestamp: Long = System.currentTimeMillis()
)

enum class AlertType {
    HIGH, LOW
}

class BiniyogViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize Room Database
    private val database: BiniyogDatabase by lazy {
        Room.databaseBuilder(
            application,
            BiniyogDatabase::class.java,
            "biniyog_bondhu_db"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository: BiniyogRepository by lazy {
        BiniyogRepository(database)
    }

    // Live Simulated Stocks
    private val _liveStocks = MutableStateFlow<List<LiveStock>>(StockService.initialStocks)
    val liveStocks: StateFlow<List<LiveStock>> = _liveStocks.asStateFlow()

    // Database Watchlist items
    val watchlistItems: StateFlow<List<WatchlistItem>> = repository.watchlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database Portfolio items
    val portfolioStocks: StateFlow<List<PortfolioStock>> = repository.portfolioStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database Anonymous Comments
    val allComments: StateFlow<List<AnonymousComment>> = repository.allComments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-App Alert Logs (Real-time fluctuation detections)
    private val _alerts = MutableStateFlow<List<PriceAlert>>(emptyList())
    val alerts: StateFlow<List<PriceAlert>> = _alerts.asStateFlow()

    // Search and Sector Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedSector = MutableStateFlow("All")
    val selectedSector = _selectedSector.asStateFlow()

    // Chat with Biniyog Guru (Gemini)
    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            Pair("আসসালামু আলাইকুম! আমি আপনার 'বিনিয়োগ বন্ধু' এআই অ্যাসিস্ট্যান্ট। ঢাকা (DSE) এবং চট্টগ্রাম (CSE) শেয়ার বাজার, মিউচুয়াল ফান্ড এবং বিনিয়োগ সংক্রান্ত যেকোনো প্রশ্ন আমাকে বাংলায় করতে পারেন। কিভাবে সাহায্য করতে পারি?", false)
        )
    )
    val chatHistory = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    // Dark Theme preference state
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    // Track active selected stock for details/comment sheet
    private val _selectedStock = MutableStateFlow<LiveStock?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    // Alert thresholds (notify if a stock gains/loses more than 0.8% in a simulation tick)
    private val alertThresholdPercent = 0.8

    init {
        // Start simulated real-time fluctuation loop
        startSimulation()
    }

    private fun startSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(5000) // update every 5 seconds
                val previousList = _liveStocks.value
                val updatedList = StockService.fluctuatePrices(previousList)
                _liveStocks.value = updatedList

                // Detect price fluctuations and trigger alerts
                detectAlerts(previousList, updatedList)
            }
        }
    }

    private fun detectAlerts(oldList: List<LiveStock>, newList: List<LiveStock>) {
        val newAlerts = mutableListOf<PriceAlert>()
        for (i in oldList.indices) {
            val old = oldList[i]
            val new = newList[i]
            val diffPercent = ((new.currentPrice - old.currentPrice) / old.currentPrice) * 100.0

            if (Math.abs(diffPercent) >= alertThresholdPercent) {
                val formattedDiff = String.format("%.2f", Math.abs(diffPercent))
                val isHigh = diffPercent > 0
                val message = if (isHigh) {
                    "${new.companyNameBangla} (${new.symbol}) এর দর ${formattedDiff}% বৃদ্ধি পেয়ে ${new.currentPrice} টাকা হয়েছে! 📈"
                } else {
                    "${new.companyNameBangla} (${new.symbol}) এর দর ${formattedDiff}% হ্রাস পেয়ে ${new.currentPrice} টাকা হয়েছে! 📉"
                }
                newAlerts.add(
                    PriceAlert(
                        symbol = new.symbol,
                        message = message,
                        type = if (isHigh) AlertType.HIGH else AlertType.LOW
                    )
                )
            }
        }

        if (newAlerts.isNotEmpty()) {
            _alerts.value = (newAlerts + _alerts.value).take(30) // limit to latest 30 alerts
        }
    }

    // --- Search & Sector Filtering ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedSector(sector: String) {
        _selectedSector.value = sector
    }

    // --- Stock Actions ---
    fun selectStock(stock: LiveStock?) {
        _selectedStock.value = stock
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // --- Watchlist Database Operations ---
    fun toggleWatchlist(stock: LiveStock) {
        viewModelScope.launch {
            val isFav = watchlistItems.value.any { it.symbol == stock.symbol }
            if (isFav) {
                repository.removeFromWatchlist(stock.symbol, stock.exchange)
            } else {
                repository.addToWatchlist(stock.symbol, stock.exchange)
            }
        }
    }

    // --- Portfolio Database Operations ---
    fun addPortfolioStock(symbol: String, shares: Double, buyPrice: Double, exchange: String) {
        viewModelScope.launch {
            val stockDetail = _liveStocks.value.firstOrNull { it.symbol == symbol }
            val companyName = stockDetail?.companyName ?: symbol
            repository.addPortfolioStock(
                PortfolioStock(
                    symbol = symbol,
                    companyName = companyName,
                    exchange = exchange,
                    shares = shares,
                    buyPrice = buyPrice
                )
            )
        }
    }

    fun sellPortfolioStock(id: Int) {
        viewModelScope.launch {
            repository.removePortfolioStockById(id)
        }
    }

    // --- Anonymous Comments Operations ---
    fun addAnonymousComment(symbol: String, commentText: String, rating: Int, anonymousAlias: String) {
        viewModelScope.launch {
            val alias = if (anonymousAlias.trim().isEmpty()) "নামহীন বিনিয়োগকারী" else anonymousAlias
            repository.addComment(
                AnonymousComment(
                    companySymbol = symbol,
                    rating = rating,
                    commentText = commentText,
                    alias = alias
                )
            )
        }
    }

    // --- Chatbot Integration ---
    fun askBiniyogGuru(message: String) {
        if (message.trim().isEmpty()) return
        
        // Add user question to history
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add(Pair(message, true))
        _chatHistory.value = currentHistory

        _isChatLoading.value = true

        viewModelScope.launch {
            val reply = GeminiClient.getInvestmentInsight(message)
            val updatedHistory = _chatHistory.value.toMutableList()
            updatedHistory.add(Pair(reply, false))
            _chatHistory.value = updatedHistory
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            Pair("আসসালামু আলাইকুম! আমি আপনার 'বিনিয়োগ বন্ধু' এআই অ্যাসিস্ট্যান্ট। ঢাকা (DSE) এবং চট্টগ্রাম (CSE) শেয়ার বাজার, মিউচুয়াল ফান্ড এবং বিনিয়োগ সংক্রান্ত যেকোনো প্রশ্ন আমাকে বাংলায় করতে পারেন। কিভাবে সাহায্য করতে পারি?", false)
        )
    }

    fun dismissAlert(alertId: Long) {
        _alerts.value = _alerts.value.filterNot { it.id == alertId }
    }
}
