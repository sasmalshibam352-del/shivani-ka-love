package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Preset
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier
) {
    val bands by viewModel.bands.collectAsStateWithLifecycle()
    val bassBoost by viewModel.bassBoost.collectAsStateWithLifecycle()
    val virtualizer by viewModel.virtualizer.collectAsStateWithLifecycle()
    val loudness by viewModel.loudness.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val selectedPresetId by viewModel.selectedPresetId.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val visualizerStyle by viewModel.visualizerStyle.collectAsStateWithLifecycle()
    val visualizerAmplitudes by viewModel.visualizerAmplitudes.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkVoid),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizer Logo",
                            tint = ElectricCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SLEEK EQ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp,
                                color = Color.White
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkVoid,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkVoid)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sleek Audio Visualizer Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.2f), NeonPurple.copy(alpha = 0.1f))), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                // Background futuristic grid line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridCount = 8
                    val stepY = size.height / gridCount
                    for (i in 1 until gridCount) {
                        val y = stepY * i
                        drawLine(
                            color = GridLineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }
                }

                // Render Visualizer depending on Style
                when (visualizerStyle) {
                    VisualizerStyle.SPECTRUM_BARS -> {
                        SpectrumBarsVisualizer(amplitudes = visualizerAmplitudes)
                    }
                    VisualizerStyle.RADIAL_WAVE -> {
                        RadialWaveVisualizer(amplitudes = visualizerAmplitudes, isPlaying = isPlaying)
                    }
                    VisualizerStyle.LASER_LINE -> {
                        LaserLineVisualizer(amplitudes = visualizerAmplitudes)
                    }
                    VisualizerStyle.LIQUID_WAVE -> {
                        LiquidWaveVisualizer(amplitudes = visualizerAmplitudes, isPlaying = isPlaying)
                    }
                }

                // Small Overlay for Style Choice & Track Duration
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(DarkVoid.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Bars",
                        tint = if (visualizerStyle == VisualizerStyle.SPECTRUM_BARS) ElectricCyan else MutedSlate,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.setVisualizerStyle(VisualizerStyle.SPECTRUM_BARS) }
                    )
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = "Radial",
                        tint = if (visualizerStyle == VisualizerStyle.RADIAL_WAVE) ElectricCyan else MutedSlate,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.setVisualizerStyle(VisualizerStyle.RADIAL_WAVE) }
                    )
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Laser",
                        tint = if (visualizerStyle == VisualizerStyle.LASER_LINE) ElectricCyan else MutedSlate,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.setVisualizerStyle(VisualizerStyle.LASER_LINE) }
                    )
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = "Liquid Wave",
                        tint = if (visualizerStyle == VisualizerStyle.LIQUID_WAVE) ElectricCyan else MutedSlate,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.setVisualizerStyle(VisualizerStyle.LIQUID_WAVE) }
                    )
                }

                // Genre Badge
                Text(
                    text = currentTrack.genre.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // 2. Preset Chips Layout
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRESETS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MutedSlate,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Row(
                        modifier = Modifier
                            .clickable { showSaveDialog = true }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = "Save Preset",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp).testTag("save_preset_button")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SAVE NEW",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(presets) { preset ->
                        val isSelected = selectedPresetId == preset.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.15f) else SurfaceCard)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ElectricCyan else MutedSlate.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.applyPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (preset.isCustom) Icons.Default.Person else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isSelected) ElectricCyan else MutedSlate,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                if (preset.isCustom) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Preset",
                                        tint = LaserPink.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .clickable { viewModel.deletePreset(preset) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Vertical Equalizer Slider Rails (5 bands)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.01f))), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val bandLabels = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")
                val bandRoles = listOf("Bass", "Low-Mid", "Mids", "High-Mid", "Treble")

                bands.forEachIndexed { index, dbValue ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = if (dbValue >= 0) "+${dbValue.toInt()}" else dbValue.toInt().toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (abs(dbValue) > 0.1f) ElectricCyan else Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.height(16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Custom Vertical Slider
                        VerticalSlider(
                            value = dbValue,
                            range = -12f..12f,
                            onValueChange = { newValue -> viewModel.setBand(index, newValue) },
                            modifier = Modifier
                                .weight(1f)
                                .width(36.dp)
                                .testTag("eq_slider_${bandLabels[index]}")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bandLabels[index],
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = bandRoles[index],
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MutedSlate,
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            // 4. Custom Dials/Level Knobs for Sound Effects (Bass Boost, Virtualizer, Loudness)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EffectSliderCard(
                    title = "BASS BOOST",
                    value = bassBoost,
                    onValueChange = { viewModel.setBassBoost(it) },
                    glowColor = NeonPurple,
                    icon = Icons.Default.SpeakerGroup,
                    modifier = Modifier.weight(1f).testTag("bass_boost_dial")
                )
                EffectSliderCard(
                    title = "VIRTUALIZER",
                    value = virtualizer,
                    onValueChange = { viewModel.setVirtualizer(it) },
                    glowColor = ElectricCyan,
                    icon = Icons.Default.SurroundSound,
                    modifier = Modifier.weight(1f).testTag("virtualizer_dial")
                )
                EffectSliderCard(
                    title = "LOUDNESS",
                    value = loudness,
                    onValueChange = { viewModel.setLoudness(it) },
                    glowColor = NeonGreen,
                    icon = Icons.Default.VolumeUp,
                    modifier = Modifier.weight(1f).testTag("loudness_dial")
                )
            }

            // 5. Simulated Music Player Controller
            PlaybackController(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                progress = playbackProgress,
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextTrack() },
                onPrev = { viewModel.prevTrack() },
                onSeek = { viewModel.seekTo(it) }
            )
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        var presetName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showSaveDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Save Custom Preset",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = MutedSlate,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancel", color = MutedSlate)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (presetName.isNotBlank()) {
                                    viewModel.saveCustomPreset(presetName)
                                    showSaveDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Visualizer Implementations ----------------

@Composable
fun SpectrumBarsVisualizer(amplitudes: List<Float>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barCount = amplitudes.size
        val gap = 6f
        val totalGapsWidth = gap * (barCount - 1)
        val barWidth = (size.width - totalGapsWidth) / barCount

        val neonGradient = Brush.verticalGradient(
            colors = listOf(LaserPink, ElectricCyan),
            startY = size.height,
            endY = 0f
        )

        amplitudes.forEachIndexed { index, amp ->
            val barHeight = amp * size.height * 0.9f
            val x = index * (barWidth + gap)
            val y = size.height - barHeight

            // Draw shadow glowing bar behind
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(LaserPink.copy(alpha = 0.15f), ElectricCyan.copy(alpha = 0.15f))
                ),
                topLeft = Offset(x - 2f, y - 2f),
                size = Size(barWidth + 4f, barHeight + 4f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw primary bar
            drawRoundRect(
                brush = neonGradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun RadialWaveVisualizer(amplitudes: List<Float>, isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = size.height * 0.22f

        // Draw animated rotating vinyl core
        drawCircle(
            color = Color.Black,
            radius = baseRadius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = SurfaceCard,
            radius = baseRadius * 0.95f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = ElectricCyan.copy(alpha = 0.15f),
            radius = baseRadius * 0.8f,
            center = Offset(centerX, centerY)
        )
        // Record center pinhole
        drawCircle(
            color = ElectricCyan,
            radius = 6f,
            center = Offset(centerX, centerY)
        )

        // Draw outer glowing wave frequencies
        val barCount = amplitudes.size
        val angleStep = 360f / barCount

        for (i in 0 until barCount) {
            val amp = amplitudes[i]
            val angleDegrees = i * angleStep + (if (isPlaying) rotation else 0f)
            val angleRadians = Math.toRadians(angleDegrees.toDouble())

            val startX = centerX + baseRadius * cos(angleRadians).toFloat()
            val startY = centerY + baseRadius * sin(angleRadians).toFloat()

            val maxBarLength = size.height * 0.35f
            val endX = centerX + (baseRadius + amp * maxBarLength) * cos(angleRadians).toFloat()
            val endY = centerY + (baseRadius + amp * maxBarLength) * sin(angleRadians).toFloat()

            // Draw frequency ray with neon green & cyan gradient
            val colorFactor = i.toFloat() / barCount
            val neonColor = lerp(ElectricCyan, NeonPurple, colorFactor)

            drawLine(
                color = neonColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun LaserLineVisualizer(amplitudes: List<Float>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val barCount = amplitudes.size
        val stepX = size.width / (barCount - 1)

        path.moveTo(0f, size.height / 2f)

        for (i in 0 until barCount) {
            val amp = amplitudes[i]
            val x = i * stepX
            // Symmetrical wave oscillations
            val multiplier = if (i % 2 == 0) 1f else -1f
            val y = (size.height / 2f) + (amp * size.height * 0.45f * multiplier)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevAmp = amplitudes[i - 1]
                val prevMultiplier = if ((i - 1) % 2 == 0) 1f else -1f
                val prevY = (size.height / 2f) + (prevAmp * size.height * 0.45f * prevMultiplier)

                // Draw smooth curve using control points
                val controlX1 = prevX + stepX / 2f
                val controlY1 = prevY
                val controlX2 = prevX + stepX / 2f
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }

        // Draw laser shadow glow
        drawPath(
            path = path,
            color = ElectricCyan.copy(alpha = 0.3f),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw primary neon laser line
        drawPath(
            path = path,
            color = ElectricCyan,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// ---------------- Custom Equalizer Band Sliders ----------------

@Composable
fun VerticalSlider(
    value: Float,
    range: ClosedRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val calculatedValue = range.start + ((1f - (offset.y / size.height)) * (range.endInclusive - range.start))
                    onValueChange(calculatedValue.coerceIn(range))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag y coordinates are top-to-bottom.
                    // Scale offset based on container height
                    val yPos = change.position.y
                    val heightPx = size.height
                    val fraction = 1f - (yPos / heightPx).coerceIn(0f, 1f)
                    val calculatedValue = range.start + fraction * (range.endInclusive - range.start)
                    onValueChange(calculatedValue.coerceIn(range))
                }
            }
    ) {
        val height = maxHeight
        val width = maxWidth

        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.width / 2f
            val halfTrackWidth = 2.dp.toPx()

            // 1. Draw Slider Rail Background
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(centerY - halfTrackWidth, 0f),
                size = Size(halfTrackWidth * 2, size.height),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Tick Marks along slider
            val tickCount = 9
            val tickSpacing = size.height / (tickCount - 1)
            for (i in 0 until tickCount) {
                val y = i * tickSpacing
                val isCenter = i == tickCount / 2
                val tickWidth = if (isCenter) 10.dp.toPx() else 6.dp.toPx()
                drawLine(
                    color = if (isCenter) ElectricCyan.copy(alpha = 0.6f) else MutedSlate.copy(alpha = 0.3f),
                    start = Offset(centerY - tickWidth / 2f, y),
                    end = Offset(centerY + tickWidth / 2f, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Calculate active level y position
            val progressFraction = (value - range.start) / (range.endInclusive - range.start)
            val thumbY = size.height * (1f - progressFraction)

            // 2. Draw Active Range Rail (glow color)
            val fillBrush = Brush.verticalGradient(
                colors = listOf(ElectricCyan, NeonPurple),
                startY = thumbY,
                endY = size.height
            )
            drawRoundRect(
                brush = fillBrush,
                topLeft = Offset(centerY - halfTrackWidth, thumbY),
                size = Size(halfTrackWidth * 2, size.height - thumbY),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 3. Draw Slider Thumb Handle (Fader knob)
            val thumbRadius = 10.dp.toPx()
            // Outer Glow
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.25f),
                radius = thumbRadius + 4.dp.toPx(),
                center = Offset(centerY, thumbY)
            )
            // Thumb primary body
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(centerY, thumbY)
            )
            // Neon accent inner core
            drawCircle(
                color = ElectricCyan,
                radius = thumbRadius * 0.5f,
                center = Offset(centerY, thumbY)
            )
        }
    }
}

// ---------------- Effect Cards (Rotary adjustments) ----------------

@Composable
fun EffectSliderCard(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    glowColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MutedSlate,
                        fontSize = 9.sp
                    )
                )
            }

            // Radial Knob / Circular Dial Indicator
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Vertical drag to increase/decrease
                            val sensitivity = 150f
                            val delta = -dragAmount.y / sensitivity
                            val newValue = (value + delta).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f - 6.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Base dial track
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = radius,
                        center = center
                    )

                    // Draw Dial Sweep Arc representing level
                    val startAngle = 135f
                    val sweepAngle = 270f * value

                    drawArc(
                        color = glowColor.copy(alpha = 0.15f),
                        startAngle = startAngle,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = glowColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner tick indicator representing dial angle
                    val dialAngleRadians = Math.toRadians((startAngle + sweepAngle).toDouble())
                    val tickStartX = center.x + (radius * 0.4f) * cos(dialAngleRadians).toFloat()
                    val tickStartY = center.y + (radius * 0.4f) * sin(dialAngleRadians).toFloat()
                    val tickEndX = center.x + (radius * 0.9f) * cos(dialAngleRadians).toFloat()
                    val tickEndY = center.y + (radius * 0.9f) * sin(dialAngleRadians).toFloat()

                    drawLine(
                        color = Color.White,
                        start = Offset(tickStartX, tickStartY),
                        end = Offset(tickEndX, tickEndY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Center Number percent
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ---------------- Playback Controller Card ----------------

@Composable
fun PlaybackController(
    currentTrack: Track,
    isPlaying: Boolean,
    progress: Int,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = currentTrack.artist,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MutedSlate
                            )
                        )
                    }
                }

                // Control Buttons Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(36.dp).testTag("prev_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .clickable { onPlayPauseToggle() }
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp).testTag("next_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Progress Slider
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Slider(
                    value = progress.toFloat(),
                    valueRange = 0f..currentTrack.durationSeconds.toFloat(),
                    onValueChange = { onSeek(it.toInt()) },
                    colors = SliderDefaults.colors(
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = Color.Black.copy(alpha = 0.3f),
                        thumbColor = ElectricCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(progress),
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedSlate)
                    )
                    Text(
                        text = formatTime(currentTrack.durationSeconds),
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedSlate)
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidWaveVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidWave")
    val timePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Calculate average amplitude of frequency groups
        val lowFreqAvg = ((amplitudes.getOrNull(0) ?: 0.1f) + (amplitudes.getOrNull(1) ?: 0.1f) + (amplitudes.getOrNull(2) ?: 0.1f)) / 3f
        val midFreqAvg = ((amplitudes.getOrNull(5) ?: 0.1f) + (amplitudes.getOrNull(7) ?: 0.1f) + (amplitudes.getOrNull(9) ?: 0.1f)) / 3f
        val highFreqAvg = ((amplitudes.getOrNull(14) ?: 0.1f) + (amplitudes.getOrNull(17) ?: 0.1f) + (amplitudes.getOrNull(19) ?: 0.1f)) / 3f

        // Wave baseline levels (how high they fill the box)
        val baseFillHeight1 = height * 0.45f
        val baseFillHeight2 = height * 0.52f
        val baseFillHeight3 = height * 0.60f

        // Wave 1: Background Wave (Bass driven, LaserPink)
        val wave1Path = Path()
        val segments = 4
        val segmentWidth = width / segments

        // Set start point
        val startY1 = baseFillHeight1 + sin(timePhase) * (10.dp.toPx() + lowFreqAvg * 15.dp.toPx())
        wave1Path.moveTo(0f, startY1)

        for (i in 0 until segments) {
            val nextX = (i + 1) * segmentWidth
            val phaseOffset = i * (Math.PI.toFloat() / 2f)
            val nextY = baseFillHeight1 + sin(timePhase + phaseOffset) * (10.dp.toPx() + lowFreqAvg * 15.dp.toPx())

            // Cubic Bezier control points for standard fluid SVG wave S-curve
            val controlX1 = i * segmentWidth + segmentWidth / 3f
            val controlY1 = baseFillHeight1 + sin(timePhase + phaseOffset - 0.5f) * (12.dp.toPx() + lowFreqAvg * 18.dp.toPx())
            val controlX2 = i * segmentWidth + 2f * segmentWidth / 3f
            val controlY2 = baseFillHeight1 + sin(timePhase + phaseOffset + 0.5f) * (12.dp.toPx() + lowFreqAvg * 18.dp.toPx())

            wave1Path.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
        }
        wave1Path.lineTo(width, height)
        wave1Path.lineTo(0f, height)
        wave1Path.close()

        drawPath(
            path = wave1Path,
            color = LaserPink.copy(alpha = 0.22f)
        )

        // Wave 2: Midground Wave (Mid-frequency driven, NeonPurple)
        val wave2Path = Path()
        val startY2 = baseFillHeight2 + cos(timePhase * 1.2f) * (8.dp.toPx() + midFreqAvg * 12.dp.toPx())
        wave2Path.moveTo(0f, startY2)

        for (i in 0 until segments) {
            val nextX = (i + 1) * segmentWidth
            val phaseOffset = i * (Math.PI.toFloat() / 2f) + (Math.PI.toFloat() / 4f)
            val nextY = baseFillHeight2 + cos(timePhase * 1.2f + phaseOffset) * (8.dp.toPx() + midFreqAvg * 12.dp.toPx())

            val controlX1 = i * segmentWidth + segmentWidth / 3f
            val controlY1 = baseFillHeight2 + cos(timePhase * 1.2f + phaseOffset - 0.4f) * (10.dp.toPx() + midFreqAvg * 15.dp.toPx())
            val controlX2 = i * segmentWidth + 2f * segmentWidth / 3f
            val controlY2 = baseFillHeight2 + cos(timePhase * 1.2f + phaseOffset + 0.4f) * (10.dp.toPx() + midFreqAvg * 15.dp.toPx())

            wave2Path.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
        }
        wave2Path.lineTo(width, height)
        wave2Path.lineTo(0f, height)
        wave2Path.close()

        drawPath(
            path = wave2Path,
            color = NeonPurple.copy(alpha = 0.35f)
        )

        // Wave 3: Foreground Wave (High-frequency driven, ElectricCyan with dynamic linear gradient)
        val wave3Path = Path()
        val startY3 = baseFillHeight3 + sin(timePhase * 1.5f + 1f) * (6.dp.toPx() + highFreqAvg * 8.dp.toPx())
        wave3Path.moveTo(0f, startY3)

        for (i in 0 until segments) {
            val nextX = (i + 1) * segmentWidth
            val phaseOffset = i * (Math.PI.toFloat() / 2f) + (Math.PI.toFloat() / 2f)
            val nextY = baseFillHeight3 + sin(timePhase * 1.5f + phaseOffset + 1f) * (6.dp.toPx() + highFreqAvg * 8.dp.toPx())

            val controlX1 = i * segmentWidth + segmentWidth / 3f
            val controlY1 = baseFillHeight3 + sin(timePhase * 1.5f + phaseOffset + 0.5f) * (8.dp.toPx() + highFreqAvg * 10.dp.toPx())
            val controlX2 = i * segmentWidth + 2f * segmentWidth / 3f
            val controlY2 = baseFillHeight3 + sin(timePhase * 1.5f + phaseOffset + 1.5f) * (8.dp.toPx() + highFreqAvg * 10.dp.toPx())

            wave3Path.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
        }
        wave3Path.lineTo(width, height)
        wave3Path.lineTo(0f, height)
        wave3Path.close()

        val foregroundGradient = Brush.verticalGradient(
            colors = listOf(ElectricCyan.copy(alpha = 0.85f), ElectricCyan.copy(alpha = 0.25f)),
            startY = baseFillHeight3 - 10.dp.toPx(),
            endY = height
        )

        drawPath(
            path = wave3Path,
            brush = foregroundGradient
        )

        // Draw a bright highlighting edge curve on top of the foreground wave (Neon Stroke)
        val wave3StrokePath = Path()
        wave3StrokePath.moveTo(0f, startY3)
        for (i in 0 until segments) {
            val nextX = (i + 1) * segmentWidth
            val phaseOffset = i * (Math.PI.toFloat() / 2f) + (Math.PI.toFloat() / 2f)
            val nextY = baseFillHeight3 + sin(timePhase * 1.5f + phaseOffset + 1f) * (6.dp.toPx() + highFreqAvg * 8.dp.toPx())

            val controlX1 = i * segmentWidth + segmentWidth / 3f
            val controlY1 = baseFillHeight3 + sin(timePhase * 1.5f + phaseOffset + 0.5f) * (8.dp.toPx() + highFreqAvg * 10.dp.toPx())
            val controlX2 = i * segmentWidth + 2f * segmentWidth / 3f
            val controlY2 = baseFillHeight3 + sin(timePhase * 1.5f + phaseOffset + 1.5f) * (8.dp.toPx() + highFreqAvg * 10.dp.toPx())

            wave3StrokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY)
        }

        drawPath(
            path = wave3StrokePath,
            color = ElectricCyan,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

// Simple color lerping helper
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    val r = start.red + fraction * (end.red - start.red)
    val g = start.green + fraction * (end.green - start.green)
    val b = start.blue + fraction * (end.blue - start.blue)
    val a = start.alpha + fraction * (end.alpha - start.alpha)
    return Color(r, g, b, a)
}
