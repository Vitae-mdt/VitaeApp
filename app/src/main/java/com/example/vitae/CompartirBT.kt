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

class CompartirBT : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private lateinit var EnviarDatos: Button
    private var adaptadorBluetooth: BluetoothAdapter? = null
    private var mmSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private lateinit var VolverDashboard: Button
    private lateinit var VolverDashboardBt: Button
    private lateinit var binding: ActivityCompartirBtBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompartirBtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EnviarDatos = findViewById(R.id.boton_enviar_uid)
        VolverDashboard = findViewById(R.id.boton_volver_dashboard)
        VolverDashboardBt = findViewById(R.id.boton_volver_dashboard_bt)

        VolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        VolverDashboardBt.setOnClickListener {
            finish()
        }

        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter()
        if (adaptadorBluetooth == null) {
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        EnviarDatos.setOnClickListener{
            if (!adaptadorBluetooth!!.isEnabled){
                val intentoBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intentoBT, REQUEST_ENABLE_BT)
            } else {
                var direccionBt = "ESP32_Idaca"
                var dispositivo = adaptadorBluetooth!!.getRemoteDevice(direccionBt)

                try{
                    val UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    val mmSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(UUID)
                    mmSocket.connect()
                    val outputStream = mmSocket.outputStream

                    var user = FirebaseAuth.getInstance().currentUser?.uid
                    var userUid = user?.toString()?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()
                    outputStream.write(userUid)
                    outputStream.flush()
                    Toast.makeText(this, "Datos enviados", Toast.LENGTH_SHORT).show()

                } catch (e: Exception){
                    Toast.makeText(this, "Error al enviar los datos", Toast.LENGTH_SHORT).show()
                }
                finally {
                    try {
                        mmSocket?.close()
                    }
                    catch (e: Exception){
                        Toast.makeText(this, "Error al cerrar el socket", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }







    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth no habilitado", Toast.LENGTH_SHORT).show()
        }
    }
}