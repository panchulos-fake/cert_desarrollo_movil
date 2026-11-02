package com.duoc.prestaaula.domain.model

data class TimeBlock(val number: Int, val start: String, val end: String) {
    val label: String get() = "Bloque $number ($start - $end)"
}

object TimeBlocks {
    val all = listOf(
        TimeBlock(1, "08:00", "08:45"),
        TimeBlock(2, "08:45", "09:30"),
        TimeBlock(3, "09:45", "10:30"),
        TimeBlock(4, "10:30", "11:15"),
        TimeBlock(5, "11:30", "12:15"),
        TimeBlock(6, "12:15", "13:00"),
        TimeBlock(7, "14:00", "14:45"),
        TimeBlock(8, "14:45", "15:30")
    )

    fun byNumber(n: Int): TimeBlock = all.firstOrNull { it.number == n } ?: all.first()
    fun label(n: Int): String = byNumber(n).label
}

object WeekDays {
    val names = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    val numbers = listOf(1, 2, 3, 4, 5)
    fun name(day: Int): String = names.getOrElse(day - 1) { "Día $day" }
}

object EquipmentTypes {
    val all = listOf("Notebook", "Tablet", "Proyector", "Cable HDMI", "Parlante", "Otro")
}
