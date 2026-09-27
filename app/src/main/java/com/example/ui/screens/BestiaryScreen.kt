package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

data class MobEntry(
    val name: String,
    val dimension: String,
    val hostility: HostilityType,
    val hp: Int,
    val attack: String,
    val drops: String,
    val spawn: String,
    val tip: String,
    val accentColor: Color
)

enum class HostilityType(val title: String, val color: Color) {
    PASSIVE("Passive", Color(0xFF4CAF50)),
    NEUTRAL("Neutral", Color(0xFFFF9800)),
    HOSTILE("Hostile", Color(0xFFE53935)),
    BOSS("Boss", Color(0xFFAB47BC))
}

@Composable
fun BestiaryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedHostility by remember { mutableStateOf<HostilityType?>(null) }
    var expandedMob by remember { mutableStateOf<String?>("Creeper") }

    val mobList = remember {
        listOf(
            MobEntry(
                name = "Creeper",
                dimension = "Overworld",
                hostility = HostilityType.HOSTILE,
                hp = 20,
                attack = "Up to 49 (Explosion)",
                drops = "0-2 Gunpowder, Music Disc (if killed by skeleton)",
                spawn = "Light level 0 on solid blocks in Overworld",
                tip = "Silently sneaks up on players. Starts hissing when within 3 blocks. Hit-and-run tactics with sprint knocks them back.",
                accentColor = Color(0xFF4CAF50)
            ),
            MobEntry(
                name = "Zombie",
                dimension = "Overworld",
                hostility = HostilityType.HOSTILE,
                hp = 20,
                attack = "3 (Normal) / 4.5 (Hard)",
                drops = "Rotten Flesh, Iron Ingot, Carrot, Potato",
                spawn = "Light level 0 in darkness, burns in sunlight",
                tip = "Attacks in swarms. Can infect Villagers. Keep distance with a sweep attack sword.",
                accentColor = Color(0xFF2E7D32)
            ),
            MobEntry(
                name = "Skeleton",
                dimension = "Overworld",
                hostility = HostilityType.HOSTILE,
                hp = 20,
                attack = "Ranged Bow (up to 5)",
                drops = "0-2 Bones, 0-2 Arrows, Bow (rare)",
                spawn = "Darkness, burns in sunlight unless wearing helmet",
                tip = "Always equip a Shield! Blocking arrows deflects them back and neutralizes their ranged advantage.",
                accentColor = Color(0xFFB0BEC5)
            ),
            MobEntry(
                name = "Enderman",
                dimension = "All Dimensions",
                hostility = HostilityType.NEUTRAL,
                hp = 40,
                attack = "7 (Normal) / 10.5 (Hard)",
                drops = "0-1 Ender Pearl",
                spawn = "End, Nether Warped Forest, Overworld light 0",
                tip = "Will only attack if you look directly at their head. Wear a Carved Pumpkin or stand in water to stay completely safe.",
                accentColor = Color(0xFF7E57C2)
            ),
            MobEntry(
                name = "Blaze",
                dimension = "Nether",
                hostility = HostilityType.HOSTILE,
                hp = 20,
                attack = "Fireballs (5 + Fire damage)",
                drops = "0-1 Blaze Rod (Essential for Eyes of Ender & Brewing)",
                spawn = "Nether Fortress spawners",
                tip = "Drink a Potion of Fire Resistance to negate all fireball damage completely! Snowballs deal 3 damage to them.",
                accentColor = Color(0xFFFFB300)
            ),
            MobEntry(
                name = "Wither Skeleton",
                dimension = "Nether",
                hostility = HostilityType.HOSTILE,
                hp = 20,
                attack = "Stone Sword + Wither Effect (10s)",
                drops = "Coal, Bones, Wither Skeleton Skull (2.5%)",
                spawn = "Nether Fortress corridors",
                tip = "They are 2.4 blocks tall. Build a 2-block high ceiling barrier; they cannot pass under it while you hit their feet!",
                accentColor = Color(0xFF212121)
            ),
            MobEntry(
                name = "Ghast",
                dimension = "Nether",
                hostility = HostilityType.HOSTILE,
                hp = 10,
                attack = "Explosive Fireball",
                drops = "0-2 Gunpowder, 0-1 Ghast Tear",
                spawn = "Nether Wastes, Soul Sand Valley, Basalt Deltas",
                tip = "You can deflect their fireballs back at them using any projectile or melee strike for an instant one-hit kill achievement!",
                accentColor = Color(0xFFECEFF1)
            ),
            MobEntry(
                name = "Warden",
                dimension = "Overworld",
                hostility = HostilityType.HOSTILE,
                hp = 500,
                attack = "30 (Melee) / 15 (Sonic Boom through shields)",
                drops = "1 Sculk Catalyst, 5 XP",
                spawn = "Deep Dark biome when Sculk Shrieker is activated 4 times",
                tip = "Blind mob that tracks through vibrations and smell. Sneak with Shift, throw snowballs/arrows away from you as sound decoys!",
                accentColor = Color(0xFF006064)
            ),
            MobEntry(
                name = "Iron Golem",
                dimension = "Overworld",
                hostility = HostilityType.NEUTRAL,
                hp = 100,
                attack = "7 to 21 (Launches players into air)",
                drops = "3-5 Iron Ingots, 0-2 Poppies",
                spawn = "Villages with 3+ gossiping villagers, or crafted (4 Iron Blocks + Pumpkin)",
                tip = "Pillar up 3 blocks high to attack them safely without them being able to reach you.",
                accentColor = Color(0xFFCFD8DC)
            ),
            MobEntry(
                name = "Ender Dragon",
                dimension = "The End",
                hostility = HostilityType.BOSS,
                hp = 200,
                attack = "Dragon Breath, Wing Slap, Fireballs",
                drops = "12,000 XP (first kill), Dragon Egg",
                spawn = "Central End Island",
                tip = "First destroy all End Crystals on top of Obsidian pillars using a bow or snowballs before attempting to deal direct damage.",
                accentColor = Color(0xFF6A1B9A)
            ),
            MobEntry(
                name = "Pig",
                dimension = "Overworld",
                hostility = HostilityType.PASSIVE,
                hp = 10,
                attack = "None",
                drops = "1-3 Raw Porkchop",
                spawn = "Grassy biomes in Overworld with light 9+",
                tip = "Can be ridden with a Saddle and steered with a Carrot on a Stick.",
                accentColor = Color(0xFFF48FB1)
            ),
            MobEntry(
                name = "Villager",
                dimension = "Overworld",
                hostility = HostilityType.PASSIVE,
                hp = 20,
                attack = "None",
                drops = "None",
                spawn = "Natural villages across Plains, Desert, Savanna, Taiga, Snow biomes",
                tip = "Assign professions using workstations (Lectern for Mending Books, Fletcher Table for easy emeralds with sticks!).",
                accentColor = Color(0xFF8D6E63)
            )
        )
    }

    val filteredMobs = remember(searchQuery, selectedHostility) {
        mobList.filter { mob ->
            val matchesHostility = selectedHostility == null || mob.hostility == selectedHostility
            val matchesSearch = searchQuery.isBlank() ||
                    mob.name.contains(searchQuery, ignoreCase = true) ||
                    mob.dimension.contains(searchQuery, ignoreCase = true) ||
                    mob.drops.contains(searchQuery, ignoreCase = true)
            matchesHostility && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(16.dp)
            .testTag("bestiary_screen")
    ) {
        // Header
        Text(
            text = "Mob Bestiary",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Stats, attack values, loot tables & survival strategies",
            color = Color(0xFF81C784),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search mob by name, loot or dimension...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF81C784)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFF424242),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Hostility Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedHostility == null,
                    onClick = { selectedHostility = null },
                    label = { Text("All Mobs") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2E7D32),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF2B2D31),
                        labelColor = Color(0xFFBDBDBD)
                    )
                )
            }
            items(HostilityType.values()) { type ->
                FilterChip(
                    selected = selectedHostility == type,
                    onClick = { selectedHostility = if (selectedHostility == type) null else type },
                    label = { Text(type.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = type.color,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF2B2D31),
                        labelColor = Color(0xFFBDBDBD)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mob List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredMobs) { mob ->
                val isExpanded = expandedMob == mob.name
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedMob = if (isExpanded) null else mob.name }
                        .testTag("mob_card_${mob.name.lowercase().replace(" ", "_")}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(mob.accentColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = mob.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mob.dimension,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                color = mob.hostility.color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.border(1.dp, mob.hostility.color, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = mob.hostility.title.uppercase(),
                                    color = mob.hostility.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Stats
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${mob.hp} HP (${mob.hp / 2} Hearts)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(mob.attack, color = Color(0xFFFFD54F), fontSize = 12.sp)
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF383A40))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Loot Drops:", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(mob.drops, color = Color(0xFFE0E0E0), fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Spawn Conditions:", color = Color(0xFF90CAF9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(mob.spawn, color = Color(0xFFE0E0E0), fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Pro Strategy & Tip:", color = Color(0xFFFFB74D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(mob.tip, color = Color(0xFFB0BEC5), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
