package com.example.vitae

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class verHistoria : AppCompatActivity() {

    //declara las variables nombre, fecha_nacimiento, altura, edad, peso, lugar_nacimiento, correo_personal, numero_casa, numero_personal, tipo_sangre, alergias, alergia_2, claustrofobia, observaciones, boton_regresar_inicio_ver_historias
    //las cuales seran inicializadas mas tarde
    private lateinit var nombre: TextView
    private lateinit var fecha_nacimiento: TextView
    private lateinit var altura: TextView
    private lateinit var edad: TextView
    private lateinit var peso: TextView
    private lateinit var lugar_nacimiento: TextView
    private lateinit var correo_personal: TextView
    private lateinit var numero_casa: TextView
    private lateinit var numero_personal: TextView
    private lateinit var tipo_sangre: TextView
    private lateinit var alergias: TextView
    private lateinit var alergia_2: TextView
    private lateinit var claustrofobia: TextView
    private lateinit var observaciones: TextView
    private lateinit var boton_regresar_inicio_ver_historias: Button

    //declara las variables auth y db que se conecta a la base de datos de firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_historia)

        //inicializa las variables auth y db que se conecta a la base de datos de firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        //vincula los elementos de la vista con las variables correspondientes
        nombre = findViewById(R.id.texto_nombre)
        fecha_nacimiento = findViewById(R.id.texto_fecha_nacimiento)
        altura = findViewById(R.id.texto_altura)
        edad = findViewById(R.id.texto_edad)
        peso = findViewById(R.id.texto_peso)
        lugar_nacimiento = findViewById(R.id.texto_lugar_nacimiento)
        correo_personal = findViewById(R.id.texto_correo_personal)
        numero_casa = findViewById(R.id.texto_numero_casa)
        numero_personal = findViewById(R.id.texto_numero_personal)
        tipo_sangre = findViewById(R.id.texto_tipo_sangre)
        alergias = findViewById(R.id.texto_alergias)
        alergia_2 = findViewById(R.id.texto_alergia_2)
        claustrofobia = findViewById(R.id.texto_claustrofobia)
        observaciones = findViewById(R.id.texto_observaciones)
        boton_regresar_inicio_ver_historias = findViewById(R.id.boton_regresar_inicio_ver_historias)

        //escucha el evento de clic en el botón de regresar y ejecuta la acción que lleva a la actividad Dahsboard
        boton_regresar_inicio_ver_historias.setOnClickListener {
            finish()

        }
        //ejecuta la función recibirDatos()
        recibirDatos()


    }
    //se crea la funcion recibirDatos que recibe los datos del usuario de la base de datos de firebase
    //validando que el usuario exista en la base de datos y sea valido
    //si el usuario no existe, genera una excepcion
    //si el usuario existe, recibe los datos del usuario de la base de datos de firebase y los muestra en la vista
    private fun recibirDatos() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            db.collection("informacionUsuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nombre.text = "Nombre Completo: " + document.getString("NombreCompleto")
                        fecha_nacimiento.text = "Fecha de Nacimiento: " +  document.getString("FechaNacimiento")
                        val Altura = document.getLong("Altura")?.toString() ?: "N/A"
                        val Edad = document.getLong("Edad")?.toString() ?: "N/A"
                        val Peso = document.getLong("Peso")?.toString() ?: "N/A"
                        altura.text = "Edad: $Edad"
                        peso.text = "Peso: $Peso"
                        edad.text = "Altura: $Altura"
                        lugar_nacimiento.text = "Lugar de Nacimiento: " + document.getString("LugarNacimiento")
                        correo_personal.text = "Direccion de correo: " + document.getString("CorreoPersonal")
                        numero_casa.text = "Numero local: " + document.getString("NumeroCasa")
                        numero_personal.text = "Numero personal: " + document.getString("NumeroPersonal")
                        tipo_sangre.text = "Tipo de sangre: " + document.getString("TipoSangre")

                    } else {
                        Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener() { exception ->
                    Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }

            db.collection("datosMedicos").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        alergias.text = "Alergias: " + document.getString("Alergia")
                        alergia_2.text = "Alergico a: " + document.getString("EspecificaAlergias")
                        claustrofobia.text = "Claustrofobia: " + document.getString("Claustrofobia")
                        observaciones.text = "Observaciones adicionales: " + document.getString("Observaciones")

                    } else {
                        Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_SHORT).show()
        }
    }
}
