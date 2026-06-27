package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isCustom: Boolean = true,
    // Equalizer Bands (-12.0f to 12.0f dB)
    val band60: Float = 0.0f,
    val band230: Float = 0.0f,
    val band910: Float = 0.0f,
    val band4000: Float = 0.0f,
    val band14000: Float = 0.0f,
    // Extra effects (0.0f to 1.0f)
    val bassBoost: Float = 0.0f,
    val virtualizer: Float = 0.0f,
    val loudness: Float = 0.0f
) {
    fun toPreset(): Preset {
        return Preset(
            id = id,
            name = name,
            isCustom = isCustom,
            bands = listOf(band60, band230, band910, band4000, band14000),
            bassBoost = bassBoost,
            virtualizer = virtualizer,
            loudness = loudness
        )
    }

    companion object {
        fun fromPreset(preset: Preset): PresetEntity {
            return PresetEntity(
                id = if (preset.isCustom) preset.id else 0, // system presets shouldn't write their IDs directly to DB as auto-incrementing
                name = preset.name,
                isCustom = preset.isCustom,
                band60 = preset.bands.getOrElse(0) { 0f },
                band230 = preset.bands.getOrElse(1) { 0f },
                band910 = preset.bands.getOrElse(2) { 0f },
                band4000 = preset.bands.getOrElse(3) { 0f },
                band14000 = preset.bands.getOrElse(4) { 0f },
                bassBoost = preset.bassBoost,
                virtualizer = preset.virtualizer,
                loudness = preset.loudness
            )
        }
    }
}

// Domain Model representation
data class Preset(
    val id: Int = 0,
    val name: String,
    val isCustom: Boolean,
    val bands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f), // 5 bands
    val bassBoost: Float = 0f,
    val virtualizer: Float = 0f,
    val loudness: Float = 0f
)
