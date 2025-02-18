package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityEditarDashboardBinding

class EditarDashboard : AppCompatActivity() {

    //declaracion de variables para los botones y el texto de ayuda que seran inicializados mas tarde

    private lateinit var binding : ActivityEditarDashboardBinding
    private lateinit var botonEditarDatosPersonales : Button
    private lateinit var botonEditarDatosMedicos : Button
    private lateinit var botonVolverDashboard : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_dashboard)

        //vincula el diseño de la actividad con el código

        botonEditarDatosPersonales = findViewById(R.id.boton_editar_datos_personales)
        botonEditarDatosMedicos = findViewById(R.id.boton_editar_datos_medicos)
        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)

        //instruccion de escucha para el boton de editar datos personales
        //instruccion de escucha para el boton de editar datos medicos
        //instruccion de escucha para el boton de volver al dashboard

        binding = ActivityEditarDashboardBinding.inflate(layoutInflater)
        botonEditarDatosMedicos.setOnClickListener {
            editarDatosMedicos()
        }
        botonEditarDatosPersonales.setOnClickListener {
            editarDatosPerosnales()
        }
        botonVolverDashboard.setOnClickListener {
            volverDashboard()
        }
    }

    //funciones que llevan a las actividades correspondientes
    //al presionar los botones

    private fun editarDatosPerosnales (){
        val intent = Intent(this, EditarHistoriaDatosPersonales::class.java)
        startActivity(intent)

    }
    private fun editarDatosMedicos () {
        val intent = Intent(this, EditarHistoriaDatosMedicos::class.java)
        startActivity(intent)
    }
    private fun volverDashboard () {
        val intent = Intent(this, Dahsboard::class.java)
        startActivity(intent)
    }

}