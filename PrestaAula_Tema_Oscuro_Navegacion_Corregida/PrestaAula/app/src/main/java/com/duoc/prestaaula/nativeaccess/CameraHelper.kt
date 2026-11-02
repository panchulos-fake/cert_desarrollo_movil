package com.duoc.prestaaula.nativeaccess

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Recurso nativo 1: cámara. Las fotos se guardan en almacenamiento interno de la app. */
object CameraHelper {
    fun createPhotoFile(context: Context): File {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        return File(dir, "devolucion_${System.currentTimeMillis()}.jpg")
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
