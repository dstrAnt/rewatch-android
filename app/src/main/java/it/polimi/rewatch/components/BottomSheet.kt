package it.polimi.rewatch.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterMedia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BottomSheetViewModel.Factory::class)
class BottomSheetViewModel @AssistedInject constructor(
    @Assisted("id") private val id: MediaId,
    val fakeRepo: FakeRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: MediaId,
        ): BottomSheetViewModel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val watchedItem = fakeRepo.watchedList
        .map { list -> list.find { it.id == id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Keeps it alive briefly during config changes
            initialValue = null, // The starting value while it searches
        )

    @RequiresApi(Build.VERSION_CODES.O)
    val watchlistItem = fakeRepo.watchlist
        .map { list -> list.find { it.id == id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Keeps it alive briefly during config changes
            initialValue = null, // The starting value while it searches
        )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    media: PosterMedia,
    viewModel: BottomSheetViewModel = hiltViewModel<BottomSheetViewModel, BottomSheetViewModel.Factory>(
        key = media.id.toString(),
        creationCallback = { it.create(media.id as MediaId) },
    ),
    actions: NavigationActions,
    onDismiss: () -> Unit,
) {
    val watchlistItem by viewModel.watchlistItem.collectAsState()
    val watchedItem by viewModel.watchedItem.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, // Locks the sheet to its exact content height
    )
    val scope = rememberCoroutineScope()

    val navigateAndClose: (MediaId) -> Unit = { id ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
                actions.onNavigateToMedia(id)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        //dragHandle = {  },
    ) {
        // 1. Extract repeated casts and conditions into local variables
        val mediaId = media.id as MediaId
        val isWatched = watchedItem != null
        val isWatchlisted = watchlistItem != null
        val overscrollEffect = rememberOverscrollEffect()


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .overscroll(overscrollEffect)
                .padding(start = 16.dp, end = 16.dp), // 2. Combine paddings
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            // Header
            Column {
                Text(
                    text = media.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { navigateAndClose(mediaId) },
                )
                if (media.releaseDate != null) Text(text = media.releaseDate.toString().formatDate("MMM YYYY"))
            }

            HorizontalDivider(Modifier.layoutEvade(16.dp))
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                SheetIconButton(
                    onClick = { viewModel.fakeRepo.toggleWatchedList(mediaId) },
                    icon = if (isWatched) Icons.Filled.Visibility else Icons.Outlined.Visibility,
                    label = "Watched",
                )
                SheetIconButton(
                    onClick = { viewModel.fakeRepo.toggleWatchlist(mediaId) },
                    icon = if (isWatchlisted) Icons.Default.AccessTimeFilled else Icons.Default.AccessTime,
                    label = "Watchlist",
                )
            }

            HorizontalDivider(Modifier.layoutEvade(16.dp))
            // Rating
            StarRating(
                rating = (watchedItem?.rating ?: 0f) / 2,
                starSize = 45.dp, // 3. Inlined the single-use variable
                onRatingChanged = { viewModel.fakeRepo.setRating(mediaId, it * 2) },
                // 4. Center the rating without needing an extra wrapper Row
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            )

            HorizontalDivider(Modifier.layoutEvade(16.dp))
            // Watch Status
            Text(
                text = if (isWatched) {
                    // Safely format assuming watchedItem.date handles nulls cleanly with your extension
                    "Content watched on ${watchedItem?.date.toString().formatDate("d MMM YYYY")}"
                } else {
                    "You haven't watched this content yet"
                },
            )

            HorizontalDivider(Modifier.layoutEvade(16.dp))
            // Open Page Action
            Text(
                text = "See content page",
                modifier = Modifier.clickable { navigateAndClose(mediaId) },
            )

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SheetIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    Column {
        IconButton(
            modifier = Modifier.size(60.dp),
            onClick = onClick,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(37.dp),
            )
        }
        Text(label)
    }
}
