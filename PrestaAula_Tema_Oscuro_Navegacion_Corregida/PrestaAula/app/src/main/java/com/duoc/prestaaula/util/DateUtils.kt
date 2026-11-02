package com.duoc.prestaaula.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private const val DAY_MILLIS = 86_400_000L

    fun today(): Long = LocalDate.now().toEpochDay()

    fun format(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dayFormat)

    fun formatMillis(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(dateTimeFormat)

    /** El DatePicker de Material trabaja con medianoche UTC. */
    fun toUtcMillis(epochDay: Long): Long = epochDay * DAY_MILLIS

    fun fromUtcMillis(millis: Long): Long = Math.floorDiv(millis, DAY_MILLIS)
}
