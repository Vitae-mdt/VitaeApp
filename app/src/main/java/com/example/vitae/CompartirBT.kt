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

    //declaracion de variables que seran inicalizadas mas tarde
    //UUIDs de los servicios y características BLE
    //Codigo de solicitud de permisos para identificarlo al solicitarlos
    private lateinit var botonVolverCompartir: Button
    private lateinit var botonCompartir: Button
    private lateinit var botonVolverDashboard: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var adaptadorBt: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val serviceUUID = "12345678-1234-5678-1234-567812345678"
    private val characteristicUUID = "87654321-4321-8765-4321-876543218765"
    //private var AES_LLAVE = "4e7f1a8d2b9c6e3f0a5d8c7b2e9f1a4d".toByteArray() //llave de encriptado 32 bytes
    //private var AES_IV = "3c4d5e6f7a8b9c0d".toByteArray() // vector de inicializacion de 16 bytes
    private lateinit var binding: ActivityCompartirBtBinding
    private lateinit var ayudaBluetooth: Button
    companion object val PERMISSIONS_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        //configuracion de la actividad y vinculacion del diseño con el codigo
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompartirBtBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //inicializacion de los botones y obtencion de la instancia de FirebaseAuth
        //obtencion del adaptador Bluetooth y verificacion de su estado

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
            mostrarDialogoUbicacion()
        }

        //escuchador de eventos para el boton de compartir
        //fuerza la desconexion del dispositivo Bluetooth si esta conectado
        //verifica si los permisos de ubicacion y conexion Bluetooth estan concedidos
        //si no lo estan, solicita los permisos
        //si los permisos estan concedidos, inicia el proceso de escaneo para encontrar el dispositivo Bluetooth
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

        //escuchador de eventos para los botones de volver y volver al dashboard
        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        botonVolverCompartir.setOnClickListener {
            val intent = Intent(this, DashboardBluetooth::class.java)
            startActivity(intent)
        }
    }

    //funcion que solicita los permisos de ubicacion y conexion Bluetooth
    private fun permisos(){
        val permisosRequeridos = mutableListOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.INTERNET
        )
        //verifica si la version de androiod es mayor o igual a la version 12
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permisosRequeridos.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            permisosRequeridos.add(android.Manifest.permission.BLUETOOTH_SCAN)
        }
        //filtra los permisos que no estan concedidos
        val permisosNegados = permisosRequeridos.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        //si hay permisos que no estan concedidos, solicita los permisos
        if (permisosNegados.isNotEmpty()) {
            requestPermissions(permisosNegados.toTypedArray(), 1)
        } else{
            //si todos los permisos estan concedidos, inicia el proceso de escaneo
           empezarScaneo()
        }

    }
    //Funcion que maneja el resultado de las solicitudes de permisos
    // Se verifica si los permisos fueron otorgados para continuar con el flujo de la app
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
    //funcion que inicia el proceso de escaneo para encontrar el dispositivo Bluetooth
    //se configura el escaneo para que solo busque dispositivos BLE
    //se verifica si el dispositivo tiene el permiso para conectarse a Bluetooth
    //si lo tiene, se inicia el escaneo utilizando ScanCallback para manejar los resultados del escaneo y encontrar el dispositivo exacto
    //luego detiene el escaneo y se conecta al dispositivo encontrado, iniciando la funcion conexionDispositivo
    private fun empezarScaneo() {
        val escaneo = adaptadorBt.bluetoothLeScanner
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        escaneo.startScan(object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.device?.let { device ->
                    if (device.name == "ESP32-BLE-Receiver") {
                        if (device.name == "ESP32-BLE-Receiver") {
                        escaneo.stopScan(this)
                        conexionDispositivo(device)
                        }
                    }
                }
                }
            })
        Toast.makeText(this, "Buscando y conectando a ESP32", Toast.LENGTH_SHORT).show()
    }
    //funcion que se encarga de establecer la conexion con el dispositivo Bluetooth
    //se verifica si el dispositivo tiene el permiso para conectarse a Bluetooth
    //si lo tiene, se crea una instancia de BluetoothGatt para establecer la conexion
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

    //funcion que maneja los callbacks del dispositivo Bluetooth, definiendo los eventos de conexion y desconexion
    //se verifica si la conexion fue exitosa y se inicia el descubrimiento de servicios
    //si el dispositivo se desconecta, se cierra la conexion y se reinicia el proceso
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt?.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt?.close()
                bluetoothGatt = null
            }
        }
        //funcion que maneja los callbacks de descubrimiento de servicios
        //se verifica si el descubrimiento fue exitoso y se obtiene el servicio y la caracteristica a utilizar
        //si es exitoso, se envia el UID al dispositivo Bluetooth mediante enviarUID
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val servicio = gatt?.getService(UUID.fromString(serviceUUID))
                if (servicio == null) {
                    return
                }
                val caracteristica = servicio.getCharacteristic(UUID.fromString(characteristicUUID))
                if (caracteristica == null) {
                    return
                }
                enviarUID(gatt, caracteristica)
            } else {
            }
        }

        //funcion que maneja los callbacks de escritura de datos
        // Confirma si la operación fue exitosa o falló.
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            } else {
            }
        }
    }
    // envía el UID del usuario autenticado a través de una característica Bluetooth específica
    // verifica los permisos necesarios, convierte el UID en bytes y realiza la escritura en la característica
    // luego, desconecta el dispositivo tras un breve retraso
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
            return
        }
        gatt.writeCharacteristic(caracteristica)
        Toast.makeText(this, "UID enviado", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Id enviado: $uid", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            gatt.disconnect()
        } ,1000)
    }
    //cierra la conexion con el dispositivo Bluetooth
    //verifica los permisos necesarios y cierra la conexion

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.close()
    }

    //funcion que muestra un dialogo de alerta para activar el Bluetooth
    //se crea un dialogo de alerta con un titulo y un mensaje
    //se agregan botones para activar el Bluetooth y cancelar la accion
    //se muestra el dialogo y se finaliza la actividad
    private fun mostrarDialogoBt() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Bluetooth Desactivado")
        builder.setMessage("Para usar esta función, por favor activa el Bluetooth en la configuración del teléfono.")
        builder.setPositiveButton("Activar") { _, _ ->
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    //funcion que muestra un dialogo de alerta para activar la ubicacion
    //se crea un dialogo de alerta con un titulo y un mensaje
    //se agregan botones para activar la ubicacion y cancelar la accion
    //se muestra el dialogo y se finaliza la actividad
    private fun mostrarDialogoUbicacion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ubicación Desactivada")
        builder.setMessage("Para usar esta función, por favor activa la ubicación en la configuración del teléfono.")
        builder.setPositiveButton("Activar") { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }
    //funcion que fuerza la desconexion del dispositivo Bluetooth
    //verifica los permisos necesarios y cierra la conexion
    //si el dispositivo no esta conectado, muestra un reinicio de conexion
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
            Toast.makeText(this, "Reiniciando conexion al ESP32", Toast.LENGTH_SHORT).show()
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


