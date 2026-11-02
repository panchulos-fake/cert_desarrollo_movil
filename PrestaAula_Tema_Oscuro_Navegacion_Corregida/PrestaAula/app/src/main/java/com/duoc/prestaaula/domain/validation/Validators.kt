package com.duoc.prestaaula.domain.validation

/**
 * Reglas de validación centralizadas y desacopladas de la interfaz.
 * Cada función retorna el mensaje de error o null si el valor es válido.
 */
object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    private val PERSON_NAME = Regex("^[\\p{L} .'-]+\$")
    private val INTERNAL_CODE = Regex("^[A-Z]{2}-\\d{3}\$")

    fun email(value: String): String? = when {
        value.isBlank() -> "Ingresa tu correo electrónico"
        !EMAIL.matches(value.trim()) -> "Formato de correo inválido (ej. nombre@colegio.cl)"
        else -> null
    }

    fun loginPassword(value: String): String? =
        if (value.isBlank()) "Ingresa tu contraseña" else null

    fun newPassword(value: String): String? = when {
        value.isBlank() -> "Crea una contraseña"
        value.length < 8 -> "Debe tener al menos 8 caracteres"
        value.none { it.isUpperCase() } -> "Debe incluir una letra mayúscula"
        value.none { it.isLowerCase() } -> "Debe incluir una letra minúscula"
        value.none { it.isDigit() } -> "Debe incluir un número"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Repite tu contraseña"
        password != confirm -> "Las contraseñas no coinciden"
        else -> null
    }

    fun personName(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "Ingresa tu nombre completo"
            v.length < 3 -> "Debe tener al menos 3 caracteres"
            !PERSON_NAME.matches(v) -> "Solo se permiten letras y espacios"
            v.split(Regex("\\s+")).size < 2 -> "Ingresa nombre y apellido"
            else -> null
        }
    }

    fun equipmentType(value: String): String? =
        if (value.isBlank()) "Selecciona el tipo de equipo" else null

    fun brand(value: String): String? = when {
        value.isBlank() -> "Ingresa la marca"
        value.trim().length < 2 -> "Debe tener al menos 2 caracteres"
        value.trim().length > 30 -> "Máximo 30 caracteres"
        else -> null
    }

    fun internalCode(value: String): String? = when {
        value.isBlank() -> "Ingresa el código interno"
        !INTERNAL_CODE.matches(value.trim().uppercase()) -> "Formato inválido. Usa 2 letras, guion y 3 dígitos (ej. NB-001)"
        else -> null
    }

    fun equipmentNotes(value: String): String? =
        if (value.length > 120) "Máximo 120 caracteres" else null

    fun course(value: String): String? = when {
        value.isBlank() -> "Ingresa el curso (ej. 5° Básico A)"
        value.trim().length < 2 -> "Debe tener al menos 2 caracteres"
        value.trim().length > 30 -> "Máximo 30 caracteres"
        else -> null
    }

    fun reservationDate(epochDay: Long, today: Long): String? = when {
        epochDay < today -> "La fecha no puede ser anterior a hoy"
        epochDay > today + 60 -> "Solo se puede reservar hasta con 60 días de anticipación"
        else -> null
    }

    fun observations(value: String, required: Boolean): String? = when {
        required && value.isBlank() -> "Describe el estado en que se devuelve el equipo"
        required && value.trim().length < 10 -> "Describe con al menos 10 caracteres"
        value.length > 200 -> "Máximo 200 caracteres"
        else -> null
    }

    fun photo(path: String?, required: Boolean): String? =
        if (required && path.isNullOrBlank()) "Debes adjuntar una fotografía del daño" else null

    fun availabilityRange(startBlock: Int, endBlock: Int): String? =
        if (endBlock < startBlock) "El bloque final no puede ser anterior al inicial" else null
}
