package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityDashboardBluetoothBinding

class DashboardBluetooth : AppCompatActivity() {

    //declaracion de variables para los botones y el texto de ayuda que seran inicializados mas tarde

    private lateinit var botonVolverDashboard: Button
    private lateinit var botonVolverDashboardCompartir: Button
    private lateinit var botonEnviarUid: Button
    private lateinit var textoAyudaBt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard_bluetooth)

        //vincula el diseño de la actividad con el código

        botonVolverDashboard = findViewById(R.id.boton_volver_dashboard)
        botonVolverDashboardCompartir = findViewById(R.id.boton_volver_dashboard_compartir)
        botonEnviarUid = findViewById(R.id.boton_enviar_uid)
        textoAyudaBt = findViewById(R.id.texto_ayuda_bt)

        //instruccion de escucha para el boton de volver al dashboard
        //instruccion de escucha para el boton de volver a la actividad anterior
        //instruccion de escucha para el boton de enviar ID de usuario
        //instruccion de escucha para el boton de ayuda

        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        botonVolverDashboardCompartir.setOnClickListener {
            val intent = Intent(this, CompartirDashboard::class.java)
            startActivity(intent)
        }
        botonEnviarUid.setOnClickListener {
            val intent = Intent(this, CompartirBT::class.java)
            startActivity(intent)
        }
        textoAyudaBt.setOnClickListener {
            val intent = Intent(this, AyudaBT::class.java)
            startActivity(intent)
        }



    }
}