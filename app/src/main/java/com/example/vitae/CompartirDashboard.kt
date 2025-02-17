package com.example.vitae

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityCompartirDashboardBinding


class CompartirDashboard : AppCompatActivity() {
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

        val binding = ActivityCompartirDashboardBinding.inflate(layoutInflater)

        botonVolverDashboard.setOnClickListener {
            val intent = Intent(this, Dahsboard::class.java)
            startActivity(intent)
        }
        botonCompartirNFC.setOnClickListener {
            val intent = Intent(this, DashboardNFC::class.java)
            startActivity(intent)
        }
        botonCompartirBt.setOnClickListener {
            val intent = Intent(this, DashboardBluetooth::class.java)
            startActivity(intent)
        }

    }
}