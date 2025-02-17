package com.example.vitae


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityCrearHistorias2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearHistorias2 : AppCompatActivity() {

    //declara las variables binding, alergias, claustrofobia, observaciones, botonDatosMedicos, especificaAlergias
    //las cuales seran inicializadas mas tarde
    private lateinit var binding: ActivityCrearHistorias2Binding
    private lateinit var alergias: RadioGroup
    private lateinit var claustrofobia: RadioGroup
    private lateinit var observaciones: EditText
    private lateinit var botonDatosMedicos: Button
    private lateinit var especificaAlergias: EditText

    //declara la variable db que se conecta a la base de datos de firebase
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_historias2)
        //vincula los elementos de la vista con las variables correspondientes
        alergias = findViewById(R.id.boton_alergias)
        claustrofobia = findViewById(R.id.boton_claustrofobia)
        botonDatosMedicos = findViewById(R.id.boton_datos_medicos)
        observaciones = findViewById(R.id.observaciones_adicionales)
        especificaAlergias = findViewById(R.id.especifica_alergias)

        //escucha el evento de clic en el botón de alergias para desplegar el campo especificaAlergias
        alergias.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.si_alergia) {
                especificaAlergias.visibility = View.VISIBLE
            } else {
                especificaAlergias.visibility = View.GONE

            }

            }
        //escucha el evento de clic en el botón de datos médicos y ejecuta la función guardaDatosMedicos()
        botonDatosMedicos.setOnClickListener {
            guardaDatosMedicos()
        }

    }

    //se crea la funcion guardaDatosMedicos que guarda los datos médicos del usuario en la base de datos de firebase
    //validando que el usuario exista en la base de datos y sea valido
    //si el usuario no existe, genera una excepcion y devuelve al dashboard
    //si el usuario existe, guarda los datos médicos del usuario en la base de datos de firebase
    private fun guardaDatosMedicos() {
        val user = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("usuarios").document(user).get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    val alergia = when (alergias.checkedRadioButtonId) {
                        R.id.si_alergia -> "Si"
                        R.id.no_alergia -> "No"
                        else -> ""
                    }
                    val claustrofobia = when (claustrofobia.checkedRadioButtonId) {
                        R.id.si_claustrofobia -> "Si"
                        R.id.no_claustrofobia -> "No"
                        else -> ""
                    }
                    val observaciones = observaciones.text.toString()
                    val especificaAlergias = especificaAlergias.text.toString()
                    val datosMedicos = hashMapOf(
                        "Alergia" to alergia,
                        "Claustrofobia" to claustrofobia,
                        "Observaciones" to observaciones,
                        "EspecificaAlergias" to if (alergia == "Si") especificaAlergias else "",
                        "IdUsuario" to user
                    )
                    db.collection("datosMedicos").document(user).set(datosMedicos)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos médicos guardados correctamente", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Dahsboard::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar los datos médicos", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()

                }


            }
    }


}

