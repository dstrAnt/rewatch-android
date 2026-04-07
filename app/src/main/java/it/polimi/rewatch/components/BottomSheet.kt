package it.polimi.rewatch.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import it.polimi.rewatch.screens.tracking.getRatingHeader
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showDatePicker = remember { mutableStateOf(false) }
    val mediaId = media.id as MediaId
    val isWatched = watchedItem != null
    val isWatchlisted = watchlistItem != null

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

    if (showDatePicker.value) {
        WatchDatePickerDialog(
            initialSelectedDateMillis = LocalDate.now().toMillis(),
            onDismiss = { showDatePicker.value = false },
            onDateSelected = { selectedMillis ->
                viewModel.persistRepo.setWatchDate(
                    mediaId,
                    selectedMillis.toLocalDate().atTime(LocalTime.now()),
                )
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        val overscrollEffect = rememberOverscrollEffect()

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .overscroll(overscrollEffect)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                // Header
                BottomSheetHeader(
                    title = media.title.orEmpty(),
                    releaseDate = media.releaseDate?.toString()?.formatDate("MMM YYYY"),
                    onClick = { navigateAndClose(mediaId) },
                )

                HorizontalDivider(Modifier.layoutEvade(16.dp))
                // Actions
                BottomSheetActions(
                    isWatched = isWatched,
                    isWatchlisted = isWatchlisted,
                    onWatchClick = {
                        if (isWatched && (watchedItem?.rating ?: 0f) > 0f) {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = "Please remove your rating before unwatching.",
                                    withDismissAction = true,
                                )
                            }
                        } else {
                            viewModel.persistRepo.toggleWatchedList(mediaId)
                        }
                    },
                    onWatchlistClick = { viewModel.persistRepo.toggleWatchlist(mediaId) },
                )

                HorizontalDivider(Modifier.layoutEvade(16.dp))
                // Rating
                BottomSheetRating(
                    rating = (watchedItem?.rating ?: 0f) / 2,
                    onRatingChanged = { viewModel.persistRepo.setRating(mediaId, it * 2) },
                )

                HorizontalDivider(Modifier.layoutEvade(16.dp))
                // Watch Status
                BottomSheetWatchStatus(
                    isWatched = isWatched,
                    watchDate = watchedItem?.date?.toString()?.formatDate("d MMM YYYY"),
                    onDateClick = { showDatePicker.value = true },
                )

                HorizontalDivider(Modifier.layoutEvade(16.dp))
                // Footer Action
                Text(
                    text = "See content page",
                    modifier = Modifier.clickable { navigateAndClose(mediaId) },
                )

                Spacer(modifier = Modifier.navigationBarsPadding())
            }

            DismissableSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}

// --- Extracted Stateless Components ---
@Composable
private fun BottomSheetHeader(
    title: String,
    releaseDate: String?,
    onClick: () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable { onClick() },
        )
        if (releaseDate != null) {
            Text(text = releaseDate)
        }
    }
}

@Composable
private fun BottomSheetActions(
    isWatched: Boolean,
    isWatchlisted: Boolean,
    onWatchClick: () -> Unit,
    onWatchlistClick: () -> Unit,
) = Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceAround,
) {
    SheetIconButton(
        onClick = onWatchClick,
        icon = if (isWatched) Icons.Filled.Visibility else Icons.Outlined.Visibility,
        label = if (isWatched) "Watched" else "Watch",
    )
    SheetIconButton(
        onClick = onWatchlistClick,
        icon = if (isWatchlisted) Icons.Default.AccessTimeFilled else Icons.Default.AccessTime,
        label = if (isWatchlisted) "In watchlist" else "  Watchlist  ",
    )
}

@Composable
private fun ColumnScope.BottomSheetRating(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
) {
    StarRating(
        rating = rating,
        starSize = 45.dp,
        onRatingChanged = onRatingChanged,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(),
    )
    Text(
        text = if (rating > 0f) getRatingHeader(rating * 2) else "Rate now",
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
}

@Composable
private fun BottomSheetWatchStatus(
    isWatched: Boolean,
    watchDate: String?,
    onDateClick: () -> Unit,
) {
    if (isWatched) {
        Row {
            Text("Content watched on ")
            Text(
                text = watchDate.orEmpty(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onDateClick() },
            )
        }
    } else {
        Text("You haven't watched this content yet")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchDatePickerDialog(
    initialSelectedDateMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
) {
    val tomorrowMillis = remember {
        LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val selectableDates = remember(tomorrowMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis < tomorrowMillis
            override fun isSelectableYear(year: Int) = year <= LocalDate.now().year + 1
        }
    }
    val datePickerState = rememberDatePickerState(
        selectableDates = selectableDates,
        initialSelectedDateMillis = initialSelectedDateMillis,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismiss()
                },
            ) { Text("Select") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissableSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        val swipeState = rememberSwipeToDismissBoxState()

        LaunchedEffect(swipeState.currentValue) {
            if (swipeState.currentValue != SwipeToDismissBoxValue.Settled) {
                snackbarData.dismiss()
            }
        }

        SwipeToDismissBox(
            state = swipeState,
            backgroundContent = {},
            content = { Snackbar(snackbarData = snackbarData) },
        )
    }
}

@Composable
fun SheetIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
