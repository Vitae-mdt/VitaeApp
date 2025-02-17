package com.example.vitae

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityLogBinding
import com.google.firebase.auth.FirebaseAuth

//declara la clase Log que hereda de AppCompatActivity
class Log : AppCompatActivity() {

    //declara las variables binding y auth que seran inicializadas mas tarde
    private lateinit var binding: ActivityLogBinding
    private lateinit var auth: FirebaseAuth

//inicializa la vista y los listeners de los botones y crea un objeto de autenticacion de firebase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        //ejecuta el intento de inicio de sesion cuando se hace clic en el boton y valida los campos de correo y contraseña,
        //si son validos se inicia sesion con el metodo signInWithEmailAndPassword de firebase auth,
        //inicializa las variables email y password que seran utilizadas mas tarde
        //ejecuta notificacion en el caso de que el inicio de sesion sea no exitoso y si los campos se encuentran en blanco

        binding.botonIniciosesion.setOnClickListener {
            val email = binding.iniciosesionMail.text.toString()
            val password = binding.iniciosesionPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, Dahsboard::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        //ejecuta el flotante que valida las credenciales para el cambio de contraseña y muestra un dialogo de alerta
        //se inicializan las variables mail y view que seran utilizadas mas tarde
        //se crea el metodo de escucha de los botones "Enviar" y "Cancelar" para cerrar el dialogo de alerta
        //se crea el dialogo de alerta y se muestra en pantalla
        //si el dialogo es diferente a un campo en blanco se ejecuta el metodo comparaMail que valida el correo electronico
        binding.olvideContraseA.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.olv_contra, null)
            val mail = view.findViewById<EditText>(R.id.editablerecuperacion)
            builder.setView(view)
            val dialog = builder.create()
            view.findViewById<Button>(R.id.boton_enviar).setOnClickListener {
                comparaMail(mail)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.boton_cancelar).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

            }
            dialog.show()


        }
        //se crea el metodo de escucha para el boton de registrar y se envia a la pestana de registro
        binding.RedirigirReg.setOnClickListener {
            val intent = Intent(this, Reg::class.java)
            startActivity(intent)
        }

    }

    //se crea la funcion comparaMail que valida el correo electronico y si es valido se envia un correo electronico de recuperacion
    private fun comparaMail(mail: EditText) {
        if (mail.text.toString().isEmpty()) {
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail.text.toString()).matches()) {
            return
        }
        auth.sendPasswordResetEmail(mail.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Se ha enviado un correo electrónico", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}