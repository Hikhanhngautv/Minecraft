package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waypoints")
data class Waypoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dimension: String, // "Overworld", "Nether", "The End"
    val x: Int,
    val y: Int,
    val z: Int,
    val category: String, // "Base", "Portal", "Stronghold", "Village", "Fortress", "Mineshaft", "Ancient City", "Custom"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_worlds")
data class SavedWorld(
    @PrimaryKey val id: String = "default_world",
    val name: String,
    val seed: Long,
    val gameMode: String, // "SURVIVAL", "CREATIVE"
    val timeOfDay: Float, // 0.0f to 1.0f
    val playerX: Float,
    val playerY: Float,
    val playerHealth: Int,
    val playerHunger: Int,
    val inventoryJson: String, // Serialized hotbar & items
    val modifiedBlocksJson: String, // Map of coordinate changes
    val updatedAt: Long = System.currentTimeMillis()
)
