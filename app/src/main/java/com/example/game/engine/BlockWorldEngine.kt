package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.crafting.RecipeBook
import com.example.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

class BlockWorldEngine(
    val width: Int = 100,
    val height: Int = 40,
    var isCreative: Boolean = false,
    val seed: Long = 1337L
) {
    val worldBlocks = Array(width) { Array(height) { BlockType.AIR } }
    
    // Player State
    var playerX: Float = width / 2f
    var playerY: Float = 16f
    var playerVx: Float = 0f
    var playerVy: Float = 0f
    var playerHealth: Int = 20
    var playerHunger: Int = 20
    var isGrounded: Boolean = false
    var facingRight: Boolean = true
    var armSwing: Float = 0f
    var hurtTimer: Float = 0f

    // Input States
    var moveLeft: Boolean = false
    var moveRight: Boolean = false
    var jumpRequested: Boolean = false

    // Hotbar (7 slots)
    val hotbar = Array(7) { ItemStack() }
    var selectedSlot: Int = 0

    // Full 20-slot Inventory
    val inventory = Array(20) { ItemStack() }

    // Day/Night Cycle (0.0f = morning, 0.25f = noon, 0.5f = dusk, 0.75f = midnight, 1.0f = dawn)
    var timeOfDay: Float = 0.15f
    val dayDurationSeconds: Float = 120f // 2 minutes per cycle

    // Dynamic Game Entities
    val mobs = mutableListOf<Mob>()
    val particles = mutableListOf<Particle>()
    val floatingDrops = mutableListOf<FloatingDrop>()
    var breakingBlock: BreakingBlock? = null
    var activeTnt = mutableListOf<TntEntity>()

    // Viewport camera
    var cameraX: Float = playerX
    var cameraY: Float = playerY

    data class TntEntity(
        var x: Float,
        var y: Float,
        var fuse: Float = 2.0f,
        var flash: Boolean = false
    )

    private val _stateUpdateTrigger = MutableStateFlow(0L)
    val stateUpdateTrigger: StateFlow<Long> = _stateUpdateTrigger.asStateFlow()

    init {
        generateTerrain()
        setupStartingInventory()
    }

    private fun generateTerrain() {
        val rand = Random(seed)
        val surfaceLevel = 18

        for (x in 0 until width) {
            // Perlin-like simple hill sine wave
            val hillOffset = (sin(x * 0.12) * 2.5 + cos(x * 0.05) * 2.0).toInt()
            val groundY = (surfaceLevel + hillOffset).coerceIn(12, 25)

            // Bedrock at bottom
            worldBlocks[x][height - 1] = BlockType.BEDROCK
            if (rand.nextFloat() < 0.5f) {
                worldBlocks[x][height - 2] = BlockType.BEDROCK
            }

            for (y in 0 until height - 1) {
                when {
                    y < groundY -> {
                        worldBlocks[x][y] = BlockType.AIR
                    }
                    y == groundY -> {
                        worldBlocks[x][y] = BlockType.GRASS
                    }
                    y in (groundY + 1)..(groundY + 4) -> {
                        worldBlocks[x][y] = BlockType.DIRT
                    }
                    y in (groundY + 5) until (height - 1) -> {
                        // Deep underground generation with ores and caves
                        val oreRoll = rand.nextFloat()
                        val isCave = (sin(x * 0.3) * cos(y * 0.3) > 0.65) && y > groundY + 7

                        if (isCave) {
                            worldBlocks[x][y] = if (y >= height - 4 && rand.nextFloat() < 0.6f) BlockType.LAVA else BlockType.AIR
                        } else {
                            worldBlocks[x][y] = when {
                                y >= height - 6 && oreRoll < 0.04f -> BlockType.DIAMOND_ORE
                                y >= height - 8 && oreRoll < 0.03f -> BlockType.OBSIDIAN
                                y >= height - 12 && oreRoll < 0.06f -> BlockType.GOLD_ORE
                                oreRoll < 0.12f -> BlockType.IRON_ORE
                                oreRoll < 0.22f -> BlockType.COAL_ORE
                                else -> BlockType.STONE
                            }
                        }
                    }
                }
            }
        }

        // Generate Trees on surface
        for (x in 4 until width - 4 step 8) {
            if (rand.nextFloat() < 0.75f) {
                val groundY = getGroundHeightAt(x)
                if (groundY in 10 until 28 && worldBlocks[x][groundY] == BlockType.GRASS) {
                    val trunkHeight = rand.nextInt(3, 5)
                    // Trunk
                    for (ty in 1..trunkHeight) {
                        if (groundY - ty >= 0) {
                            worldBlocks[x][groundY - ty] = BlockType.OAK_LOG
                        }
                    }
                    // Canopy
                    val leafTop = groundY - trunkHeight
                    for (lx in (x - 2)..(x + 2)) {
                        for (ly in (leafTop - 2)..(leafTop)) {
                            if (lx in 0 until width && ly >= 0) {
                                if (worldBlocks[lx][ly] == BlockType.AIR && (abs(lx - x) + abs(ly - leafTop) <= 3)) {
                                    worldBlocks[lx][ly] = BlockType.OAK_LEAVES
                                }
                            }
                        }
                    }
                    if (leafTop - 3 >= 0) {
                        worldBlocks[x][leafTop - 3] = BlockType.OAK_LEAVES
                    }
                }
            }
        }

        // Position player on safe surface ground
        val spawnX = width / 2
        val spawnY = getGroundHeightAt(spawnX) - 2f
        playerX = spawnX.toFloat()
        playerY = spawnY.coerceAtLeast(4f)
        cameraX = playerX
        cameraY = playerY

        // Spawn initial friendly & hostile mobs
        spawnMob(MobType.PIG, playerX - 6f, getGroundHeightAt((playerX - 6).toInt()) - 1f)
        spawnMob(MobType.SHEEP, playerX + 7f, getGroundHeightAt((playerX + 7).toInt()) - 1f)
        spawnMob(MobType.ZOMBIE, playerX + 16f, getGroundHeightAt((playerX + 16).toInt()) - 1f)
    }

    private fun getGroundHeightAt(x: Int): Int {
        for (y in 0 until height) {
            if (worldBlocks[x][y].isSolid) return y
        }
        return height - 5
    }

    fun setupStartingInventory() {
        if (isCreative) {
            hotbar[0] = ItemStack(blockType = BlockType.GRASS, count = 64)
            hotbar[1] = ItemStack(blockType = BlockType.OAK_PLANKS, count = 64)
            hotbar[2] = ItemStack(blockType = BlockType.COBBLESTONE, count = 64)
            hotbar[3] = ItemStack(blockType = BlockType.GLASS, count = 64)
            hotbar[4] = ItemStack(blockType = BlockType.TORCH, count = 64)
            hotbar[5] = ItemStack(blockType = BlockType.TNT, count = 64)
            hotbar[6] = ItemStack(itemType = ItemType.DIAMOND_PICKAXE, count = 1)
        } else {
            hotbar[0] = ItemStack(itemType = ItemType.WOODEN_PICKAXE, count = 1)
            hotbar[1] = ItemStack(itemType = ItemType.WOODEN_AXE, count = 1)
            hotbar[2] = ItemStack(itemType = ItemType.WOODEN_SWORD, count = 1)
            hotbar[3] = ItemStack(blockType = BlockType.OAK_PLANKS, count = 16)
            hotbar[4] = ItemStack(blockType = BlockType.TORCH, count = 8)
            hotbar[5] = ItemStack(itemType = ItemType.BREAD, count = 6)
            hotbar[6] = ItemStack(blockType = BlockType.CRAFTING_TABLE, count = 1)
        }
    }

    fun spawnMob(type: MobType, x: Float, y: Float) {
        val mob = Mob(
            id = System.nanoTime() + Random.nextLong(1000),
            type = type,
            x = x.coerceIn(1f, (width - 2).toFloat()),
            y = y.coerceIn(1f, (height - 3).toFloat())
        )
        mobs.add(mob)
    }

    fun addItemToInventory(stack: ItemStack): Boolean {
        // Try hotbar first
        for (i in 0 until 7) {
            if (hotbar[i].blockType == stack.blockType && hotbar[i].itemType == stack.itemType) {
                hotbar[i] = hotbar[i].copy(count = hotbar[i].count + stack.count)
                return true
            }
        }
        for (i in 0 until 7) {
            if (hotbar[i].isEmpty) {
                hotbar[i] = stack
                return true
            }
        }
        // Try inventory
        for (i in 0 until 20) {
            if (inventory[i].blockType == stack.blockType && inventory[i].itemType == stack.itemType) {
                inventory[i] = inventory[i].copy(count = inventory[i].count + stack.count)
                return true
            }
        }
        for (i in 0 until 20) {
            if (inventory[i].isEmpty) {
                inventory[i] = stack
                return true
            }
        }
        return false
    }

    // Called every physics tick (~60fps, dt ~ 0.016s)
    fun update(dt: Float) {
        // 1. Time of Day
        timeOfDay = (timeOfDay + (dt / dayDurationSeconds)) % 1.0f

        // 2. Player Input Movement
        val walkSpeed = 0.12f
        if (moveLeft) {
            playerVx = -walkSpeed
            facingRight = false
        } else if (moveRight) {
            playerVx = walkSpeed
            facingRight = true
        } else {
            playerVx *= 0.7f
            if (abs(playerVx) < 0.01f) playerVx = 0f
        }

        // Jump
        if (jumpRequested && isGrounded) {
            playerVy = -0.32f
            isGrounded = false
            jumpRequested = false
        }

        // Gravity
        val gravity = 0.018f
        playerVy = (playerVy + gravity).coerceAtMost(0.45f)

        // Collision Check for Player
        resolvePlayerPhysics()

        // Arm swing decay
        if (armSwing > 0f) {
            armSwing = (armSwing - dt * 4f).coerceAtLeast(0f)
        }
        if (hurtTimer > 0f) {
            hurtTimer = (hurtTimer - dt).coerceAtLeast(0f)
        }

        // Camera follow smooth
        cameraX += (playerX - cameraX) * 0.12f
        cameraY += (playerY - cameraY) * 0.12f

        // 3. Mobs Physics & AI
        updateMobs(dt)

        // 4. Update TNT explosions
        updateTnt(dt)

        // 5. Update Floating Drops
        updateFloatingDrops(dt)

        // 6. Update Particles
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.008f // gravity
            p.life -= dt * 1.5f
            if (p.life <= 0f) {
                particleIterator.remove()
            }
        }

        // 7. Update Breaking Block
        breakingBlock?.let { bb ->
            bb.progress += dt / bb.requiredTime
            armSwing = 1f
            if (bb.progress >= 1.0f) {
                breakBlock(bb.x, bb.y)
                breakingBlock = null
            }
        }

        _stateUpdateTrigger.value = System.currentTimeMillis()
    }

    private fun resolvePlayerPhysics() {
        val playerW = 0.6f
        val playerH = 1.6f

        // Horizontal step
        val nextX = playerX + playerVx
        val minX = (nextX - playerW / 2).toInt()
        val maxX = (nextX + playerW / 2).toInt()
        val minY = (playerY - playerH).toInt()
        val maxY = (playerY - 0.05f).toInt()

        var collidesX = false
        for (bx in minX..maxX) {
            for (by in minY..maxY) {
                if (isBlockSolid(bx, by)) {
                    collidesX = true
                    break
                }
            }
            if (collidesX) break
        }

        if (!collidesX) {
            playerX = nextX.coerceIn(1f, (width - 2).toFloat())
        } else {
            playerVx = 0f
        }

        // Vertical step
        val nextY = playerY + playerVy
        val pMinX = (playerX - playerW / 2 + 0.05f).toInt()
        val pMaxX = (playerX + playerW / 2 - 0.05f).toInt()

        if (playerVy > 0) {
            // Falling down
            val checkY = (nextY).toInt()
            var hitGround = false
            for (bx in pMinX..pMaxX) {
                if (isBlockSolid(bx, checkY)) {
                    hitGround = true
                    break
                }
            }
            if (hitGround) {
                playerY = checkY.toFloat()
                playerVy = 0f
                isGrounded = true
            } else {
                playerY = nextY
                isGrounded = false
            }
        } else if (playerVy < 0) {
            // Jumping up
            val checkY = (nextY - playerH).toInt()
            var hitCeiling = false
            for (bx in pMinX..pMaxX) {
                if (isBlockSolid(bx, checkY)) {
                    hitCeiling = true
                    break
                }
            }
            if (hitCeiling) {
                playerY = checkY + 1f + playerH
                playerVy = 0f
            } else {
                playerY = nextY
            }
        }

        // Environmental Hazards (Lava)
        val currentBlock = getBlockAt(playerX.toInt(), playerY.toInt())
        if (currentBlock == BlockType.LAVA && !isCreative) {
            if (hurtTimer <= 0f) {
                playerHealth = (playerHealth - 4).coerceAtLeast(0)
                hurtTimer = 0.8f
                spawnBlockParticles(playerX, playerY, Color(0xFFFF5722), 8)
            }
        }
    }

    private fun updateMobs(dt: Float) {
        val mobIter = mobs.iterator()
        while (mobIter.hasNext()) {
            val mob = mobIter.next()
            if (mob.health <= 0) {
                // Drop items
                when (mob.type) {
                    MobType.PIG -> spawnFloatingDrop(mob.x, mob.y, ItemStack(itemType = ItemType.RAW_PORKCHOP, count = Random.nextInt(1, 3)))
                    MobType.SHEEP -> spawnFloatingDrop(mob.x, mob.y, ItemStack(itemType = ItemType.WHEAT, count = 1))
                    MobType.ZOMBIE -> spawnFloatingDrop(mob.x, mob.y, ItemStack(itemType = ItemType.IRON_INGOT, count = if (Random.nextFloat() < 0.2f) 1 else 0))
                    MobType.CREEPER -> spawnFloatingDrop(mob.x, mob.y, ItemStack(itemType = ItemType.GUNPOWDER, count = Random.nextInt(1, 3)))
                }
                spawnBlockParticles(mob.x, mob.y - 0.5f, Color.White, 12)
                mobIter.remove()
                continue
            }

            // AI Logic
            val distToPlayer = hypot(playerX - mob.x, playerY - mob.y)

            if (mob.type.isHostile) {
                // Chase player
                if (distToPlayer < 14f) {
                    mob.facingRight = playerX > mob.x
                    val dir = if (mob.facingRight) 1f else -1f
                    mob.vx = dir * 0.05f

                    // Creeper fuse
                    if (mob.type == MobType.CREEPER) {
                        if (distToPlayer < 2.5f) {
                            mob.fuseTimer += dt
                            if (mob.fuseTimer > 1.5f) {
                                // Explode!
                                explode(mob.x.toInt(), (mob.y - 0.5f).toInt(), radius = 3)
                                mob.health = 0
                            }
                        } else {
                            mob.fuseTimer = (mob.fuseTimer - dt).coerceAtLeast(0f)
                        }
                    }

                    // Zombie attack
                    if (mob.type == MobType.ZOMBIE && distToPlayer < 1.2f && !isCreative) {
                        if (hurtTimer <= 0f) {
                            playerHealth = (playerHealth - mob.type.attackDamage).coerceAtLeast(0)
                            hurtTimer = 0.8f
                            playerVy = -0.15f
                            playerVx = if (mob.facingRight) 0.1f else -0.1f
                        }
                    }
                } else {
                    mob.vx = 0f
                }
            } else {
                // Passive wander
                mob.walkTimer += dt
                if (mob.walkTimer > 3f) {
                    mob.walkTimer = 0f
                    if (Random.nextFloat() < 0.6f) {
                        mob.facingRight = Random.nextBoolean()
                        mob.vx = if (mob.facingRight) 0.03f else -0.03f
                    } else {
                        mob.vx = 0f
                    }
                }
            }

            // Gravity
            mob.vy = (mob.vy + 0.018f).coerceAtMost(0.4f)

            // Step mob X
            val nextX = mob.x + mob.vx
            val footX = nextX.toInt()
            val footY = mob.y.toInt()

            if (isBlockSolid(footX, footY - 1)) {
                // Mob jump over 1 block obstacle
                if (mob.isGrounded && !isBlockSolid(footX, footY - 2)) {
                    mob.vy = -0.22f
                } else {
                    mob.vx = 0f
                }
            } else {
                mob.x = nextX.coerceIn(1f, (width - 2).toFloat())
            }

            // Step mob Y
            val nextY = mob.y + mob.vy
            val checkY = nextY.toInt()
            if (isBlockSolid(mob.x.toInt(), checkY)) {
                mob.y = checkY.toFloat()
                mob.vy = 0f
                mob.isGrounded = true
            } else {
                mob.y = nextY
                mob.isGrounded = false
            }

            if (mob.hurtTimer > 0f) {
                mob.hurtTimer = (mob.hurtTimer - dt).coerceAtLeast(0f)
            }
        }

        // Night time mob spawner
        if (timeOfDay in 0.5f..0.9f && mobs.count { it.type.isHostile } < 5) {
            if (Random.nextFloat() < 0.005f) {
                val spawnSide = if (Random.nextBoolean()) 18f else -18f
                val sx = (playerX + spawnSide).coerceIn(4f, (width - 5).toFloat())
                val sy = getGroundHeightAt(sx.toInt()) - 2f
                val type = if (Random.nextBoolean()) MobType.ZOMBIE else MobType.CREEPER
                spawnMob(type, sx, sy)
            }
        }
    }

    private fun updateTnt(dt: Float) {
        val iter = activeTnt.iterator()
        while (iter.hasNext()) {
            val tnt = iter.next()
            tnt.fuse -= dt
            tnt.flash = (tnt.fuse * 8).toInt() % 2 == 0
            if (tnt.fuse <= 0f) {
                explode(tnt.x.toInt(), tnt.y.toInt(), radius = 4)
                iter.remove()
            }
        }
    }

    fun explode(centerX: Int, centerY: Int, radius: Int) {
        // Destroy blocks within radius
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                if (dx * dx + dy * dy <= radius * radius) {
                    val bx = centerX + dx
                    val by = centerY + dy
                    if (bx in 0 until width && by in 0 until height) {
                        val blk = worldBlocks[bx][by]
                        if (blk != BlockType.AIR && blk != BlockType.BEDROCK && blk != BlockType.OBSIDIAN) {
                            worldBlocks[bx][by] = BlockType.AIR
                            if (Random.nextFloat() < 0.35f) {
                                val (dropBlock, dropItem) = blk.getDrop()
                                if (dropBlock != null) spawnFloatingDrop(bx + 0.5f, by + 0.5f, ItemStack(blockType = dropBlock))
                            }
                        }
                    }
                }
            }
        }

        // Spawn explosion particles
        for (i in 0 until 35) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = Random.nextFloat() * 0.25f
            particles.add(
                Particle(
                    x = centerX + 0.5f,
                    y = centerY + 0.5f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 2 == 0) Color(0xFFFF9800) else Color(0xFF616161),
                    life = 1.0f,
                    size = Random.nextFloat() * 10f + 5f
                )
            )
        }

        // Damage player if nearby
        val distToPlayer = hypot(playerX - (centerX + 0.5f), playerY - (centerY + 0.5f))
        if (distToPlayer < radius + 2 && !isCreative) {
            val dmg = ((1f - distToPlayer / (radius + 2)) * 18).toInt().coerceAtLeast(4)
            playerHealth = (playerHealth - dmg).coerceAtLeast(0)
            playerVy = -0.3f
            playerVx = (playerX - centerX) * 0.08f
            hurtTimer = 0.8f
        }
    }

    private fun updateFloatingDrops(dt: Float) {
        val iter = floatingDrops.iterator()
        while (iter.hasNext()) {
            val drop = iter.next()
            drop.age += dt
            drop.vy = (drop.vy + 0.012f).coerceAtMost(0.2f)
            drop.y += drop.vy
            if (isBlockSolid(drop.x.toInt(), drop.y.toInt())) {
                drop.y = drop.y.toInt().toFloat()
                drop.vy = 0f
            }

            // Magnetic pickup towards player
            val dist = hypot(playerX - drop.x, (playerY - 0.8f) - drop.y)
            if (dist < 1.8f) {
                if (addItemToInventory(drop.item)) {
                    spawnBlockParticles(drop.x, drop.y, Color.Yellow, 4)
                    iter.remove()
                }
            }
        }
    }

    fun startMining(bx: Int, by: Int) {
        if (bx !in 0 until width || by !in 0 until height) return
        val block = worldBlocks[bx][by]
        if (block == BlockType.AIR || block == BlockType.BEDROCK) return

        val dist = hypot(playerX - (bx + 0.5f), (playerY - 0.8f) - (by + 0.5f))
        if (dist > 5.5f) return

        if (isCreative) {
            breakBlock(bx, by)
            return
        }

        // Calculate time
        val selectedItem = hotbar[selectedSlot]
        val multiplier = if (selectedItem.itemType?.isTool == true) {
            selectedItem.itemType.miningMultiplier
        } else {
            1.0f
        }
        val reqTime = (block.hardness / multiplier).coerceAtLeast(0.1f)

        breakingBlock = BreakingBlock(bx, by, progress = 0f, requiredTime = reqTime)
    }

    fun stopMining() {
        breakingBlock = null
    }

    fun attackMob(mob: Mob) {
        val dist = hypot(playerX - mob.x, (playerY - 0.8f) - mob.y)
        if (dist > 3.5f) return

        val selectedItem = hotbar[selectedSlot]
        val damage = if (selectedItem.itemType?.isTool == true) {
            selectedItem.itemType.attackDamage
        } else {
            2
        }

        mob.health -= damage
        mob.hurtTimer = 0.4f
        mob.vy = -0.15f
        mob.vx = if (facingRight) 0.12f else -0.12f
        armSwing = 1f
        spawnBlockParticles(mob.x, mob.y - 0.8f, Color.Red, 8)
    }

    fun breakBlock(bx: Int, by: Int) {
        if (bx !in 0 until width || by !in 0 until height) return
        val block = worldBlocks[bx][by]
        if (block == BlockType.AIR || block == BlockType.BEDROCK) return

        if (block == BlockType.TNT) {
            worldBlocks[bx][by] = BlockType.AIR
            activeTnt.add(TntEntity(bx + 0.5f, by + 0.5f))
            return
        }

        worldBlocks[bx][by] = BlockType.AIR
        spawnBlockParticles(bx + 0.5f, by + 0.5f, getBlockParticleColor(block), 10)

        if (!isCreative) {
            val (dropBlock, dropItem) = block.getDrop()
            if (dropBlock != null) {
                spawnFloatingDrop(bx + 0.5f, by + 0.5f, ItemStack(blockType = dropBlock))
            } else if (dropItem.isNotEmpty()) {
                val itemType = when (dropItem) {
                    "coal" -> ItemType.COAL
                    "diamond" -> ItemType.DIAMOND
                    "apple" -> ItemType.APPLE
                    else -> ItemType.STICK
                }
                spawnFloatingDrop(bx + 0.5f, by + 0.5f, ItemStack(itemType = itemType))
            }
        }
    }

    fun placeBlock(bx: Int, by: Int): Boolean {
        if (bx !in 0 until width || by !in 0 until height) return false
        if (worldBlocks[bx][by] != BlockType.AIR) return false

        // Check reach
        val dist = hypot(playerX - (bx + 0.5f), (playerY - 0.8f) - (by + 0.5f))
        if (dist > 5.5f) return false

        // Must be adjacent to an existing solid block or surface
        val hasNeighbor = (bx > 0 && worldBlocks[bx - 1][by].isSolid) ||
                (bx < width - 1 && worldBlocks[bx + 1][by].isSolid) ||
                (by > 0 && worldBlocks[bx][by - 1].isSolid) ||
                (by < height - 1 && worldBlocks[bx][by + 1].isSolid) ||
                (by == height - 1)

        if (!hasNeighbor) return false

        val selected = hotbar[selectedSlot]
        if (selected.isBlock && selected.blockType != null) {
            // Cannot place inside player bounding box if solid
            if (selected.blockType.isSolid) {
                val playerMinX = playerX - 0.3f
                val playerMaxX = playerX + 0.3f
                val playerMinY = playerY - 1.6f
                val playerMaxY = playerY
                if (bx + 1 > playerMinX && bx < playerMaxX && by + 1 > playerMinY && by < playerMaxY) {
                    return false
                }
            }

            worldBlocks[bx][by] = selected.blockType
            spawnBlockParticles(bx + 0.5f, by + 0.5f, getBlockParticleColor(selected.blockType), 6)
            armSwing = 1f

            if (!isCreative) {
                if (selected.count > 1) {
                    hotbar[selectedSlot] = selected.copy(count = selected.count - 1)
                } else {
                    hotbar[selectedSlot] = ItemStack()
                }
            }
            return true
        } else if (selected.itemType?.isFood == true) {
            // Eat food
            if (playerHealth < 20 || playerHunger < 20) {
                playerHealth = (playerHealth + selected.itemType.foodHeal).coerceAtMost(20)
                playerHunger = (playerHunger + selected.itemType.foodHeal).coerceAtMost(20)
                spawnBlockParticles(playerX, playerY - 1.2f, Color.Green, 8)
                if (!isCreative) {
                    if (selected.count > 1) {
                        hotbar[selectedSlot] = selected.copy(count = selected.count - 1)
                    } else {
                        hotbar[selectedSlot] = ItemStack()
                    }
                }
                return true
            }
        }
        return false
    }

    private fun spawnFloatingDrop(x: Float, y: Float, stack: ItemStack) {
        if (stack.isEmpty) return
        floatingDrops.add(
            FloatingDrop(
                id = System.nanoTime() + Random.nextLong(1000),
                x = x,
                y = y,
                vx = (Random.nextFloat() - 0.5f) * 0.05f,
                vy = -0.1f,
                item = stack
            )
        )
    }

    private fun spawnBlockParticles(x: Float, y: Float, color: Color, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = Random.nextFloat() * 0.08f + 0.02f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 0.04f,
                    color = color,
                    life = 1.0f,
                    size = Random.nextFloat() * 5f + 3f
                )
            )
        }
    }

    fun isBlockSolid(bx: Int, by: Int): Boolean {
        if (bx !in 0 until width || by !in 0 until height) return true
        return worldBlocks[bx][by].isSolid
    }

    fun getBlockAt(bx: Int, by: Int): BlockType {
        if (bx !in 0 until width || by !in 0 until height) return BlockType.BEDROCK
        return worldBlocks[bx][by]
    }

    private fun getBlockParticleColor(block: BlockType): Color {
        return when (block) {
            BlockType.GRASS -> Color(0xFF4CAF50)
            BlockType.DIRT -> Color(0xFF795548)
            BlockType.STONE, BlockType.COBBLESTONE -> Color(0xFF9E9E9E)
            BlockType.OAK_LOG -> Color(0xFF5D4037)
            BlockType.OAK_LEAVES -> Color(0xFF388E3C)
            BlockType.OAK_PLANKS -> Color(0xFFD7CCC8)
            BlockType.COAL_ORE -> Color(0xFF212121)
            BlockType.IRON_ORE -> Color(0xFFD7CCC8)
            BlockType.GOLD_ORE -> Color(0xFFFFD54F)
            BlockType.DIAMOND_ORE -> Color(0xFF4DD0E1)
            BlockType.OBSIDIAN -> Color(0xFF311B92)
            BlockType.SAND -> Color(0xFFFFF59D)
            BlockType.GLASS -> Color(0xFFE0F7FA)
            BlockType.TNT -> Color(0xFFD32F2F)
            BlockType.TORCH -> Color(0xFFFFC107)
            BlockType.LAVA -> Color(0xFFFF5722)
            BlockType.WATER -> Color(0xFF2196F3)
            else -> Color(0xFFBDBDBD)
        }
    }
}
