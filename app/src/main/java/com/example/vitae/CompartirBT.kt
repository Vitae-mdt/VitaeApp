package com.example.vitae

import android.Manifest
import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CompartirBT : AppCompatActivity() {

    private lateinit var botonVolverCompartir: Button
    private lateinit var botonCompartir: Button
    private lateinit var botonVolverDashboard: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var adaptadorBt: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val serviceUUID =
        "12345678-1234-5678-1234-567812345678" // Replace with your ESP32 service UUID
    private val characteristicUUID =
        "87654321-4321-8765-4321-876543218765" // Replace with your ESP32 characteristic UUID

    //    private var AES_LLAVE = "4e7f1a8d2b9c6e3f0a5d8c7b2e9f1a4d".toByteArray() //llave de encriptado 32 bytes
//    private var AES_IV = "3c4d5e6f7a8b9c0d".toByteArray() // vector de inicializacion de 16 bytes
    private lateinit var binding: ActivityCompartirBtBinding
    private lateinit var ayudaBluetooth: Button
    companion object val PERMISSIONS_REQUEST_CODE = 1


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

            forzarDesconexion()

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {

                permisos()

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
    }

    private fun permisos(){
        val permisosRequeridos = mutableListOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.INTERNET
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permisosRequeridos.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            permisosRequeridos.add(android.Manifest.permission.BLUETOOTH_SCAN)
        }
        val permisosNegados = permisosRequeridos.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (permisosNegados.isNotEmpty()) {
            requestPermissions(permisosNegados.toTypedArray(), 1)
        } else{
           empezarScaneo()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                empezarScaneo()
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun empezarScaneo() {
        val escaneo = adaptadorBt.bluetoothLeScanner
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        escaneo.startScan(object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let { device ->
                    Log.d("Bluetooth", "Dispositivo encontrado: ${device.name}")
                    if (device.name == "ESP32-BLE-Receiver") {
                        if (device.name == "ESP32-BLE-Receiver") {
                        escaneo.stopScan(this)
                        conexionDispositivo(device)
                        }
                    }
                }
                }
            })
        Toast.makeText(this, "Buscando ESP32...", Toast.LENGTH_SHORT).show()
    }

    private fun conexionDispositivo(device: android.bluetooth.BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("Bluetooth", "Conectado a ESP32")
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d("Bluetooth", "Desconectado de ESP32")
                gatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val servicio = gatt?.getService(UUID.fromString(serviceUUID))
                if (servicio == null) {
                    Log.e("Bluetooth", "Service not found")
                    return
                }
                val caracteristica = servicio.getCharacteristic(UUID.fromString(characteristicUUID))
                if (caracteristica == null) {
                    Log.e("Bluetooth", "Characteristic not found")
                    return
                }
                enviarUID(gatt, caracteristica)
            } else {
                Log.e("Bluetooth", "Service discovery failed")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Bluetooth", "UID sent successfully")
            } else {
                Log.e("Bluetooth", "Failed to send UID")
            }
        }
    }

    private fun enviarUID(gatt: BluetoothGatt, caracteristica: BluetoothGattCharacteristic) {
        val uid = auth.currentUser?.uid ?: return
        if (uid == null) {
            Toast.makeText(this, "UID no encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        caracteristica.value = uid.toByteArray(Charsets.UTF_8)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        gatt.writeCharacteristic(caracteristica)
        Toast.makeText(this, "UID enviado", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Id enviado: $uid", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            gatt.disconnect()
        } ,1000)
//        gatt.disconnect()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothGatt?.close()
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
            ayudaBluetooth.text =
                "Bluetooth está desactivado. Por favor, actívalo para usar esta función."
        }
        builder.setCancelable(false) // Prevent the user from dismissing the dialog by tapping outside
        builder.show()
    }

    private fun forzarDesconexion(){
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
        }else {
            Toast.makeText(this, "No hay conexión", Toast.LENGTH_SHORT).show()
        }
    }
    }

//    // AES encriptado
//    private fun encriptadoAES(data: ByteArray): ByteArray {
//        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
//        val llave = SecretKeySpec(AES_LLAVE, "AES")
//        val vector = IvParameterSpec(AES_IV)
//        cifrado.init(Cipher.ENCRYPT_MODE, llave, vector)
//        return cifrado.doFinal(data)
//    }



//    AES desencriptado
//    private fun desencriptadoAES(data: ByteArray): ByteArray {
//        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
//        val llave = SecretKeySpec(AES_LLAVE, "AES")
//        val vector = IvParameterSpec(AES_IV)
//        cifrado.init(Cipher.DECRYPT_MODE, llave, vector)
//        return cifrado.doFinal(data)
//    }


