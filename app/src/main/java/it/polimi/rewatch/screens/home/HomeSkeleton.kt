package it.polimi.rewatch.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.SkeletonPart
import it.polimi.rewatch.common.itemFullSpan

@Composable
fun HomeSkeleton() {
    CustomLazyGrid(
        contentPadding = PaddingValues(bottom = 24.dp, top = 92.dp),
        userScrollEnabled = false,
    ) { columnCount -> // <-- 1. Cattura la variabile calcolata dalla CustomLazyGrid
        repeat(3) {
            skeletonSection(columnCount)
        }
    }
}

private fun LazyGridScope.skeletonSection(columnCount: Int) {
    itemFullSpan {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
        ) {
            // --- Titolo Sezione ---
            SkeletonPart(
                modifier = Modifier.padding(vertical = 8.dp),
                width = 120.dp,
                height = 30.dp,
                shape = RoundedCornerShape(4.dp),
            )
        }
    }
    items(count = 3 * columnCount) {
        SkeletonPart(
            modifier = Modifier.aspectRatio(2f / 3f),
            shape = RoundedCornerShape(5.dp),
        )
    }
    itemFullSpan {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            SkeletonPart(
                width = 100.dp,
                height = 32.dp,
                modifier = Modifier.clip(CircleShape),
            )
        }
    }
}