package it.polimi.rewatch.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.AuthState
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.model.MediaType
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map


@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: PersistRepository,
) : ViewModel() {
    val state = repository.authState

    private val _errorMessage = MutableStateFlow<String?>(null)

    private inline fun <T> Flow<Result<List<T>>>.countBy(crossinline predicate: (T) -> Boolean): Flow<Int> {
        return this.map { result ->
            result.getOrElse { error ->
                _errorMessage.value = error.message
                emptyList()
            }.count(predicate)
        }
    }

    val watchlistMovies = repository.watchlistFlow.countBy { it.id.type == MediaType.MOVIE }
    val watchedMovies = repository.watchedFlow.countBy { it.id.type == MediaType.MOVIE }

    val watchlistShows = repository.watchlistFlow.countBy { it.id.type == MediaType.TV_SHOW }
    val watchedShows = repository.watchedFlow.countBy { it.id.type == MediaType.TV_SHOW }
    val email: Flow<String?> = repository.authState.map { if (it is AuthState.Authenticated) it.email else null }

    fun logout() {
        repository.logout()
    }

}

/**
 * Screen to display the basic information about the user (email, usage stats)
 * and manage login and logout operations
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfilePage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
    vm: ProfileViewModel = hiltViewModel(),
) {
    val authState by vm.state.collectAsStateWithLifecycle(AuthState.SignedOut)

    val watchedMovies by vm.watchedMovies.collectAsStateWithLifecycle(0)
    val watchedShows by vm.watchedShows.collectAsStateWithLifecycle(0)
    val watchlistShows by vm.watchlistShows.collectAsStateWithLifecycle(0)
    val watchlistMovies by vm.watchlistMovies.collectAsStateWithLifecycle(0)
    val email by vm.email.collectAsStateWithLifecycle("")

    fun handleLogout() {
        vm.logout()
        //actions.onClearAndNavigateToSettings()
    }

    Scaffold(
//        topBar = {
//            BasicTopBar("Profile", rememberVectorPainter(Icons.Default.Person), false) {}
//        },
        modifier = Modifier
            .padding(
                start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
            ).testTag("profile_page"),
    ) { paddingValues ->
        BoxWithConstraints(
            Modifier
                .padding(
                    bottom = outerPadding.calculateBottomPadding(),
                )
                .fillMaxSize(),
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(16.dp)
                    .padding(top = paddingValues.calculateTopPadding())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp),
                ) {
                    Spacer(Modifier.height(5.dp))
                    val e = if (authState is AuthState.Authenticated) email else "Guest"
                    ProfileBox(Icons.Default.Person, e)
                    Column(
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProfileBoxRow("Movies in watchlist", "$watchlistMovies", true)
                        ProfileBoxRow("Movies watched", "$watchedMovies", true)
                        ProfileBoxRow("Tv Shows in watchlist", "$watchlistShows", true)
                        ProfileBoxRow("Tv Shows watched", "$watchedShows")
                    }
                    // login / logout
                    val color = when (authState) {
                        is AuthState.Authenticated -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }

                    fun callback() = when (authState) {
                        is AuthState.Authenticated -> (::handleLogout)()
                        else -> actions.onNavigateToLogin()
                    }

                    val label = when (authState) {
                        is AuthState.Authenticated -> "Logout"
                        else -> "Login"
                    }
                    val icon = when (authState) {
                        is AuthState.Authenticated -> Icons.AutoMirrored.Filled.Logout
                        else -> Icons.AutoMirrored.Filled.Login
                    }
                    LogButton(color, { callback() }, label, icon)
                    Spacer(Modifier.height(16.dp))
                }
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Rewatch-Android v0.9b", style = MaterialTheme.typography.bodySmall)
                    Text("Politecnico di Milano © 2026", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun LogButton(
    color: Color,
    callback: () -> Unit,
    label: String,
    icon: ImageVector,
) {
    val boxShape = RoundedCornerShape(5.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .clip(boxShape)
            .border(
                BorderStroke(1.dp, color),
                boxShape,
            )
            .clickable(onClick = callback)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = color)
        Spacer(Modifier.width(10.dp))
        Icon(icon, contentDescription = null, tint = color)
    }
}


@Composable
fun ProfileBoxRow(label: String, value: String, withDivider: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Text(label)
        Text(value)
    }
    if (withDivider) HorizontalDivider()
}

@Composable
fun ProfileBox(icon: ImageVector, email: String?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(45.dp),
            )
        }
        Text(
            text = email.orEmpty(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}