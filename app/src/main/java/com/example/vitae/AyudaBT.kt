package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityAyudaBtBinding

class AyudaBT : AppCompatActivity() {

    //declara las variables botonVolverBt y botonVolverDashboard que seran inicializadas mas tarde

    private lateinit var botonVolverBt: Button
    private lateinit var botonVolverDashboard: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ayuda_bt)
        val binding = ActivityAyudaBtBinding.inflate(layoutInflater)

        //inicializa las variables botonVolverBt y botonVolverDashboard
        //se crea el metodo de escucha para el boton de volver y se envia a la pestana de dashboard
        //se crea el metodo de escucha para el boton de volver a la pantalla anterior y se envia a la pestana anterior

        botonVolverBt = findViewById(R.id.boton_volver_bt)
        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)

        botonVolverBt.setOnClickListener {
            finish()
        }
        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
    }
}