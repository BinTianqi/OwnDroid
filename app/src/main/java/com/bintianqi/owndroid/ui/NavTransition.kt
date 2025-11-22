package com.bintianqi.owndroid.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Learned from AOSP's Activity animation
 * `frameworks/base/core/res/res/anim/activity_xxx_xxx.xml`
 */
object NavTransition {
    val StandardAccelerateEasing = CubicBezierEasing(0.3F, 0F, 1F, 1F)

    val enterTransition: EnterTransition = slideInHorizontally(
        tween(450, easing = FastOutSlowInEasing),
        { 96 }
    ) + fadeIn(
        tween(83, 50, LinearEasing)
    )

    val exitTransition: ExitTransition = slideOutHorizontally(
        tween(450, easing = StandardAccelerateEasing),
        { -96 }
    ) + fadeOut(tween(100, 200, LinearEasing))

    val popEnterTransition: EnterTransition = slideInHorizontally(
        tween(450, easing = FastOutSlowInEasing),
        { -96 }
    )

    val popExitTransition: ExitTransition =
        slideOutHorizontally(
            tween(450, easing = FastOutSlowInEasing),
            { 96 }
        ) + fadeOut(
            tween(83, 35, LinearEasing)
        )
}
