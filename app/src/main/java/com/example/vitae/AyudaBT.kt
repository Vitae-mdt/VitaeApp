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

    private lateinit var botonVolverBt: Button
    private lateinit var botonVolverDashboard: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ayuda_bt)
        val binding = ActivityAyudaBtBinding.inflate(layoutInflater)

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