package com.example.rewatch

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.rewatch.movie.MoviePage
import com.example.rewatch.theme.RewatchTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            )
        )
        setContent {
            RewatchTheme {
                RewatchApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun RewatchApp() {
    var currentDestination by rememberSaveable {
        mutableStateOf(
            AppDestinations.HOME
        )
    }

    val navController = rememberNavController()

    // 2. Define the NavHost and your screens


    Scaffold(
        bottomBar = {
            // Usiamo direttamente NavigationBar per avere il controllo totale
            NavigationBar(
                modifier = Modifier.fillMaxHeight(0.09f), // Ecco i tuoi 60dp
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp // Opzionale: rimuove l'ombra se vuoi un look più flat,

            ) {
                AppDestinations.entries.forEach { destination ->
                    val isSelected = destination == currentDestination

                    NavigationBarItem(
                        selected = isSelected, onClick = {
                            currentDestination = destination
                        }, label = null, // Rimuoviamo il testo
                        alwaysShowLabel = false, icon = {
                            val iconModifier = if (destination.imgVec == null) Modifier.size(
                                20.dp
                            ) else Modifier
                            if (destination.imgVec != null) {
                                Icon(
                                    imageVector = destination.imgVec,
                                    contentDescription = destination.label
                                )
                            } else {
                                Icon(
                                    painter = painterResource(
                                        id = destination.intIcon!!
                                    ),
                                    contentDescription = destination.label,
                                    modifier = iconModifier
                                )
                            }
                        })
                }
            }
        }) { innerPadding ->

        LocalContext.current // Get the Activity context

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentDestination) {
                AppDestinations.MOVIE -> MoviePage(
                    1317288,
                    padding = PaddingValues(
                        innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        innerPadding.calculateTopPadding(),
                        innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    ),
                    onNavigateBack = {},
                )
//            AppDestinations.TV -> MoviePageState(1) {}


                AppDestinations.HOME -> {
                    // Your Home Page Composable
                    Text("Home Page")
                }

                AppDestinations.SEARCH -> {
                    SearchPage(
                        onNavigateBack = { navController.popBackStack() },
//                    onMovieClick = { movieId: Int ->
//                        // Navigate to detail page, hiding bottom bar if desired
//                        navController.navigate("movie_page/$movieId")
//                    })
                    )
                }


                else -> {}
            }
        }
    }
}


enum class AppDestinations(
    val label: String,
    val imgVec: ImageVector? = null,
    val intIcon: Int? = null,
) {
    HOME("Home", Icons.Default.Home), SEARCH(
        "Search", Icons.Default.Search
    ),
    MOVIE(
        "Movies", intIcon = R.drawable.movie_svgrepo_com
    ),
    TV(
        "Tv Shows", intIcon = R.drawable.tv_svgrepo_com
    ),
    PROFILE(
        "Profile", Icons.Default.AccountBox
    ),
}

