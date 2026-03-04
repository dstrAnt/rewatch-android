package com.example.rewatch // Use your actual package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.rewatch.ui.theme.RewatchTheme

class MovieActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Extract the movie ID passed from the SearchPage
        // The -1 is a default fallback value if nothing was passed
        val movieId =
            intent.getIntExtra("EXTRA_MOVIE_ID", -1)

        setContent {
            // Make sure to wrap this in your app's specific Theme!
            RewatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Render your Composable
                    MoviePageState(
                        movieId = movieId,
                        // 3. To go back, we simply kill this Activity
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}