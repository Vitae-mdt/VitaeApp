package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityCompartirDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CompartirDashboard : AppCompatActivity() {

    //declara las variables botonVolverDashboard, botonCompartirNFC y botonCompartirBt que seran inicializadas mas tarde

    private lateinit var botonVolverDashboard: Button
    private lateinit var botonCompartirNFC: Button
    private lateinit var botonCompartirBt: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_compartir_dashboard)
        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)
        botonCompartirNFC = findViewById(R.id.boton_compartir_NFC)
        botonCompartirBt = findViewById(R.id.boton_compartir_bt)

        //inicializa las variables botonVolverDashboard, botonCompartirNFC y botonCompartirBt y binding

        val binding = ActivityCompartirDashboardBinding.inflate(layoutInflater)

        //se crea el metodo de escucha para el boton de volver y se envia a la pestana de dashboard
        //se crea el metodo de escucha para el boton de compartir NFC y se envia a la pestana de NFC
        //se crea el metodo de escucha para el boton de compartir Bluetooth y se envia a la pestana de Bluetooth

        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        botonCompartirNFC.setOnClickListener {
            var auth = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            var db = FirebaseFirestore.getInstance()
            db.collection("informacionUsuarios").document(auth).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("datosMedicos").document(auth).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val intent = Intent(this, DashboardNFC::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "No se encontraron datos registrados para compartir", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    Toast.makeText(this, "No se encontraron datos registrados para compartir", Toast.LENGTH_SHORT).show()
                }
            }

        }
        botonCompartirBt.setOnClickListener {
            var auth = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            var db = FirebaseFirestore.getInstance()
            db.collection("informacionUsuarios").document(auth).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("datosMedicos").document(auth).get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val intent = Intent(this, DashboardBluetooth::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "No se encontraron datos registrados para compartir", Toast.LENGTH_SHORT).show()
                            }
                       }
                    }
                else {
                    Toast.makeText(this, "No se encontraron datos registrados para compartir", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}