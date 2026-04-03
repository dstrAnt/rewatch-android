package it.polimi.rewatch.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.SkeletonPart

@Composable
fun HomeSkeleton() {
    CustomLazyGrid(
        contentPadding = PaddingValues(bottom = 24.dp),
        userScrollEnabled = false,
    ) { columnCount -> // <-- 1. Cattura la variabile calcolata dalla CustomLazyGrid
        items(3, span = { GridItemSpan(maxLineSpan) }) {
            SkeletonSection(columnCount = columnCount) // <-- 2. Passala alla sezione
        }
    }
}

@Composable
fun SkeletonSection(columnCount: Int) { // <-- 1. Aggiungi il parametro
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp),
    ) {
        // --- Titolo Sezione ---
        SkeletonPart(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            width = 120.dp,
            height = 30.dp,
            shape = RoundedCornerShape(4.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { // Numero di righe desiderato
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(columnCount) { // <-- 2. Usa il columnCount dinamico invece di 4
                        SkeletonPart(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(2f / 3f),
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
                width = 100.dp,
                height = 32.dp,
                modifier = Modifier.clip(CircleShape),
            )
        }
    }
}