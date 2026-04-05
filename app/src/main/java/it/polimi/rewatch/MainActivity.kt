@file:Suppress("AssignedValueIsNeverRead")

package it.polimi.rewatch

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import it.polimi.rewatch.common.FullScreenImageOverlay
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PARALLAX_FACTOR
import it.polimi.rewatch.common.popContentTransform
import it.polimi.rewatch.common.standardTween
import it.polimi.rewatch.components.BottomSheet
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.home.HomePage
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.person.PersonPage
import it.polimi.rewatch.screens.search.SearchPage
import it.polimi.rewatch.screens.search.SearchViewModel
import it.polimi.rewatch.screens.settings.AuthPage
import it.polimi.rewatch.screens.settings.ProfilePage
import it.polimi.rewatch.screens.tracking.TrackingPage
import it.polimi.rewatch.theme.RewatchTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

@Composable
fun RewatchApp() {
    val backStack = rememberNavBackStack(HomeNavTabs)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { BottomTabs.entries.size })
    // UI State for Overlays
    var fullScreenImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var isOverlayVisible by rememberSaveable { mutableStateOf(false) }
    var bottomSheetMedia by rememberSaveable { mutableStateOf<PosterInfo?>(null) }
    val searchViewModel = hiltViewModel<SearchViewModel>()
    val currentScreen = backStack.lastOrNull()
    // Centralized Navigation Actions
    val navActions = remember(backStack) {
        NavigationActions(
            onNavigateToPerson = { id -> backStack.add(PersonNavPage(id)) },
            onNavigateToMedia = { id -> backStack.add(MediaNavPage(id)) },
            onNavigateBack = { backStack.removeLastOrNull() },
            onFullscreenImage = { url ->
                fullScreenImageUrl = url
                isOverlayVisible = true
            },
            onBottomSheet = { info -> if (info.id is MediaId) bottomSheetMedia = info },
            onNavigateToLogin = { backStack.add(LoginNavPage) },
            onNavigateToProfile = { backStack.add(ProfileNavPage) },
            onClearAndNavigateToSettings = {
                backStack.clear()
                backStack.add(SettingsNavPage)
            },
            onBackToHome = {
                scope.launch { pagerState.scrollToBottomTab(BottomTabs.HOME) }
            },
        )
    }
    // Handle back button when in a sub-tab of the Home pager
    BackHandler(enabled = pagerState.currentBottomTab() != BottomTabs.HOME && currentScreen == HomeNavTabs) {
        scope.launch { pagerState.scrollToBottomTab(BottomTabs.HOME) }
    }
    // Reset search when navigating away
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledBottomTab() != BottomTabs.SEARCH) {
            searchViewModel.reset()
        }
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentTab = pagerState.currentBottomTab(),
                onTabSelected = { tab ->
                    scope.launch {
                        pagerState.scrollToBottomTab(tab)
                        if (tab == BottomTabs.SEARCH) searchViewModel.reset()
                    }
                    // Reset stack to Home when switching tabs
                    backStack.removeAll { true }
                    backStack.add(HomeNavTabs)
                },
            )
        },
    ) { outerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = {
                slideInHorizontally(standardTween) { it } togetherWith
                        slideOutHorizontally(standardTween) { -it / PARALLAX_FACTOR }
            },
            popTransitionSpec = { popContentTransform() },
            predictivePopTransitionSpec = { popContentTransform() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<HomeNavTabs> {
                    HorizontalPager(state = pagerState) { page ->
                        when (BottomTabs.entries[page]) {
                            BottomTabs.HOME -> HomePage(
                                actions = navActions,
                                outerPadding = outerPadding,
                            )
                            BottomTabs.SEARCH -> SearchPage(
                                actions = navActions,
                                outerPadding = outerPadding,
                                pagerState = pagerState,
                                viewModel = searchViewModel,
                            )

                            BottomTabs.MOVIE -> TrackingPage(
                                actions = navActions,
                                type = MediaType.MOVIE,
                                outerPadding = outerPadding,
                            )

                            BottomTabs.TV -> TrackingPage(
                                actions = navActions,
                                type = MediaType.TV_SHOW,
                                outerPadding = outerPadding,
                            )
                            BottomTabs.PROFILE -> ProfilePage(
                                actions = navActions,
                                outerPadding = outerPadding,
                            )
                        }
                    }
                }

                entry<MediaNavPage> { key ->
                    Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
                        MediaPage(
                            mediaId = key.mediaId,
                            actions = navActions,
                            outerPadding = outerPadding,
                        )
                    }
                }

                entry<PersonNavPage> { key ->
                    PersonPage(
                        personId = key.personId,
                        actions = navActions,
                        outerPadding = outerPadding,
                    )
                }

                entry<LoginNavPage> {
                    AuthPage(
                        actions = navActions,
                        outerPadding = outerPadding,
                    )
                }

                entry<SettingsNavPage> {
                    ProfilePage(
                        actions = navActions,
                        outerPadding = outerPadding,
                    )
                }
            },
        )
    }
    // Overlays (Bottom Sheet and Full Screen Image)
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
    currentTab: BottomTabs,
    onTabSelected: (BottomTabs) -> Unit,
) {
    val systemBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    NavigationBar(
        modifier = Modifier.height(65.dp + systemBarHeight),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        BottomTabs.entries.forEach { tab ->
            val isSelected = tab == currentTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = tab.getPainter(),
                        contentDescription = tab.label,
                        modifier = Modifier.size(tab.iconSize),
                    )
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@Serializable
data object HomeNavTabs : NavKey

@Serializable
data object LoginNavPage : NavKey

@Serializable
data object ProfileNavPage : NavKey

@Serializable
data object SettingsNavPage : NavKey

@Serializable
data class MediaNavPage(val mediaId: MediaId) : NavKey

@Serializable
data class PersonNavPage(val personId: Int) : NavKey

enum class BottomTabs(val label: String, val iconRes: Int? = null, val iconVector: ImageVector? = null) {
    HOME("Home", iconRes = R.drawable.popcorn),
    SEARCH("Search", iconVector = Icons.Default.Search),
    MOVIE("Movies", iconRes = R.drawable.movie),
    TV("Tv Shows", iconRes = R.drawable.tv),
    PROFILE("Profile", iconVector = Icons.Default.Person);

    @Composable
    fun getPainter(): Painter = when {
        iconRes != null -> painterResource(iconRes)
        iconVector != null -> rememberVectorPainter(iconVector)
        else -> error("Icon missing for $this")
    }

    val iconSize: Dp get() = if (iconRes != null) 20.dp else 24.dp
}


fun PagerState.currentBottomTab(): BottomTabs = BottomTabs.entries[currentPage]
suspend fun PagerState.scrollToBottomTab(tab: BottomTabs) = scrollToPage(BottomTabs.entries.indexOf(tab))
fun PagerState.settledBottomTab(): BottomTabs = BottomTabs.entries[settledPage]
