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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.SkeletonPart
import it.polimi.rewatch.common.layoutEvade

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
        // TabRow
        item(
            key = "TabsSkeleton",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Column(Modifier.layoutEvade()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonPart(
                        width = 100.dp,
                        height = 32.dp,
                        shape = CircleShape,
                    )
                    SkeletonPart(
                        width = 85.dp,
                        height = 32.dp,
                        shape = CircleShape,
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