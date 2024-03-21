package com.example.serviciodescarga

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import com.example.serviciodescarga.Util.Memoria
import com.example.serviciodescarga.Util.Memoria.escribirExterna
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

class DownloadService : Service() {
    override fun onCreate() {
        super.onCreate()
        //mostrarMensaje("Creando el servicio . . .")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        lateinit var url: URL
        try {
            url = URL(MainActivity.WEB)
            descargaOkHTTP(url)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            //mostrarMensaje("Error en la URL: " + MainActivity.WEB)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        //mostrarMensaje("Servicio destruido")
    }

    override fun onBind(intent: Intent): IBinder {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun descargaOkHTTP(web: URL) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(web)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error: ", e.message.toString())
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body.use { responseBody ->
                    if (!response.isSuccessful) {
                        //throw new IOException("Unexpected code " + response);
                        Log.e("Error: ", "Unexpected code $response")
                    } else {
                        // Read data on the worker thread
                        val responseData = response.body!!.string()
                        // guardar el fichero descargado en memoria externa
                        val mensaje = Memoria.escribirExterna(responseData)
                        Log.i("Descarga: ", mensaje)
                    }
                }
            }
        })
    }
}