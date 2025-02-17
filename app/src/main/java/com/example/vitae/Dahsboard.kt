package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityDahsboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class Dahsboard : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //vincula el diseño de la actividad con el código
        val binding = ActivityDahsboardBinding.inflate(layoutInflater)
        val db = FirebaseFirestore.getInstance()


        //establece la vista de la actividad con el diseño vinculado y escucha el evento de clic en el botón de cerrar sesión
        //y ejecuta la acción que lleva a la actividad de inicio de sesión
        setContentView(binding.root)
        binding.cerrarSesion.setOnClickListener {
            eliminarCache()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Log::class.java)
            startActivity(intent)
            Toast.makeText(this, "Sesión cerrada con exito", Toast.LENGTH_SHORT).show()
        }

        //escucha el evento de clic en el botón y ejecuta la acción que lleva a la actividad CrearHistorias
        binding.botonCrearHistoria.setOnClickListener {
            validarHistoriaCreada()
        }

        //valida el rol del usuario y muestra el botón de eliminar historia si es un administrador
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            binding.eliminarHistoria.visibility = View.VISIBLE
                        } else {
                            binding.eliminarHistoria.visibility = View.GONE
                        }
                    } else {
                        // Document does not exist, hide the button
                        binding.eliminarHistoria.visibility = View.GONE
                    }
                }

        }
        binding.verHistoria.setOnClickListener {
            verHistorias()
        }
        binding.editarHistoria.setOnClickListener {
            editarHistoria()
        }
        binding.eliminarHistoria.setOnClickListener {
            val intent = Intent(this, EliminarHistoria2::class.java)
            startActivity(intent)
        }
        binding.acercaVitae.setOnClickListener {
            val intent = Intent(this, AcercadeVitae::class.java)
            startActivity(intent)
        }
        binding.compartirHistoria.setOnClickListener {
            val intent = Intent(this, CompartirDashboard::class.java)
            startActivity(intent)
        }



    }

    //se crea la funcion verHistorias que recibe los datos del usuario de la base de datos de firebase
    //validando que el usuario exista en la base de datos y sea valido
    //si el usuario no existe, genera una excepcion
    //si el usuario existe ejecuta la función verHistorias() que lleva a la actividad verHistoria
    private fun verHistorias() {
        val user = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        if (user != null) {
            db.collection("informacionUsuarios").document(user).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        db.collection("datosMedicos").document(user).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val intent = Intent(this, verHistoria::class.java)
                                    startActivity(intent)
                                    Toast.makeText(this, "Historia mostrada", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(this, "No hay historia registrada", Toast.LENGTH_SHORT
                                    )
                                }


                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al obtener la historia", Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    } else {
                        Toast.makeText(this, "No hay historia registrada", Toast.LENGTH_SHORT)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener la historia", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay historia registrada", Toast.LENGTH_SHORT).show()
        }
    }

    //elimina la caché de la aplicación
    private fun eliminarCache() {
        val cacheDir = cacheDir
        if (cacheDir.isDirectory) {
            borrarDir(cacheDir)
        }
    }

    //borra el contenido de un directorio, en este caso para la cache de la aplicación
    private fun borrarDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    borrarDir(File(dir, child))
                }
            }
        }
        return dir.delete()
    }

    //se crea la funcion validarHistoriaCreada que valida si el usuario ya ha creado una historia
    //si el usuario no ha creado una historia, ejecuta la acción que lleva a la actividad CrearHistorias
    private fun validarHistoriaCreada() {

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("informacionUsuarios").document(user).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Toast.makeText(this, "Historia creada previamente", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, CrearHistorias::class.java)
                    startActivity(intent)

                }
            }
    }

    private fun editarHistoria() {

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("informacionUsuarios").document(user).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val intent = Intent(this, EditarDashboard::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Elegir Datos a Editar", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No hay historia registrada", Toast.LENGTH_SHORT).show()
                }

            }
    }
}


