package com.example.vitae
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vitae.databinding.ActivityRegBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class Reg : AppCompatActivity() {

    //declara las variables binding, auth y db que seran inicializadas mas tarde
    private lateinit var binding: ActivityRegBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // escucha el evento de clic en el botón de registro y redirige a la actividad de inicio de sesión
        binding.RedirigirLog.setOnClickListener {
            val EntrarLog = Intent(this, Log::class.java)
            startActivity(EntrarLog)
        }

        // escucha el evento de clic en el botón de registro y valida los campos de correo y contraseña
        // si son validos se crea un nuevo usuario con el metodo createUserWithEmailAndPassword de firebase auth
        // si el usuario es creado con exito se asigna un rol de administrador o de usuario regular
        binding.botonRegistro.setOnClickListener {
            val email = binding.registroMail.text.toString()
            val password = binding.registroPassword.text.toString()
            val confirmacion = binding.registroConfirmacion.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmacion.isNotEmpty()) {
                if (password == confirmacion) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                chequeaUser (it.uid, email)
                            }
                            val intent = Intent(this, Log::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //se crea la funcion chequeaUser que valida si hay usuarios en la coleccion de usuarios de la base de datos
    private fun chequeaUser(userId: String, email: String) {
        // referencia a la colección de usuarios
        val usersRef = db.collection("usuarios")

        // revisa si hay usuarios en la colección
        usersRef.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // si es el primer usuario, asignar rol de administrador
                val datosUsuario = hashMapOf(
                    "role" to "admin" // asigna el rol de administrador
                )
                usersRef.document(userId).set(datosUsuario, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Admin creado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error creando admin:  ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // no es el primer usuario, asignar rol de usuario regular
                val datosUsuario = hashMapOf(
                    "role" to "user" // asigna el rol de usuario regular
                )
                usersRef.document(userId).set(datosUsuario, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error creando usuario", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e -> // si hay un error al revisar los usuarios
            Toast.makeText(this, "Error revisando usuarios", Toast.LENGTH_SHORT).show()
        }
    }
}