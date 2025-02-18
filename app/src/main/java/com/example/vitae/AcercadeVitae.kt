package com.example.vitae

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AcercadeVitae : AppCompatActivity() {

    //declara las variables binding y botonAcercaDe que seran inicializadas mas tarde

    private lateinit var binding: AcercadeVitae
    private lateinit var botonAcercaDe: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_acercade_vitae)

        //inicializa la variable binding y botonAcercaDe
        //se crea el metodo de escucha para el boton de acerca de y se envia a la pestana de acerca de

        botonAcercaDe = findViewById(R.id.boton_acerca_de)
        botonAcercaDe.setOnClickListener {
            finish()
        }

    }
}