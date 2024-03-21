package com.example.serviciodescarga.Util

import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object Memoria {
    public fun escribirExterna(cadena: String?): String {
        val tarjeta: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val miFichero: File = File(tarjeta.absolutePath, "frases.html")
        lateinit var bw: BufferedWriter
        lateinit var mensaje: String

        try {
            tarjeta.mkdirs()
            bw = BufferedWriter(FileWriter(miFichero))
            bw.write(cadena)
            Log.i("Información: ", miFichero.absolutePath)
            mensaje = "Fichero escrito OK\n" + miFichero.absolutePath
            Log.i("Info", mensaje)
            bw.close()
        } catch (e: IOException) {
            mensaje = e.message.toString()
            Log.e("Error de E/S", mensaje)
        }
        return mensaje
    }
}