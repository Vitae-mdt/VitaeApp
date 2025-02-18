package com.example.vitae

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EliminarHistoria2 : AppCompatActivity() {

    //declaracion de variables que seran inicializadas luego
    private lateinit var botonEliminar: Button
    private lateinit var listaEliminar: ListView
    private lateinit var botonVolver: Button
    private var historiaList = mutableListOf<String>()
    private var nombreList = mutableListOf<String>() // Lista para nombres
    private var usuarioSeleccionado: String? = null
    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()
    private lateinit var adaptador: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eliminar_historia2)

        //vinculacion de variables con los elementos de la vista

        botonEliminar = findViewById(R.id.boton_eliminar)
        listaEliminar = findViewById(R.id.listaEliminar)
        botonVolver = findViewById(R.id.boton_volver)

        //inicializacion de variables para el adaptador de la lista
        historiaList = mutableListOf()
        adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, historiaList)
        listaEliminar.adapter = adaptador

        //llamada a la funcion buscarDatos para obtener los datos de la base de datos

        buscarDatos()

        //listeners de los botones para eliminar la historia seleccionada y volver a la vista anterior

        listaEliminar.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            usuarioSeleccionado = historiaList[position]
        }
        botonVolver.setOnClickListener {
            finish()
        }


        //listeners de los botones para eliminar la historia seleccionada y volver a la vista anterior
        //se verifica que el usuario seleccionado no sea nulo
        //se llama a la funcion esAdmin para verificar que el usuario actual tenga permisos de administrador
        //si el usuario es administrador se llama a la funcion eliminarHistoria para eliminar la historia seleccionada de la base de datos
        botonEliminar.setOnClickListener {
            usuarioSeleccionado?.let { userID ->
                esAdmin { isAdmin ->
                    if (isAdmin) {
                        eliminarHistoria(userID)
                        finish()
                    } else {
                        Toast.makeText(this, "No tienes permisos para eliminar historias.", Toast.LENGTH_SHORT).show()
                    }
                }

            } ?: Toast.makeText(this, "Seleccione una historia para eliminar", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //funcion para buscar los datos de la base de datos
    //se verifica que el usuario actual tenga permisos de administrador
    //se obtienen los datos de la base de datos mediante el id del usuario y la funcion get
    private fun buscarDatos() {
        esAdmin { isAdmin ->
            if (isAdmin) {
                db.collection("usuarios").get()
                    .addOnSuccessListener { documents ->
                        historiaList.clear()
                        nombreList.clear()
                        var usuariosProcesados = 0 // Contador para verificar cuándo se han procesado todos los usuarios

                        for (document in documents) {
                            val userId = document.id

                            // Verificar si existen datos en "datosMedicos" e "informacionUsuarios"
                            db.collection("datosMedicos").document(userId).get()
                                .addOnSuccessListener { datosMedicosDocument ->
                                    db.collection("informacionUsuarios").document(userId).get()
                                        .addOnSuccessListener { infoDocument ->
                                            usuariosProcesados++

                                            if (datosMedicosDocument.exists() && infoDocument.exists()) {
                                                // Si existen datos en ambas colecciones, obtener el nombre y añadir a la lista
                                                val nombre = infoDocument.getString("NombreCompleto") ?: "Nombre Desconocido"
                                                historiaList.add(userId)
                                                nombreList.add(nombre)
                                            }
                                            // Actualizar el adaptador solo después de procesar todos los usuarios
                                            if (usuariosProcesados == documents.size()) {
                                                adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombreList)
                                                listaEliminar.adapter = adaptador
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            usuariosProcesados++
                                            // Manejar error al obtener "informacionUsuarios"
                                            if (usuariosProcesados == documents.size()) {
                                                adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombreList)
                                                listaEliminar.adapter = adaptador
                                            }
                                            Toast.makeText(this, "Error al obtener informacionUsuarios: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    usuariosProcesados++
                                    // Manejar error al obtener "datosMedicos"
                                    if (usuariosProcesados == documents.size()) {
                                        adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombreList)
                                        listaEliminar.adapter = adaptador
                                    }
                                    Toast.makeText(this, "Error al obtener datosMedicos: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error al buscar los datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "No tienes permisos para ver las historias.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    //funcion para eliminar la historia seleccionada de la base de datos
    //se verifica que el usuario actual tenga permisos de administrador
    //se crea la varable elimina con la funcion batch, la  cual permite realizar varias operaciones en una sola transaccion con la base de datos
    //se eliminan los datos de la base de datos mediante el id del usuario y la funcion delete
    private fun eliminarHistoria(userID: String) {
        val elimina = db.batch()
        val datosMedicosEliminar = db.collection("datosMedicos").document(userID)
        elimina.delete(datosMedicosEliminar)
        val informacionPersonalEliminar = db.collection("informacionUsuarios").document(userID)
        elimina.delete(informacionPersonalEliminar)

        elimina.commit().addOnSuccessListener {
            Toast.makeText(this, "Historia eliminada correctamente", Toast.LENGTH_SHORT).show()
            historiaList.remove(userID)
            adaptador.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al eliminar la historia: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    //funcion para verificar que el usuario actual tenga permisos de administrador
    //se verifica que el usuario actual tenga permisos de administrador
    //se obtienen los datos de la base de datos mediante el id del usuario y la funcion get
    private fun esAdmin(callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            val chequeaUsuarios = db.collection("usuarios").document(uid)

            chequeaUsuarios.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    callback(role == "admin")
                } else {
                    callback(false)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error al verificar rol: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        } else {
            callback(false)
        }
    }
}