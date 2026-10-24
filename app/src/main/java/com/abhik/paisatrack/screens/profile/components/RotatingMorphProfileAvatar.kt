package com.abhik.paisatrack.screens.profile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.graphics.shapes.Morph
import coil.compose.AsyncImage
import com.abhik.paisatrack.R

/**
 * A profile avatar with seamless morphing shapes and continuous 360-degree rotation.
 * The shape sequence flows smoothly and connects in an infinite loop:
 * Cookie12Sided -> SoftBurst -> Clover8Leaf -> VerySunny -> Cookie12Sided.
 * Each transition rotates 360 degrees while the inner picture/icon remains upright.
 *
 * Rotation and morphing are active when [isActive] is true and the screen lifecycle is RESUMED.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RotatingMorphProfileAvatar(
    profilePicUrl: String?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    avatarSize: Dp = 88.dp,
    iconSize: Dp = 46.dp
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLifecycleResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isLifecycleResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val shouldAnimate = isActive && isLifecycleResumed

    val shapeMorphs = remember {
        listOf(
            Morph(MaterialShapes.Cookie12Sided, MaterialShapes.SoftBurst),
            Morph(MaterialShapes.SoftBurst, MaterialShapes.Clover8Leaf),
            Morph(MaterialShapes.Clover8Leaf, MaterialShapes.VerySunny),
            Morph(MaterialShapes.VerySunny, MaterialShapes.Cookie12Sided)
        )
    }

    val progressAnimatable = remember { Animatable(0f) }

    LaunchedEffect(shouldAnimate) {
        if (shouldAnimate) {
            while (true) {
                val startVal = progressAnimatable.value % 4f
                progressAnimatable.snapTo(startVal)
                val duration = (((4f - startVal) / 4f) * 8000).toInt().coerceAtLeast(16)
                progressAnimatable.animateTo(
                    targetValue = 4f,
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                )
                progressAnimatable.snapTo(0f)
            }
        }
    }

    val animationProgress = progressAnimatable.value
    val currentRotation = animationProgress * 360f
    val normalizedProgress = animationProgress % 4f
    val currentSegment = normalizedProgress.toInt().coerceIn(0, 3)
    val segmentProgress = (normalizedProgress - currentSegment).coerceIn(0f, 1f)
    val activeMorph = shapeMorphs[currentSegment]

    val currentShape = remember(activeMorph, segmentProgress) {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val path = Path()
                activeMorph.toPath(progress = segmentProgress, path = path)
                val scaleMatrix = Matrix().apply {
                    scale(x = size.width, y = size.height)
                }
                path.transform(scaleMatrix)
                val bounds = path.getBounds()
                path.translate(size.center - bounds.center)
                return Outline.Generic(path)
            }
        }
    }

    Box(
        modifier = modifier
            .size(avatarSize)
            .graphicsLayer {
                rotationZ = currentRotation
            }
            .clip(currentShape)
            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
            .border(
                BorderStroke(
                    1.5.dp,
                    if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
                ),
                currentShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -currentRotation
                },
            contentAlignment = Alignment.Center
        ) {
            if (!profilePicUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profilePicUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Profile Picture",
                    tint = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
