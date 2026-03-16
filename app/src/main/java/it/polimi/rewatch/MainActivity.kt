package it.polimi.rewatch

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.person.PersonPage
import it.polimi.rewatch.screens.search.SearchPage
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.theme.RewatchTheme

@AndroidEntryPoint // 2. Add this annotation!
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
        )
        setContent {
            RewatchTheme {
                RewatchApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RewatchApp() {
    var bottomBarState by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    Scaffold(
        bottomBar = {
            BottomBar(
                bottomBarState,
                { bottomBarState = it },
            )
        },
    ) { outerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = outerPadding.calculateBottomPadding()),
        ) {
            when (bottomBarState) {
                AppDestinations.MOVIE -> TrackingPage(type = MediaType.MOVIE) {}

                AppDestinations.TV -> TrackingPage(type = MediaType.TV_SHOW) {}

                AppDestinations.HOME -> {
                    HomePage()
                }

                AppDestinations.PROFILE -> {
                    PersonPage(
                        31,
                        padding = PaddingValues(
                            outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            outerPadding.calculateTopPadding(),
                            outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        ),
                        onNavigateBack = {},
                    )
                }

                AppDestinations.SEARCH -> {
                    SearchPage(onNavigateBack = { })
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentDestination: AppDestinations,
    onClick: (AppDestinations) -> Unit,
) {
    val systemBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Usiamo direttamente NavigationBar per avere il controllo totale
    NavigationBar(
        modifier = Modifier.height(65.dp + systemBarHeight), // Ecco i tuoi 60dp
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp, // Opzionale: rimuove l'ombra se vuoi un look più flat,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        AppDestinations.entries.forEach { destination ->
            val isSelected = destination == currentDestination

            NavigationBarItem(
                selected = isSelected,
                onClick = { onClick(destination) },
                label = null, // Rimuoviamo il testo
                alwaysShowLabel = false,
                icon = {
                    if (destination.imgVec != null) Icon(
                        imageVector = destination.imgVec,
                        contentDescription = destination.label,
                    )
                    else Icon(
                        painter = painterResource(
                            id = destination.intIcon!!,
                        ),
                        contentDescription = destination.label,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val imgVec: ImageVector? = null,
    val intIcon: Int? = null,
) {
    HOME("Home", intIcon = R.drawable.popcorn),
    SEARCH(
        "Search", Icons.Default.Search,
    ),
    MOVIE(
        "Movies",
        intIcon = R.drawable.movie,
    ),
    TV(
        "Tv Shows",
        intIcon = R.drawable.tv,
    ),
    PROFILE(
        "Profile", Icons.Default.AccountBox,
    ),
}
