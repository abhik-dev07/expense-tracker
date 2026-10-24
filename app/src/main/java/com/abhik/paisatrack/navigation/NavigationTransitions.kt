package com.abhik.paisatrack.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/**
 * No-slide transitions:
 * - Tabs: handled by the persistent tab layer in AppNavigation (entries are empty placeholders).
 * - Loader: fades out over the tab layer / next screen.
 * - Push: new screen fades + scales up slightly over the stationary old one.
 * - Pop: top screen fades + scales down, revealing the screen beneath.
 * - Predictive back: linear, tracks the gesture.
 */
object NavigationTransitions {

    private const val PUSH_MS = 300
    private const val POP_MS = 240
    private const val PREDICTIVE_MS = 300
    private const val LOADER_MS = 300

    private val SoftEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    fun isTopLevelTab(key: Any?): Boolean =
        key is AppRoute.Dashboard || key is AppRoute.Insights || key is AppRoute.Collections

    private fun tabSwitch(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

    val transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val isFromLoader = initialState.key == AppRoute.Loader
        val isBetweenTabs = isTopLevelTab(initialState.key) && isTopLevelTab(targetState.key)

        when {
            isFromLoader ->
                (EnterTransition.None togetherWith fadeOut(tween(LOADER_MS, easing = SoftEasing)))
                    .apply { targetContentZIndex = -1f }

            isBetweenTabs -> tabSwitch()

            else ->
                ((fadeIn(tween(PUSH_MS, easing = SoftEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(PUSH_MS, easing = SoftEasing))) togetherWith
                    ExitTransition.None)
                    .apply { targetContentZIndex = 1f }
        }
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val isBetweenTabs = isTopLevelTab(initialState.key) && isTopLevelTab(targetState.key)

        if (isBetweenTabs) {
            tabSwitch()
        } else {
            (EnterTransition.None togetherWith
                (fadeOut(tween(POP_MS, easing = SoftEasing)) +
                    scaleOut(targetScale = 0.96f, animationSpec = tween(POP_MS, easing = SoftEasing))))
                .apply { targetContentZIndex = -1f }
        }
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { _ ->
        val isBetweenTabs = isTopLevelTab(initialState.key) && isTopLevelTab(targetState.key)

        if (isBetweenTabs) {
            tabSwitch()
        } else {
            (EnterTransition.None togetherWith
                (fadeOut(tween(PREDICTIVE_MS, easing = LinearEasing)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(PREDICTIVE_MS, easing = LinearEasing))))
                .apply { targetContentZIndex = -1f }
        }
    }
}