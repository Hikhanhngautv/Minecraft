package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PortalScreen() {
    var isOverworldToNether by remember { mutableStateOf(true) }
    var inputX by remember { mutableStateOf("800") }
    var inputY by remember { mutableStateOf("64") }
    var inputZ by remember { mutableStateOf("-1600") }

    val xVal = inputX.toIntOrNull() ?: 0
    val yVal = inputY.toIntOrNull() ?: 64
    val zVal = inputZ.toIntOrNull() ?: 0

    val (targetX, targetY, targetZ) = remember(isOverworldToNether, xVal, yVal, zVal) {
        if (isOverworldToNether) {
            Triple(floor(xVal / 8.0).toInt(), yVal, floor(zVal / 8.0).toInt())
        } else {
            Triple(xVal * 8, yVal, zVal * 8)
        }
    }

    val distanceOverworld = kotlin.math.hypot(xVal.toDouble(), zVal.toDouble()).roundToInt()
    val distanceNether = kotlin.math.hypot(targetX.toDouble(), targetZ.toDouble()).roundToInt()
    val blocksSaved = kotlin.math.abs(distanceOverworld - distanceNether)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("portal_screen")
    ) {
        // Header
        Text(
            text = "Nether Portal Calculator",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Exact 8:1 dimensional sync & portal linking optimizer",
            color = Color(0xFFBA68C8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Portal Graphic Preview
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16151D)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedPortalCanvas()

                // Overlay Dimension Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isOverworldToNether) Color(0xFF2E7D32) else Color(0xFFB71C1C),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isOverworldToNether) "OVERWORLD" else "NETHER",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { isOverworldToNether = !isOverworldToNether },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x884A148C))
                            .testTag("swap_dimension_button")
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Swap", tint = Color.White)
                    }

                    Surface(
                        color = if (isOverworldToNether) Color(0xFFB71C1C) else Color(0xFF2E7D32),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isOverworldToNether) "NETHER" else "OVERWORLD",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Coordinates
        Text(
            text = "Source Coordinates (${if (isOverworldToNether) "Overworld" else "Nether"}):",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputX,
                onValueChange = { inputX = it },
                label = { Text("X") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("portal_input_x"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBA68C8),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = inputY,
                onValueChange = { inputY = it },
                label = { Text("Y") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("portal_input_y"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBA68C8),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            OutlinedTextField(
                value = inputZ,
                onValueChange = { inputZ = it },
                label = { Text("Z") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("portal_input_z"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBA68C8),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Result Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Build Portal At Exactly (${if (isOverworldToNether) "Nether" else "Overworld"}):",
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoordinatePill("X", "$targetX", Color(0xFFE53935))
                    CoordinatePill("Y", "$targetY", Color(0xFF43A047))
                    CoordinatePill("Z", "$targetZ", Color(0xFF1E88E5))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "In-game Command: /tp @s $targetX $targetY $targetZ",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Travel Comparison Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Highway Travel Optimization",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Travelling 1 block in the Nether equals 8 blocks in the Overworld. By connecting these points with a Nether Ice Highway, you save approximately $blocksSaved blocks of walking!",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Overworld Walk:", color = Color.Gray, fontSize = 11.sp)
                        Text("${distanceOverworld}m (~${distanceOverworld / 4}s sprint)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Nether Boat on Blue Ice:", color = Color.Gray, fontSize = 11.sp)
                        Text("${distanceNether}m (~${(distanceNether / 40.0).roundToInt()}s fast travel)", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinatePill(label: String, value: String, color: Color) {
    Surface(
        color = Color(0xFF383A40),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AnimatedPortalCanvas() {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.size(70.dp, 100.dp)) {
        val pw = size.width
        val ph = size.height

        // Obsidian frame
        drawRect(Color(0xFF1E152A), Offset(0f, 0f), Size(pw, ph))

        // Purple swirling portal interior
        val portalMargin = 12f
        val interiorW = pw - portalMargin * 2
        val interiorH = ph - portalMargin * 2

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4A148C),
                    Color(0xFF7B1FA2),
                    Color(0xFF8E24AA),
                    Color(0xFF4A148C)
                )
            ),
            topLeft = Offset(portalMargin, portalMargin),
            size = Size(interiorW, interiorH)
        )

        // Portal swirls
        for (i in 0..4) {
            val waveY = portalMargin + (interiorH / 5f) * i + sin(phase + i) * 6f
            drawLine(
                color = Color(0xFFBA68C8).copy(alpha = 0.6f),
                start = Offset(portalMargin + 4f, waveY),
                end = Offset(pw - portalMargin - 4f, waveY + 8f),
                strokeWidth = 3f
            )
        }
    }
}
