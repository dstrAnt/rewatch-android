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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
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
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.ExpandableText
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.MediaScaffold
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.model.Media
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

// 1. Nuovo Enum per l'ordinamento
enum class SortOrder(val label: String) {
    POPULARITY("Popularity"),
    DATE("Date")
}

private fun String.capitalize(): String = this.replace("_", " ").lowercase().replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PersonPage(
    personId: Int,
    actions: NavigationActions,
    outerPadding: PaddingValues,
    viewModel: PersonViewModel = hiltViewModel<PersonViewModel, PersonViewModel.Factory>(
        key = personId.toString(),
        creationCallback = { it.create(personId) },
    ),
) {
    AnimatedUiState(
        state = viewModel.uiState,
        loadingContent = ::PersonSkeleton,
    ) {
        PersonContent(
            data = it,
            outerPadding = outerPadding,
            actions = actions,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PersonContent(
    data: PersonDetailsData,
    outerPadding: PaddingValues,
    actions: NavigationActions,
) {
    val details = data.details
    val scrollState = rememberLazyGridState()

    val availableTabs = remember(data) { computeAvailableTabs(data) }
    var currentTab by remember { mutableStateOf(availableTabs.firstOrNull() ?: "") }
    var selectedSort by remember { mutableStateOf(SortOrder.POPULARITY) }

    val knownFor = remember(details) {
        details.knownForDepartment?.toString()?.replace("_", " ") ?: "Acting"
    }
    val isActingPrimary = remember(knownFor) {
        knownFor.equals("Acting", ignoreCase = true)
    }

    // 2. Base list popolata in base alla nuova logica di divisione delle tab
    val baseList = remember(currentTab, data, knownFor, isActingPrimary) {
        if (currentTab.isBlank()) return@remember emptyList()

        val combinedSequence = when (currentTab) {
            "Movies" -> {
                if (isActingPrimary) data.movieCredits.cast.asSequence()
                else data.movieCredits.crew.asSequence().filter { it.department.toString().equals(knownFor, ignoreCase = true) }
            }

            "Shows" -> {
                if (isActingPrimary) data.showCredits.cast.asSequence()
                else data.showCredits.crew.asSequence().filter { it.department.toString().equals(knownFor, ignoreCase = true) }
            }

            "Acting" -> {
                data.movieCredits.cast.asSequence() + data.showCredits.cast.asSequence()
            }

            else -> {
                (data.movieCredits.crew.asSequence() + data.showCredits.crew.asSequence())
                    .filter { it.department.toString().equals(currentTab, ignoreCase = true) }
            }
        }

        combinedSequence
            .map(::Media)
            .distinctBy { it.id to it.type }
            .toList()
    }

    // 3. Ordinamento dinamico per Data o Popolarità
    val displayList = remember(baseList, selectedSort) {
        when (selectedSort) {
            SortOrder.DATE -> baseList.sortedWith(
                compareBy<Media> { it.releaseDate?.toString().isNullOrBlank() }
                    .thenByDescending { it.releaseDate?.toString() },
            )

            SortOrder.POPULARITY -> baseList.sortedByDescending {
                // NOTA: Se il tuo modello Media usa una property diversa per la popolarità (es: voteAverage o simili), modificala qui.
                it.popularity
            }
        }
    }

    MediaScaffold(
        details.name,
        actions.onNavigateBack,
        scrollState,
    ) { innerPadding ->
        CustomLazyGrid(
            state = scrollState,
            contentPadding = PaddingValues(
                top = outerPadding.calculateTopPadding(), bottom = outerPadding.calculateBottomPadding() + 5.dp,
                start = 5.dp, end = 5.dp,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layoutEvade(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfilePicture(
                        details.profileImage,
                        modifier = actions.onFullscreenImage(
                            details.profileImage?.let { build(it, width = 500, height = 500) },
                        ),
                    )

                    Spacer(Modifier.height(10.dp))
                    NameAndJob(details.name, details.knownForDepartment?.toString())
                    BirthInfo(details.birthday?.toString(), details.deathday?.toString())
                    Bio(details.biography)

                    if (availableTabs.isNotEmpty()) {
                        Column(Modifier.layoutEvade()) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            CustomTabRow(
                                availableTabs = availableTabs,
                                onClick = { it: String -> currentTab = it },
                                title = { it.capitalize() },
                                tabState = currentTab,
                            )
                        }
                    }

                    // Sezione Filtri di Ordinamento
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 3.dp)
                            .padding(horizontal = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (displayList.isNotEmpty()) {
                            SortFilterChips(selectedSort, onSortSelected = { selectedSort = it })
                        } else {
                            Spacer(Modifier.weight(1f))
                        }

                        val listSize = displayList.size
                        Text(
                            text = "$listSize Title${if (listSize > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            lineHeight = 32.sp,
                        )
                    }
                }
            }

            items(
                items = displayList,
                key = { "${it.id}_${it.type.name}" },
            ) {
                PosterItem(it, actions.onNavigateToMedia, Modifier.animateItem())
            }
        }
    }
}

// 4. Funzione aggiornata per estrarre le tab dinamicamente
private fun computeAvailableTabs(data: PersonDetailsData): List<String> {
    val knownFor = data.details.knownForDepartment?.toString()?.replace("_", " ") ?: "Acting"
    val isActingPrimary = knownFor.equals("Acting", ignoreCase = true)

    return buildSet {
        // Tab "Movies" per il dipartimento principale
        val hasPrimaryMovies = if (isActingPrimary) {
            data.movieCredits.cast.isNotEmpty()
        } else {
            data.movieCredits.crew.any { it.department.toString().equals(knownFor, ignoreCase = true) }
        }
        if (hasPrimaryMovies) add("Movies")

        // Tab "Shows" per il dipartimento principale
        val hasPrimaryShows = if (isActingPrimary) {
            data.showCredits.cast.isNotEmpty()
        } else {
            data.showCredits.crew.any { it.department.toString().equals(knownFor, ignoreCase = true) }
        }
        if (hasPrimaryShows) add("Shows")

        // Se non è primariamente un attore ma ha crediti recitativi, crea una tab "Acting" secondaria
        if (!isActingPrimary && (data.movieCredits.cast.isNotEmpty() || data.showCredits.cast.isNotEmpty())) {
            add("Acting")
        }

        // Aggiungi tutti gli altri dipartimenti (escludendo quello principale che ora è in Movies/Shows)
        sequenceOf(data.movieCredits.crew, data.showCredits.crew)
            .flatten()
            .filter { !it.department.toString().equals(knownFor, ignoreCase = true) }
            .mapTo(this) { it.department.toString().replaceFirstChar { char -> char.titlecase() } }
    }.sortedWith(
        compareBy<String> { tabName ->
            when (tabName) {
                "Movies" -> 0
                "Shows" -> 1
                "Acting" -> 2
                else -> 3
            }
        }.thenBy { it },
    )
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
private fun BirthInfo(dateOfBirth: String?, dateOfDeath: String?) {
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
private fun NameAndJob(personName: String?, job: String?) {
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

// 5. Componente dei Chip aggiornato per l'ordinamento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterChips(
    selectedSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SortOrder.entries.forEach { sort ->
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp,
            ) {
                FilterChip(
                    modifier = Modifier.height(28.dp),
                    selected = selectedSort == sort,
                    onClick = { onSortSelected(sort) },
                    label = {
                        Text(
                            text = sort.label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                )
            }
        }
    }
}