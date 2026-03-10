package it.polimi.rewatch.screens.media

/**
 * A reusable helper to avoid repeating Box + Clip + Shimmer
 */
// ... imports stay mostly the same, add FastOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.SkeletonPart
import it.polimi.rewatch.common.relativeMove


@Composable
fun MovieSkeleton() {
    val pad = 16.dp

    LazyColumn(modifier = Modifier.fillMaxSize(), userScrollEnabled = false) {
        // 1. Backdrop
        item {
            SkeletonPart(
                Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(0.dp),
            )
        }

        // 2. Header Section (Title, Poster, Info)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = pad)
                    .relativeMove((-15).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Spacer(Modifier.height(0.dp))
                    SkeletonPart(width = 180.dp, height = 28.dp) // Title
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        SkeletonPart(width = 40.dp, height = 18.dp)
                        SkeletonPart(width = 80.dp, height = 18.dp)
                    }
                    SkeletonPart(width = 120.dp, height = 16.dp) // Director
                }
                SkeletonPart(
                    width = 100.dp, height = 150.dp, shape = RoundedCornerShape(8.dp),
                ) // Poster
            }
        }

        // 3. Overview Section
        item {
            Column(
                Modifier.padding(horizontal = pad, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SkeletonPart(width = 200.dp, height = 16.dp) // Tagline
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonPart(Modifier.fillMaxWidth(), height = 14.dp)
                    SkeletonPart(Modifier.fillMaxWidth(), height = 14.dp)
                    SkeletonPart(Modifier.fillMaxWidth(0.6f), height = 14.dp)
                }
            }
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
        }

        // 4. Stats Pill & Tabs
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                SkeletonPart(width = 250.dp, height = 44.dp, shape = RoundedCornerShape(20.dp))
            }
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = pad),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { SkeletonPart(width = 60.dp, height = 20.dp) }
            }
        }

        // 5. Cast List Items
        items(5) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = pad, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonPart(width = 55.dp, height = 60.dp, shape = RoundedCornerShape(7.dp))
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SkeletonPart(width = 140.dp, height = 18.dp)
                    SkeletonPart(width = 90.dp, height = 14.dp)
                }
            }
        }
    }
}