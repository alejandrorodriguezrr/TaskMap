package com.example.taskmapfinal

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmapfinal.Login.Login

class MainActivity : AppCompatActivity() {

    private var reproductor: MediaPlayer? = null

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)

        setContentView(R.layout.activity_main)

        reproducirSonidoInicio()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, Login::class.java))
            finish()
        }, 3000)
    }

    private fun reproducirSonidoInicio() {
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
