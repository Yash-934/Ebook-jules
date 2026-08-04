package com.example.ui.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class SerializableOffset(val x: Float, val y: Float)

data class SerializableLine(
    val points: List<SerializableOffset>,
    val color: Long,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val tool: String = "doodle"
)

data class DrawnLine(
    val points: List<Offset>,
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val tool: String = "doodle"
)
