package com.example.vitae

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityDashboardNfcBinding
import com.google.firebase.auth.FirebaseAuth
import android.provider.Settings


class DashboardNFC : AppCompatActivity() {

    private lateinit var botonVolverDashboard: Button
    private lateinit var ayudaNfc: TextView
    private lateinit var botonEscribirNfc: Button
    private lateinit var botonLeerDatosNfc: Button
    private lateinit var botonCompartirNfc: Button
    private lateinit var AdaptadorNFC: NfcAdapter
    private lateinit var IntentoNFC: Intent
    private lateinit var auth: FirebaseAuth
    private lateinit var pendingIntent: PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_nfc)
        val binding = ActivityDashboardNfcBinding.inflate(layoutInflater)
        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)
        ayudaNfc = findViewById(R.id.ayuda_nfc)
        botonEscribirNfc = findViewById(R.id.boton_escribir_NFC)
        botonLeerDatosNfc = findViewById(R.id.boton_leer_datos_NFC)
        botonCompartirNfc = findViewById(R.id.boton_compartir_nfc)

        auth = FirebaseAuth.getInstance()
        AdaptadorNFC = NfcAdapter.getDefaultAdapter(this)
        if (AdaptadorNFC == null) {
            ayudaNfc.text = "El dispositivo no soporta NFC"
            finish()
        }
        if (!AdaptadorNFC.isEnabled) {
            // NFC está desactivado, pide al usuario que lo active
            mostrarDialogoActivarNFC()
        }
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

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
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        manejaNFC(intent)
    }

    override fun onResume() {
        super.onResume()
        AdaptadorNFC.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        AdaptadorNFC.disableForegroundDispatch(this)
    }

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

    private fun escribirDatosNFC(ndef: Ndef) {
     try {
         ndef.connect()
         val uid = auth.currentUser?.uid ?: return
         val ndefRecord = NdefRecord.createTextRecord(null, uid)
         val ndefMessage = NdefMessage(ndefRecord)
         ndef.writeNdefMessage(ndefMessage)
         Toast.makeText(this, "Datos escritos correctamente", Toast.LENGTH_SHORT).show()
     } catch (e: Exception) {
         Toast.makeText(this, "Error al escribir los datos", Toast.LENGTH_SHORT).show()
     }finally {
         ndef.close()
     }

    }

    private fun leerDatosNfc(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            if (ndefMessage != null) {
                val ndefRecord = ndefMessage.records[0]
                val uidBytes = ndefRecord.payload
                val uid = String(uidBytes, Charsets.UTF_8)
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
            // Puedes optar por cerrar la actividad o mostrar un mensaje
            // finish() // Opcional: Cierra la actividad si el usuario no quiere activar NFC
            ayudaNfc.text = "NFC está desactivado. Por favor, actívalo para usar esta función."
        }
        builder.setCancelable(false) // Evita que el usuario cierre el diálogo tocando fuera
        builder.show()
    }

}

