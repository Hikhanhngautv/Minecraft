package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class BlockType(
    val displayName: String,
    val isSolid: Boolean = true,
    val hardness: Float = 1.0f, // Time in seconds with bare hands
    val requiresPickaxe: Boolean = false,
    val lightEmission: Int = 0,
    val dropItemCount: Int = 1
) {
    AIR("Air", isSolid = false, hardness = 0f),
    GRASS("Grass Block", hardness = 0.6f),
    DIRT("Dirt", hardness = 0.5f),
    STONE("Stone", hardness = 1.5f, requiresPickaxe = true),
    COBBLESTONE("Cobblestone", hardness = 2.0f, requiresPickaxe = true),
    BEDROCK("Bedrock", hardness = 9999f), // Unbreakable
    OAK_LOG("Oak Wood", hardness = 1.2f),
    OAK_LEAVES("Oak Leaves", hardness = 0.2f),
    OAK_PLANKS("Oak Planks", hardness = 1.0f),
    COAL_ORE("Coal Ore", hardness = 2.0f, requiresPickaxe = true),
    IRON_ORE("Iron Ore", hardness = 2.5f, requiresPickaxe = true),
    GOLD_ORE("Gold Ore", hardness = 2.5f, requiresPickaxe = true),
    DIAMOND_ORE("Diamond Ore", hardness = 3.5f, requiresPickaxe = true),
    OBSIDIAN("Obsidian", hardness = 8.0f, requiresPickaxe = true),
    SAND("Sand", hardness = 0.5f),
    GLASS("Glass", hardness = 0.3f),
    CRAFTING_TABLE("Crafting Table", hardness = 1.2f),
    FURNACE("Furnace", hardness = 2.0f, requiresPickaxe = true),
    CHEST("Chest", hardness = 1.2f),
    BRICKS("Bricks", hardness = 2.0f, requiresPickaxe = true),
    TORCH("Torch", isSolid = false, hardness = 0.05f, lightEmission = 14),
    TNT("TNT", hardness = 0.05f),
    WATER("Water", isSolid = false, hardness = 0f),
    LAVA("Lava", isSolid = false, hardness = 0f);

    val isTransparent: Boolean
        get() = this == AIR || this == GLASS || this == TORCH || this == WATER || this == OAK_LEAVES

    fun getDrop(): Pair<BlockType?, String> {
        return when (this) {
            GRASS -> Pair(DIRT, "dirt")
            STONE -> Pair(COBBLESTONE, "cobblestone")
            COAL_ORE -> Pair(null, "coal")
            DIAMOND_ORE -> Pair(null, "diamond")
            GLASS -> Pair(null, "") // breaks without silk touch
            OAK_LEAVES -> if (Math.random() < 0.2) Pair(null, "apple") else Pair(null, "")
            else -> Pair(this, "")
        }
    }
}

enum class ItemType(
    val displayName: String,
    val isTool: Boolean = false,
    val toolType: ToolType = ToolType.NONE,
    val miningMultiplier: Float = 1.0f,
    val attackDamage: Int = 1,
    val isFood: Boolean = false,
    val foodHeal: Int = 0,
    val maxStack: Int = 64
) {
    NONE("Empty", maxStack = 0),
    STICK("Stick"),
    COAL("Coal"),
    IRON_INGOT("Iron Ingot"),
    GOLD_INGOT("Gold Ingot"),
    DIAMOND("Diamond"),
    APPLE("Apple", isFood = true, foodHeal = 4),
    BREAD("Bread", isFood = true, foodHeal = 6),
    RAW_PORKCHOP("Raw Porkchop", isFood = true, foodHeal = 3),
    COOKED_PORKCHOP("Cooked Porkchop", isFood = true, foodHeal = 8),
    GUNPOWDER("Gunpowder"),
    STRING_ITEM("String"),
    FEATHER("Feather"),
    WHEAT("Wheat"),
    
    // Tools
    WOODEN_PICKAXE("Wooden Pickaxe", isTool = true, toolType = ToolType.PICKAXE, miningMultiplier = 2.0f, attackDamage = 2, maxStack = 1),
    STONE_PICKAXE("Stone Pickaxe", isTool = true, toolType = ToolType.PICKAXE, miningMultiplier = 3.5f, attackDamage = 3, maxStack = 1),
    IRON_PICKAXE("Iron Pickaxe", isTool = true, toolType = ToolType.PICKAXE, miningMultiplier = 5.0f, attackDamage = 4, maxStack = 1),
    DIAMOND_PICKAXE("Diamond Pickaxe", isTool = true, toolType = ToolType.PICKAXE, miningMultiplier = 8.0f, attackDamage = 5, maxStack = 1),
    
    WOODEN_SWORD("Wooden Sword", isTool = true, toolType = ToolType.SWORD, miningMultiplier = 1.0f, attackDamage = 4, maxStack = 1),
    STONE_SWORD("Stone Sword", isTool = true, toolType = ToolType.SWORD, miningMultiplier = 1.0f, attackDamage = 5, maxStack = 1),
    IRON_SWORD("Iron Sword", isTool = true, toolType = ToolType.SWORD, miningMultiplier = 1.0f, attackDamage = 6, maxStack = 1),
    DIAMOND_SWORD("Diamond Sword", isTool = true, toolType = ToolType.SWORD, miningMultiplier = 1.0f, attackDamage = 7, maxStack = 1),
    
    WOODEN_AXE("Wooden Axe", isTool = true, toolType = ToolType.AXE, miningMultiplier = 2.0f, attackDamage = 3, maxStack = 1),
    STONE_AXE("Stone Axe", isTool = true, toolType = ToolType.AXE, miningMultiplier = 3.0f, attackDamage = 4, maxStack = 1),
    IRON_AXE("Iron Axe", isTool = true, toolType = ToolType.AXE, miningMultiplier = 4.0f, attackDamage = 5, maxStack = 1),
    DIAMOND_AXE("Diamond Axe", isTool = true, toolType = ToolType.AXE, miningMultiplier = 6.0f, attackDamage = 6, maxStack = 1),

    FLINT_AND_STEEL("Flint & Steel", isTool = true, toolType = ToolType.NONE, maxStack = 1)
}

enum class ToolType {
    NONE, PICKAXE, SWORD, AXE, SHOVEL
}

data class ItemStack(
    val blockType: BlockType? = null,
    val itemType: ItemType? = null,
    val count: Int = 1
) {
    val displayName: String
        get() = blockType?.displayName ?: itemType?.displayName ?: "Empty"

    val isBlock: Boolean
        get() = blockType != null && blockType != BlockType.AIR

    val isEmpty: Boolean
        get() = (blockType == null || blockType == BlockType.AIR) && (itemType == null || itemType == ItemType.NONE) || count <= 0
}

enum class MobType(
    val displayName: String,
    val maxHealth: Int,
    val isHostile: Boolean,
    val attackDamage: Int,
    val width: Float = 0.8f,
    val height: Float = 1.8f
) {
    PIG("Pig", maxHealth = 10, isHostile = false, attackDamage = 0, width = 0.9f, height = 0.9f),
    SHEEP("Sheep", maxHealth = 8, isHostile = false, attackDamage = 0, width = 0.9f, height = 1.1f),
    ZOMBIE("Zombie", maxHealth = 20, isHostile = true, attackDamage = 3, width = 0.8f, height = 1.9f),
    CREEPER("Creeper", maxHealth = 20, isHostile = true, attackDamage = 15, width = 0.7f, height = 1.7f)
}

data class Mob(
    val id: Long,
    val type: MobType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var health: Int = type.maxHealth,
    var isGrounded: Boolean = false,
    var facingRight: Boolean = true,
    var fuseTimer: Float = 0f, // For creeper
    var hurtTimer: Float = 0f,
    var walkTimer: Float = 0f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var life: Float, // 1.0 down to 0
    val maxLife: Float = 1.0f,
    val size: Float = 6f
)

data class FloatingDrop(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val item: ItemStack,
    var age: Float = 0f
)

data class BreakingBlock(
    val x: Int,
    val y: Int,
    var progress: Float = 0f, // 0 to 1
    var requiredTime: Float = 1f
)
