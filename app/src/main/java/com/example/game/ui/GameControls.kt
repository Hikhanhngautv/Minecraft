package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.BlockWorldEngine
import com.example.game.model.BlockType
import com.example.game.model.ItemStack
import com.example.game.model.ItemType

@Composable
fun GameHud(
    engine: BlockWorldEngine,
    onOpenInventory: () -> Unit,
    onToggleCreative: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top status row: Hearts, Hunger, Mode badge, Inventory
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hearts (Health)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fullHearts = engine.playerHealth / 2
                val hasHalfHeart = engine.playerHealth % 2 != 0
                for (i in 0 until 10) {
                    val heartColor = when {
                        i < fullHearts -> Color(0xFFE53935)
                        i == fullHearts && hasHalfHeart -> Color(0xFFFF8A80)
                        else -> Color(0xFF424242)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(16.dp)
                            .background(heartColor, RoundedCornerShape(3.dp))
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${engine.playerHealth}/20",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Game mode & Inventory button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (engine.isCreative) Color(0xFF7B1FA2) else Color(0xFF2E7D32),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onToggleCreative() }
                        .testTag("toggle_game_mode_button")
                ) {
                    Text(
                        text = if (engine.isCreative) "CREATIVE" else "SURVIVAL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onOpenInventory,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x88000000), CircleShape)
                        .testTag("inventory_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Inventory",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HotbarView(
    engine: BlockWorldEngine,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xBB212121), RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFF424242), RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until 7) {
            val item = engine.hotbar[i]
            val isSelected = engine.selectedSlot == i

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color(0x66FFFFFF) else Color(0x44000000))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF616161),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { engine.selectedSlot = i }
                    .testTag("hotbar_slot_$i"),
                contentAlignment = Alignment.Center
            ) {
                if (!item.isEmpty) {
                    // Item Icon or Block colored square
                    ItemBlockBadge(item = item)

                    // Stack count badge
                    if (item.count > 1) {
                        Text(
                            text = "${item.count}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemBlockBadge(item: ItemStack) {
    if (item.isBlock && item.blockType != null) {
        val color = when (item.blockType) {
            BlockType.GRASS -> Color(0xFF4CAF50)
            BlockType.DIRT -> Color(0xFF8D6E63)
            BlockType.STONE -> Color(0xFF9E9E9E)
            BlockType.COBBLESTONE -> Color(0xFF757575)
            BlockType.OAK_LOG -> Color(0xFF5D4037)
            BlockType.OAK_LEAVES -> Color(0xFF2E7D32)
            BlockType.OAK_PLANKS -> Color(0xFFB8945F)
            BlockType.GLASS -> Color(0xFFB2EBF2)
            BlockType.TORCH -> Color(0xFFFFC107)
            BlockType.TNT -> Color(0xFFE53935)
            BlockType.CRAFTING_TABLE -> Color(0xFF8D6E63)
            BlockType.FURNACE -> Color(0xFF616161)
            BlockType.CHEST -> Color(0xFFA17838)
            BlockType.DIAMOND_ORE -> Color(0xFF4DD0E1)
            BlockType.GOLD_ORE -> Color(0xFFFFD54F)
            BlockType.IRON_ORE -> Color(0xFFD7CCC8)
            BlockType.COAL_ORE -> Color(0xFF212121)
            BlockType.OBSIDIAN -> Color(0xFF311B92)
            else -> Color(0xFF81C784)
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, RoundedCornerShape(3.dp))
                .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
        )
    } else {
        val (icon, color) = when (item.itemType) {
            ItemType.WOODEN_PICKAXE, ItemType.STONE_PICKAXE, ItemType.IRON_PICKAXE, ItemType.DIAMOND_PICKAXE ->
                Pair(Icons.Default.Build, if (item.itemType == ItemType.DIAMOND_PICKAXE) Color(0xFF4DD0E1) else Color(0xFFFFB74D))
            ItemType.WOODEN_SWORD, ItemType.STONE_SWORD, ItemType.IRON_SWORD, ItemType.DIAMOND_SWORD ->
                Pair(Icons.Default.FlashOn, if (item.itemType == ItemType.DIAMOND_SWORD) Color(0xFF4DD0E1) else Color(0xFFE57373))
            ItemType.APPLE, ItemType.BREAD, ItemType.RAW_PORKCHOP, ItemType.COOKED_PORKCHOP ->
                Pair(Icons.Default.Restaurant, Color(0xFFEF5350))
            ItemType.COAL -> Pair(Icons.Default.Circle, Color(0xFF212121))
            ItemType.DIAMOND -> Pair(Icons.Default.Diamond, Color(0xFF4DD0E1))
            ItemType.IRON_INGOT, ItemType.GOLD_INGOT -> Pair(Icons.Default.Square, Color(0xFFFFD54F))
            else -> Pair(Icons.Default.Widgets, Color(0xFF90CAF9))
        }
        Icon(imageVector = icon, contentDescription = item.displayName, tint = color, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun DPadControls(
    engine: BlockWorldEngine,
    onMineAction: () -> Unit,
    onPlaceAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left & Right Walk Controls
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Left Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .border(2.dp, Color(0x66FFFFFF), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                engine.moveLeft = true
                                tryAwaitRelease()
                                engine.moveLeft = false
                            }
                        )
                    }
                    .testTag("dpad_left_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Left",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Right Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .border(2.dp, Color(0x66FFFFFF), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                engine.moveRight = true
                                tryAwaitRelease()
                                engine.moveRight = false
                            }
                        )
                    }
                    .testTag("dpad_right_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Right",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Action Buttons: Jump, Mine/Attack, Place/Use
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            // Mine / Attack Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA7F0000))
                    .border(2.dp, Color(0xFFFF5252), CircleShape)
                    .clickable { onMineAction() }
                    .testTag("action_mine_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Hardware,
                        contentDescription = "Mine",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("MINE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Place / Use Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA1B5E20))
                    .border(2.dp, Color(0xFF66BB6A), CircleShape)
                    .clickable { onPlaceAction() }
                    .testTag("action_place_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddBox,
                        contentDescription = "Place",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("PLACE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Jump Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA0D47A1))
                    .border(2.dp, Color(0xFF42A5F5), CircleShape)
                    .clickable { engine.jumpRequested = true }
                    .testTag("action_jump_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Jump",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
