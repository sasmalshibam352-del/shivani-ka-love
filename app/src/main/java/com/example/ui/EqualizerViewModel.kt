package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Preset
import com.example.data.PresetDatabase
import com.example.data.PresetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

data class Track(
    val id: Int,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val genre: String,
    val baseBands: List<Float> // Base frequency distribution profile for this track
)

enum class VisualizerStyle {
    SPECTRUM_BARS,
    RADIAL_WAVE,
    LASER_LINE,
    LIQUID_WAVE
}

class EqualizerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PresetDatabase.getDatabase(application)
    private val repository = PresetRepository(database.presetDao())

    // Tracks list
    val tracks = listOf(
        Track(1, "Neon Horizon", "Synthwave DJ", 192, "Synthwave", listOf(7f, 4f, -1f, 3f, 5f)),
        Track(2, "Cyberpunk Pulse", "Vector Sector", 168, "Electro", listOf(9f, 6f, 0f, 4f, 6f)),
        Track(3, "Velvet Dreams", "Lofi Chillout", 210, "Lofi Hip-Hop", listOf(4f, 3f, 5f, 1f, -1f)),
        Track(4, "Starlight Echoes", "Aura Nova", 245, "Ambient", listOf(2f, 1f, 4f, 5f, 7f))
    )

    // Current equalizer slider values (5 bands)
    private val _bands = MutableStateFlow(listOf(0f, 0f, 0f, 0f, 0f))
    val bands: StateFlow<List<Float>> = _bands.asStateFlow()

    // Sound effect sliders
    private val _bassBoost = MutableStateFlow(0f)
    val bassBoost: StateFlow<Float> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(0f)
    val virtualizer: StateFlow<Float> = _virtualizer.asStateFlow()

    private val _loudness = MutableStateFlow(0f)
    val loudness: StateFlow<Float> = _loudness.asStateFlow()

    // Selected preset (built-in or custom)
    private val _selectedPresetId = MutableStateFlow<Int?>(-1) // Start with "Flat" preset (ID: -1)
    val selectedPresetId: StateFlow<Int?> = _selectedPresetId.asStateFlow()

    // All presets (system + custom DB presets)
    val presets: StateFlow<List<Preset>> = repository.allPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.systemPresets
        )

    // Current player track
    private val _currentTrack = MutableStateFlow(tracks[0])
    val currentTrack: StateFlow<Track> = _currentTrack.asStateFlow()

    // Is track playing
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Simulated track playback progress (in seconds)
    private val _playbackProgress = MutableStateFlow(0)
    val playbackProgress: StateFlow<Int> = _playbackProgress.asStateFlow()

    // Custom visualizer visual style
    private val _visualizerStyle = MutableStateFlow(VisualizerStyle.LIQUID_WAVE)
    val visualizerStyle: StateFlow<VisualizerStyle> = _visualizerStyle.asStateFlow()

    // Real-time reactive visualizer bar heights (20 bands)
    private val _visualizerAmplitudes = MutableStateFlow(List(20) { 0.1f })
    val visualizerAmplitudes: StateFlow<List<Float>> = _visualizerAmplitudes.asStateFlow()

    private var playbackJob: Job? = null
    private var visualizerJob: Job? = null

    init {
        // Initialize default preset (Flat)
        applyPreset(repository.systemPresets[0])
        startPlaybackSimulators()
    }

    private fun startPlaybackSimulators() {
        // Playback progress ticker
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isPlaying.value) {
                    val nextSec = _playbackProgress.value + 1
                    if (nextSec >= _currentTrack.value.durationSeconds) {
                        // Loop or next track
                        nextTrack()
                    } else {
                        _playbackProgress.value = nextSec
                    }
                }
            }
        }

        // Live visualizer ticker
        visualizerJob?.cancel()
        visualizerJob = viewModelScope.launch {
            var wavePhase = 0.0f
            val random = Random(42)
            while (true) {
                delay(40) // ~25 FPS for buttery-smooth animations
                wavePhase += 0.15f
                if (_isPlaying.value) {
                    // Update amplitudes dynamically based on current track profile + active EQ bands + effects
                    val eq = _bands.value
                    val bassVal = _bassBoost.value
                    val loudnessVal = _loudness.value
                    val virtVal = _virtualizer.value
                    val baseBands = _currentTrack.value.baseBands

                    val newAmps = List(20) { index ->
                        // Determine which frequency cluster this visualizer bar belongs to
                        // low (0-5), mid-low (6-9), mid (10-13), mid-high (14-17), high (18-19)
                        val eqIndex = when (index) {
                            in 0..4 -> 0 // 60Hz
                            in 5..8 -> 1 // 230Hz
                            in 9..12 -> 2 // 910Hz
                            in 13..16 -> 3 // 4kHz
                            else -> 4 // 14kHz
                        }

                        val bandEqDb = eq[eqIndex] // -12dB to +12dB
                        val bandBase = baseBands[eqIndex] // 0 to 10 typical profile

                        // Convert EQ dB to a multiplier (e.g. -12dB is ~0.25x, 0dB is 1x, +12dB is ~2x)
                        val eqMultiplier = 1f + (bandEqDb / 12f) * 0.8f

                        // Calculate visualizer height base on sine waves + some randomness
                        val sineWave1 = sin(wavePhase * 2.5f + index * 0.4f)
                        val sineWave2 = sin(wavePhase * 1.2f - index * 0.7f)
                        val noise = random.nextFloat() * 0.3f
                        val baseWave = (sineWave1 * 0.4f + sineWave2 * 0.3f).coerceIn(-1f, 1f) + 1f // 0.0 to 2.0

                        // Apply Track base bands profile
                        val profileFactor = (bandBase / 10f) * 0.7f + 0.3f

                        var finalAmp = baseWave * profileFactor * eqMultiplier * 0.45f

                        // Boost frequencies based on extra effects
                        if (eqIndex == 0) {
                            // Boost sub-bass bars with the bassBoost knob!
                            finalAmp += bassVal * 0.5f * (1.0f + sin(wavePhase * 5.0f + index))
                        }
                        if (eqIndex == 4) {
                            // High treble sparkles with loudness!
                            finalAmp += loudnessVal * 0.3f * (1.0f + sin(wavePhase * 4.0f - index))
                        }

                        // Virtualizer adds subtle widening/random fluctuations
                        finalAmp *= (1.0f + virtVal * 0.25f * (random.nextFloat() - 0.5f))

                        finalAmp.coerceIn(0.08f, 1.1f)
                    }
                    _visualizerAmplitudes.value = newAmps
                } else {
                    // Deco decay waveform when paused
                    val decayAmps = _visualizerAmplitudes.value.map { it * 0.85f + 0.012f }
                    _visualizerAmplitudes.value = decayAmps.map { it.coerceAtLeast(0.05f) }
                }
            }
        }
    }

    // Set interactive slider value
    fun setBand(index: Int, dbValue: Float) {
        val currentList = _bands.value.toMutableList()
        currentList[index] = dbValue.coerceIn(-12f, 12f)
        _bands.value = currentList
        checkMatchesPreset()
    }

    // Set effects
    fun setBassBoost(value: Float) {
        _bassBoost.value = value.coerceIn(0f, 1f)
        checkMatchesPreset()
    }

    fun setVirtualizer(value: Float) {
        _virtualizer.value = value.coerceIn(0f, 1f)
        checkMatchesPreset()
    }

    fun setLoudness(value: Float) {
        _loudness.value = value.coerceIn(0f, 1f)
        checkMatchesPreset()
    }

    // Change Visualizer Style
    fun setVisualizerStyle(style: VisualizerStyle) {
        _visualizerStyle.value = style
    }

    // Apply specific preset
    fun applyPreset(preset: Preset) {
        _bands.value = preset.bands
        _bassBoost.value = preset.bassBoost
        _virtualizer.value = preset.virtualizer
        _loudness.value = preset.loudness
        _selectedPresetId.value = preset.id
    }

    // Check if the current custom state perfectly matches an existing preset
    private fun checkMatchesPreset() {
        val currentBands = _bands.value
        val currentBass = _bassBoost.value
        val currentVirtualizer = _virtualizer.value
        val currentLoudness = _loudness.value

        val matchedPreset = presets.value.find { preset ->
            // Match within subtle precision due to floats
            val bandsMatch = preset.bands.zip(currentBands).all { (a, b) -> Math.abs(a - b) < 0.2f }
            val bassMatch = Math.abs(preset.bassBoost - currentBass) < 0.05f
            val virtMatch = Math.abs(preset.virtualizer - currentVirtualizer) < 0.05f
            val loudMatch = Math.abs(preset.loudness - currentLoudness) < 0.05f
            bandsMatch && bassMatch && virtMatch && loudMatch
        }

        _selectedPresetId.value = matchedPreset?.id
    }

    // Save current slider config as custom preset
    fun saveCustomPreset(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val preset = Preset(
                name = name,
                isCustom = true,
                bands = _bands.value,
                bassBoost = _bassBoost.value,
                virtualizer = _virtualizer.value,
                loudness = _loudness.value
            )
            val newId = repository.savePreset(preset)
            _selectedPresetId.value = newId.toInt()
        }
    }

    // Delete preset
    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
            if (_selectedPresetId.value == preset.id) {
                // Revert to flat preset
                _selectedPresetId.value = -1
                applyPreset(repository.systemPresets[0])
            }
        }
    }

    // Playback control
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun nextTrack() {
        val currentIndex = tracks.indexOf(_currentTrack.value)
        val nextIndex = (currentIndex + 1) % tracks.size
        _currentTrack.value = tracks[nextIndex]
        _playbackProgress.value = 0
    }

    fun prevTrack() {
        val currentIndex = tracks.indexOf(_currentTrack.value)
        val prevIndex = if (currentIndex - 1 < 0) tracks.size - 1 else currentIndex - 1
        _currentTrack.value = tracks[prevIndex]
        _playbackProgress.value = 0
    }

    fun seekTo(seconds: Int) {
        _playbackProgress.value = seconds.coerceIn(0, _currentTrack.value.durationSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        visualizerJob?.cancel()
    }
}
