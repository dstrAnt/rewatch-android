package it.polimi.rewatch.screens.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.SkeletonPart

@Composable
fun TrackingSkeleton(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 7.dp),
        userScrollEnabled = false,
    ) {
        // Chips Row
        item(
            key = "ChipsSkeleton",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(4) {
                    SkeletonPart(
                        width = 60.dp,
                        height = 25.dp,
                        shape = RoundedCornerShape(5.dp),
                    )
                }

            }
        }

        // Media
        items(20) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // PosterItem
                SkeletonPart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                    shape = RoundedCornerShape(5.dp),
                )

                // Stars
                SkeletonPart(
                    width = 50.dp,
                    height = 10.dp,
                    shape = RoundedCornerShape(2.dp),
                )
            }
        }
    }
}