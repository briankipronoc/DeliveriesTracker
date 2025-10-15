package com.kiprono.mamambogaqrapp

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * -------------------------------------------
 *  Transition Utilities
 *  Used to handle smooth page transitions
 *  between Dashboard, Deliveries & Profile
 * -------------------------------------------
 */

/**
 * Opens a new activity with a slide animation.
 */
fun Activity.navigateWithAnimation(intent: Intent) {
    startActivity(intent)
    // Slide the new screen in from the right
    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
}

/**
 * Closes current activity with fade animation.
 */
fun Activity.navigateBackWithAnimation() {
    finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

/**
 * Wrap your composable content with this to get
 * a smooth fade-in/fade-out animation on load.
 */
@Composable
fun AnimatedScreen(
    fadeInDuration: Int = 700,
    fadeOutDuration: Int = 500,
    content: @Composable () -> Unit
) {
    // Tracks visibility state for animation
    val visibleState = remember { mutableStateOf(false) }

    // Start animation after a small delay
    LaunchedEffect(Unit) {
        delay(100)
        visibleState.value = true
    }

    AnimatedVisibility(
        visible = visibleState.value,
        enter = fadeIn(animationSpec = tween(fadeInDuration)),
        exit = fadeOut(animationSpec = tween(fadeOutDuration))
    ) {
        content()
    }
}