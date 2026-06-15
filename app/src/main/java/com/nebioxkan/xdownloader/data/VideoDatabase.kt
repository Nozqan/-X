package com.nebioxkan.xdownloader.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entity ──────────────────────────────────────────────────────────────────

@Entity(tableName = "video_history")
data class VideoHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String,       // "TIKTOK" | "INSTAGRAM" — Twitter ayrı tabloda
    val filePath: String,
    val thumbnailUrl: String?,
    val downloadedAt: Long = System.currentTimeMillis()
)

// ─── DAO ─────────────────────────────────────────────────────────────────────

@Dao
interface VideoHistoryDao {

    @Query("SELECT * FROM video_history WHERE platform = :platform ORDER BY downloadedAt DESC")
    fun getByPlatform(platform: String): Flow<List<VideoHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VideoHistoryEntity)

    @Delete
    suspend fun delete(item: VideoHistoryEntity)

    @Query("DELETE FROM video_history WHERE platform = :platform")
    suspend fun clearPlatform(platform: String)
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(entities = [VideoHistoryEntity::class], version = 1, exportSchema = false)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoHistoryDao(): VideoHistoryDao

    companion object {
        @Volatile private var INSTANCE: VideoDatabase? = null

        fun getInstance(context: android.content.Context): VideoDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    VideoDatabase::class.java,
                    "video_history.db"
                ).build().also { INSTANCE = it }
            }
    }
}
