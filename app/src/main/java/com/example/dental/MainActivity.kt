package com.example.dental

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dental.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent { DentalTheme { AppNav() } }
    }
}

private val topRoutes = listOf("home", "appointments", "patients", "doctors", "services")

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()
    val idArg = listOf(navArgument("id") { type = NavType.IntType })
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    Scaffold(
        containerColor = BgColor, contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (route != null && route in topRoutes) BottomBar(route) { r ->
                nav.navigate(r) { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true }
            }
        }
    ) { pad ->
        NavHost(
            nav, startDestination = "welcome", modifier = Modifier.padding(pad),
            enterTransition = { fadeIn(tween(220)) }, exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) }, popExitTransition = { fadeOut(tween(160)) }
        ) {
            composable("welcome") {
                WelcomeScreen { nav.navigate("home") { popUpTo("welcome") { inclusive = true } } }
            }
            composable("home") { HomeScreen(vm, nav) }
            composable("appointments") { AppointmentsScreen(vm, nav) }
            composable("appointment_edit/{id}", idArg) { AppointmentFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
            composable("patients") { PatientsScreen(vm, nav) }
            composable("patient_edit/{id}", idArg) { PatientFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
            composable("doctors") { DoctorsScreen(vm, nav) }
            composable("doctor_edit/{id}", idArg) { DoctorFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
            composable("services") { ServicesScreen(vm, nav) }
            composable("service_edit/{id}", idArg) { ServiceFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
            composable("positions") { PositionsScreen(vm, nav) }
            composable("position_edit/{id}", idArg) { PositionFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
            composable("records") { RecordsScreen(vm, nav) }
            composable("record_edit/{id}", idArg) { RecordFormScreen(vm, nav, it.arguments?.getInt("id") ?: 0) }
        }
    }
}
