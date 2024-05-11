package com.bintianqi.owndroid.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

object Animations{
    private const val INITIAL_OFFSET_VALUE = 96
    private const val TARGET_OFFSET_VALUE = 96

    private val bezier = CubicBezierEasing(0.4f, 0f, 0f, 1f)

    private val tween: FiniteAnimationSpec<IntOffset> = tween(450, easing = bezier)
    
    val navHostEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(150, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {INITIAL_OFFSET_VALUE}
        )
    }
    
    val navHostExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(83, easing = LinearEasing)) +
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {-TARGET_OFFSET_VALUE}
        )
    }
    
    val navHostPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(83, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {-INITIAL_OFFSET_VALUE}
        )
    }
    
    val navHostPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(83, easing = LinearEasing)) +
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {TARGET_OFFSET_VALUE}
        )
    }
    
}
