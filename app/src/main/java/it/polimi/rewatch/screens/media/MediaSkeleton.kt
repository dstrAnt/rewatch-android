package it.polimi.rewatch.screens.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.SkeletonPart
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.common.relativeMove

@Composable
fun MovieSkeleton(outerPadding: PaddingValues = PaddingValues(0.dp)) {
    val pad = 16.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        contentPadding = PaddingValues(
            start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = outerPadding.calculateBottomPadding(),
        ),
    ) {
        // Backdrop
        item {
            val horizontalPadding =
                outerPadding.calculateStartPadding(LayoutDirection.Ltr) + outerPadding.calculateEndPadding(
                    LayoutDirection.Ltr,
                )
            SkeletonPart(
                Modifier
                    .layoutEvade(horizontalPadding)
                    .fillMaxWidth()
                    .height(230.dp),
                shape = RoundedCornerShape(0.dp),
            )
        }
        // Header Section (Title, Poster, Info)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = pad)
                    .relativeMove((-24).dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(Modifier.height(32.dp))
                    SkeletonPart(width = 220.dp, height = 28.dp) // Title
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SkeletonPart(width = 40.dp, height = 18.dp) // Cert
                        SkeletonPart(width = 120.dp, height = 18.dp) // Date/Time
                    }
                    SkeletonPart(
                        width = 160.dp,
                        height = 16.dp,
                        modifier = Modifier.padding(top = 4.dp),
                    ) // Director
                }
                SkeletonPart(
                    width = 100.dp, height = 150.dp, shape = RoundedCornerShape(5.dp),
                ) // Poster
            }
        }
        // Overview Section
        item {
            Column(
                Modifier.padding(horizontal = pad),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                SkeletonPart(width = 200.dp, height = 18.dp) // Tagline
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkeletonPart(Modifier.fillMaxWidth(), height = 14.dp)
                    SkeletonPart(Modifier.fillMaxWidth(), height = 14.dp)
                    SkeletonPart(Modifier.fillMaxWidth(0.6f), height = 14.dp)
                }
            }
        }
        // Ratings
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                SkeletonPart(width = 250.dp, height = 44.dp, shape = RoundedCornerShape(20.dp))
            }
        }
        // Tracking
        item {
            Column {
                HorizontalDivider()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SkeletonPart(width = 380.dp, height = 44.dp, shape = RoundedCornerShape(20.dp))
                }
            }
        }
        // Tabs
        item {
            Column {
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 25.dp),
                    horizontalArrangement = Arrangement.spacedBy(35.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { SkeletonPart(width = 50.dp, height = 18.dp) }
                }
                Spacer(Modifier.height(7.dp))
            }
        }
        // Cast List Items
        items(5) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = pad, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonPart(width = 55.dp, height = 60.dp, shape = RoundedCornerShape(7.dp))
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonPart(width = 140.dp, height = 20.dp)
                    SkeletonPart(width = 90.dp, height = 14.dp)
                }
            }
        }
    }
}
