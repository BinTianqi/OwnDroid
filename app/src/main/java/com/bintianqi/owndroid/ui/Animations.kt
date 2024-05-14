package com.bintianqi.owndroid.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

object Animations{
    private const val INITIAL_OFFSET_VALUE = 96
    private const val TARGET_OFFSET_VALUE = 96

    private val bezier = CubicBezierEasing(0.20f, 0.85f, 0.0f, 1f)

    private val tween: FiniteAnimationSpec<IntOffset> = tween(durationMillis = 650, easing = bezier, delayMillis = 50)
    
    val navHostEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(100, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {INITIAL_OFFSET_VALUE}
        )
    }
    
    val navHostExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(100, easing = LinearEasing)) +
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {-TARGET_OFFSET_VALUE}
        )
    }
    
    val navHostPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(100, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {-INITIAL_OFFSET_VALUE}
        )
    }
    
    val navHostPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(100, easing = LinearEasing)) +
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {TARGET_OFFSET_VALUE}
        )
    }
    
}
