package com.example.taskmapfinal

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var reproductor: MediaPlayer? = null

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { vista, insets ->
            val barrasSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom)
            insets
        }

        reproducirSonidoInicio()

        Handler(Looper.getMainLooper()).postDelayed({
            val intentLogin = Intent(this, Login::class.java)
            startActivity(intentLogin)
            finish()
        }, 3000)
    }

    private fun reproducirSonidoInicio() {
        // El archivo debe estar en: res/raw/bienvenida.mp3
        reproductor = MediaPlayer.create(this, R.raw.bienvenida)
        reproductor?.setOnCompletionListener {
            it.release()
            reproductor = null
        }
        reproductor?.start()
    }

    override fun onDestroy() {
        reproductor?.release()
        reproductor = null
        super.onDestroy()
    }
}
