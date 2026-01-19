package com.example.taskmapfinal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Login : AppCompatActivity() {

    private lateinit var tilCorreo: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText

    private lateinit var btnEntrar: MaterialButton
    private lateinit var btnGoogle: MaterialButton

    private lateinit var tvOlvido: TextView
    private lateinit var tvCrear: TextView

    override fun onCreate(estadoInstancia: Bundle?) {
        super.onCreate(estadoInstancia)
        setContentView(R.layout.login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        iniciarVistas()
        iniciarListeners()
    }

    private fun iniciarVistas() {
        tilCorreo = findViewById(R.id.tilEmail)
        tilContrasena = findViewById(R.id.tilPassword)
        etCorreo = findViewById(R.id.etEmail)
        etContrasena = findViewById(R.id.etPassword)

        btnEntrar = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)

        tvOlvido = findViewById(R.id.tvForgot)
        tvCrear = findViewById(R.id.tvCreate)
    }

    private fun iniciarListeners() {
        btnEntrar.setOnClickListener {
            val correo = etCorreo.text?.toString()?.trim().orEmpty()
            val contrasena = etContrasena.text?.toString()?.trim().orEmpty()

            if (!validarFormulario(correo, contrasena)) return@setOnClickListener

            Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvOlvido.setOnClickListener {
            Toast.makeText(this, "Recuperar contraseña pendiente", Toast.LENGTH_SHORT).show()
        }

        tvCrear.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google Sign-In pendiente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarFormulario(correo: String, contrasena: String): Boolean {
        var correcto = true

        if (correo.isEmpty()) {
            tilCorreo.error = "Introduce el email"
            correcto = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.error = "Email no válido"
            correcto = false
        } else {
            tilCorreo.error = null
        }

        if (contrasena.isEmpty()) {
            tilContrasena.error = "Introduce la contraseña"
            correcto = false
        } else if (contrasena.length < 6) {
            tilContrasena.error = "Mínimo 6 caracteres"
            correcto = false
        } else {
            tilContrasena.error = null
        }

        return correcto
    }
}
