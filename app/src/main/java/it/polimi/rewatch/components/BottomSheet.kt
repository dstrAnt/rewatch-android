package it.polimi.rewatch.components

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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

// Da LocalDate a Long (Milliseconds UTC)

fun LocalDate.toMillis(): Long {
    return this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

// Da Long (Milliseconds UTC) a LocalDate

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
}

@HiltViewModel(assistedFactory = BottomSheetViewModel.Factory::class)
class BottomSheetViewModel @AssistedInject constructor(
    @Assisted private val id: MediaId,
    val persistRepo: PersistRepository,
    ) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(id: MediaId): BottomSheetViewModel
    }


    val watchedItem = persistRepo.watchedFlow
        .map { result -> result.getOrNull()?.find { it.id == id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Keeps it alive briefly during config changes
            initialValue = null, // The starting value while it searches
        )


    val watchlistItem = persistRepo.watchlistFlow
        .onEach { list ->
            // 1. Check if the flow is even triggering
            //  println("DEBUG_FLOWSHEET: Repo emitted a list with ${list.size} items")
        }
        .map { result ->
            val foundItem = result.getOrNull()?.find { it.id == id }
            // 2. Check if our equality logic is working
            println("DEBUG_FLOWSHEET: Searching for $id. Found: ${foundItem?.id}")
            foundItem
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null, // The starting value while it searches
        )
}

/**
 * [BottomSheet] is a drawer that comes out from the bottom of the screen and
 * provides the user with all the commands to manage the presence of a content in their lists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    media: PosterInfo,
    viewModel: BottomSheetViewModel = hiltViewModel<BottomSheetViewModel, BottomSheetViewModel.Factory>(
        key = "sheet_${media.id}",
        creationCallback = { it.create(media.id as MediaId) },
    ),
    actions: NavigationActions,
    onDismiss: () -> Unit,
) {
    val watchlistItem by viewModel.watchlistItem.collectAsStateWithLifecycle()
    val watchedItem by viewModel.watchedItem.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, // Locks the sheet to its exact content height
    )
    var showDatePicker by remember { mutableStateOf(false) }
    val tomorrowMillis = LocalDate.now()
        .plusDays(1)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
//                val minDate = viewModel.media?.releaseDate?.toJavaLocalDate()?.toMillis() ?: 0
                return utcTimeMillis < tomorrowMillis //&& utcTimeMillis > minDate
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= LocalDate.now().year + 1
            }
        }
    }
    val datePickerState = rememberDatePickerState(
        selectableDates = selectableDates,
        initialSelectedDateMillis = LocalDate.now().toMillis(), // funzione extension definita prima
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
    val mediaId = media.id as MediaId

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            viewModel.persistRepo.setWatchDate(mediaId, selectedMillis.toLocalDate().atTime(LocalTime.now()))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Dismiss")
                }
            },
        ) {
            DatePicker(state = datePickerState)
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
                    onClick = { viewModel.persistRepo.toggleWatchedList(mediaId) },
                    icon = if (isWatched) Icons.Filled.Visibility else Icons.Outlined.Visibility,
                    label = "Watched",
                )
                SheetIconButton(
                    onClick = { viewModel.persistRepo.toggleWatchlist(mediaId) },
                    icon = if (isWatchlisted) Icons.Default.AccessTimeFilled else Icons.Default.AccessTime,
                    label = "Watchlist",
                )
            }

            HorizontalDivider(Modifier.layoutEvade(16.dp))
            // Rating
            StarRating(
                rating = (watchedItem?.rating ?: 0f) / 2,
                starSize = 45.dp,
                onRatingChanged = { viewModel.persistRepo.setRating(mediaId, it * 2) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            )

            HorizontalDivider(Modifier.layoutEvade(16.dp))
            // Watch Status
            if (isWatched) {
                Row {
                    Text("Content watched on ")
                    Text(
                        watchedItem?.date.toString().formatDate("d MMM YYYY"),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            showDatePicker = true
                        },
                    )
                }
            } else {
                Text("You haven't watched this content yet")
            }
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
fun SheetIconButton(
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
