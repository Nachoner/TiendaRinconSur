package com.example.rinconsur

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.rinconsur.navegations.AppNavigation
import com.example.rinconsur.view.theme.RinconSurTheme
import com.example.rinconsur.viewmodels.DashboardViewModel
import com.example.rinconsur.viewmodels.LoginViewModel
import com.example.rinconsur.viewmodels.RegistroViewModel
import com.example.rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.rinconsur.viewmodels.TurnoViewModel
import com.example.rinconsur.viewmodels.UsuarioViewModel

class MainActivity : ComponentActivity() {

    // ViewModels con alcance de la Activity
    private val loginViewModel: LoginViewModel by viewModels()
    private val turnoViewModel: TurnoViewModel by viewModels()
    private val registroViewModel: RegistroViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()
    private val tipoUsuarioViewModel: TipoUsuarioViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tema claro fijo: los íconos de las barras del sistema siempre oscuros
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            RinconSurTheme {
                AppNavigation(
                    loginViewModel,
                    turnoViewModel,
                    registroViewModel,
                    usuarioViewModel,
                    tipoUsuarioViewModel,
                    dashboardViewModel
                )
            }
        }
    }
}