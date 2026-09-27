package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.engine.BlockWorldEngine
import com.example.game.model.*
import kotlin.math.*

@Composable
fun GameCanvas(
    engine: BlockWorldEngine,
    modifier: Modifier = Modifier,
    onBlockTap: (Int, Int) -> Unit = { _, _ -> },
    onBlockLongPress: (Int, Int) -> Unit = { _, _ -> },
    onMobTap: (Mob) -> Unit = {}
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(engine) {
                detectTapGestures(
                    onTap = { offset ->
                        // Calculate world coords from screen coords
                        val blockSize = 48f
                        val screenCenterX = size.width / 2f
                        val screenCenterY = size.height / 2f

                        val worldX = (offset.x - screenCenterX) / blockSize + engine.cameraX
                        val worldY = (offset.y - screenCenterY) / blockSize + engine.cameraY

                        // Check if tapped a mob
                        val tappedMob = engine.mobs.find { mob ->
                            val dx = abs(mob.x - worldX)
                            val dy = abs((mob.y - mob.type.height / 2) - worldY)
                            dx < mob.type.width / 2 + 0.3f && dy < mob.type.height / 2 + 0.3f
                        }

                        if (tappedMob != null) {
                            onMobTap(tappedMob)
                        } else {
                            val bx = floor(worldX).toInt()
                            val by = floor(worldY).toInt()
                            onBlockTap(bx, by)
                        }
                    },
                    onLongPress = { offset ->
                        val blockSize = 48f
                        val screenCenterX = size.width / 2f
                        val screenCenterY = size.height / 2f

                        val worldX = (offset.x - screenCenterX) / blockSize + engine.cameraX
                        val worldY = (offset.y - screenCenterY) / blockSize + engine.cameraY

                        val bx = floor(worldX).toInt()
                        val by = floor(worldY).toInt()
                        onBlockLongPress(bx, by)
                    }
                )
            }
    ) {
        val blockSize = 48f
        val screenCenterX = size.width / 2f
        val screenCenterY = size.height / 2f

        // Draw Dynamic Sky
        drawSky(engine.timeOfDay)

        // Viewport bounds in block coordinates
        val minBx = max(0, floor(engine.cameraX - (screenCenterX / blockSize) - 1).toInt())
        val maxBx = min(engine.width - 1, ceil(engine.cameraX + (screenCenterX / blockSize) + 1).toInt())
        val minBy = max(0, floor(engine.cameraY - (screenCenterY / blockSize) - 1).toInt())
        val maxBy = min(engine.height - 1, ceil(engine.cameraY + (screenCenterY / blockSize) + 1).toInt())

        // 1. Draw Blocks
        for (bx in minBx..maxBx) {
            for (by in minBy..maxBy) {
                val block = engine.worldBlocks[bx][by]
                if (block != BlockType.AIR) {
                    val px = screenCenterX + (bx - engine.cameraX) * blockSize
                    val py = screenCenterY + (by - engine.cameraY) * blockSize
                    drawBlock(block, px, py, blockSize)
                }
            }
        }

        // 2. Draw Breaking Overlay
        engine.breakingBlock?.let { bb ->
            if (bb.x in minBx..maxBx && bb.y in minBy..maxBy) {
                val px = screenCenterX + (bb.x - engine.cameraX) * blockSize
                val py = screenCenterY + (bb.y - engine.cameraY) * blockSize
                drawCrackStage(px, py, blockSize, bb.progress)
            }
        }

        // 3. Draw Active Primed TNT
        for (tnt in engine.activeTnt) {
            val px = screenCenterX + (tnt.x - 0.5f - engine.cameraX) * blockSize
            val py = screenCenterY + (tnt.y - 0.5f - engine.cameraY) * blockSize
            val flashColor = if (tnt.flash) Color.White else Color(0xFFD32F2F)
            drawRect(
                color = flashColor,
                topLeft = Offset(px, py),
                size = Size(blockSize, blockSize)
            )
            // Draw band
            drawRect(
                color = Color.White,
                topLeft = Offset(px, py + blockSize * 0.35f),
                size = Size(blockSize, blockSize * 0.3f)
            )
        }

        // 4. Draw Floating Drops
        for (drop in engine.floatingDrops) {
            val px = screenCenterX + (drop.x - engine.cameraX) * blockSize
            val bobY = sin(drop.age * 5f) * 4f
            val py = screenCenterY + (drop.y - engine.cameraY) * blockSize + bobY
            drawDrop(drop.item, px, py, blockSize * 0.45f)
        }

        // 5. Draw Mobs
        for (mob in engine.mobs) {
            val px = screenCenterX + (mob.x - engine.cameraX) * blockSize
            val py = screenCenterY + (mob.y - engine.cameraY) * blockSize
            drawMob(mob, px, py, blockSize)
        }

        // 6. Draw Player (Steve)
        val playerPx = screenCenterX + (engine.playerX - engine.cameraX) * blockSize
        val playerPy = screenCenterY + (engine.playerY - engine.cameraY) * blockSize
        drawPlayer(engine, playerPx, playerPy, blockSize)

        // 7. Draw Particles
        for (p in engine.particles) {
            val px = screenCenterX + (p.x - engine.cameraX) * blockSize
            val py = screenCenterY + (p.y - engine.cameraY) * blockSize
            drawCircle(
                color = p.color.copy(alpha = p.life),
                radius = p.size * (p.life.coerceIn(0.2f, 1f)),
                center = Offset(px, py)
            )
        }

        // 8. Night Darkness & Torch Light Overlay
        drawLightingOverlay(engine, minBx, maxBx, minBy, maxBy, blockSize, screenCenterX, screenCenterY)
    }
}

private fun DrawScope.drawSky(timeOfDay: Float) {
    // 0.0 to 0.4: Day, 0.4 to 0.5: Sunset, 0.5 to 0.9: Night, 0.9 to 1.0: Sunrise
    val dayColor = Color(0xFF78A7FF)
    val sunsetColor = Color(0xFFE65100)
    val nightColor = Color(0xFF090D1A)

    val skyColor = when {
        timeOfDay < 0.4f -> dayColor
        timeOfDay < 0.5f -> {
            val t = (timeOfDay - 0.4f) / 0.1f
            lerp(dayColor, sunsetColor, t)
        }
        timeOfDay < 0.55f -> {
            val t = (timeOfDay - 0.5f) / 0.05f
            lerp(sunsetColor, nightColor, t)
        }
        timeOfDay < 0.85f -> nightColor
        timeOfDay < 0.95f -> {
            val t = (timeOfDay - 0.85f) / 0.1f
            lerp(nightColor, sunsetColor, t)
        }
        else -> {
            val t = (timeOfDay - 0.95f) / 0.05f
            lerp(sunsetColor, dayColor, t)
        }
    }

    drawRect(color = skyColor)

    // Draw Sun & Moon
    val celestialAngle = (timeOfDay * 2 * PI).toFloat()
    val sunDistance = size.height * 0.4f
    val sunX = size.width / 2f + cos(celestialAngle - PI.toFloat() / 2f) * (size.width * 0.45f)
    val sunY = size.height * 0.45f + sin(celestialAngle - PI.toFloat() / 2f) * sunDistance

    // Sun (Square blocky Minecraft style)
    if (sunY < size.height * 0.7f && timeOfDay in 0.0f..0.55f) {
        drawRect(
            color = Color(0xFFFFF176),
            topLeft = Offset(sunX - 24f, sunY - 24f),
            size = Size(48f, 48f)
        )
    }

    // Moon (opposite)
    val moonX = size.width / 2f - cos(celestialAngle - PI.toFloat() / 2f) * (size.width * 0.45f)
    val moonY = size.height * 0.45f - sin(celestialAngle - PI.toFloat() / 2f) * sunDistance
    if (moonY < size.height * 0.7f && (timeOfDay > 0.45f || timeOfDay < 0.05f)) {
        drawRect(
            color = Color(0xFFEEEEEE),
            topLeft = Offset(moonX - 20f, moonY - 20f),
            size = Size(40f, 40f)
        )
    }

    // Stars at night
    if (timeOfDay in 0.5f..0.9f) {
        val starAlpha = when {
            timeOfDay < 0.55f -> (timeOfDay - 0.5f) / 0.05f
            timeOfDay > 0.85f -> (0.9f - timeOfDay) / 0.05f
            else -> 1f
        }
        val starRand = kotlin.random.Random(42)
        for (i in 0 until 40) {
            val sx = starRand.nextFloat() * size.width
            val sy = starRand.nextFloat() * size.height * 0.55f
            drawRect(
                color = Color.White.copy(alpha = starAlpha * 0.8f),
                topLeft = Offset(sx, sy),
                size = Size(3f, 3f)
            )
        }
    }
}

private fun DrawScope.drawBlock(block: BlockType, px: Float, py: Float, s: Float) {
    when (block) {
        BlockType.GRASS -> {
            // Dirt base
            drawRect(Color(0xFF866043), Offset(px, py), Size(s, s))
            // Lush grass top (top 28%)
            drawRect(Color(0xFF5D9938), Offset(px, py), Size(s, s * 0.32f))
            // Grass dripping pixels
            drawRect(Color(0xFF4C802D), Offset(px + s * 0.2f, py + s * 0.32f), Size(s * 0.15f, s * 0.15f))
            drawRect(Color(0xFF4C802D), Offset(px + s * 0.6f, py + s * 0.32f), Size(s * 0.2f, s * 0.2f))
        }
        BlockType.DIRT -> {
            drawRect(Color(0xFF866043), Offset(px, py), Size(s, s))
            // Speckles
            drawRect(Color(0xFF6B4B32), Offset(px + s * 0.2f, py + s * 0.3f), Size(s * 0.2f, s * 0.2f))
            drawRect(Color(0xFF9E7554), Offset(px + s * 0.6f, py + s * 0.65f), Size(s * 0.2f, s * 0.2f))
        }
        BlockType.STONE -> {
            drawRect(Color(0xFF757575), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF616161), Offset(px + s * 0.15f, py + s * 0.2f), Size(s * 0.25f, s * 0.2f))
            drawRect(Color(0xFF9E9E9E), Offset(px + s * 0.55f, py + s * 0.55f), Size(s * 0.3f, s * 0.25f))
        }
        BlockType.COBBLESTONE -> {
            drawRect(Color(0xFF616161), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF424242), Offset(px + 2f, py + 2f), Size(s * 0.45f, s * 0.45f))
            drawRect(Color(0xFF757575), Offset(px + s * 0.5f, py + s * 0.5f), Size(s * 0.45f, s * 0.45f))
            drawRect(Color(0xFF9E9E9E), Offset(px + s * 0.1f, py + s * 0.7f), Size(s * 0.35f, s * 0.25f))
        }
        BlockType.OAK_LOG -> {
            drawRect(Color(0xFF6D5032), Offset(px, py), Size(s, s))
            // Tree bark stripes
            drawRect(Color(0xFF4E3721), Offset(px + s * 0.25f, py), Size(s * 0.18f, s))
            drawRect(Color(0xFF4E3721), Offset(px + s * 0.65f, py), Size(s * 0.18f, s))
        }
        BlockType.OAK_LEAVES -> {
            drawRect(Color(0xFF388E3C), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF2E7D32), Offset(px + s * 0.1f, py + s * 0.1f), Size(s * 0.35f, s * 0.35f))
            drawRect(Color(0xFF4CAF50), Offset(px + s * 0.55f, py + s * 0.5f), Size(s * 0.35f, s * 0.35f))
        }
        BlockType.OAK_PLANKS -> {
            drawRect(Color(0xFFB8945F), Offset(px, py), Size(s, s))
            // Planks horizontal lines
            drawRect(Color(0xFF8D6E40), Offset(px, py + s * 0.32f), Size(s, 2f))
            drawRect(Color(0xFF8D6E40), Offset(px, py + s * 0.65f), Size(s, 2f))
            drawRect(Color(0xFF8D6E40), Offset(px + s * 0.5f, py), Size(2f, s * 0.32f))
            drawRect(Color(0xFF8D6E40), Offset(px + s * 0.3f, py + s * 0.65f), Size(2f, s * 0.35f))
        }
        BlockType.COAL_ORE -> {
            drawRect(Color(0xFF757575), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF1E1E1E), Offset(px + s * 0.2f, py + s * 0.25f), Size(s * 0.25f, s * 0.25f))
            drawRect(Color(0xFF1E1E1E), Offset(px + s * 0.55f, py + s * 0.55f), Size(s * 0.3f, s * 0.3f))
        }
        BlockType.IRON_ORE -> {
            drawRect(Color(0xFF757575), Offset(px, py), Size(s, s))
            drawRect(Color(0xFFD7CCC8), Offset(px + s * 0.2f, py + s * 0.2f), Size(s * 0.28f, s * 0.28f))
            drawRect(Color(0xFFBCAAA4), Offset(px + s * 0.55f, py + s * 0.55f), Size(s * 0.3f, s * 0.3f))
        }
        BlockType.GOLD_ORE -> {
            drawRect(Color(0xFF757575), Offset(px, py), Size(s, s))
            drawRect(Color(0xFFFFD54F), Offset(px + s * 0.2f, py + s * 0.25f), Size(s * 0.25f, s * 0.25f))
            drawRect(Color(0xFFFFC107), Offset(px + s * 0.55f, py + s * 0.55f), Size(s * 0.28f, s * 0.28f))
        }
        BlockType.DIAMOND_ORE -> {
            drawRect(Color(0xFF757575), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF4DD0E1), Offset(px + s * 0.2f, py + s * 0.2f), Size(s * 0.25f, s * 0.25f))
            drawRect(Color(0xFF26C6DA), Offset(px + s * 0.55f, py + s * 0.55f), Size(s * 0.3f, s * 0.3f))
        }
        BlockType.OBSIDIAN -> {
            drawRect(Color(0xFF1A1528), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF311B92), Offset(px + s * 0.2f, py + s * 0.3f), Size(s * 0.25f, s * 0.25f))
            drawRect(Color(0xFF4A148C), Offset(px + s * 0.6f, py + s * 0.6f), Size(s * 0.25f, s * 0.25f))
        }
        BlockType.BEDROCK -> {
            drawRect(Color(0xFF212121), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF424242), Offset(px + s * 0.1f, py + s * 0.1f), Size(s * 0.3f, s * 0.3f))
            drawRect(Color(0xFF121212), Offset(px + s * 0.5f, py + s * 0.5f), Size(s * 0.4f, s * 0.4f))
        }
        BlockType.SAND -> {
            drawRect(Color(0xFFE0C890), Offset(px, py), Size(s, s))
            drawRect(Color(0xFFD2B57B), Offset(px + s * 0.3f, py + s * 0.4f), Size(s * 0.2f, s * 0.2f))
        }
        BlockType.GLASS -> {
            drawRect(Color(0x33E0F7FA), Offset(px, py), Size(s, s))
            drawRect(Color(0x88FFFFFF), Offset(px, py), Size(s, s), style = Stroke(width = 2f))
            drawLine(Color(0x66FFFFFF), Offset(px + 4f, py + 4f), Offset(px + s * 0.4f, py + s * 0.4f), strokeWidth = 2f)
        }
        BlockType.CRAFTING_TABLE -> {
            drawRect(Color(0xFFB8945F), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF6D5032), Offset(px, py), Size(s, s * 0.25f))
            drawRect(Color(0xFF4E3721), Offset(px + s * 0.2f, py + s * 0.4f), Size(s * 0.6f, s * 0.45f))
        }
        BlockType.FURNACE -> {
            drawRect(Color(0xFF616161), Offset(px, py), Size(s, s))
            // Opening
            drawRect(Color(0xFF212121), Offset(px + s * 0.25f, py + s * 0.3f), Size(s * 0.5f, s * 0.45f))
        }
        BlockType.CHEST -> {
            drawRect(Color(0xFFA17838), Offset(px, py), Size(s, s))
            drawRect(Color(0xFF6D5032), Offset(px, py), Size(s, s), style = Stroke(width = 3f))
            drawRect(Color(0xFFEEEEEE), Offset(px + s * 0.4f, py + s * 0.35f), Size(s * 0.2f, s * 0.25f))
        }
        BlockType.BRICKS -> {
            drawRect(Color(0xFF9E4B3E), Offset(px, py), Size(s, s))
            drawRect(Color(0xFFD7CCC8), Offset(px, py + s * 0.5f), Size(s, 2f))
            drawRect(Color(0xFFD7CCC8), Offset(px + s * 0.5f, py), Size(2f, s * 0.5f))
        }
        BlockType.TORCH -> {
            // Stick
            drawRect(Color(0xFF8D6E40), Offset(px + s * 0.42f, py + s * 0.35f), Size(s * 0.16f, s * 0.55f))
            // Flame
            drawRect(Color(0xFFFFC107), Offset(px + s * 0.38f, py + s * 0.15f), Size(s * 0.24f, s * 0.25f))
            drawRect(Color(0xFFFF5722), Offset(px + s * 0.42f, py + s * 0.18f), Size(s * 0.16f, s * 0.18f))
        }
        BlockType.TNT -> {
            drawRect(Color(0xFFD32F2F), Offset(px, py), Size(s, s))
            // White band
            drawRect(Color.White, Offset(px, py + s * 0.35f), Size(s, s * 0.3f))
            drawRect(Color.Black, Offset(px + s * 0.2f, py + s * 0.42f), Size(s * 0.6f, s * 0.16f))
        }
        BlockType.WATER -> {
            drawRect(Color(0x992196F3), Offset(px, py), Size(s, s))
        }
        BlockType.LAVA -> {
            drawRect(Color(0xEEFF5722), Offset(px, py), Size(s, s))
            drawRect(Color(0xFFFFC107), Offset(px + s * 0.2f, py + s * 0.2f), Size(s * 0.3f, s * 0.25f))
        }
        BlockType.AIR -> {}
    }
}

private fun DrawScope.drawCrackStage(px: Float, py: Float, s: Float, progress: Float) {
    val stage = (progress * 5).toInt().coerceIn(1, 5)
    val crackColor = Color.Black.copy(alpha = 0.65f)

    if (stage >= 1) {
        drawLine(crackColor, Offset(px + s * 0.5f, py + s * 0.1f), Offset(px + s * 0.5f, py + s * 0.5f), strokeWidth = 2.5f)
    }
    if (stage >= 2) {
        drawLine(crackColor, Offset(px + s * 0.5f, py + s * 0.5f), Offset(px + s * 0.2f, py + s * 0.8f), strokeWidth = 2.5f)
    }
    if (stage >= 3) {
        drawLine(crackColor, Offset(px + s * 0.5f, py + s * 0.5f), Offset(px + s * 0.8f, py + s * 0.7f), strokeWidth = 2.5f)
    }
    if (stage >= 4) {
        drawLine(crackColor, Offset(px + s * 0.2f, py + s * 0.3f), Offset(px + s * 0.7f, py + s * 0.3f), strokeWidth = 2.5f)
    }
    if (stage >= 5) {
        drawLine(crackColor, Offset(px + s * 0.1f, py + s * 0.9f), Offset(px + s * 0.9f, py + s * 0.1f), strokeWidth = 2.5f)
    }
}

private fun DrawScope.drawDrop(item: ItemStack, px: Float, py: Float, size: Float) {
    if (item.isBlock && item.blockType != null) {
        drawBlock(item.blockType, px - size / 2, py - size / 2, size)
    } else {
        // Draw item miniature
        val color = when (item.itemType) {
            ItemType.DIAMOND -> Color(0xFF4DD0E1)
            ItemType.IRON_INGOT -> Color(0xFFE0E0E0)
            ItemType.GOLD_INGOT -> Color(0xFFFFD54F)
            ItemType.COAL -> Color(0xFF212121)
            ItemType.APPLE -> Color(0xFFE53935)
            ItemType.BREAD -> Color(0xFFFFA000)
            ItemType.STICK -> Color(0xFF8D6E63)
            ItemType.GUNPOWDER -> Color(0xFF757575)
            else -> Color(0xFF90CAF9)
        }
        drawCircle(color, radius = size * 0.6f, center = Offset(px, py))
    }
}

private fun DrawScope.drawMob(mob: Mob, px: Float, py: Float, blockSize: Float) {
    val mw = mob.type.width * blockSize
    val mh = mob.type.height * blockSize
    val topLeftX = px - mw / 2f
    val topLeftY = py - mh

    val damageTint = if (mob.hurtTimer > 0f) Color.Red.copy(alpha = 0.6f) else Color.Transparent

    when (mob.type) {
        MobType.PIG -> {
            // Pink body
            drawRect(Color(0xFFF48FB1), Offset(topLeftX, topLeftY + mh * 0.2f), Size(mw, mh * 0.6f))
            // Snout
            val snoutX = if (mob.facingRight) topLeftX + mw * 0.7f else topLeftX - mw * 0.1f
            drawRect(Color(0xFFEC407A), Offset(snoutX, topLeftY + mh * 0.35f), Size(mw * 0.35f, mh * 0.3f))
            // Legs
            drawRect(Color(0xFFF48FB1), Offset(topLeftX + mw * 0.1f, topLeftY + mh * 0.75f), Size(mw * 0.2f, mh * 0.25f))
            drawRect(Color(0xFFF48FB1), Offset(topLeftX + mw * 0.7f, topLeftY + mh * 0.75f), Size(mw * 0.2f, mh * 0.25f))
            // Eye
            val eyeX = if (mob.facingRight) topLeftX + mw * 0.75f else topLeftX + mw * 0.15f
            drawRect(Color.Black, Offset(eyeX, topLeftY + mh * 0.25f), Size(mw * 0.12f, mh * 0.12f))
        }
        MobType.SHEEP -> {
            // Fluffy wool body
            drawRoundRect(Color(0xFFFAFAFA), Offset(topLeftX, topLeftY + mh * 0.15f), Size(mw, mh * 0.65f), CornerRadius(6f, 6f))
            // Pink face
            val headX = if (mob.facingRight) topLeftX + mw * 0.65f else topLeftX - mw * 0.1f
            drawRect(Color(0xFFF8BBD0), Offset(headX, topLeftY + mh * 0.25f), Size(mw * 0.35f, mh * 0.35f))
            // Legs
            drawRect(Color(0xFFF8BBD0), Offset(topLeftX + mw * 0.15f, topLeftY + mh * 0.75f), Size(mw * 0.18f, mh * 0.25f))
            drawRect(Color(0xFFF8BBD0), Offset(topLeftX + mw * 0.65f, topLeftY + mh * 0.75f), Size(mw * 0.18f, mh * 0.25f))
        }
        MobType.ZOMBIE -> {
            // Green head
            drawRect(Color(0xFF2E7D32), Offset(topLeftX + mw * 0.2f, topLeftY), Size(mw * 0.6f, mh * 0.35f))
            // Cyan Shirt
            drawRect(Color(0xFF00ACC1), Offset(topLeftX + mw * 0.15f, topLeftY + mh * 0.35f), Size(mw * 0.7f, mh * 0.35f))
            // Dark Jeans
            drawRect(Color(0xFF1565C0), Offset(topLeftX + mw * 0.2f, topLeftY + mh * 0.7f), Size(mw * 0.6f, mh * 0.3f))
            // Outstretched zombie arms
            val armX = if (mob.facingRight) topLeftX + mw * 0.6f else topLeftX - mw * 0.4f
            drawRect(Color(0xFF2E7D32), Offset(armX, topLeftY + mh * 0.38f), Size(mw * 0.7f, mh * 0.15f))
        }
        MobType.CREEPER -> {
            val expand = (mob.fuseTimer / 1.5f) * 10f
            val creeperColor = if (mob.fuseTimer > 0f && (mob.fuseTimer * 10).toInt() % 2 == 0) Color.White else Color(0xFF43A047)
            // Head
            drawRect(creeperColor, Offset(topLeftX + mw * 0.15f - expand / 2, topLeftY - expand), Size(mw * 0.7f + expand, mh * 0.4f + expand))
            // Iconic sad Creeper face
            val hx = topLeftX + mw * 0.15f
            drawRect(Color.Black, Offset(hx + mw * 0.15f, topLeftY + mh * 0.12f), Size(mw * 0.15f, mh * 0.1f))
            drawRect(Color.Black, Offset(hx + mw * 0.4f, topLeftY + mh * 0.12f), Size(mw * 0.15f, mh * 0.1f))
            drawRect(Color.Black, Offset(hx + mw * 0.25f, topLeftY + mh * 0.22f), Size(mw * 0.2f, mh * 0.15f))
            // Body
            drawRect(creeperColor, Offset(topLeftX + mw * 0.2f, topLeftY + mh * 0.4f), Size(mw * 0.6f, mh * 0.4f))
            // 4 Feet
            drawRect(creeperColor, Offset(topLeftX + mw * 0.1f, topLeftY + mh * 0.8f), Size(mw * 0.8f, mh * 0.2f))
        }
    }

    if (mob.hurtTimer > 0f) {
        drawRect(damageTint, Offset(topLeftX, topLeftY), Size(mw, mh))
    }
}

private fun DrawScope.drawPlayer(engine: BlockWorldEngine, px: Float, py: Float, blockSize: Float) {
    val pw = 0.6f * blockSize
    val ph = 1.6f * blockSize
    val topLeftX = px - pw / 2f
    val topLeftY = py - ph

    // Head
    drawRect(Color(0xFF8D6E63), Offset(topLeftX + pw * 0.15f, topLeftY), Size(pw * 0.7f, ph * 0.3f))
    // Hair
    drawRect(Color(0xFF3E2723), Offset(topLeftX + pw * 0.15f, topLeftY), Size(pw * 0.7f, ph * 0.08f))
    // Face eyes
    val eyeX = if (engine.facingRight) topLeftX + pw * 0.55f else topLeftX + pw * 0.22f
    drawRect(Color.White, Offset(eyeX, topLeftY + ph * 0.14f), Size(pw * 0.18f, ph * 0.06f))
    drawRect(Color(0xFF1565C0), Offset(eyeX + (if (engine.facingRight) pw * 0.08f else 0f), topLeftY + ph * 0.14f), Size(pw * 0.1f, ph * 0.06f))

    // Torso (Cyan Blue shirt)
    drawRect(Color(0xFF00ACC1), Offset(topLeftX + pw * 0.15f, topLeftY + ph * 0.3f), Size(pw * 0.7f, ph * 0.42f))

    // Legs (Deep blue jeans)
    drawRect(Color(0xFF1A237E), Offset(topLeftX + pw * 0.18f, topLeftY + ph * 0.72f), Size(pw * 0.64f, ph * 0.28f))

    // Arm (with swing animation when mining/attacking)
    val armPivotX = if (engine.facingRight) topLeftX + pw * 0.75f else topLeftX + pw * 0.15f
    val armPivotY = topLeftY + ph * 0.35f
    val baseAngle = if (engine.facingRight) 15f else -15f
    val swingAngle = baseAngle + (if (engine.facingRight) 1f else -1f) * engine.armSwing * 65f

    rotate(swingAngle, Offset(armPivotX, armPivotY)) {
        // Arm
        drawRect(Color(0xFF00ACC1), Offset(armPivotX - pw * 0.12f, armPivotY), Size(pw * 0.24f, ph * 0.25f))
        drawRect(Color(0xFF8D6E63), Offset(armPivotX - pw * 0.12f, armPivotY + ph * 0.25f), Size(pw * 0.24f, ph * 0.12f))

        // Tool / Held item in hand
        val held = engine.hotbar[engine.selectedSlot]
        if (!held.isEmpty) {
            val toolColor = when {
                held.itemType?.name?.contains("DIAMOND") == true -> Color(0xFF4DD0E1)
                held.itemType?.name?.contains("IRON") == true -> Color(0xFFE0E0E0)
                held.itemType?.name?.contains("STONE") == true -> Color(0xFF757575)
                held.itemType?.name?.contains("WOODEN") == true -> Color(0xFF8D6E63)
                held.isBlock -> Color(0xFF4CAF50)
                else -> Color(0xFFFFC107)
            }
            drawRect(
                toolColor,
                Offset(armPivotX - 3f, armPivotY + ph * 0.35f),
                Size(7f, ph * 0.3f)
            )
        }
    }

    // Hurt flash
    if (engine.hurtTimer > 0f) {
        drawRect(Color.Red.copy(alpha = 0.5f), Offset(topLeftX, topLeftY), Size(pw, ph))
    }
}

private fun DrawScope.drawLightingOverlay(
    engine: BlockWorldEngine,
    minBx: Int,
    maxBx: Int,
    minBy: Int,
    maxBy: Int,
    blockSize: Float,
    screenCenterX: Float,
    screenCenterY: Float
) {
    val nightFactor = when {
        engine.timeOfDay in 0.5f..0.9f -> {
            when {
                engine.timeOfDay < 0.6f -> (engine.timeOfDay - 0.5f) / 0.1f
                engine.timeOfDay > 0.8f -> (0.9f - engine.timeOfDay) / 0.1f
                else -> 1f
            }
        }
        else -> 0f
    }

    if (nightFactor > 0.05f) {
        // Collect light sources (torches + lava + player small light)
        val lights = mutableListOf<Offset>()

        // Player gentle glow
        val pScreenX = screenCenterX + (engine.playerX - engine.cameraX) * blockSize
        val pScreenY = screenCenterY + (engine.playerY - 0.8f - engine.cameraY) * blockSize
        lights.add(Offset(pScreenX, pScreenY))

        // Find visible torches and lava
        for (bx in minBx..maxBx) {
            for (by in minBy..maxBy) {
                val block = engine.worldBlocks[bx][by]
                if (block == BlockType.TORCH || block == BlockType.LAVA) {
                    val lx = screenCenterX + (bx + 0.5f - engine.cameraX) * blockSize
                    val ly = screenCenterY + (by + 0.5f - engine.cameraY) * blockSize
                    lights.add(Offset(lx, ly))
                }
            }
        }

        // Apply dark vignette with radial cutouts for lights
        for (light in lights) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x33FFB300),
                        Color.Transparent
                    ),
                    center = light,
                    radius = blockSize * 4.5f
                ),
                radius = blockSize * 4.5f,
                center = light
            )
        }

        // Ambient night shadow
        drawRect(
            color = Color(0x99000511).copy(alpha = 0.72f * nightFactor)
        )
    }
}
