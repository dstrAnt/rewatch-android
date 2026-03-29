package it.polimi.rewatch

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import it.polimi.rewatch.common.AnimatedNavDisplay
import it.polimi.rewatch.common.FullScreenImageOverlay
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.PosterMedia
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.person.PersonPage
import it.polimi.rewatch.screens.search.SearchPage
import it.polimi.rewatch.screens.search.SearchViewModel
import it.polimi.rewatch.screens.settings.LoginPage
import it.polimi.rewatch.screens.settings.ProfilePage
import it.polimi.rewatch.screens.settings.SettingsPage
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.theme.RewatchTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AndroidEntryPoint // 2. Add this annotation!
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
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
    val backStack = rememberNavBackStack(HomeTabs)
    var fullScreenImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var isOverlayVisible by rememberSaveable { mutableStateOf(false) }
    var bottomSheetMedia by rememberSaveable { mutableStateOf<PosterMedia?>(null) }
    val navActions = remember(backStack) {
        NavigationActions(
            onNavigateToPerson = { id -> backStack.add(PersonPage(id)) },
            onNavigateToMedia = { id -> backStack.add(MediaPage(id)) },
            onNavigateBack = { backStack.removeLastOrNull() },
            onFullscreenImage = {
                Modifier.clickable {
                    fullScreenImageUrl = it
                    isOverlayVisible = true
                }
            },
            onBottomSheet = {
                if (it.id is MediaId) bottomSheetMedia = it
            },
            onNavigateToLogin = {
                backStack.add(LoginPage)
            },
            onNavigateToProfile = {
                backStack.add(ProfilePage)
            },
            onClearAndNavigateToSettings = {
                backStack.clear()
                backStack.add(SettingsPage)
            },
        )
    }
    val pagerState = rememberPagerState(pageCount = { AppDestinations.entries.size })
    val currentScreen = backStack.lastOrNull()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0 && currentScreen == HomeTabs) {
        // Snap the user back to the Home tab atomically
        scope.launch {
            pagerState.scrollToPage(0)
        }
//        bottomBarState = AppDestinations.HOME
//
//        backStack.clear()
//        backStack.add(Home)
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                AppDestinations.entries[pagerState.currentPage],
                {
                    scope.launch {
                        pagerState.scrollToPage(AppDestinations.entries.indexOf(it))
                    }
//                    bottomBarState = it
//
                    backStack.removeAll({ true })
                    backStack.add(HomeTabs)
                },
            )
        },
    ) { outerPadding ->
        // 5. Render the stack using NavDisplay
        AnimatedNavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<HomeTabs>(metadata = noAnimationMetadata()) {
                    // Update HomePage to accept lambdas instead of navController
                    HorizontalPager(
                        state = pagerState,
                        //modifier = Modifier.padding(innerPadding)
                    ) { page ->
                        when (AppDestinations.entries.get(page)) {
                            AppDestinations.HOME -> HomePage(
                                actions = navActions,
                                outerPadding = outerPadding,
                            )
                            AppDestinations.SEARCH -> {
                                val viewModel = hiltViewModel<SearchViewModel>()
                                viewModel.reset()
                                SearchPage(
                                    actions = navActions,
                                    outerPadding = outerPadding,
                                    pagerState = pagerState,
                                    viewModel = viewModel,
                                )
                            }

                            AppDestinations.MOVIE -> TrackingPage(
                                actions = navActions,
                                type = MediaType.MOVIE,
                                outerPadding = outerPadding,
                            )

                            AppDestinations.TV -> TrackingPage(
                                actions = navActions,
                                type = MediaType.TV_SHOW,
                                outerPadding = outerPadding,
                            )

                            AppDestinations.SETTINGS -> SettingsPage(outerPadding, navActions)
                        }
                    }
                }

                entry<MediaPage> { key ->
                    // 4. Look how clean this is! No more backStackEntry.arguments?.getString()
                    // You just access key.movieId directly.
                    Box(
                        Modifier.background(MaterialTheme.colorScheme.surface),
                    ) {
                        MediaPage(
                            actions = navActions,
                            mediaId = key.mediaId, // Type-safe property
                            outerPadding = outerPadding,
                        )
                    }
                }

                entry<PersonPage> { key ->
                    PersonPage(
                        actions = navActions,
                        personId = key.personId,
                        outerPadding = outerPadding,
                    )
                }
                entry<LoginPage> {
                    LoginPage(
                        actions = navActions,
                        outerPadding = outerPadding,
                    )
                }
                entry<ProfilePage> {
                    ProfilePage(
                        outerPadding = outerPadding,
                        actions = navActions,
                    )
                }
                entry<SettingsPage> {
                    SettingsPage(
                        actions = navActions,
                        outerPadding = outerPadding,
                    )
                }
            },
        )
        // Add your movie details route here just like before
        // composable("movie_page/{movieId}") { ... }
    }
    bottomSheetMedia?.let {
        BottomSheet(media = it, onDismiss = { bottomSheetMedia = null }, actions = navActions)
    }
    val animationSpec = tween<Float>(durationMillis = 300)
    AnimatedVisibility(
        visible = isOverlayVisible,
        enter = fadeIn(animationSpec = animationSpec),
        exit = fadeOut(animationSpec = animationSpec),
    ) {
        // Use lastSeenImageUrl instead of fullScreenImageUrl here!
        FullScreenImageOverlay(
            imageUrl = fullScreenImageUrl.orEmpty(),
            onDismiss = { isOverlayVisible = false },
        )
    }
}

@Composable
private fun noAnimationMetadata(): Map<String, Any> = metadata {
    put(NavDisplay.TransitionKey) {
        // Slide new content up, keeping the old content in place underneath
        EnterTransition.None togetherWith ExitTransition.None
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,      // Color of the icon when active
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface, // Color of the icon when inactive
                ),
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

@Serializable
data object HomeTabs : NavKey

@Serializable
data object LoginPage : NavKey

@Serializable
data object ProfilePage : NavKey

@Serializable
data object SettingsPage : NavKey

@Serializable
data class MediaPage(val mediaId: MediaId) : NavKey

@Serializable
data class PersonPage(val personId: Int) : NavKey
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
    SETTINGS(
        "Settings", Icons.Default.Settings,
    ),
}
