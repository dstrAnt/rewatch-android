package com.example.rewatch.movie


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.rewatch.relativeMove


@Composable
fun MovieSkeleton() {
    val pad = 16.dp

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // 1. Backdrop Skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shimmerEffect()
            )
        }

        // 2. Title and Info Section Skeleton
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = pad)
                    .relativeMove((-20).dp), // Mantengo la tua logica di posizionamento
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(10.dp))
                    // Titolo
                    Box(
                        Modifier
                            .size(
                                180.dp,
                                28.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Spacer(Modifier.height(10.dp))
                    // Info Row (Certificazione, Data, Runtime)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            Modifier
                                .size(
                                    40.dp,
                                    18.dp
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            Modifier
                                .size(
                                    80.dp,
                                    18.dp
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    // Director
                    Box(
                        Modifier
                            .size(
                                120.dp,
                                16.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
                // Poster
                Box(
                    Modifier
                        .size(
                            100.dp,
                            150.dp
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect()
                )
            }
        }

        item {
            Column(
                modifier = Modifier.padding(bottom = pad),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(5.dp))

                // Tagline Skeleton (una riga singola più corta)
                Box(
                    Modifier
                        .padding(horizontal = pad)
                        .size(
                            200.dp,
                            16.dp
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                // Overview Skeleton (3 righe di testo simulate)
                Column(
                    modifier = Modifier.padding(horizontal = pad),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    ) // Ultima riga più corta
                }
            }

            // Divider quasi invisibile per mantenere le spaziature
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = Color.Gray.copy(alpha = 0.1f)
            )
        }

        // 3. Stats Pill Skeleton
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(
                            250.dp,
                            44.dp
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
            }
        }

        // 4. Tabs Skeleton
        item {
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = pad),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) {
                    Box(
                        Modifier
                            .size(
                                60.dp,
                                20.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
        }

        // 5. List Items Skeleton (Simuliamo i PersonItem del Cast)
        items(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = pad,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    Modifier
                        .size(
                            55.dp,
                            60.dp
                        )
                        .clip(RoundedCornerShape(7.dp))
                        .shimmerEffect()
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Box(
                        Modifier
                            .size(
                                140.dp,
                                18.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .size(
                                90.dp,
                                14.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    background(brush)
}