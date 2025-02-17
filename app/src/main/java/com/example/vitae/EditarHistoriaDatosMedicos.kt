package com.example.vitae

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarHistoriaDatosMedicos : AppCompatActivity() {

    private lateinit var actualizarDatosMedicos: TextView
    private lateinit var actualizarAlergia: TextView
    private lateinit var botonActualizarAlergias: RadioGroup
    private lateinit var actualizarSiAlergia: RadioButton
    private lateinit var actualizarNoAlergia: RadioButton
    private lateinit var actualizarEspecificaAlergias: EditText
    private lateinit var actualizarClaustrofobia: TextView
    private lateinit var botonActualizarClaustrofobia: RadioGroup
    private lateinit var actualizarSiClaustrofobia: RadioButton
    private lateinit var actualizarNoClaustrofobia: RadioButton
    private lateinit var actualizarObservacionesAdicionales: EditText
    private lateinit var botonActualizarDatosMedicos: Button
    private lateinit var botonCancelar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_historia_datos_medicos)

        actualizarDatosMedicos = findViewById(R.id.actualizar_datos_medicos)
        actualizarAlergia = findViewById(R.id.actualizar_alergia)
        botonActualizarAlergias = findViewById(R.id.boton_actualizar_alergias)
        actualizarSiAlergia = findViewById(R.id.actualizar_si_alergia)
        actualizarNoAlergia = findViewById(R.id.actualizar_no_alergia)
        actualizarEspecificaAlergias = findViewById(R.id.actualizar_especifica_alergias)
        actualizarClaustrofobia = findViewById(R.id.actualizar_claustrofobia)
        botonActualizarClaustrofobia = findViewById(R.id.boton_actualizar_claustrofobia)
        actualizarSiClaustrofobia = findViewById(R.id.actualizar_si_claustrofobia)
        actualizarNoClaustrofobia = findViewById(R.id.actualizar_no_claustrofobia)
        actualizarObservacionesAdicionales = findViewById(R.id.actualizar_observaciones_adicionales)
        botonActualizarDatosMedicos = findViewById(R.id.boton_actualizar_datos_medicos)
        botonCancelar = findViewById(R.id.boton_cancelar)

        buscarDatosMedicos()

        botonActualizarAlergias.setOnCheckedChangeListener { _, checkedId ->
            actualizarEspecificaAlergias.visibility = if (checkedId == R.id.actualizar_si_alergia) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        botonActualizarDatosMedicos.setOnClickListener {
            actualizarDatosMedicos()
        }
        botonCancelar.setOnClickListener {
            finish()
        }
    }

    private fun buscarDatosMedicos() {
        val user = auth.currentUser?.uid
        if (user != null) {
            db.collection("datosMedicos").document(user).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        actualizarSiAlergia.isChecked = document.getString("Alergia") == "Si"
                        actualizarNoAlergia.isChecked = document.getString("Alergia") == "No"
                        actualizarEspecificaAlergias.setText(document.getString("EspecificaAlergias"))
                        actualizarSiClaustrofobia.isChecked =
                            document.getString("Claustrofobia") == "Si"
                        actualizarNoClaustrofobia.isChecked =
                            document.getString("Claustrofobia") == "No"
                        actualizarObservacionesAdicionales.setText(document.getString("Observaciones"))
                    } else {
                        Toast.makeText(this, "No se encontraron datos médicos", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al buscar los datos médicos", Toast.LENGTH_SHORT)
                        .show()


                }


        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarDatosMedicos(){
        val user = auth.currentUser?.uid
        if (user != null) {
            val actualizarData = hashMapOf<String, Any>(
                "Alergia" to if (actualizarSiAlergia.isChecked) "Si" else "No",
                "Claustrofobia" to if (actualizarSiClaustrofobia.isChecked) "Si" else "No",
                "Observaciones" to actualizarObservacionesAdicionales.text.toString(),
                "EspecificaAlergias" to if (actualizarSiAlergia.isChecked) actualizarEspecificaAlergias.text.toString() else ""
            )

            db.collection("datosMedicos").document(user).update(actualizarData)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Datos médicos actualizados correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()


                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al actualizar los datos médicos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

    }
}