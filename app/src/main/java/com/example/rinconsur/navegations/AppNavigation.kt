package com.example.rinconsur.navegations

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rinconsur.repository.model.esOperador
import com.example.rinconsur.repository.model.esSupervisor
import com.example.rinconsur.view.ui.DashboardScreen
import com.example.rinconsur.view.ui.HomeScreen
import com.example.rinconsur.view.ui.LoginScreen
import com.example.rinconsur.view.ui.RegistroFormScreen
import com.example.rinconsur.view.ui.RegistroListScreen
import com.example.rinconsur.view.ui.TipoUsuarioScreen
import com.example.rinconsur.view.ui.TurnoScreen
import com.example.rinconsur.view.ui.UsuarioFormScreen
import com.example.rinconsur.view.ui.UsuarioListScreen
import com.example.rinconsur.view.utils.navEnterTransition
import com.example.rinconsur.view.utils.navExitTransition
import com.example.rinconsur.view.utils.navPopEnterTransition
import com.example.rinconsur.view.utils.navPopExitTransition
import com.example.rinconsur.viewmodels.DashboardViewModel
import com.example.rinconsur.viewmodels.LoginViewModel
import com.example.rinconsur.viewmodels.RegistroViewModel
import com.example.rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.rinconsur.viewmodels.TurnoViewModel
import com.example.rinconsur.viewmodels.UsuarioViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val TURNO = "turno"
    const val REGISTROS = "registros"
    const val REGISTRO_FORM = "registro_form?id={id}"
    const val DASHBOARD = "dashboard"
    const val USUARIOS = "usuarios"
    const val USUARIO_FORM = "usuario_form?id={id}"
    const val TIPOS = "tipos_usuario"

    fun registroForm(id: Long = 0) = "registro_form?id=$id"
    fun usuarioForm(id: Long = 0) = "usuario_form?id=$id"
}

data class MenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val soloSupervisor: Boolean = false,
    val soloOperador: Boolean = false
)

/** Opciones del menú lateral. Cada una puede limitarse a un tipo de usuario. */
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.TURNO, "Turno", Icons.Default.AccessTime, soloOperador = true),
    MenuItem(Routes.REGISTROS, "Salidas", Icons.AutoMirrored.Filled.ExitToApp, soloOperador = true),
    MenuItem(Routes.DASHBOARD, "Métricas", Icons.Default.BarChart, soloSupervisor = true),
    MenuItem(Routes.USUARIOS, "Usuarios", Icons.Default.People, soloSupervisor = true),
    MenuItem(Routes.TIPOS, "Tipos de usuario", Icons.Default.Badge, soloSupervisor = true)
)

private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.TURNO -> "Turno"
    Routes.REGISTROS -> "Salidas"
    Routes.REGISTRO_FORM -> "Registro de salida"
    Routes.DASHBOARD -> "Métricas"
    Routes.USUARIOS -> "Usuarios"
    Routes.USUARIO_FORM -> "Usuario"
    Routes.TIPOS -> "Tipos de usuario"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    turnoViewModel: TurnoViewModel,
    registroViewModel: RegistroViewModel,
    usuarioViewModel: UsuarioViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val usuario = loginState.usuarioLogueado
    val turnoActivo by turnoViewModel.turnoActivo.collectAsStateWithLifecycle()

    // Informa quién inició sesión al ViewModel de turnos y limpia su formulario
    LaunchedEffect(usuario?.id) {
        turnoViewModel.setUsuario(usuario?.id)
        turnoViewModel.nuevo()
    }

    // Informa el turno activo al ViewModel de registros
    LaunchedEffect(turnoActivo?.id) {
        registroViewModel.setTurno(turnoActivo?.id)
    }

    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val esSupervisor = usuario?.esSupervisor == true
    val esOperador = usuario?.esOperador == true

    // El menú solo muestra lo que corresponde al tipo de usuario
    val itemsVisibles = menuItems.filter { item ->
        usuario != null &&
                (!item.soloSupervisor || esSupervisor) &&
                (!item.soloOperador || esOperador)
    }
    val isTopLevel = itemsVisibles.any { it.route == currentRoute }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLogin,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = usuario?.nombre ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = usuario?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                itemsVisibles.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTopLevel(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        loginViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isLogin) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                turnoViewModel = turnoViewModel,
                registroViewModel = registroViewModel,
                usuarioViewModel = usuarioViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                dashboardViewModel = dashboardViewModel,
                usuarioActualId = usuario?.id,
                nombreUsuario = usuario?.nombre ?: "",
                tipoUsuario = when {
                    usuario == null -> ""
                    esSupervisor -> "Supervisor"
                    esOperador -> "Operador"
                    else -> "Usuario"
                },
                esSupervisor = esSupervisor,
                esOperador = esOperador,
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    turnoViewModel: TurnoViewModel,
    registroViewModel: RegistroViewModel,
    usuarioViewModel: UsuarioViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    dashboardViewModel: DashboardViewModel,
    usuarioActualId: Long?,
    nombreUsuario: String,
    tipoUsuario: String,
    esSupervisor: Boolean,
    esOperador: Boolean,
    onNavigateTopLevel: (String) -> Unit
) {
    val idArg = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })

    // Animación: deslizamiento horizontal entre pantallas (view/utils/Animations.kt)
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Routes.HOME) {
            HomeScreen(nombre = nombreUsuario, tipo = tipoUsuario)
        }

        // Pantallas del operador
        composable(Routes.TURNO) {
            Protegida(permitido = esOperador) {
                TurnoScreen(turnoViewModel)
            }
        }
        composable(Routes.REGISTROS) {
            Protegida(permitido = esOperador) {
                RegistroListScreen(
                    registroViewModel = registroViewModel,
                    turnoViewModel = turnoViewModel,
                    onAgregar = { navController.navigate(Routes.registroForm()) },
                    onEditar = { id -> navController.navigate(Routes.registroForm(id)) },
                    onIrATurno = { onNavigateTopLevel(Routes.TURNO) }
                )
            }
        }
        composable(Routes.REGISTRO_FORM, arguments = idArg) { entry ->
            Protegida(permitido = esOperador) {
                RegistroFormScreen(
                    registroViewModel = registroViewModel,
                    turnoViewModel = turnoViewModel,
                    registroId = entry.arguments?.getLong("id") ?: 0L,
                    onDone = { navController.popBackStack() }
                )
            }
        }

        // Pantallas del supervisor
        composable(Routes.DASHBOARD) {
            Protegida(permitido = esSupervisor) {
                DashboardScreen(dashboardViewModel)
            }
        }
        composable(Routes.USUARIOS) {
            Protegida(permitido = esSupervisor) {
                UsuarioListScreen(
                    viewModel = usuarioViewModel,
                    usuarioActualId = usuarioActualId,
                    onAgregar = { navController.navigate(Routes.usuarioForm()) },
                    onEditar = { id -> navController.navigate(Routes.usuarioForm(id)) }
                )
            }
        }
        composable(Routes.USUARIO_FORM, arguments = idArg) { entry ->
            Protegida(permitido = esSupervisor) {
                UsuarioFormScreen(
                    viewModel = usuarioViewModel,
                    tipoUsuarioViewModel = tipoUsuarioViewModel,
                    usuarioId = entry.arguments?.getLong("id") ?: 0L,
                    onDone = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.TIPOS) {
            Protegida(permitido = esSupervisor) {
                TipoUsuarioScreen(tipoUsuarioViewModel)
            }
        }
    }
}

/** Muestra la pantalla solo si el tipo de usuario tiene permiso. */
@Composable
private fun Protegida(permitido: Boolean, contenido: @Composable () -> Unit) {
    if (permitido) contenido() else SinPermiso()
}

@Composable
private fun SinPermiso() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No tienes permiso para ver esta pantalla",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}