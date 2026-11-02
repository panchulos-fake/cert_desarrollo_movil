package com.duoc.prestaaula.domain.model

/** Resultado de una operación de negocio: éxito con valor o fallo con mensaje para el usuario. */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val message: String) : Outcome<Nothing>
}
