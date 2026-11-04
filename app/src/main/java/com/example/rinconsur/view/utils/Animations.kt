package com.example.rinconsur.view.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private const val DURACION = 350

// Al avanzar: la pantalla nueva entra desde la derecha
val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = tween(DURACION)) { it } + fadeIn(tween(DURACION))
}

// Al avanzar: la pantalla anterior sale un poco hacia la izquierda
val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = tween(DURACION)) { -it / 3 } + fadeOut(tween(DURACION))
}

// Al volver: la pantalla anterior reaparece desde la izquierda
val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = tween(DURACION)) { -it / 3 } + fadeIn(tween(DURACION))
}

// Al volver: la pantalla actual sale hacia la derecha
val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = tween(DURACION)) { it } + fadeOut(tween(DURACION))
}