package com.example.rewatch.movie

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.rewatch.theme.RewatchTheme

class MovieActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            )
        )
        // 1. Extract the movie ID passed from the SearchPage
        // The -1 is a default fallback value if nothing was passed
        val movieId = intent.getIntExtra("EXTRA_MOVIE_ID", -1)

        setContent {
            // Make sure to wrap this in your app's specific Theme!
            RewatchTheme {
                Scaffold { padding ->


                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // 2. Render your Composable
                        MoviePageState(
                            movieId = movieId,
                            padding = padding,
                            // 3. To go back, we simply kill this Activity
                            onNavigateBack = { finish() })
                    }
                }

            }
        }
    }
}