package com.binbin.androidowner.ui.theme

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

class Animations(myContext: Context){
    private val fade: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
    private val spring:FiniteAnimationSpec<IntOffset> = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)
    
    val navIconEnterTransition:EnterTransition = expandHorizontally() + fadeIn()
    val navIconExitTransition:ExitTransition = shrinkHorizontally() + fadeOut()
    
    private val screenWidth = myContext.resources.displayMetrics.widthPixels
    private val initialOffsetValue = screenWidth/8
    private val targetOffsetValue = screenWidth/8
    
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
