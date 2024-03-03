package com.binbin.androidowner.ui.theme

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavBackStackEntry

class Animations(myContext: Context){
    private val springFade = tween<Float>(durationMillis = 150)
    private val springSlide:SpringSpec<IntOffset> = SpringSpec(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow, null)
    
    private val navIconSpringSlide:SpringSpec<IntSize> = SpringSpec(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow, null)
    val navIconEnterTransition:EnterTransition = expandHorizontally(navIconSpringSlide) + fadeIn()
    val navIconExitTransition:ExitTransition = shrinkHorizontally(navIconSpringSlide) + fadeOut()
    
    private val screenWidth = myContext.resources.displayMetrics.widthPixels
    private val initialOffsetValue = screenWidth/12
    private val targetOffsetValue = screenWidth/12
    
    val navHostEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = springFade) +
        slideIntoContainer(
            animationSpec = springSlide,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {initialOffsetValue}
        )
    }
    
    val navHostExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = springFade) +
        slideOutOfContainer(
            animationSpec = springSlide,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {-targetOffsetValue}
        )
    }
    
    val navHostPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = springFade) +
        slideIntoContainer(
            animationSpec = springSlide,
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = {-initialOffsetValue}
        )
    }
    
    val navHostPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = springFade) +
        slideOutOfContainer(
            animationSpec = springSlide,
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = {targetOffsetValue}
        )
    }
    
}
