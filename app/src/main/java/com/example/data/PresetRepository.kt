package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PresetRepository(private val presetDao: PresetDao) {

    // Default built-in presets
    val systemPresets = listOf(
        Preset(id = -1, name = "Flat", isCustom = false, bands = listOf(0f, 0f, 0f, 0f, 0f), bassBoost = 0f, virtualizer = 0f, loudness = 0f),
        Preset(id = -2, name = "Bass Booster", isCustom = false, bands = listOf(8f, 5f, 1f, 0f, -2f), bassBoost = 0.6f, virtualizer = 0.2f, loudness = 0.3f),
        Preset(id = -3, name = "Vocal Booster", isCustom = false, bands = listOf(-3f, 1f, 6f, 4f, 2f), bassBoost = 0.1f, virtualizer = 0.1f, loudness = 0.4f),
        Preset(id = -4, name = "Rock", isCustom = false, bands = listOf(5f, 3f, -1f, 2f, 4f), bassBoost = 0.4f, virtualizer = 0.3f, loudness = 0.2f),
        Preset(id = -5, name = "Pop", isCustom = false, bands = listOf(-1f, 2f, 4f, 3f, -1f), bassBoost = 0.3f, virtualizer = 0.1f, loudness = 0.3f),
        Preset(id = -6, name = "Jazz", isCustom = false, bands = listOf(3f, 1f, -2f, 2f, 3f), bassBoost = 0.2f, virtualizer = 0.4f, loudness = 0.1f),
        Preset(id = -7, name = "Classical", isCustom = false, bands = listOf(4f, 2f, 0f, 2f, -2f), bassBoost = 0.1f, virtualizer = 0.5f, loudness = 0.1f),
        Preset(id = -8, name = "Hip Hop", isCustom = false, bands = listOf(6f, 4f, 0f, 1f, 3f), bassBoost = 0.5f, virtualizer = 0.3f, loudness = 0.4f),
        Preset(id = -9, name = "Electronic", isCustom = false, bands = listOf(6f, 2f, 1f, 3f, 5f), bassBoost = 0.5f, virtualizer = 0.4f, loudness = 0.3f)
    )

    // Flow that combines system presets and database custom presets
    val allPresets: Flow<List<Preset>> = presetDao.getAllPresets().map { entities ->
        systemPresets + entities.map { it.toPreset() }
    }

    suspend fun savePreset(preset: Preset): Long {
        val entity = PresetEntity.fromPreset(preset)
        return if (preset.id > 0) {
            presetDao.updatePreset(entity)
            preset.id.toLong()
        } else {
            presetDao.insertPreset(entity)
        }
    }

    suspend fun deletePreset(preset: Preset) {
        if (preset.isCustom) {
            presetDao.deletePresetById(preset.id)
        }
    }
}
