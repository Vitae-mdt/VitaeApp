package com.example.vitae

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vitae.databinding.ActivityDashboardNfcBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and


class DashboardNFC : AppCompatActivity() {

    //declara las variables que seran inicializadas mas tarde

    private lateinit var botonVolverDashboard: Button
    private lateinit var ayudaNfc: TextView
    private lateinit var botonEscribirNfc: Button
    private lateinit var botonLeerDatosNfc: Button
    private lateinit var botonCompartirNfc: Button
    private lateinit var AdaptadorNFC: NfcAdapter
    private lateinit var IntentoNFC: Intent
    private lateinit var auth: FirebaseAuth
    private lateinit var pendingIntent: PendingIntent
    //llave de encriptado 32 bytes
    private var AES_LLAVE = byteArrayOf(
         0x4e.toByte(), 0x7f.toByte(), 0x1a.toByte(), 0x8d.toByte(),
         0x2b.toByte(), 0x9c.toByte(), 0x6e.toByte(), 0x3f.toByte(),
          0x0a.toByte(), 0x5d.toByte(), 0x8c.toByte(), 0x7b.toByte(),
          0x2e.toByte(), 0x9f.toByte(), 0x1a.toByte(), 0x4d.toByte()
    )

    // Vector de Inicialización (IV) de 16 bytes
    private var AES_IV = byteArrayOf(
        0x3c.toByte(), 0x4d.toByte(), 0x5e.toByte(), 0x6f.toByte(),
        0x7a.toByte(), 0x8b.toByte(), 0x9c.toByte(), 0x0d.toByte(),
        0x3c.toByte(), 0x4d.toByte(), 0x5e.toByte(), 0x6f.toByte(),
        0x7a.toByte(), 0x8b.toByte(), 0x9c.toByte(), 0x0d.toByte()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_nfc)
        val binding = ActivityDashboardNfcBinding.inflate(layoutInflater)
        //vincula los elementos de la vista con las variables correspondientes
        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)
        ayudaNfc = findViewById(R.id.ayuda_nfc)
        botonEscribirNfc = findViewById(R.id.boton_escribir_NFC)
        botonLeerDatosNfc = findViewById(R.id.boton_leer_datos_NFC)
        botonCompartirNfc = findViewById(R.id.boton_compartir_nfc)

        //inicializa las variables auth y AdaptadorNFC que se conecta a la base de datos de firebase
        auth = FirebaseAuth.getInstance()
        // la variable AdaptadorNFC se inicializa con el adaptador NFC del dispositivo
        // si el dispositivo no soporta NFC se muestra un mensaje y se cierra la actividad
        AdaptadorNFC = NfcAdapter.getDefaultAdapter(this)
        if (AdaptadorNFC == null) {
            ayudaNfc.text = "El dispositivo no soporta NFC"
            finish()
        }
        //verifica cuando el dispositivo soporta NFC y si esta activado
        if (!AdaptadorNFC.isEnabled) {
            // NFC está desactivado, pide al usuario que lo active
            mostrarDialogoActivarNFC()
        }
        // Crea un Intent para la actividad actual y configura flags para evitar múltiples instancias y manejar intents repetidos (FLAG_ACTIVITY_SINGLE_TOP).
        // Luego, crea un PendingIntent que contiene este Intent, usando flags específicas para la versión de Android (FLAG_MUTABLE para Android 12+).
        // El PendingIntent se utilizará para manejar eventos NFC.
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        //se declara la variable pendingIntent que contiene el PendingIntent creado anteriormente
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        //escucha el evento de clic en el botón de escribir NFC y muestra un mensaje
        //escucha el evento de clic en el botón de leer datos NFC y muestra un mensaje
        //escucha el evento de clic en el botón de volver al dashboard y abre la actividad correspondiente
        //escucha el evento de clic en el botón de ayuda NFC y abre la actividad correspondiente

        botonEscribirNfc.setOnClickListener {
            Toast.makeText(this, "Acerque la Tarjeta NFC al dispositivo para cargar el ID", Toast.LENGTH_SHORT).show()
        }

        botonLeerDatosNfc.setOnClickListener {
            Toast.makeText(this, "Acerque la Tarjeta NFC al dispositivo para leer los datos", Toast.LENGTH_SHORT).show()
        }


        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        ayudaNfc.setOnClickListener {
            val intent = Intent(this, AyudaNFC::class.java)
            startActivity(intent)
        }





    }

    //se crea la funcion onNewIntent que maneja los eventos NFC
    //se crea la funcion onResume que activa el adaptador NFC y le da prioridad a la hora de escuchar los eventos NFC
    //se crea la funcion onPause que desactiva el adaptador NFC y le quita la prioridad a la hora de escuchar los eventos NFC
    //se crea la funcion manejaNFC que maneja los eventos NFC
    //se crea la funcion escribirDatosNFC que escribe los datos del usuario en la tarjeta NFC
    //se crea la funcion leerDatosNfc que lee los datos del usuario de la tarjeta NFC
    //se crea la funcion mostrarDialogoActivarNFC que muestra un dialogo de alerta cuando el NFC esta desactivado

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        manejaNFC(intent)
    }
    //// Habilita el despachador en primer plano para que esta actividad reciba los intents de descubrimiento de etiquetas NFC.
    override fun onResume() {
        super.onResume()
        AdaptadorNFC.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    //// Deshabilita el despachador en primer plano para evitar que la actividad reciba intents NFC cuando no está en primer plano.
    override fun onPause() {
        super.onPause()
        AdaptadorNFC.disableForegroundDispatch(this)
    }

    // Obtiene el objeto Tag de la etiqueta NFC del Intent.  El objeto Tag contiene información sobre la etiqueta.
    // Verifica si se obtuvo un objeto Tag válido. Si es nulo, significa que no se detectó una etiqueta o hubo un error.
    // Verifica si se pudo obtener un objeto Ndef. Si es nulo, significa que la etiqueta no contiene datos Ndef o no está formateada correctamente.
    // Si la acción del Intent es ACTION_NDEF_DISCOVERED, significa que se está leyendo datos de la etiqueta.
    // Si la acción del Intent no es ACTION_NDEF_DISCOVERED, significa que se está escribiendo datos en la etiqueta.
    private fun manejaNFC(intent: Intent) {

        val datos: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (datos != null) {
            val ndef = Ndef.get(datos)
            if (ndef != null) {
                if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED){
                    leerDatosNfc(ndef)
                }
                else {
                    escribirDatosNFC(ndef)
                }
            }
        }
    }

    // Obtiene el UID del usuario actual desde Firebase Authentication, si auth.currentUser?.uid es nulo (el usuario no está autenticado), la función retorna inmediatamente (evitando errores).
    // Crea un objeto NdefRecord que contiene el UID del usuario como texto plano.
    // Crea un objeto NdefMessage que contiene el objeto NdefRecord.
    // Conecta con la etiqueta NFC y escribe el objeto NdefMessage en la etiqueta.
    // Muestra un mensaje Toast indicando que los datos se han escrito correctamente.
    // En caso de error, muestra un mensaje Toast indicando que hubo un error al escribir los datos.
    // Finalmente, cierra la conexión con la etiqueta NFC.
    private fun escribirDatosNFC(ndef: Ndef) {
     try {
         ndef.connect()
         val uid = auth.currentUser?.uid ?: return
         val encriptado = encriptadoAES(uid.toByteArray())
//         val longitud = encriptado.size
//         Toast.makeText(this, "Longitud del UID: $longitud", Toast.LENGTH_SHORT).show()
//         val datosEncriptados = encriptado.joinToString("") { "%02x".format(it) }
//         Toast.makeText(this, "Datos encriptados: $datosEncriptados", Toast.LENGTH_SHORT).show()
         val codificacionUID = Base64.getEncoder().encodeToString(encriptado)
         val ndefRecord = NdefRecord.createTextRecord(null, codificacionUID)
         val ndefMessage = NdefMessage(ndefRecord)
         ndef.writeNdefMessage(ndefMessage)
         Toast.makeText(this, "Datos escritos correctamente", Toast.LENGTH_SHORT).show()
     } catch (e: Exception) {
         Toast.makeText(this, "Error al escribir los datos", Toast.LENGTH_SHORT).show()
     }finally {
         ndef.close()
     }

    }


    // Crea la conexión con la etiqueta NFC y lee los datos Ndef del mensaje.
    //Esta funcion se encarga de decodificar y leer los datos del usuario, saltando el codigo de lenguaje y definiendo el tipo de codificacion
    //El manejo de salto de codigo de lenguaje se realiza con la variable languageCodeLength
    //El manejo de la codificacion se realiza con la variable textEncoding
    // Si se pudo leer los datos correctamente, muestra un mensaje Toast con el ID del usuario.
    // Si hubo un error al leer los datos, muestra un mensaje Toast indicando que hubo un error.
    // Si no se encontraron datos, muestra un mensaje Toast indicando que no se encontraron datos.
    // Finalmente, cierra la conexión con la etiqueta NFC.
    private fun leerDatosNfc(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            if (ndefMessage != null) {
                val ndefRecord = ndefMessage.records[0]
                val codificacionUIDBytes = ndefRecord.payload
                val codigoTexto = if ((codificacionUIDBytes[0] and 128.toByte()).toInt() == 0) "UTF-8" else "UTF-16"
                val CodigoLenguaje = codificacionUIDBytes[0] and 127.toByte()
                val codificacionUID = String(codificacionUIDBytes, CodigoLenguaje + 1, codificacionUIDBytes.size - CodigoLenguaje - 1, Charsets.UTF_8)
                val encriptadoUID = try {
                    Base64.getDecoder().decode(codificacionUID)
                }catch (e: Exception) {
                    Toast.makeText(this, "Datos invalidos Base64", Toast.LENGTH_SHORT).show()
                    return
                }
                val desencriptadoUID = try{
                    desencriptadoAES(encriptadoUID)
                }catch (e: Exception) {
                    Toast.makeText(this, "Error al desencriptar los datos", Toast.LENGTH_SHORT).show()
                    return
                }
                val uid = String(desencriptadoUID, Charsets.UTF_8)
                Toast.makeText(this, "ID de Usuario: $uid", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al leer los datos", Toast.LENGTH_SHORT).show()
        } finally {
           ndef.close()

        }
    }

    // Muestra un diálogo de alerta para activar el NFC.
    // El diálogo contiene un botón para activar el NFC y otro para cancelar.
    // Si el usuario hace clic en el botón de activar, se abre la configuración de NFC del sistema.
    // Si el usuario hace clic en el botón de cancelar, la actividad se cierra.
    // El diálogo no se puede cancelar tocando fuera de él.
    private fun mostrarDialogoActivarNFC() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("NFC Desactivado")
        builder.setMessage("Para usar esta función, por favor activa el NFC en la configuración del teléfono.")
        builder.setPositiveButton("Activar") { _, _ ->
            // Abre la configuración de NFC del sistema
            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()

            ayudaNfc.text = "NFC está desactivado. Por favor, actívalo para usar esta función."
        }
        builder.setCancelable(false) // Evita que el usuario cierre el diálogo tocando fuera
        builder.show()
    }


    //se crea la funcion encriptadoAES que encripta los datos del usuario
    //con el metodo AES, basado en la llave y el vector de inicializacion
    private fun encriptadoAES (data: ByteArray): ByteArray {
        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val llave = SecretKeySpec(AES_LLAVE, "AES")
        val vector = IvParameterSpec(AES_IV)
        cifrado.init(Cipher.ENCRYPT_MODE, llave, vector)
        return cifrado.doFinal(data)
    }

    //se crea la funcion desencriptadoAES que desencripta los datos del usuario
    //con el metodo AES, basado en la llave y el vector de inicializacion
    private fun desencriptadoAES (data: ByteArray): ByteArray {
        val cifrado = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val llave = SecretKeySpec(AES_LLAVE, "AES")
        val vector = IvParameterSpec(AES_IV)
        cifrado.init(Cipher.DECRYPT_MODE, llave, vector)
        return cifrado.doFinal(data)
    }
//
//    private fun comparentirNfc() {
//        val uid = auth.currentUser?.uid ?: run {
//            Toast.makeText(this, "No se pudo obtener el UID del usuario", Toast.LENGTH_SHORT).show()
//            return
//        }
//        val encriptado = encriptadoAES(uid.toByteArray())
//        val codificacionUID = Base64.getEncoder().encodeToString(encriptado)
//        val ndefRecord = NdefRecord.createTextRecord(null, codificacionUID)
//        val ndefMessage = NdefMessage(ndefRecord)
//
//        val adaptadorNFC = NfcAdapter.getDefaultAdapter(this)
//        adaptadorNFC.setNdefPushMessageCallback(this, this)
//        Toast.makeText(this, "Acerca el dispositivo al lector para compartir el ID", Toast.LENGTH_SHORT).show()
//
//
//    }
}

