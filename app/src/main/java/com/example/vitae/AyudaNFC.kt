package com.example.vitae

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityAyudaNfcBinding

class AyudaNFC : AppCompatActivity() {

    //declara la variable binding que sera inicializada mas tarde

    private lateinit var binding: ActivityAyudaNfcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ayuda_nfc)

        //inicializa la variable binding

        binding = ActivityAyudaNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //se crea el metodo de escucha para el boton de volver y se envia a la pestana de dashboard
        //se crea el metodo de escucha para el boton de volver a la pantalla anterior y se envia a la pestana anterior

        binding.botonVolverDashboardNfc.setOnClickListener {
            val intent = Intent(this, DashboardNFC::class.java)
            startActivity(intent)
        }
        binding.botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
    }
}