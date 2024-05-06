package com.bintianqi.owndroid.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry

class Animations{
    val animateListSize:FiniteAnimationSpec<IntSize> = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntSize.VisibilityThreshold)

    private val initialOffsetValue = 96
    private val targetOffsetValue = 96

    private val bezier = CubicBezierEasing(0.3f, 0f, 0f, 1f)

    private val tween: FiniteAnimationSpec<IntOffset> = tween(450, easing = bezier)
    
    val navHostEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(83, easing = LinearEasing)) +
        slideIntoContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {initialOffsetValue}
        )
    }
    
    val navHostExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(
            animationSpec = tween,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {-targetOffsetValue}
        )
    }
    
    val navHostPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
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
