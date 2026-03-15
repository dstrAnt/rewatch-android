package it.polimi.rewatch.screens.person

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageUrlBuilder.build
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.MediaScaffold
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.screens.media.ExpandableText
import it.polimi.rewatch.screens.media.Media
import it.polimi.rewatch.screens.media.MediaType
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

enum class MediaTypeFilter(val label: String) {
    ALL("All"),
    MOVIE("Cinema"),
    TV_SHOW("TV")
}

// Helper for formatting strings like "acting_role" to "Acting Role"
private fun String.capitalize(): String = this.replace("_", " ").lowercase().replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PersonPage(
    personId: Int,
    onNavigateBack: () -> Unit,
    padding: PaddingValues,
    viewModel: PersonViewModel = hiltViewModel<PersonViewModel, PersonViewModel.Factory>(
        creationCallback = { it.create(personId) },
    ),
) {
    AnimatedUiState(
        state = viewModel.uiState,
        loadingContent = ::PersonSkeleton,
    ) {
        PersonContent(
            data = it,
            padding = padding,
            onNavigateBack = onNavigateBack,
        )
    }
}

// ... other imports
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PersonContent(
    data: PersonDetailsData,
    padding: PaddingValues,
    onNavigateBack: () -> Unit,
) {
    // 1. Change to GridState
    val details = data.details
    val scrollState = rememberLazyGridState()
    // --- Data & State Logic (Moved from DisplayTab) ---
    val availableTabs = remember(data) { computeAvailableTabs(data) }
    var currentTab by remember { mutableStateOf(availableTabs.first()) }
    var selectedFilter by remember { mutableStateOf(MediaTypeFilter.ALL) }
    val baseList = remember(currentTab, data) {
        if (currentTab.isBlank()) return@remember emptyList()
        val isActing = currentTab.equals("Acting", ignoreCase = true)
        // 1. Combine raw data as sequences to avoid intermediate list allocations
        val combinedSequence = if (isActing) {
            data.movieCredits.cast.asSequence() + data.showCredits.cast.asSequence()
        } else {
            (data.movieCredits.crew.asSequence() + data.showCredits.crew.asSequence())
                // 2. Removed redundant .toString().capitalize()
                .filter { it.department.toString().equals(currentTab, ignoreCase = true) }
        }
        // 3. Apply transformations sequentially
        combinedSequence
            .map(::Media)
            .distinctBy { it.id to it.type }
            .sortedWith(
                compareBy<Media> { it.stringDate.isNullOrBlank() }
                    .thenByDescending { it.stringDate },
            )
            .toList() // Terminal operation: builds the final list just once
    }
    val displayList = remember(baseList, selectedFilter) {
        when (selectedFilter) {
            MediaTypeFilter.ALL -> baseList
            MediaTypeFilter.MOVIE -> baseList.filter { it.type == MediaType.MOVIE }
            MediaTypeFilter.TV_SHOW -> baseList.filter { it.type == MediaType.TV_SHOW }
        }
    }
    // --------------------------------------------------
    MediaScaffold(
        details.name,
        onNavigateBack,
        scrollState,
    ) { innerPadding, fullscreenImageModifier ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 5.dp,
                start = 7.dp, end = 7.dp,
            ),
            modifier = Modifier
                .fillMaxSize(),
        ) {
            // 3. HEADER SECTION: Takes up all 4 columns via GridItemSpan
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfilePicture(
                        details.profileImage,
                        modifier = fullscreenImageModifier(
                            details.profileImage?.let { build(it, width = 500, height = 500) },
                        ),
                    )

                    Spacer(Modifier.height(10.dp))
                    NameAndJob(details.name, details.knownForDepartment?.toString())
                    BirthInfo(details.birthday?.toString(), details.deathday?.toString())
                    Bio(details.biography)

                    Column(
                        Modifier.layoutEvade(),
                    ) {
                        PersonTabs(
                            availableTabs = availableTabs,
                            selectedTabIndex = availableTabs.indexOf(currentTab),
                            onTabSelected = { currentTab = availableTabs[it] },
                        )
                    }
                    // Header for the Grid Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (baseList.any { it.type == MediaType.MOVIE } && baseList.any { it.type == MediaType.TV_SHOW }) {
                            MediaTypeFilterChips(selectedFilter, onFilterSelected = { selectedFilter = it })
                        }

                        Text(
                            text = "${displayList.size} Titles",
                            style = MaterialTheme.typography.labelMedium,
                            lineHeight = 32.sp,
                        )
                    }
                }
            }
            // 4. GRID SECTION: Automatically populates the 4 columns
            items(
                items = displayList,
                key = { "${it.id}_${it.type.name}" },
            ) {
                PosterItem(it, Modifier.animateItem())
            }
        }
    }
}

// Pure function, no longer a @Composable
private fun computeAvailableTabs(data: PersonDetailsData): List<String> {
    val knownFor = data.details.knownForDepartment?.toString()?.replace("_", " ")

    return buildSet {
        if (data.movieCredits.cast.isNotEmpty() || data.showCredits.cast.isNotEmpty()) {
            add("Acting")
        }
        // Avoids multiple intermediate list allocations
        sequenceOf(data.movieCredits.crew, data.showCredits.crew)
            .flatten()
            .mapTo(this) { it.department.toString().replaceFirstChar { char -> char.titlecase() } }
    }.sortedWith(
        compareBy<String> { tabName ->
            when {
                tabName.equals(knownFor, ignoreCase = true) -> 0
                tabName.equals("Acting", ignoreCase = true) -> 1
                else -> 2
            }
        }.thenBy { it },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PersonTabs(
    availableTabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = Color.DarkGray)

    PrimaryScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 8.dp, modifier = modifier) {
        availableTabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title.capitalize(),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun Bio(bio: String?) {
    if (!bio.isNullOrBlank()) {
        Spacer(Modifier.height(16.dp))
        ExpandableText(bio, modifier = Modifier.padding(horizontal = 16.dp), maxLines = 3)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BirthInfo(
    dateOfBirth: String?,
    dateOfDeath: String?,
) {
    if (dateOfBirth == null) return

    Spacer(Modifier.height(24.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InfoColumn(
            label = "Born",
            value = dateOfBirth.formatDate("d MMMM yyyy"),
            alignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .padding(end = 24.dp),
        )

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = Color.DarkGray,
        )
        val rightLabel = if (dateOfDeath == null) "Age" else "Died"
        val rightValue = dateOfDeath?.formatDate("d MMMM yyyy") ?: calculatePersonAge(dateOfBirth)

        InfoColumn(
            label = rightLabel,
            value = rightValue,
            alignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp),
        )
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
        )
    }
}

@Composable
private fun NameAndJob(
    personName: String?,
    job: String?,
) {
    if (personName != null) {
        Text(
            text = personName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
    if (job != null) {
        Text(
            text = job.capitalize(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfilePicture(profileImage: TmdbImage?, modifier: Modifier = Modifier) {
    if (profileImage != null) {
        RewatchAsyncImage(
            model = build(profileImage, width = 150, height = 250),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculatePersonAge(birthday: String?): String {
    if (birthday.isNullOrBlank()) return " - "
    return try {
        val age = Period.between(LocalDate.parse(birthday), LocalDate.now()).years
        "$age years"
    } catch (_: DateTimeParseException) {
        "Wrong date format"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaTypeFilterChips(
    selectedFilter: MediaTypeFilter,
    onFilterSelected: (MediaTypeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaTypeFilter.entries.forEach { filter ->
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides (1.dp)) {
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(text = filter.label, style = MaterialTheme.typography.labelMedium) },
                    contentPadding = PaddingValues(4.dp),
                )
            }
        }
    }
}