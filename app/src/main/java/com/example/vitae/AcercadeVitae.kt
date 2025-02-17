package com.example.vitae

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AcercadeVitae : AppCompatActivity() {

    private lateinit var binding: AcercadeVitae
    private lateinit var botonAcercaDe: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acercade_vitae)


        botonAcercaDe = findViewById(R.id.boton_acerca_de)
        botonAcercaDe.setOnClickListener {
            finish()
        }

    }
}