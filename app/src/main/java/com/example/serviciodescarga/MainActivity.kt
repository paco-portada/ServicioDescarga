package com.example.serviciodescarga

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.serviciodescarga.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityMainBinding
    lateinit var intentFilter: IntentFilter
    lateinit var broadcastReceiver: BroadcastReceiver

    companion object {
        private const val REQUEST_CONNECT = 1
        const val WEB = "https://dam.org.es/ficheros/frases.html"
        const val ACTION_RESP = "RESPUESTA_DESCARGA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        binding.botonIniciar.setOnClickListener(this)
        binding.botonParar.setOnClickListener(this)

        intentFilter = IntentFilter(ACTION_RESP)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        broadcastReceiver = ReceptorOperacion()
        // registerReceiver(broadcastReceiver, intentFilter);
    }

    public override fun onResume() {
        super.onResume()
        //---registrar el receptor ---
        registerReceiver(broadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
    }

    public override fun onPause() {
        super.onPause()
        //--- anular el registro del recpetor ---
        unregisterReceiver(broadcastReceiver)
    }

    inner class ReceptorOperacion : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val respuesta = intent.getStringExtra("resultado")

            binding.salida.text = respuesta
            mostrarMensaje(respuesta!!);
        }
    }

    override fun onClick(v: View) {
        binding.salida.text = ""
        lateinit var i: Intent;

        if (v === binding.botonIniciar) {
            if (comprobarPermiso()) {
                if (!binding.switch1.isChecked) {
                    // uso con Service
                    startService(Intent(this@MainActivity, DownloadService::class.java))
                } else {
                    // uso con IntentService
                    i = Intent(this, DownloadIntentService::class.java)
                    i.putExtra("web", WEB)
                    startService(i)
                }
            }
        }
        if (v === binding.botonParar) {
            if (!binding.switch1.isChecked) {
                stopService(Intent(this@MainActivity, DownloadService::class.java))
            } else {
                stopService(Intent(this@MainActivity, DownloadIntentService::class.java))
            }
        }
    }

    private fun comprobarPermiso(): Boolean {
        //https://developer.android.com/training/permissions/requesting?hl=es-419
        val permiso = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        // Manifest.permission.INTERNET
        var concedido = false
        // comprobar los permisos
        if (ActivityCompat.checkSelfPermission(
                this,
                permiso
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // pedir los permisos necesarios, porque no están concedidos
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permiso)) {
                concedido = false
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permiso), REQUEST_CONNECT)
                // Cuando se cierre el cuadro de diálogo se ejecutará onRequestPermissionsResult
            }
        } else {
            concedido = true
        }
        return concedido
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permiso = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        //Manifest.permission.INTERNET;
        // chequeo los permisos de nuevo
        if (requestCode == REQUEST_CONNECT) if (ActivityCompat.checkSelfPermission(
                this,
                permiso
            ) == PackageManager.PERMISSION_GRANTED
        ) // permiso concedido
            startService(
                Intent(
                    this@MainActivity,
                    DownloadService::class.java
                )
            ) else  // no hay permiso
            mostrarMensaje("No se ha concedido permiso para escribir en memoria externa")
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

}