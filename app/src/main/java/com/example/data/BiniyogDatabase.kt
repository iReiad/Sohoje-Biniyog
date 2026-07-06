package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "portfolio_stocks")
data class PortfolioStock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val companyName: String,
    val exchange: String, // "DSE" or "CSE"
    val shares: Double,
    val buyPrice: Double,
    val buyDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "watchlist_items")
data class WatchlistItem(
    @PrimaryKey val symbol: String,
    val exchange: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "anonymous_comments")
data class AnonymousComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companySymbol: String,
    val rating: Int, // 1 to 5
    val commentText: String,
    val alias: String, // e.g. "নামহীন বিনিয়োগকারী"
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_stocks ORDER BY id DESC")
    fun getAllPortfolioStocks(): Flow<List<PortfolioStock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioStock(stock: PortfolioStock)

    @Delete
    suspend fun deletePortfolioStock(stock: PortfolioStock)

    @Query("DELETE FROM portfolio_stocks WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_items ORDER BY timestamp DESC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem)

    @Delete
    suspend fun deleteWatchlistItem(item: WatchlistItem)

    @Query("SELECT EXISTS(SELECT * FROM watchlist_items WHERE symbol = :symbol)")
    fun isInWatchlist(symbol: String): Flow<Boolean>
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM anonymous_comments WHERE companySymbol = :symbol ORDER BY timestamp DESC")
    fun getCommentsForCompany(symbol: String): Flow<List<AnonymousComment>>

    @Query("SELECT * FROM anonymous_comments ORDER BY timestamp DESC")
    fun getAllComments(): Flow<List<AnonymousComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: AnonymousComment)
}

// --- AppDatabase ---

@Database(
    entities = [PortfolioStock::class, WatchlistItem::class, AnonymousComment::class],
    version = 1,
    exportSchema = false
)
abstract class BiniyogDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun commentDao(): CommentDao
}

// --- Repository Pattern ---

class BiniyogRepository(private val database: BiniyogDatabase) {
    val portfolioStocks: Flow<List<PortfolioStock>> = database.portfolioDao().getAllPortfolioStocks()
    val watchlistItems: Flow<List<WatchlistItem>> = database.watchlistDao().getAllWatchlistItems()
    val allComments: Flow<List<AnonymousComment>> = database.commentDao().getAllComments()

    suspend fun addPortfolioStock(stock: PortfolioStock) {
        database.portfolioDao().insertPortfolioStock(stock)
    }

    suspend fun removePortfolioStockById(id: Int) {
        database.portfolioDao().deleteById(id)
    }

    suspend fun addToWatchlist(symbol: String, exchange: String) {
        database.watchlistDao().insertWatchlistItem(WatchlistItem(symbol, exchange))
    }

    suspend fun removeFromWatchlist(symbol: String, exchange: String) {
        database.watchlistDao().deleteWatchlistItem(WatchlistItem(symbol, exchange))
    }

    fun isInWatchlist(symbol: String): Flow<Boolean> {
        return database.watchlistDao().isInWatchlist(symbol)
    }

    fun getCommentsForCompany(symbol: String): Flow<List<AnonymousComment>> {
        return database.commentDao().getCommentsForCompany(symbol)
    }

    suspend fun addComment(comment: AnonymousComment) {
        database.commentDao().insertComment(comment)
    }
}
