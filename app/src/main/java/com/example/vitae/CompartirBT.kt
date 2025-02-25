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
import android.provider.Settings
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
    private lateinit var socket : BluetoothSocket
    private var AES_LLAVE = "4e7f1a8d2b9c6e3f0a5d8c7b2e9f1a4d".toByteArray() //llave de encriptado 32 bytes
    private var AES_IV = "3c4d5e6f7a8b9c0d".toByteArray() // vector de inicializacion de 16 bytes
    private lateinit var binding: ActivityCompartirBtBinding
    private lateinit var ayudaBluetooth: Button


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

        if (adaptadorBt != null) {
            mostrarDialogoBt()
        }

        botonCompartir.setOnClickListener {
            enviarUID()
        }

        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        botonVolverCompartir.setOnClickListener {
            val intent = Intent(this, DashboardBluetooth::class.java)
            startActivity(intent)
        }


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

        val direccionESP = "ESP32"
        val dispositivoESP = adaptadorBt.getRemoteDevice(direccionESP)

        try {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            socket = dispositivoESP.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            val output = socket.outputStream
            output.write(codificacionUID.toByteArray())
            output.flush()
            Toast.makeText(this, "UID enviado", Toast.LENGTH_SHORT).show()

        }catch (e: Exception) {
            Toast.makeText(this, "Error al enviar el UID", Toast.LENGTH_SHORT).show()
        }finally {
            socket.close()
        }

    }
    // AES encryption function
    private fun encriptadoAES(data: ByteArray): ByteArray {
        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val llave = SecretKeySpec(AES_LLAVE, "AES")
        val vector = IvParameterSpec(AES_IV)
        cifrado.init(Cipher.ENCRYPT_MODE, llave, vector)
        return cifrado.doFinal(data)
    }

    // AES decryption function
    private fun desencriptadoAES(data: ByteArray): ByteArray {
        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val llave = SecretKeySpec(AES_LLAVE, "AES")
        val vector = IvParameterSpec(AES_IV)
        cifrado.init(Cipher.DECRYPT_MODE, llave, vector)
        return cifrado.doFinal(data)
    }
}