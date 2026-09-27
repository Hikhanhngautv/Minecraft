package com.example.data.local

import androidx.room.*
import com.example.data.model.SavedWorld
import com.example.data.model.Waypoint
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints ORDER BY createdAt DESC")
    fun getAllWaypoints(): Flow<List<Waypoint>>

    @Query("SELECT * FROM waypoints WHERE dimension = :dimension ORDER BY createdAt DESC")
    fun getWaypointsByDimension(dimension: String): Flow<List<Waypoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(waypoint: Waypoint): Long

    @Delete
    suspend fun deleteWaypoint(waypoint: Waypoint)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun deleteWaypointById(id: Long)
}

@Dao
interface SavedWorldDao {
    @Query("SELECT * FROM saved_worlds WHERE id = :id LIMIT 1")
    suspend fun getWorld(id: String): SavedWorld?

    @Query("SELECT * FROM saved_worlds WHERE id = :id LIMIT 1")
    fun observeWorld(id: String): Flow<SavedWorld?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorld(world: SavedWorld)
}

@Database(entities = [Waypoint::class, SavedWorld::class], version = 1, exportSchema = false)
abstract class MineWorldDatabase : RoomDatabase() {
    abstract fun waypointDao(): WaypointDao
    abstract fun savedWorldDao(): SavedWorldDao

    companion object {
        @Volatile
        private var INSTANCE: MineWorldDatabase? = null

        fun getDatabase(context: android.content.Context): MineWorldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MineWorldDatabase::class.java,
                    "mineworld_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
