package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.crafting.RecipeBook
import com.example.game.engine.BlockWorldEngine
import com.example.game.model.BlockType
import com.example.game.model.ItemStack
import com.example.game.model.ItemType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MineWorld", appName)
  }

  @Test
  fun `engine initializes terrain and player properly`() {
    val engine = BlockWorldEngine(seed = 42L)
    assertTrue(engine.width > 0)
    assertTrue(engine.height > 0)
    assertTrue(engine.playerHealth == 20)
    // Check bedrock at bottom
    assertEquals(BlockType.BEDROCK, engine.worldBlocks[10][engine.height - 1])
  }

  @Test
  fun `inventory accepts items and stacks`() {
    val engine = BlockWorldEngine()
    val initialStack = ItemStack(blockType = BlockType.DIRT, count = 5)
    assertTrue(engine.addItemToInventory(initialStack))
  }

  @Test
  fun `recipe book contains core crafting recipes`() {
    val recipes = RecipeBook.recipes
    assertTrue(recipes.isNotEmpty())
    assertTrue(recipes.any { it.name == "Oak Planks" })
    assertTrue(recipes.any { it.name == "Diamond Pickaxe" })
  }
}
