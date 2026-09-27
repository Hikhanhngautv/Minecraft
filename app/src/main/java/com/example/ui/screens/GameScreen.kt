package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.MineWorldDatabase
import com.example.data.model.SavedWorld
import com.example.game.engine.BlockWorldEngine
import com.example.game.model.BlockType
import com.example.game.model.Mob
import com.example.game.ui.DPadControls
import com.example.game.ui.GameCanvas
import com.example.game.ui.GameHud
import com.example.game.ui.HotbarView
import com.example.game.ui.InventoryDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GameScreen(
    onNavigateToCrafting: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { MineWorldDatabase.getDatabase(context) }
    val engine = remember { BlockWorldEngine() }

    var showInventoryDialog by remember { mutableStateOf(false) }

    // Haptic feedback function
    val triggerHaptic = remember {
        { durationMs: Long ->
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(durationMs)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Load saved world on launch
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val saved = database.savedWorldDao().getWorld("default_world")
            if (saved != null) {
                withContext(Dispatchers.Main) {
                    engine.playerX = saved.playerX
                    engine.playerY = saved.playerY
                    engine.playerHealth = saved.playerHealth
                    engine.playerHunger = saved.playerHunger
                    engine.isCreative = (saved.gameMode == "CREATIVE")
                    engine.timeOfDay = saved.timeOfDay
                }
            }
        }
    }

    // Auto-save periodically
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(15000) // Every 15 seconds
            withContext(Dispatchers.IO) {
                database.savedWorldDao().saveWorld(
                    SavedWorld(
                        id = "default_world",
                        name = "My Survival World",
                        seed = engine.seed,
                        gameMode = if (engine.isCreative) "CREATIVE" else "SURVIVAL",
                        timeOfDay = engine.timeOfDay,
                        playerX = engine.playerX,
                        playerY = engine.playerY,
                        playerHealth = engine.playerHealth,
                        playerHunger = engine.playerHunger,
                        inventoryJson = "",
                        modifiedBlocksJson = ""
                    )
                )
            }
        }
    }

    // 60 FPS Game Loop
    LaunchedEffect(engine) {
        var lastTime = System.nanoTime()
        while (isActive) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastTime = now

            engine.update(dt)
            delay(16) // ~60fps
        }
    }

    // Observe trigger to recompose HUD and controls
    val trigger by engine.stateUpdateTrigger.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("game_screen")
    ) {
        // 2D Game Canvas
        GameCanvas(
            engine = engine,
            modifier = Modifier.fillMaxSize(),
            onBlockTap = { bx, by ->
                // If air -> try place block, if solid -> try mine
                if (engine.worldBlocks[bx][by] == BlockType.AIR) {
                    val placed = engine.placeBlock(bx, by)
                    if (placed) triggerHaptic(20)
                } else {
                    engine.startMining(bx, by)
                    triggerHaptic(25)
                }
            },
            onBlockLongPress = { bx, by ->
                engine.breakBlock(bx, by)
                triggerHaptic(40)
            },
            onMobTap = { mob ->
                engine.attackMob(mob)
                triggerHaptic(40)
            }
        )

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HUD at top
            GameHud(
                engine = engine,
                onOpenInventory = { showInventoryDialog = true },
                onToggleCreative = {
                    engine.isCreative = !engine.isCreative
                    engine.setupStartingInventory()
                    triggerHaptic(30)
                }
            )

            // Bottom controls: Hotbar + DPad & Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hotbar
                HotbarView(
                    engine = engine,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // DPad & Action Buttons
                DPadControls(
                    engine = engine,
                    onMineAction = {
                        // Mine adjacent block in facing direction
                        val targetBx = (engine.playerX + if (engine.facingRight) 1f else -1f).toInt()
                        val targetBy = (engine.playerY - 0.5f).toInt()
                        engine.startMining(targetBx, targetBy)
                        triggerHaptic(30)
                    },
                    onPlaceAction = {
                        val targetBx = (engine.playerX + if (engine.facingRight) 1f else -1f).toInt()
                        val targetBy = (engine.playerY - 0.5f).toInt()
                        val placed = engine.placeBlock(targetBx, targetBy)
                        if (placed) triggerHaptic(25)
                    }
                )
            }
        }

        // Inventory & Quick Craft Dialog
        if (showInventoryDialog) {
            InventoryDialog(
                engine = engine,
                onDismiss = { showInventoryDialog = false }
            )
        }
    }
}
