package com.bintianqi.owndroid.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

class Animations{
    private val initialOffsetValue = 96
    private val targetOffsetValue = 96

    private val bezier = CubicBezierEasing(0.4f, 0f, 0f, 1f)

    private val tween: FiniteAnimationSpec<IntOffset> = tween(400, easing = bezier)
    
    val navHostEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(83, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {initialOffsetValue}
        )
    }
    
    val navHostExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(83, easing = LinearEasing)) +
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {-targetOffsetValue}
        )
    }
    
    val navHostPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(83, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {-initialOffsetValue}
        )
    }
    
    val navHostPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(83, easing = LinearEasing)) +
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {targetOffsetValue}
        )
    }
    
}
