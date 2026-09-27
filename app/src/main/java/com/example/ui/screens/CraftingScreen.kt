package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import com.example.game.crafting.CraftingRecipe
import com.example.game.crafting.RecipeBook
import com.example.game.crafting.RecipeCategory
import com.example.game.model.ItemStack
import com.example.game.ui.ItemBlockBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftingScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<RecipeCategory?>(null) }
    var selectedRecipe by remember { mutableStateOf(RecipeBook.recipes.first()) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: 3x3 Crafting, 1: Furnace Smelting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(16.dp)
            .testTag("crafting_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Crafting Codex",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "3x3 Workbench & Smelting Guide",
                    color = Color(0xFF81C784),
                    fontSize = 12.sp
                )
            }

            // Sub-tabs: Workbench vs Furnace
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2B2D31))
                    .padding(2.dp)
            ) {
                Surface(
                    color = if (activeSubTab == 0) Color(0xFF4CAF50) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { activeSubTab = 0 }
                ) {
                    Text(
                        "Crafting",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    color = if (activeSubTab == 1) Color(0xFFE65100) else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { activeSubTab = 1 }
                ) {
                    Text(
                        "Furnace",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSubTab == 0) {
            // Interactive 3x3 Workbench Preview Card
            WorkbenchPreviewCard(recipe = selectedRecipe)

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recipe_search_input"),
                placeholder = { Text("Search recipes (e.g. pickaxe, tnt)...", color = Color(0xFF9E9E9E), fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF81C784)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedContainerColor = Color(0xFF2B2D31),
                    unfocusedContainerColor = Color(0xFF2B2D31),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${RecipeBook.recipes.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF2B2D31),
                            labelColor = Color(0xFFBDBDBD)
                        )
                    )
                }
                items(RecipeCategory.values()) { cat ->
                    val count = RecipeBook.recipes.count { it.category == cat }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text("${cat.title} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF2B2D31),
                            labelColor = Color(0xFFBDBDBD)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recipe List
            val filteredRecipes = remember(searchQuery, selectedCategory) {
                RecipeBook.recipes.filter { recipe ->
                    val matchesCategory = selectedCategory == null || recipe.category == selectedCategory
                    val matchesSearch = searchQuery.isBlank() ||
                            recipe.name.contains(searchQuery, ignoreCase = true) ||
                            recipe.description.contains(searchQuery, ignoreCase = true)
                    matchesCategory && matchesSearch
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredRecipes) { recipe ->
                    val isSelected = recipe.id == selectedRecipe.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF2E4630) else Color(0xFF2B2D31)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRecipe = recipe }
                            .testTag("recipe_item_${recipe.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ItemBlockBadge(item = recipe.output)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = recipe.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = recipe.category.title,
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "x${recipe.output.count}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Furnace Smelting Guide
            FurnaceGuideView()
        }
    }
}

@Composable
private fun WorkbenchPreviewCard(recipe: CraftingRecipe) {
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
                Text(
                    text = recipe.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = recipe.category.title,
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3x3 Grid + Arrow + Output
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // 3x3 Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val grid = buildGridFromPattern(recipe.pattern, recipe.key)
                    for (row in 0 until 3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (col in 0 until 3) {
                                val item = grid[row][col]
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF383A40))
                                        .border(1.dp, Color(0xFF55575E), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item != null && !item.isEmpty) {
                                        ItemBlockBadge(item = item)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Arrow
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Crafts to",
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Result slot
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1F22))
                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ItemBlockBadge(item = recipe.output)
                    if (recipe.output.count > 1) {
                        Text(
                            text = "${recipe.output.count}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (recipe.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = recipe.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun buildGridFromPattern(pattern: List<String>, key: Map<Char, ItemStack>): Array<Array<ItemStack?>> {
    val grid = Array(3) { Array<ItemStack?>(3) { null } }
    for (r in pattern.indices) {
        val rowStr = pattern[r]
        for (c in rowStr.indices) {
            val char = rowStr[c]
            if (char != ' ' && key.containsKey(char)) {
                grid[r][c] = key[char]
            }
        }
    }
    return grid
}

@Composable
private fun FurnaceGuideView() {
    val smeltingItems = listOf(
        SmeltRecipe("Raw Iron", "Iron Ingot", "Smelts raw iron ore into iron ingots for armor & tools.", "0.7 XP"),
        SmeltRecipe("Raw Gold", "Gold Ingot", "Yields gold ingots used for powered rails, clocks & armor.", "1.0 XP"),
        SmeltRecipe("Sand", "Glass", "Melts beach sand into transparent glass blocks.", "0.1 XP"),
        SmeltRecipe("Cobblestone", "Smooth Stone", "Turns rough cobblestone into clean stone blocks.", "0.1 XP"),
        SmeltRecipe("Raw Porkchop", "Cooked Porkchop", "Greatly boosts saturation and restores 8 hunger points.", "0.35 XP"),
        SmeltRecipe("Raw Beef", "Steak", "Best early-game food item for hunger and health regen.", "0.35 XP"),
        SmeltRecipe("Clay Ball", "Brick", "Bakes clay into individual building bricks.", "0.3 XP"),
        SmeltRecipe("Oak Log", "Charcoal", "Creates an emergency fuel source equal to mined coal.", "0.15 XP"),
        SmeltRecipe("Wet Sponge", "Sponge", "Dries wet sponge from Ocean Monuments for reuse.", "0.15 XP"),
        SmeltRecipe("Netherrack", "Nether Brick", "Smelts into nether bricks to craft Nether Fortress pillars.", "0.1 XP")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Furnace Smelting Codex",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = "Fuel Burn Times: Coal (80s / 8 items), Lava Bucket (1000s / 100 items), Blaze Rod (120s / 12 items).",
            color = Color(0xFFFFB74D),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(smeltingItems) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.input,
                                    color = Color(0xFFFFCC80),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "smelts to",
                                    tint = Color(0xFFFF7043),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.output,
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.desc,
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            color = Color(0xFF383A40),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.xp,
                                color = Color(0xFF4CAF50),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SmeltRecipe(
    val input: String,
    val output: String,
    val desc: String,
    val xp: String
)
