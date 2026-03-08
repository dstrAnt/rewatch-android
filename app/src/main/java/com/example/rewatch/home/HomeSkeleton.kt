package com.example.rewatch.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rewatch.SkeletonPart

@Composable
fun HomeSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        userScrollEnabled = false, // Disabilitiamo lo scroll durante il caricamento
    ) {
        // Ripetiamo 3 volte per simulare Trending, Top Rated e Recent
        items(3) {
            SkeletonSection()
        }
    }
}

@Composable
fun SkeletonSection() {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        // Simulazione del Titolo della sezione (es. "Trending")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SkeletonPart(
                modifier = Modifier.fillMaxWidth(),
                height = 30.dp,
                shape = RoundedCornerShape(4.dp),
            )
        }

        // Simulazione della Griglia a 2 righe
        // Usiamo una Row con scroll orizzontale disabilitato per mostrare i blocchi fissi
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(312.dp), // Stessa altezza della LazyHorizontalGrid reale
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Mostriamo 3 colonne di skeleton (6 poster totali per sezione)
            repeat(3) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        SkeletonPart(
                            width = 100.dp,
                            height = 150.dp,
                            shape = RoundedCornerShape(5.dp), // Stesso shape del PosterImage
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}