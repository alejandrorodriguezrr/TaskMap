package com.example.taskmapfinal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmapfinal.api.ClienteApi
import com.example.taskmapfinal.api.PeticionRegistro
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.IOException

class Registro : AppCompatActivity() {

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilEmailRegistro: TextInputLayout
    private lateinit var tilPasswordRegistro: TextInputLayout
    private lateinit var tilPasswordRepetir: TextInputLayout

    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmailRegistro: TextInputEditText
    private lateinit var etPasswordRegistro: TextInputEditText
    private lateinit var etPasswordRepetir: TextInputEditText

    private lateinit var btnRegistrar: MaterialButton
    private lateinit var tvVolverLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro)

        enlazarVistas()
        configurarEventos()
    }

    private fun enlazarVistas() {
        tilNombre = findViewById(R.id.tilNombre)
        tilEmailRegistro = findViewById(R.id.tilEmailRegistro)
        tilPasswordRegistro = findViewById(R.id.tilPasswordRegistro)
        tilPasswordRepetir = findViewById(R.id.tilPasswordRepetir)

        etNombre = findViewById(R.id.etNombre)
        etEmailRegistro = findViewById(R.id.etEmailRegistro)
        etPasswordRegistro = findViewById(R.id.etPasswordRegistro)
        etPasswordRepetir = findViewById(R.id.etPasswordRepetir)

        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)
    }

    private fun configurarEventos() {
        tvVolverLogin.setOnClickListener { irAlLogin() }
        btnRegistrar.setOnClickListener { registrarUsuario() }
    }

    private fun registrarUsuario() {
        limpiarErrores()

        val nombre = etNombre.text?.toString()?.trim().orEmpty()
        val correo = etEmailRegistro.text?.toString()?.trim().orEmpty()
        val contrasena = etPasswordRegistro.text?.toString().orEmpty()
        val contrasenaRepetida = etPasswordRepetir.text?.toString().orEmpty()

        if (!validarFormulario(nombre, correo, contrasena, contrasenaRepetida)) return

        btnRegistrar.isEnabled = false

        lifecycleScope.launch {
            try {
                val respuestaHttp = ClienteApi.api.registrar(
                    PeticionRegistro(nombre, correo, contrasena)
                )

                if (respuestaHttp.isSuccessful) {
                    val cuerpo = respuestaHttp.body()

                    if (cuerpo != null && cuerpo.ok) {
                        Toast.makeText(this@Registro, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                        irAlLogin()
                    } else {
                        btnRegistrar.isEnabled = true
                        val mensaje = cuerpo?.error ?: "No se pudo registrar"
                        if (mensaje.contains("correo", ignoreCase = true) || mensaje.contains("existe", ignoreCase = true)) {
                            tilEmailRegistro.error = mensaje
                        } else {
                            Toast.makeText(this@Registro, mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    btnRegistrar.isEnabled = true

                    val mensaje = when (respuestaHttp.code()) {
                        409 -> "Ese correo ya existe"
                        422 -> "Datos inválidos"
                        400 -> "Solicitud incorrecta"
                        404 -> "No se encuentra el endpoint en el servidor"
                        500 -> "Error interno del servidor"
                        else -> "Error HTTP: ${respuestaHttp.code()}"
                    }

                    if (respuestaHttp.code() == 409) {
                        tilEmailRegistro.error = mensaje
                    } else {
                        Toast.makeText(this@Registro, mensaje, Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: IOException) {
                btnRegistrar.isEnabled = true
                Toast.makeText(this@Registro, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                btnRegistrar.isEnabled = true
                Toast.makeText(this@Registro, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarFormulario(
        nombre: String,
        correo: String,
        contrasena: String,
        contrasenaRepetida: String
    ): Boolean {
        var ok = true

        if (nombre.isBlank()) {
            tilNombre.error = "El nombre es obligatorio"
            ok = false
        }

        if (correo.isBlank()) {
            tilEmailRegistro.error = "El email es obligatorio"
            ok = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilEmailRegistro.error = "Email no válido"
            ok = false
        }

        if (contrasena.isBlank()) {
            tilPasswordRegistro.error = "La contraseña es obligatoria"
            ok = false
        } else if (contrasena.length < 6) {
            tilPasswordRegistro.error = "Mínimo 6 caracteres"
            ok = false
        }

        if (contrasenaRepetida.isBlank()) {
            tilPasswordRepetir.error = "Repite la contraseña"
            ok = false
        } else if (contrasena != contrasenaRepetida) {
            tilPasswordRepetir.error = "Las contraseñas no coinciden"
            ok = false
        }

        return ok
    }

    private fun limpiarErrores() {
        tilNombre.error = null
        tilEmailRegistro.error = null
        tilPasswordRegistro.error = null
        tilPasswordRepetir.error = null
    }

    private fun irAlLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }
}
