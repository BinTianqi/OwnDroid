package com.binbin.androidowner.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry
import com.binbin.androidowner.displayMetrics

class Animations{
    private val fade: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
    private val spring:FiniteAnimationSpec<IntOffset> = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)
    
    val animateListSize:FiniteAnimationSpec<IntSize> = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntSize.VisibilityThreshold)
    
    private val screenWidth = displayMetrics.widthPixels
    private val initialOffsetValue = screenWidth/10
    private val targetOffsetValue = screenWidth/10
    
    val navHostEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = fade) +
        slideIntoContainer(
            animationSpec = spring,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {initialOffsetValue}
        )
    }
    
    val navHostExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = fade) +
        slideOutOfContainer(
            animationSpec = spring,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {-targetOffsetValue}
        )
    }
    
    val navHostPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = fade) +
        slideIntoContainer(
            animationSpec = spring,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {-initialOffsetValue}
        )
    }
    
    val navHostPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = fade) +
        slideOutOfContainer(
            animationSpec = spring,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {targetOffsetValue}
        )
    }
    
}
