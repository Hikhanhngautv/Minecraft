package com.example.data.repository

import com.example.data.local.SavedWorldDao
import com.example.data.local.WaypointDao
import com.example.data.model.SavedWorld
import com.example.data.model.Waypoint
import kotlinx.coroutines.flow.Flow

class WaypointRepository(private val waypointDao: WaypointDao) {
    val allWaypoints: Flow<List<Waypoint>> = waypointDao.getAllWaypoints()

    fun getWaypointsByDimension(dim: String): Flow<List<Waypoint>> =
        waypointDao.getWaypointsByDimension(dim)

    suspend fun addWaypoint(waypoint: Waypoint): Long =
        waypointDao.insertWaypoint(waypoint)

    suspend fun removeWaypoint(waypoint: Waypoint) =
        waypointDao.deleteWaypoint(waypoint)

    suspend fun removeWaypointById(id: Long) =
        waypointDao.deleteWaypointById(id)
}

class WorldRepository(private val savedWorldDao: SavedWorldDao) {
    suspend fun loadWorld(id: String = "default_world"): SavedWorld? =
        savedWorldDao.getWorld(id)

    suspend fun saveWorld(world: SavedWorld) =
        savedWorldDao.saveWorld(world)
}
