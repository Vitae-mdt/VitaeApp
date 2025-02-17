package com.example.vitae

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitae.databinding.ActivityAyudaNfcBinding

class AyudaNFC : AppCompatActivity() {

    private lateinit var binding: ActivityAyudaNfcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ayuda_nfc)

        binding = ActivityAyudaNfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

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