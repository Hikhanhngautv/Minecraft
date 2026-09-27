package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.crafting.RecipeBook
import com.example.game.engine.BlockWorldEngine
import com.example.game.model.BlockType
import com.example.game.model.ItemStack
import com.example.game.model.ItemType

@Composable
fun InventoryDialog(
    engine: BlockWorldEngine,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Inventory & Quick Craft, 1: Creative Palette

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f)
                .testTag("inventory_dialog"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (engine.isCreative) "Creative Inventory" else "Player Inventory",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_inventory_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color(0xFF4CAF50)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Inventory & Craft", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Items & Blocks", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // Inventory slots & Crafting
                    Text(
                        text = "Hotbar (Equipped):",
                        color = Color(0xFFFFD54F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until 7) {
                            val item = engine.hotbar[i]
                            SlotBox(item = item, isHotbar = true) {
                                // Clear or move
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Crafting Recipes:",
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Quick craft recipe buttons
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(RecipeBook.recipes) { recipe ->
                            CraftRecipeCard(recipe = recipe) {
                                // Try crafting
                                craftItem(engine, recipe.output)
                            }
                        }
                    }
                } else {
                    // Creative / Item Palette
                    Text(
                        text = "Tap any item to add 16 to hotbar:",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val allPaletteItems = remember {
                        listOf(
                            ItemStack(blockType = BlockType.GRASS, count = 16),
                            ItemStack(blockType = BlockType.DIRT, count = 16),
                            ItemStack(blockType = BlockType.STONE, count = 16),
                            ItemStack(blockType = BlockType.COBBLESTONE, count = 16),
                            ItemStack(blockType = BlockType.OAK_LOG, count = 16),
                            ItemStack(blockType = BlockType.OAK_PLANKS, count = 16),
                            ItemStack(blockType = BlockType.OAK_LEAVES, count = 16),
                            ItemStack(blockType = BlockType.GLASS, count = 16),
                            ItemStack(blockType = BlockType.BRICKS, count = 16),
                            ItemStack(blockType = BlockType.TORCH, count = 16),
                            ItemStack(blockType = BlockType.TNT, count = 16),
                            ItemStack(blockType = BlockType.SAND, count = 16),
                            ItemStack(blockType = BlockType.CRAFTING_TABLE, count = 1),
                            ItemStack(blockType = BlockType.FURNACE, count = 1),
                            ItemStack(blockType = BlockType.CHEST, count = 1),
                            ItemStack(blockType = BlockType.DIAMOND_ORE, count = 16),
                            ItemStack(blockType = BlockType.GOLD_ORE, count = 16),
                            ItemStack(blockType = BlockType.IRON_ORE, count = 16),
                            ItemStack(blockType = BlockType.COAL_ORE, count = 16),
                            ItemStack(blockType = BlockType.OBSIDIAN, count = 16),
                            ItemStack(itemType = ItemType.DIAMOND_PICKAXE, count = 1),
                            ItemStack(itemType = ItemType.DIAMOND_SWORD, count = 1),
                            ItemStack(itemType = ItemType.DIAMOND_AXE, count = 1),
                            ItemStack(itemType = ItemType.BREAD, count = 16),
                            ItemStack(itemType = ItemType.APPLE, count = 16)
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(allPaletteItems) { paletteItem ->
                            Surface(
                                color = Color(0xFF383838),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable {
                                        engine.addItemToInventory(paletteItem)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ItemBlockBadge(item = paletteItem)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = paletteItem.displayName,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        maxLines = 1
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

@Composable
private fun SlotBox(
    item: ItemStack,
    isHotbar: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF383838))
            .border(1.dp, if (isHotbar) Color(0xFFFFD54F) else Color(0xFF616161), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!item.isEmpty) {
            ItemBlockBadge(item = item)
            if (item.count > 1) {
                Text(
                    text = "${item.count}",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                )
            }
        }
    }
}

@Composable
private fun CraftRecipeCard(
    recipe: com.example.game.crafting.CraftingRecipe,
    onCraft: () -> Unit
) {
    Surface(
        color = Color(0xFF383838),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCraft() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ItemBlockBadge(item = recipe.output)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = recipe.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "x${recipe.output.count}",
                        color = Color(0xFF81C784),
                        fontSize = 10.sp
                    )
                }
            }
            Text(
                text = "Craft",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun craftItem(engine: BlockWorldEngine, output: ItemStack) {
    engine.addItemToInventory(output)
}
