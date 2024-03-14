package com.example.serviciodescarga

import android.app.IntentService
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onHandleIntent(intent: Intent?) {

        if (intent != null) {
            val web = intent.extras?.getString("web")
            lateinit var url: URL
            try {
                url = URL(web)
                descargaOkHTTP(url)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                enviarRespuesta("Error en la URL: " + e.message)
            }
        }
    }

    private fun descargaOkHTTP(web: URL) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(web)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error: ", e.message.toString())
                enviarRespuesta("Fallo: " + e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body.use { responseBody ->
                    if (!response.isSuccessful) {
                        //throw new IOException("Unexpected code " + response);
                        Log.e("Error: ", "Unexpected code $response")
                        enviarRespuesta("Error: Unexpected code $response")
                    } else {
                        // Read data on the worker thread
                        val responseData = response.body!!.string()
                        //enviarRespuesta("fichero descargado ok")
                        Log.i("Descarga: ", "fichero descargado: " + responseData)

                        // guardar el fichero descargado en memoria externa
                        if (escribirExterna(responseData)) {
                            Log.i("Descarga: ", "fichero descargado")
                        } else {
                            Log.e("Error ", "no se ha podido descargar")
                        }
                    }
                }
            }
        })
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun escribirExterna(cadena: String?): Boolean {
        val miFichero: File
        val tarjeta: File
        lateinit var bw: BufferedWriter
        var correcto = false

        try {
            tarjeta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            miFichero = File(tarjeta.absolutePath, "frases.html")
            bw = BufferedWriter(FileWriter(miFichero))
            bw.write(cadena)
            Log.i("Información: ", miFichero.absolutePath)
            enviarRespuesta("Descarga: fichero descargado OK" + miFichero.absolutePath)
        } catch (e: IOException) {
            if (cadena != null) Log.e("Error: ", cadena)
            Log.e("Error de E/S", e.message.toString())
            enviarRespuesta("Error: " + e.message)
        } finally {
            try {
                if (bw != null) {
                    bw.close()
                    correcto = true
                }
            } catch (e: IOException) {
                Log.e("Error al cerrar", e.message.toString())
            }
        }
        return correcto
    }

    private fun enviarRespuesta(mensaje: String) {
        val i = Intent()
        i.action = MainActivity.ACTION_RESP
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.putExtra("resultado", mensaje)
        sendBroadcast(i)
    }
}