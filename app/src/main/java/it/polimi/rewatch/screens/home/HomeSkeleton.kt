package it.polimi.rewatch.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.SkeletonPart

@Composable
fun HomeSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        userScrollEnabled = false,
    ) {
        items(3) {
            SkeletonSection()
        }
    }
}

@Composable
fun SkeletonSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
    ) {
        // --- Titolo Sezione ---
        SkeletonPart(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            width = 120.dp, // Larghezza verosimile per un titolo
            height = 30.dp,
            shape = RoundedCornerShape(4.dp),
        )

        // --- Griglia 2x4 Adattiva ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { // 2 Righe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(4) { // 4 Colonne
                        SkeletonPart(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(2f / 3f), // Fondamentale: deve coincidere con MovieItem
                            shape = RoundedCornerShape(5.dp),
                        )
                    }
                }
            }
        }

        // --- Pulsante Expand ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            SkeletonPart(
                modifier = Modifier.fillMaxWidth(),
                height = 32.dp,
            )
        }
    }
}