package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityCrearHistoriasBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearHistorias : AppCompatActivity() {

    //declara las variables binding, NombreCompleto, FechaNacimiento, LugarNacimiento, Altura, Peso, Edad,
    // CorreoPersonal, NumeroCasa, NumeroPersonal, TipoSangre, btnSiguiente, btnRegresarInicio
    // las cuales seran inicializadas mas tarde
    lateinit var binding: ActivityCrearHistoriasBinding
    lateinit var NombreCompleto: EditText
    lateinit var FechaNacimiento: EditText
    lateinit var LugarNacimiento: EditText
    lateinit var Altura: EditText
    lateinit var Peso: EditText
    lateinit var Edad: EditText
    lateinit var CorreoPersonal: EditText
    lateinit var NumeroCasa: EditText
    lateinit var NumeroPersonal: EditText
    lateinit var TipoSangre: EditText
    lateinit var btnSiguiente: Button
    lateinit var btnRegresarInicio: Button

    //declara la variable db que se conecta a la base de datos de firebase
    private var db = FirebaseFirestore.getInstance()

    //inicializa la vista y los listeners de los botones y crea un objeto de autenticacion de firebase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_historias)

        binding = ActivityCrearHistoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //vincula los elementos de la vista con las variables correspondientes
        NombreCompleto = findViewById(R.id.nombre_completo)
        FechaNacimiento = findViewById(R.id.fecha_nacimiento)
        LugarNacimiento = findViewById(R.id.lugar_nacimiento)
        Altura = findViewById(R.id.altura)
        Peso = findViewById(R.id.peso)
        Edad = findViewById(R.id.edad)
        CorreoPersonal = findViewById(R.id.correo_personal)
        NumeroCasa = findViewById(R.id.numero_casa)
        NumeroPersonal = findViewById(R.id.numero_personal)
        TipoSangre = findViewById(R.id.tipo_sangre)
        btnSiguiente = findViewById(R.id.boton_datos_personales)
        btnRegresarInicio = findViewById(R.id.boton_regresar)


        //escucha el evento de clic en el botón de siguiente y ejecuta la función guardaDato()
        btnSiguiente.setOnClickListener {
            guardaDato()


        }
        //escucha el evento de clic en el botón de regresar y ejecuta la acción que lleva a la actividad Dahsboard
        btnRegresarInicio.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }

    }
    //se crea la funcion guardaDato que guarda los datos del usuario en la base de datos de firebase
    //validando que el usuario exista en la base de datos y sea valido
    //si el usuario no existe, genera una excepcion e inicia la actividad CrearHistorias
    //si el usuario existe, guarda los datos del usuario en la base de datos de firebase
    //y muestra un mensaje de éxito e inicia la actividad CrearHistorias2
    private  fun guardaDato() {
        val user = FirebaseAuth.getInstance().currentUser ?.uid ?: return
        db.collection("usuarios").document(user).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val informacionUsuarios = hashMapOf(
                        "NombreCompleto" to NombreCompleto.text.toString(),
                        "FechaNacimiento" to FechaNacimiento.text.toString(),
                        "LugarNacimiento" to LugarNacimiento.text.toString(),
                        "Altura" to Altura.text.toString().toIntOrNull() ,
                        "Peso" to Peso.text.toString().toIntOrNull(),
                        "Edad" to Edad.text.toString().toIntOrNull(),
                        "CorreoPersonal" to CorreoPersonal.text.toString(),
                        "NumeroCasa" to NumeroCasa.text.toString(),
                        "NumeroPersonal" to NumeroPersonal.text.toString(),
                        "TipoSangre" to TipoSangre.text.toString(),
                        "IdUsuario" to user,
                    )
                    db.collection("informacionUsuarios").document(user).set(informacionUsuarios)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos de usuario guardados correctamente", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, CrearHistorias2::class.java)
                            startActivity(intent)

                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar los datos de usuario", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CrearHistorias::class.java)
                    startActivity(intent)

                }
            }

    }
}