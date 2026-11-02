package produkcja.meble.mobile.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    title: String,
    onDismiss: () -> Unit,
) {
    FullScreenImageDialog(
        images = listOf(imageUrl),
        initialIndex = 0,
        title = title,
        onDismiss = onDismiss,
    )
}

@Composable
fun FullScreenImageDialog(
    images: List<String>,
    initialIndex: Int = 0,
    title: String,
    onDismiss: () -> Unit,
) {
    if (images.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by rememberSaveable(initialIndex, images) {
        mutableIntStateOf(initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)))
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(currentIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    val currentRawUrl = images.getOrNull(currentIndex) ?: images.first()
    val resolvedUrl = remember(currentRawUrl) { resolveImageUrl(currentRawUrl) ?: currentRawUrl }
    val context = LocalContext.current
    val thumbScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f)),
        ) {
            // Główny kontener obrazka ze skalowaniem i przesuwaniem
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            },
                        )
                    }
                    .pointerInput(currentIndex) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            scale = newScale
                            if (newScale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(resolvedUrl)
                        .crossfade(true)
                        .listener(
                            onError = { _, result ->
                                android.util.Log.e("FullScreenImage", "Błąd ładowania zdjęcia: $resolvedUrl", result.throwable)
                            }
                        )
                        .build(),
                    contentDescription = "$title (${currentIndex + 1}/${images.size})",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                        ),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    },
                    error = {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImageNotSupported,
                                contentDescription = "Nie udało się załadować zdjęcia",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Nie udało się pobrać zdjęcia",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                )
            }

            // Poprzednie zdjęcie (strzałka po lewej)
            if (images.size > 1 && currentIndex > 0) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp),
                ) {
                    IconButton(
                        onClick = { currentIndex-- },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Poprzednie zdjęcie",
                        )
                    }
                }
            }

            // Następne zdjęcie (strzałka po prawej)
            if (images.size > 1 && currentIndex < images.lastIndex) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                ) {
                    IconButton(
                        onClick = { currentIndex++ },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Następne zdjęcie",
                        )
                    }
                }
            }

            // Pasek górny z tytułem, licznikiem i przyciskiem zamknięcia
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (images.size > 1) {
                            "Zdjęcie ${currentIndex + 1} z ${images.size} • Przybliż dotknięciem 2x"
                        } else {
                            "Dotknij 2x lub rozsuń palce, aby przybliżyć"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AnimatedVisibility(
                        visible = scale > 1.05f,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f),
                        ) {
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOutMap,
                                    contentDescription = "Zresetuj powiększenie",
                                    tint = Color.White,
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zamknij pełny ekran",
                            )
                        }
                    }
                }
            }

            // Dolny pasek miniaturek (jeśli więcej niż 1 zdjęcie)
            if (images.size > 1) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(thumbScrollState)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        images.forEachIndexed { idx, url ->
                            val isSelected = idx == currentIndex
                            val thumbResolved = resolveImageUrl(url) ?: url
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(
                                        border = if (isSelected) {
                                            BorderStroke(2.dp, Color.White)
                                        } else {
                                            BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .clickable { currentIndex = idx },
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(thumbResolved)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Miniatura ${idx + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
