package com.example.game.crafting

import com.example.game.model.BlockType
import com.example.game.model.ItemStack
import com.example.game.model.ItemType

data class CraftingRecipe(
    val id: String,
    val name: String,
    val category: RecipeCategory,
    val pattern: List<String>, // e.g. ["P", "P"] or ["PPP", " S ", " S "]
    val key: Map<Char, ItemStack>,
    val output: ItemStack,
    val description: String = ""
)

enum class RecipeCategory(val title: String) {
    TOOLS("Tools"),
    COMBAT("Combat"),
    BUILDING("Building"),
    REDSTONE("Redstone"),
    FOOD("Food & Misc")
}

object RecipeBook {
    private val woodPlanks = ItemStack(blockType = BlockType.OAK_PLANKS)
    private val stick = ItemStack(itemType = ItemType.STICK)
    private val cobblestone = ItemStack(blockType = BlockType.COBBLESTONE)
    private val ironIngot = ItemStack(itemType = ItemType.IRON_INGOT)
    private val diamond = ItemStack(itemType = ItemType.DIAMOND)
    private val coal = ItemStack(itemType = ItemType.COAL)
    private val oakLog = ItemStack(blockType = BlockType.OAK_LOG)
    private val wheat = ItemStack(itemType = ItemType.WHEAT)

    val recipes: List<CraftingRecipe> = listOf(
        // Building
        CraftingRecipe(
            id = "oak_planks",
            name = "Oak Planks",
            category = RecipeCategory.BUILDING,
            pattern = listOf("L"),
            key = mapOf('L' to oakLog),
            output = ItemStack(blockType = BlockType.OAK_PLANKS, count = 4),
            description = "Basic building material converted from logs."
        ),
        CraftingRecipe(
            id = "sticks",
            name = "Sticks",
            category = RecipeCategory.TOOLS,
            pattern = listOf("P", "P"),
            key = mapOf('P' to woodPlanks),
            output = ItemStack(itemType = ItemType.STICK, count = 4),
            description = "Used to craft tools, torches, and weapons."
        ),
        CraftingRecipe(
            id = "crafting_table",
            name = "Crafting Table",
            category = RecipeCategory.BUILDING,
            pattern = listOf("PP", "PP"),
            key = mapOf('P' to woodPlanks),
            output = ItemStack(blockType = BlockType.CRAFTING_TABLE, count = 1),
            description = "Unlocks the full 3x3 crafting grid."
        ),
        CraftingRecipe(
            id = "furnace",
            name = "Furnace",
            category = RecipeCategory.BUILDING,
            pattern = listOf("CCC", "C C", "CCC"),
            key = mapOf('C' to cobblestone),
            output = ItemStack(blockType = BlockType.FURNACE, count = 1),
            description = "Used to smelt ores, cook raw food, and melt sand into glass."
        ),
        CraftingRecipe(
            id = "chest",
            name = "Chest",
            category = RecipeCategory.BUILDING,
            pattern = listOf("PPP", "P P", "PPP"),
            key = mapOf('P' to woodPlanks),
            output = ItemStack(blockType = BlockType.CHEST, count = 1),
            description = "Store up to 27 item stacks safely."
        ),
        CraftingRecipe(
            id = "torches",
            name = "Torches",
            category = RecipeCategory.BUILDING,
            pattern = listOf("C", "S"),
            key = mapOf('C' to coal, 'S' to stick),
            output = ItemStack(blockType = BlockType.TORCH, count = 4),
            description = "Provide light, prevent hostile mob spawning."
        ),

        // Tools
        CraftingRecipe(
            id = "wooden_pickaxe",
            name = "Wooden Pickaxe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("PPP", " S ", " S "),
            key = mapOf('P' to woodPlanks, 'S' to stick),
            output = ItemStack(itemType = ItemType.WOODEN_PICKAXE),
            description = "Mine stone, coal, and cobblestone."
        ),
        CraftingRecipe(
            id = "stone_pickaxe",
            name = "Stone Pickaxe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("CCC", " S ", " S "),
            key = mapOf('C' to cobblestone, 'S' to stick),
            output = ItemStack(itemType = ItemType.STONE_PICKAXE),
            description = "Mines iron ore and lapis lazuli."
        ),
        CraftingRecipe(
            id = "iron_pickaxe",
            name = "Iron Pickaxe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("III", " S ", " S "),
            key = mapOf('I' to ironIngot, 'S' to stick),
            output = ItemStack(itemType = ItemType.IRON_PICKAXE),
            description = "Mines gold, redstone, and diamond ore."
        ),
        CraftingRecipe(
            id = "diamond_pickaxe",
            name = "Diamond Pickaxe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("DDD", " S ", " S "),
            key = mapOf('D' to diamond, 'S' to stick),
            output = ItemStack(itemType = ItemType.DIAMOND_PICKAXE),
            description = "Mines obsidian and ancient debris with supreme durability."
        ),
        CraftingRecipe(
            id = "wooden_axe",
            name = "Wooden Axe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("PP", "PS", " S"),
            key = mapOf('P' to woodPlanks, 'S' to stick),
            output = ItemStack(itemType = ItemType.WOODEN_AXE),
            description = "Quickly cuts down wood logs and wooden blocks."
        ),
        CraftingRecipe(
            id = "stone_axe",
            name = "Stone Axe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("CC", "CS", " S"),
            key = mapOf('C' to cobblestone, 'S' to stick),
            output = ItemStack(itemType = ItemType.STONE_AXE),
            description = "Sturdier axe for chopping wood and high single-hit combat damage."
        ),
        CraftingRecipe(
            id = "iron_axe",
            name = "Iron Axe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("II", "IS", " S"),
            key = mapOf('I' to ironIngot, 'S' to stick),
            output = ItemStack(itemType = ItemType.IRON_AXE),
            description = "Fast woodchopper and high attack damage."
        ),
        CraftingRecipe(
            id = "diamond_axe",
            name = "Diamond Axe",
            category = RecipeCategory.TOOLS,
            pattern = listOf("DD", "DS", " S"),
            key = mapOf('D' to diamond, 'S' to stick),
            output = ItemStack(itemType = ItemType.DIAMOND_AXE),
            description = "Peak chopping speed and devastating single-strike power."
        ),

        // Weapons
        CraftingRecipe(
            id = "wooden_sword",
            name = "Wooden Sword",
            category = RecipeCategory.COMBAT,
            pattern = listOf("P", "P", "S"),
            key = mapOf('P' to woodPlanks, 'S' to stick),
            output = ItemStack(itemType = ItemType.WOODEN_SWORD),
            description = "Deal 4 damage to monsters and animals."
        ),
        CraftingRecipe(
            id = "stone_sword",
            name = "Stone Sword",
            category = RecipeCategory.COMBAT,
            pattern = listOf("C", "C", "S"),
            key = mapOf('C' to cobblestone, 'S' to stick),
            output = ItemStack(itemType = ItemType.STONE_SWORD),
            description = "Deal 5 damage with higher durability."
        ),
        CraftingRecipe(
            id = "iron_sword",
            name = "Iron Sword",
            category = RecipeCategory.COMBAT,
            pattern = listOf("I", "I", "S"),
            key = mapOf('I' to ironIngot, 'S' to stick),
            output = ItemStack(itemType = ItemType.IRON_SWORD),
            description = "Standard knight blade dealing 6 damage."
        ),
        CraftingRecipe(
            id = "diamond_sword",
            name = "Diamond Sword",
            category = RecipeCategory.COMBAT,
            pattern = listOf("D", "D", "S"),
            key = mapOf('D' to diamond, 'S' to stick),
            output = ItemStack(itemType = ItemType.DIAMOND_SWORD),
            description = "Deadly weapon dealing 7 base damage."
        ),

        // Food & Misc
        CraftingRecipe(
            id = "bread",
            name = "Bread",
            category = RecipeCategory.FOOD,
            pattern = listOf("WWW"),
            key = mapOf('W' to wheat),
            output = ItemStack(itemType = ItemType.BREAD),
            description = "Restores 5 hunger points and saturation."
        ),
        CraftingRecipe(
            id = "tnt",
            name = "TNT",
            category = RecipeCategory.REDSTONE,
            pattern = listOf("GSG", "SGS", "GSG"),
            key = mapOf(
                'G' to ItemStack(itemType = ItemType.GUNPOWDER),
                'S' to ItemStack(blockType = BlockType.SAND)
            ),
            output = ItemStack(blockType = BlockType.TNT),
            description = "Explosive block primed by fire, redstone, or attack."
        )
    )
}
