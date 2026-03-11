package it.polimi.rewatch.screens.person

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageUrlBuilder.build
import coil3.compose.AsyncImage
import it.polimi.rewatch.common.PosterItem
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.screens.media.AppearingTopBar
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

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PersonContent(
    data: PersonDetailsData,
    padding: PaddingValues,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    val profileImage = data.details.profileImage
    val personName = data.details.name
    val job = data.details.knownForDepartment.toString()
    val dateOfBirth = data.details.birthday?.toString()
    val dateOfDeath = data.details.deathday?.toString()
    val bio = data.details.biography

    Scaffold(
        topBar = {
            AppearingTopBar(
                scrollState,
                personName,
                onNavigateBack,
            )
        },
    ) {
        LazyColumn(
            state = scrollState,
        ) {
            item {
                Spacer(
                    Modifier
                        .height(padding.calculateTopPadding()),
                )
                ProfilePicture(profileImage)
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                ) {
                    NameAndJob(personName, job)
                    BirthInfo(dateOfBirth, dateOfDeath)
                    Bio(bio)
                    // lista delle Tab disponibili
                    val availableTabs = computeAvailableTabs(data)
                    var selectedTabIndex by remember { mutableIntStateOf(0) }

                    PersonTabs(
                        availableTabs = availableTabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                    )

                    DisplayTab(availableTabs, selectedTabIndex, data)
                }
            }
        }
    }
}


@Composable
private fun DisplayTab(
    availableTabs: List<String>,
    selectedTabIndex: Int,
    data: PersonDetailsData,
) {
    val currentTab = availableTabs.getOrNull(selectedTabIndex) ?: return

    // 1. STATO DEI CHIP
    var selectedFilter by remember { mutableStateOf(MediaTypeFilter.ALL) }

    // 2. LISTA BASE (Operazioni pesanti: mapping, distinct, sorting)
    val baseList = remember(currentTab, data) {
        val rawList = when (currentTab) {
            "Acting" -> {
                val movies = data.movieCredits.cast.map(::Media)
                val shows = data.showCredits.cast.map(::Media)
                movies + shows
            }
            else -> {
                val movies = data.movieCredits.crew
                    .filter { it.department.toString().replace("_", " ").equals(currentTab, ignoreCase = true) }
                    .map(::Media)

                val shows = data.showCredits.crew
                    .filter { it.department.toString().replace("_", " ").equals(currentTab, ignoreCase = true) }
                    .map(::Media)
                movies + shows
            }
        }

        rawList
            .distinctBy { it.id to it.type }
            .sortedWith(
                compareBy<Media> { it.stringDate.isNullOrBlank() }
                    .thenByDescending { it.stringDate },
            )
    }

    // 3. LISTA FILTRATA (Operazione iper-veloce legata al chip)
    val displayList = remember(baseList, selectedFilter) {
        when (selectedFilter) {
            MediaTypeFilter.ALL -> baseList
            // Sostituisci "MediaType.MOVIE" con l'enum o la stringa esatta usata nel tuo oggetto Media
            MediaTypeFilter.MOVIE -> baseList.filter { it.type == MediaType.MOVIE }
            MediaTypeFilter.TV_SHOW -> baseList.filter { it.type == MediaType.TV_SHOW }
        }
    }

    // --- UI della Tab ---

    // Header con i Chip e il contatore allineati
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!baseList.none { it.type == MediaType.MOVIE } && !baseList.none { it.type == MediaType.TV_SHOW })
            MediaTypeFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }, // Aggiorna lo stato quando clicchi
            )

        Text(
            text = "${displayList.size} Titles",
            style = MaterialTheme.typography.labelMedium, // Stile opzionale per pulizia visiva
            lineHeight = 32.sp,
        )
    }

    // Griglia dei risultati
    displayList.chunked(4).forEach { rowItems ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowItems.forEach { item ->
                key(item.id, item.type) {
                    Box(modifier = Modifier.weight(1f)) {
                        CreditItem(
                            id = item.id,
                            title = item.title ?: "Untitled", // Ricordati il fix degli Untitled discusso prima se serve!
                            posterPath = item.posterPath,
                            mediaType = item.type,
                        )
                    }
                }
            }

            if (rowItems.size < 4) {
                repeat(4 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun computeAvailableTabs(
    data: PersonDetailsData,
): MutableList<String> {
    val knownFor = data.details.knownForDepartment?.toString()

    val availableTabs = remember(data) {
        val tabs = mutableSetOf<String>()

        // Se c'è almeno un ruolo come attore (film o serie), aggiungiamo la tab Acting
        val hasActingRoles = data.movieCredits.cast.isNotEmpty() || data.showCredits.cast.isNotEmpty()
        if (hasActingRoles) {
            tabs.add("Acting")
        }

        // Estraiamo e puliamo i dipartimenti da ENTRAMBE le liste crew
        val movieCrewDepts = data.movieCredits.crew.map { it.department.toString() }
        val showCrewDepts = data.showCredits.crew.map { it.department.toString() }

        val allCrewDepts = (movieCrewDepts + showCrewDepts).map { dept ->
            dept.replace("_", " ")
                .lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        tabs.addAll(allCrewDepts)

        // Ordinamento con match esatto
        val sortedTabs = tabs.toList().sortedWith(
            compareBy<String> { tabName ->
                when {
                    tabName.equals(knownFor?.replace("_", " "), ignoreCase = true) -> 0
                    tabName.equals("Acting", ignoreCase = true) -> 1
                    else -> 2
                }
            }.thenBy { it },
        )

        sortedTabs.toMutableList()
    }

    return availableTabs
}
@Composable
fun CreditItem(
    title: String,
    posterPath: String?,
    modifier: Modifier = Modifier,
    mediaType: MediaType,
    id: Int,
) {
    LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PosterItem(id, posterPath, title, mediaType)
    }
}



@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PersonTabs(
    availableTabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = Color.DarkGray)
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = 8.dp,
    ) {
        availableTabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(LocalLocale.current.platformLocale) else it.toString()
                        }.replace("_", " "),
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
        ExpandableText(
            bio,
            modifier = Modifier.padding(horizontal = 16.dp),
            maxLines = 3,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BirthInfo(
    dateOfBirth: String?,
    dateOfDeath: String?,
) {
    if (dateOfBirth != null) {
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "Born",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                )
                Text(
                    text = dateOfBirth.formatDate("d MMMM yyyy"),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White,
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color.DarkGray,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                if (dateOfDeath == null) {
                    Text(
                        text = "Age",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                    )
                    Text(
                        text = calculatePersonAge(dateOfBirth),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                    )
                } else {
                    Text(
                        text = "Died",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                    )
                    Text(
                        text = dateOfDeath.formatDate("d MMMM yyyy"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun NameAndJob(
    personName: String?,
    job: String?,
) {
    if (personName != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = personName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
    if (job != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = job.replace("_", " "),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun ProfilePicture(profileImage: TmdbImage?) {
    if (profileImage != null)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            AsyncImage(
                model = build(
                    profileImage, width = 150, height = 250,
                ),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculatePersonAge(
    birthday: String?,
): String {
    if (birthday.isNullOrBlank()) return " - "

    return try {
        val birthDate = LocalDate.parse(birthday)
        val today = LocalDate.now()
        val age = Period.between(birthDate, today).years
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
            // --- QUESTA È LA PARTE CRUCIALE ---
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides (1.dp),
            ) {
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    contentPadding = PaddingValues(4.dp),
                )
            }
        }
    }
}