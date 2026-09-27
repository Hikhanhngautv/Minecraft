package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MineWorldDatabase
import com.example.data.model.Waypoint
import com.example.data.repository.WaypointRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WaypointsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { MineWorldDatabase.getDatabase(context) }
    val repository = remember { WaypointRepository(database.waypointDao()) }

    val waypoints by repository.allWaypoints.collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDimensionFilter by remember { mutableStateOf("All") }

    // Seed starter waypoints if empty
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // If empty, insert sample coordinates
            val currentList = database.waypointDao().getAllWaypoints()
            // We can check or insert starter points
            repository.addWaypoint(
                Waypoint(
                    name = "Main Base",
                    dimension = "Overworld",
                    x = 120,
                    y = 70,
                    z = -350,
                    category = "Base",
                    notes = "Storage room, farm, and enchanting library."
                )
            )
            repository.addWaypoint(
                Waypoint(
                    name = "Nether Fortress",
                    dimension = "Nether",
                    x = 45,
                    y = 58,
                    z = -90,
                    category = "Fortress",
                    notes = "Blaze spawner & Nether Wart garden."
                )
            )
            repository.addWaypoint(
                Waypoint(
                    name = "Stronghold Portal",
                    dimension = "Overworld",
                    x = 1540,
                    y = 42,
                    z = 820,
                    category = "Stronghold",
                    notes = "End Portal room found using Eyes of Ender."
                )
            )
        }
    }

    val filteredList = remember(waypoints, selectedDimensionFilter) {
        if (selectedDimensionFilter == "All") {
            waypoints
        } else {
            waypoints.filter { it.dimension == selectedDimensionFilter }
        }
    }

    fun copyTeleportCommand(wp: Waypoint) {
        val cmd = "/tp @s ${wp.x} ${wp.y} ${wp.z}"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Minecraft TP", cmd)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied: $cmd", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(16.dp)
            .testTag("waypoints_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Coordinate Book",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Save bases, strongholds & quick /tp generator",
                    color = Color(0xFF81C784),
                    fontSize = 12.sp
                )
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("add_waypoint_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Waypoint")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dimension Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val dimensions = listOf("All", "Overworld", "Nether", "The End")
            items(dimensions) { dim ->
                FilterChip(
                    selected = selectedDimensionFilter == dim,
                    onClick = { selectedDimensionFilter = dim },
                    label = { Text(dim) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (dim) {
                            "Nether" -> Color(0xFFB71C1C)
                            "The End" -> Color(0xFF4A148C)
                            else -> Color(0xFF2E7D32)
                        },
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF2B2D31),
                        labelColor = Color(0xFFBDBDBD)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Waypoints List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No waypoints saved yet. Tap + to add one!",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList, key = { it.id }) { wp ->
                    WaypointCard(
                        waypoint = wp,
                        onCopyCommand = { copyTeleportCommand(wp) },
                        onDelete = {
                            coroutineScope.launch(Dispatchers.IO) {
                                repository.removeWaypoint(wp)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWaypointDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newWp ->
                coroutineScope.launch(Dispatchers.IO) {
                    repository.addWaypoint(newWp)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WaypointCard(
    waypoint: Waypoint,
    onCopyCommand: () -> Unit,
    onDelete: () -> Unit
) {
    val dimColor = when (waypoint.dimension) {
        "Nether" -> Color(0xFFE53935)
        "The End" -> Color(0xFFAB47BC)
        else -> Color(0xFF43A047)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = dimColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(1.dp, dimColor, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = waypoint.dimension.uppercase(),
                            color = dimColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = waypoint.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(
                        onClick = onCopyCommand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Command", tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coordinates Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("X: ${waypoint.x}", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Y: ${waypoint.y}", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Z: ${waypoint.z}", color = Color(0xFF42A5F5), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Tag: ${waypoint.category}", color = Color(0xFFFFB74D), fontSize = 12.sp)
            }

            if (waypoint.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = waypoint.notes,
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AddWaypointDialog(
    onDismiss: () -> Unit,
    onSave: (Waypoint) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dimension by remember { mutableStateOf("Overworld") }
    var x by remember { mutableStateOf("0") }
    var y by remember { mutableStateOf("64") }
    var z by remember { mutableStateOf("0") }
    var category by remember { mutableStateOf("Base") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Base", "Portal", "Stronghold", "Village", "Fortress", "Ancient City", "End City", "Mineshaft", "Custom")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Waypoint",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Waypoint Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dimension Selector
                Text("Dimension:", color = Color.Gray, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Overworld", "Nether", "The End").forEach { dim ->
                        Surface(
                            color = if (dimension == dim) Color(0xFF2E7D32) else Color(0xFF383838),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { dimension = dim }
                        ) {
                            Text(
                                text = dim,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // X, Y, Z
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = x,
                        onValueChange = { x = it },
                        label = { Text("X") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = y,
                        onValueChange = { y = it },
                        label = { Text("Y") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = z,
                        onValueChange = { z = it },
                        label = { Text("Z") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    Waypoint(
                                        name = name.trim(),
                                        dimension = dimension,
                                        x = x.toIntOrNull() ?: 0,
                                        y = y.toIntOrNull() ?: 64,
                                        z = z.toIntOrNull() ?: 0,
                                        category = category,
                                        notes = notes.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
