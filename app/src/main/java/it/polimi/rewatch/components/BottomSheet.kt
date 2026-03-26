package it.polimi.rewatch.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterMedia
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
    onDismiss: () -> Unit,
) {
    val watchlistItem by viewModel.watchlistItem.collectAsState()
    val watchedItem by viewModel.watchedItem.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        //sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        //contentColor = MaterialTheme.colorScheme.surface,
    ) {
        val iconSize = 45.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 2. Add the verticalScroll modifier to your content
                //verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Text(
                    media.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    media.releaseDate.toString().formatDate("YYYY"),
                )
            }
            HorizontalDivider(Modifier.layoutEvade(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                SheetIconButton(
                    { viewModel.fakeRepo.toggleWatchedList(media.id as MediaId) },
                    if (watchedItem != null) Icons.Filled.Visibility else Icons.Outlined.Visibility,
                    "Watched",
                )

                SheetIconButton(
                    { viewModel.fakeRepo.toggleWatchlist(media.id as MediaId) },
                    if (watchlistItem != null) Icons.Default.AccessTimeFilled else Icons.Default.AccessTime,
                    "Watchlist",
                )
            }
            HorizontalDivider(Modifier.layoutEvade(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                val currentRating = watchedItem?.rating ?: 0.0f
                StarRating(
                    currentRating,
                    starSize = iconSize,
                    onRatingChanged = {
                        viewModel.fakeRepo.setRating(
                            media.id as MediaId,
                            it,
                        )
                    },
                )
            }
            HorizontalDivider(Modifier.layoutEvade(16.dp))
            Text(
                "Movie was watched",
            )
            HorizontalDivider(Modifier.layoutEvade(16.dp))
            Text(
                "Open Movie",
            )


            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SheetIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    text: String,
) {
    Column {
        IconButton(
            modifier = Modifier.size(60.dp),
            onClick = onClick,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(37.dp),
            )
        }
        Text(text)
    }
}
