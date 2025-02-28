package com.example.vitae

import android.app.ComponentCaller
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityCompartirBtBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.OutputStream
import java.util.UUID
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CompartirBT : AppCompatActivity() {

    private lateinit var botonVolverCompartir: Button
    private lateinit var  botonCompartir: Button
    private lateinit var botonVolverDashboard : Button
    private lateinit var auth : FirebaseAuth
    private lateinit var adaptadorBt : BluetoothAdapter
    private  var socket : BluetoothSocket? = null
    private var AES_LLAVE = "4e7f1a8d2b9c6e3f0a5d8c7b2e9f1a4d".toByteArray() //llave de encriptado 32 bytes
    private var AES_IV = "3c4d5e6f7a8b9c0d".toByteArray() // vector de inicializacion de 16 bytes
    private lateinit var binding: ActivityCompartirBtBinding
    private lateinit var ayudaBluetooth: Button
    private var ESP32: BluetoothDevice? = null


    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent){
            when (intent.action){
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.name == "ESP32"){
                        ESP32 = device
                        adaptadorBt.cancelDiscovery()
                        enviarUID()
                    }
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompartirBtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)
        botonVolverCompartir = findViewById(R.id.boton_volver_dashboard_bt)
        botonCompartir = findViewById(R.id.boton_enviar_uid)

        auth = FirebaseAuth.getInstance()
        adaptadorBt = BluetoothAdapter.getDefaultAdapter()

        if (adaptadorBt == null) {
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (!adaptadorBt.isEnabled) {
            mostrarDialogoBt()
        }

        botonCompartir.setOnClickListener {
           if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
               ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
           } else{
               iniciarDescubrimiento()
           }

        }

        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        botonVolverCompartir.setOnClickListener {
            val intent = Intent(this, DashboardBluetooth::class.java)
            startActivity(intent)
        }
        val intentFilter = android.content.IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, intentFilter)

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                iniciarDescubrimiento()
            } else {
                Toast.makeText(this, "Permiso de ubicacion necesario", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun iniciarDescubrimiento() {
        if (adaptadorBt.isDiscovering) {
            adaptadorBt.cancelDiscovery()
        }
        adaptadorBt.startDiscovery()
        Toast.makeText(this, "Buscando ESP32", Toast.LENGTH_SHORT).show()

        }



    private fun mostrarDialogoBt() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Bluetooth Desactivado")
        builder.setMessage("Para usar esta función, por favor activa el Bluetooth en la configuración del teléfono.")
        builder.setPositiveButton("Activar") { _, _ ->
            // Open Bluetooth settings
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
            ayudaBluetooth.text = "Bluetooth está desactivado. Por favor, actívalo para usar esta función."
        }
        builder.setCancelable(false) // Prevent the user from dismissing the dialog by tapping outside
        builder.show()
    }

    private fun enviarUID() {
        val uid = auth.currentUser?.uid ?: return
        val uidEncriptado = encriptadoAES(uid.toByteArray())
        val codificacionUID = Base64.getEncoder().encodeToString(uidEncriptado)

        if (ESP32 == null) {
            Toast.makeText(this, "ESP32 no encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            socket = ESP32?.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            val output = socket?.outputStream
            output?.write(codificacionUID.toByteArray())
            output?.flush()
            Toast.makeText(this, "UID enviado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al enviar el UID", Toast.LENGTH_SHORT).show()
        }finally {
            try {
                socket?.close()
                } catch (e: Exception) {
                Toast.makeText(this, "Error al cerrar el socket", Toast.LENGTH_SHORT).show()
            }
        }

    }
    // AES encriptado
    private fun encriptadoAES(data: ByteArray): ByteArray {
        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val llave = SecretKeySpec(AES_LLAVE, "AES")
        val vector = IvParameterSpec(AES_IV)
        cifrado.init(Cipher.ENCRYPT_MODE, llave, vector)
        return cifrado.doFinal(data)
    }
}
//    AES desencriptado
//    private fun desencriptadoAES(data: ByteArray): ByteArray {
//        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
//        val llave = SecretKeySpec(AES_LLAVE, "AES")
//        val vector = IvParameterSpec(AES_IV)
//        cifrado.init(Cipher.DECRYPT_MODE, llave, vector)
//        return cifrado.doFinal(data)
//    }

