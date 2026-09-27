package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PotionRecipe(
    val name: String,
    val base: String,
    val ingredient: String,
    val effect: String,
    val duration: String,
    val color: Color
)

@Composable
fun BrewingScreen() {
    val coroutineScope = rememberCoroutineScope()

    var selectedIngredient by remember { mutableStateOf("Nether Wart") }
    var baseBottle by remember { mutableStateOf("Water Bottle") }
    var brewedPotion by remember { mutableStateOf<String?>(null) }
    var isBrewing by remember { mutableStateOf(false) }
    var brewProgress by remember { mutableFloatStateOf(0f) }

    val ingredientsList = listOf(
        "Nether Wart", "Sugar", "Glistering Melon", "Magma Cream",
        "Ghast Tear", "Blaze Powder", "Golden Carrot", "Spider Eye",
        "Fermented Spider Eye", "Pufferfish", "Rabbit's Foot", "Phantom Membrane",
        "Redstone Dust", "Glowstone Dust", "Gunpowder"
    )

    val potionDatabase = listOf(
        PotionRecipe("Awkward Potion", "Water Bottle", "Nether Wart", "Base potion required for almost all effects", "No effect", Color(0xFF673AB7)),
        PotionRecipe("Potion of Healing", "Awkward Potion", "Glistering Melon", "Instantly heals 4 HP (Instant Health)", "Instant", Color(0xFFE91E63)),
        PotionRecipe("Potion of Fire Resistance", "Awkward Potion", "Magma Cream", "Immunity to fire, lava, and blaze fireballs", "3:00 (8:00 with Redstone)", Color(0xFFFF9800)),
        PotionRecipe("Potion of Swiftness (Speed)", "Awkward Potion", "Sugar", "+20% movement speed and wider FOV", "3:00 (Speed II: 1:30)", Color(0xFF03A9F4)),
        PotionRecipe("Potion of Strength", "Awkward Potion", "Blaze Powder", "+3 melee damage to all attacks", "3:00 (Strength II: 1:30)", Color(0xFFB71C1C)),
        PotionRecipe("Potion of Night Vision", "Awkward Potion", "Golden Carrot", "See clearly in pitch black caves and underwater", "3:00 (8:00 with Redstone)", Color(0xFF1B5E20)),
        PotionRecipe("Potion of Invisibility", "Night Vision", "Fermented Spider Eye", "Renders player invisible to mobs & players", "3:00 (8:00 with Redstone)", Color(0xFF78909C)),
        PotionRecipe("Potion of Regeneration", "Awkward Potion", "Ghast Tear", "Rapidly heals 1 HP every 2.5 seconds", "0:45 (Regen II: 0:22)", Color(0xFFF06292)),
        PotionRecipe("Potion of Water Breathing", "Awkward Potion", "Pufferfish", "Prevents drowning oxygen meter from depleting", "3:00 (8:00 with Redstone)", Color(0xFF00ACC1)),
        PotionRecipe("Potion of Poison", "Awkward Potion", "Spider Eye", "Deals continuous damage down to 1/2 heart", "0:45", Color(0xFF4CAF50)),
        PotionRecipe("Potion of Weakness", "Water Bottle", "Fermented Spider Eye", "Reduces attack damage by 4 (used to cure Zombie Villagers!)", "1:30 (4:00 with Redstone)", Color(0xFF795548)),
        PotionRecipe("Potion of Harming", "Healing / Poison", "Fermented Spider Eye", "Instantly inflicts 6 damage (Instant Damage)", "Instant", Color(0xFF4A148C)),
        PotionRecipe("Potion of Slow Falling", "Awkward Potion", "Phantom Membrane", "Float gently to ground without fall damage", "1:30 (4:00 with Redstone)", Color(0xFFFFF9C4)),
        PotionRecipe("Potion of Leaping", "Awkward Potion", "Rabbit's Foot", "Jump higher over 1.5 blocks and reduce fall damage", "3:00 (Jump Boost II: 1:30)", Color(0xFF8BC34A))
    )

    fun startBrew() {
        if (isBrewing) return
        isBrewing = true
        brewProgress = 0f
        coroutineScope.launch {
            for (step in 1..20) {
                delay(100)
                brewProgress = step / 20f
            }

            // Determine output
            brewedPotion = when {
                baseBottle == "Water Bottle" && selectedIngredient == "Nether Wart" -> "Awkward Potion"
                baseBottle == "Water Bottle" && selectedIngredient == "Fermented Spider Eye" -> "Potion of Weakness"
                baseBottle == "Awkward Potion" && selectedIngredient == "Glistering Melon" -> "Potion of Healing"
                baseBottle == "Awkward Potion" && selectedIngredient == "Magma Cream" -> "Potion of Fire Resistance"
                baseBottle == "Awkward Potion" && selectedIngredient == "Sugar" -> "Potion of Swiftness (Speed)"
                baseBottle == "Awkward Potion" && selectedIngredient == "Blaze Powder" -> "Potion of Strength"
                baseBottle == "Awkward Potion" && selectedIngredient == "Golden Carrot" -> "Potion of Night Vision"
                baseBottle == "Awkward Potion" && selectedIngredient == "Ghast Tear" -> "Potion of Regeneration"
                baseBottle == "Awkward Potion" && selectedIngredient == "Spider Eye" -> "Potion of Poison"
                baseBottle == "Awkward Potion" && selectedIngredient == "Pufferfish" -> "Potion of Water Breathing"
                baseBottle == "Awkward Potion" && selectedIngredient == "Phantom Membrane" -> "Potion of Slow Falling"
                baseBottle == "Potion of Night Vision" && selectedIngredient == "Fermented Spider Eye" -> "Potion of Invisibility"
                baseBottle == "Potion of Healing" && selectedIngredient == "Fermented Spider Eye" -> "Potion of Harming"
                else -> "Mundane Potion"
            }
            isBrewing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(16.dp)
            .testTag("brewing_screen")
    ) {
        // Header
        Text(
            text = "Brewing Stand Simulator",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Alchemy laboratory & comprehensive potion recipes",
            color = Color(0xFFBA68C8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Brewing Stand Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Fuel & Ingredient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Blaze Fuel Gauge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9800)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = "Fuel", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Blaze Powder Fuel: 100%", color = Color(0xFFFFB74D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isBrewing) {
                        Text("Brewing...", color = Color(0xFFBA68C8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ingredient Slot (Top)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF383A40))
                        .border(2.dp, Color(0xFFBA68C8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color(0xFFBA68C8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedIngredient,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Vertical Brewing Tubes
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(if (isBrewing) Color(0xFFBA68C8) else Color(0xFF55575E))
                )

                // Bottom 3 Bottle Slots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val bottleName = if (brewedPotion != null) brewedPotion!! else baseBottle
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1F22))
                                .border(1.5.dp, Color(0xFF64B5F6), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Bottle",
                                tint = if (brewedPotion != null) Color(0xFFE91E63) else Color(0xFF64B5F6),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Result Potion or Prompt
                if (brewedPotion != null) {
                    Surface(
                        color = Color(0xFF4A148C),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Result: $brewedPotion!",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Base: $baseBottle",
                        color = Color(0xFF90CAF9),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Brew & Reset
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { startBrew() },
                        enabled = !isBrewing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("brew_potion_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Brew Potion")
                    }

                    OutlinedButton(
                        onClick = {
                            baseBottle = if (brewedPotion != null) brewedPotion!! else if (baseBottle == "Water Bottle") "Awkward Potion" else "Water Bottle"
                            brewedPotion = null
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Change Base", color = Color(0xFFE0E0E0))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Potion Recipes Codex
        Text(
            text = "Potion Recipes Reference",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(potionDatabase) { recipe ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            baseBottle = recipe.base
                            selectedIngredient = recipe.ingredient
                            brewedPotion = null
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(recipe.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recipe.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = recipe.duration,
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(recipe.base, color = Color(0xFF90CAF9), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ ${recipe.ingredient}", color = Color(0xFFCE93D8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(recipe.effect, color = Color(0xFFB0BEC5), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
