package com.example.vitae

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarHistoriaDatosPersonales : AppCompatActivity() {

    //declaracion de variables que eran inicializadas luego

    private lateinit var binding: EditarHistoriaDatosPersonales
    private lateinit var editarNombreCompleto: EditText
    private lateinit var editarFechaNacimiento: EditText
    private lateinit var editarLugarNacimiento: EditText
    private lateinit var editarAltura: EditText
    private lateinit var editarPeso: EditText
    private lateinit var editarEdad: EditText
    private lateinit var editarCorreoPersonal: EditText
    private lateinit var editarNumeroCasa: EditText
    private lateinit var editarNumeroPersonal: EditText
    private lateinit var editarTipoSangre: EditText
    private lateinit var botonDatosPersonalesReingreso: Button
    private lateinit var botonCancelar: Button

    //declaracion de variables de firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_historia_datos_personales)

        //vinculacion de variables con los elementos de la vista

        editarNombreCompleto = findViewById(R.id.reingrese_nombre_completo)
        editarFechaNacimiento = findViewById(R.id.reingrese_fecha_nacimiento)
        editarLugarNacimiento = findViewById(R.id.reingrese_lugar_nacimiento)
        editarAltura = findViewById(R.id.reingrese_altura)
        editarPeso = findViewById(R.id.peso)
        editarEdad = findViewById(R.id.reingrese_edad)
        editarCorreoPersonal = findViewById(R.id.reingrese_correo_personal)
        editarNumeroCasa = findViewById(R.id.reingrese_numero_casa)
        editarNumeroPersonal = findViewById(R.id.reingrese_numero_personal)
        editarTipoSangre = findViewById(R.id.reingrese_tipo_sangre)
        botonDatosPersonalesReingreso = findViewById(R.id.boton_datos_personales_reingreso)
        botonCancelar = findViewById(R.id.boton_cancelar)

        //llamada a la funcion buscarDatos para obtener los datos del usuario

        buscarDatos()

        //listeners de los botones para actualizar los datos del usuario

        botonDatosPersonalesReingreso.setOnClickListener {
            actualizarDatos()
        }

        botonCancelar.setOnClickListener {
            finish()

        }

    }

    //funcion para buscar los datos del usuario en la base de datos
    //si el usuario no esta autenticado se muestra un mensaje de error
    //si el usuario esta autenticado se obtienen los datos del usuario de la base de datos mediante el id del usuario y la funcion get
    private fun buscarDatos(){
        val user = auth.currentUser?.uid
        if (user != null){
            db.collection("informacionUsuarios").document(user).get()
                .addOnSuccessListener { document ->
                    if (document != null){
                        editarNombreCompleto.setText(document.getString("NombreCompleto"))
                        editarFechaNacimiento.setText(document.getString("FechaNacimiento"))
                        editarLugarNacimiento.setText(document.getString("LugarNacimiento"))
                        //se intercambian los valores de int en la base de datos a long y luego a string para que se muestren en la vista
                        editarAltura.setText(document.getLong("Altura")?.toString())
                        editarPeso.setText(document.getLong("Peso")?.toString())
                        editarEdad.setText(document.getLong("Edad")?.toString())
                        editarCorreoPersonal.setText(document.getString("CorreoPersonal"))
                        editarNumeroCasa.setText(document.getString("NumeroCasa"))
                        editarNumeroPersonal.setText(document.getString("NumeroPersonal"))
                        editarTipoSangre.setText(document.getString("TipoSangre"))

        } else {
            Toast.makeText(this, "No se encontraron datos", Toast.LENGTH_SHORT).show()
                    }
                    }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al buscar los datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()

        }
    }

    //funcion para actualizar los datos del usuario en la base de datos
    //si el usuario no esta autenticado se muestra un mensaje de error
    //si el usuario esta autenticado se actualiza los datos del usuario en la base de datos mediante el id del usuario y la funcion set

    private fun actualizarDatos(){
        val user = auth.currentUser?.uid
        if (user != null){
            val datosActualizados = hashMapOf(
                "NombreCompleto" to editarNombreCompleto.text.toString(),
                "FechaNacimiento" to editarFechaNacimiento.text.toString(),
                "LugarNacimiento" to editarLugarNacimiento.text.toString(),
                //se intercambian los valores de string a int para que se guarden en la base de datos, revirtiendo el proceso en la funcion anterior
                "Altura" to editarAltura.text.toString().toIntOrNull(),
                "Peso" to editarPeso.text.toString().toIntOrNull(),
                "Edad" to editarEdad.text.toString().toIntOrNull(),
                "CorreoPersonal" to editarCorreoPersonal.text.toString(),
                "NumeroCasa" to editarNumeroCasa.text.toString(),
                "NumeroPersonal" to editarNumeroPersonal.text.toString(),
                "TipoSangre" to editarTipoSangre.text.toString()
            )
            db.collection("informacionUsuarios").document(user).set(datosActualizados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show()
                }

        }else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}