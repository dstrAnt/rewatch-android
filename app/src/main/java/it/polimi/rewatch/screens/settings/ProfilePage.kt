package it.polimi.rewatch.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.model.MediaType
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PersistRepository,
) : ViewModel() {
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

    val email = repository.email

    fun logout() {
        repository.logout()
    }
}


@Composable
fun ProfilePage(
    outerPadding: PaddingValues,
    actions: NavigationActions,
    vm: ProfileViewModel = hiltViewModel(),
) {
    val watchedMovies by vm.watchedMovies.collectAsStateWithLifecycle(0)
    val watchedShows by vm.watchedShows.collectAsStateWithLifecycle(0)
    val watchlistShows by vm.watchlistShows.collectAsStateWithLifecycle(0)
    val watchlistMovies by vm.watchlistMovies.collectAsStateWithLifecycle(0)
    val email by vm.email.collectAsStateWithLifecycle("")

    val scrollState = rememberScrollState()

    fun handleLogout() {
        vm.logout()
        actions.onClearAndNavigateToSettings()
    }
    Scaffold(
        topBar = {
            BasicTopBar(actions.onNavigateBack, "Profile")
        },
        modifier = Modifier
            .padding(
                start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) { paddingValues ->

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()                // handle keyboard space need
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = outerPadding.calculateBottomPadding(),
                )
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp),
        ) {
            ProfileBox(Icons.Default.Person, email)
            val boxShape = RoundedCornerShape(5.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileBoxRow("Movies in watchlist", "$watchlistMovies", true)
                ProfileBoxRow("Movies watched", "$watchedMovies", true)
                ProfileBoxRow("TV Shows in watchlist", "$watchlistShows", true)
                ProfileBoxRow("TV Shows watched", "$watchedShows")
            }
            // logout
            val redColor = MaterialTheme.colorScheme.error
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(boxShape)
                    .border(
                        BorderStroke(1.dp, redColor),
                        boxShape,
                    )
                    .clickable(onClick = ::handleLogout)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Logout", color = redColor)
                Spacer(Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = redColor)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

}

@Composable
private fun ProfileBoxRow(label: String, value: String, withDivider: Boolean = false) {
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
                modifier = Modifier.size(55.dp),
            )
        }
        Text(
            text = email.orEmpty(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}