package it.polimi.rewatch.screens.person

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.SkeletonPart

@Composable
fun PersonSkeleton() {
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            // TopBar vuota durante lo skeleton
        },
    ) { padding ->
        LazyColumn(
            state = scrollState,
            userScrollEnabled = false, // Evitiamo lo scroll durante il caricamento
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = padding.calculateTopPadding() + 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // --- Foto Profilo ---
                    SkeletonPart(
                        width = 150.dp,
                        height = 200.dp,
                        shape = RoundedCornerShape(12.dp),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // --- Nome e Job ---
                        SkeletonPart(width = 200.dp, height = 25.dp)
                        Spacer(Modifier.height(4.dp))
                        SkeletonPart(width = 120.dp, height = 15.dp)

                        Spacer(Modifier.height(24.dp))

                        // --- BirthInfo Skeleton ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 24.dp),
                                horizontalAlignment = Alignment.End,
                            ) {
                                SkeletonPart(width = 40.dp, height = 14.dp)
                                Spacer(Modifier.height(4.dp))
                                SkeletonPart(width = 100.dp, height = 18.dp)
                            }

                            // Linea centrale
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .background(Color.DarkGray.copy(alpha = 0.3f)),
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 24.dp),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                SkeletonPart(width = 40.dp, height = 14.dp)
                                Spacer(Modifier.height(4.dp))
                                SkeletonPart(width = 60.dp, height = 18.dp)
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // --- Bio Skeleton ---
                        Column(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                        ) {
                            repeat(3) { // 3 righe di testo bio
                                SkeletonPart(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    height = 14.dp,
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // --- Tabs Skeleton ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            repeat(4) {
                                SkeletonPart(width = 80.dp, height = 30.dp, shape = RoundedCornerShape(15.dp))
                            }
                        }

                        Spacer(Modifier.height(24.dp))
//                        Spacer(Modifier.padding(top = 4.dp))
                        Row(
                            Modifier
                                .padding(vertical = 5.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                        ) {
                            SkeletonPart(
                                Modifier
                                    .width(70.dp)
                                    .height(20.dp),
                            )
                        }

                        repeat(2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                repeat(4) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        SkeletonPart(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(2f / 3f),
                                            shape = RoundedCornerShape(8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}