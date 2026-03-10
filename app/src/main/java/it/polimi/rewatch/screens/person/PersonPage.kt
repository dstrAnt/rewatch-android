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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageUrlBuilder.build
import app.moviebase.tmdb.model.TmdbPersonCredit
import coil3.compose.AsyncImage
import it.polimi.rewatch.common.PosterItem
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.screens.media.AppearingTopBar
import it.polimi.rewatch.screens.media.ExpandableText
import it.polimi.rewatch.screens.media.MediaType
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException
import java.util.Locale.getDefault

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
    val actingRoles = data.credits.cast
    val crewJobs = data.credits.crew

    Scaffold(
        topBar = {
            AppearingTopBar(
                scrollState,
                personName,
                onNavigateBack,
            )
        },
        containerColor = Color.Black,
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
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
                    val availableTabs = computeAvailableTabs(data, actingRoles, crewJobs)
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
    // 1. Determiniamo quale tab è selezionata
    val currentTab = availableTabs.getOrNull(selectedTabIndex)
    // 2. Filtriamo i dati in base alla tab
    val displayList = remember(currentTab, data.credits) {
        when (currentTab) {
            "Acting" -> data.credits.cast
                .sortedByDescending { it.releaseDate }
                .map {
                    // Struttura: Triple(ID, Titolo to Ruolo, PosterPath)
                    Triple(it.id, it.title to (it.character ?: "Unknown Role"), it.posterPath)
                }

            else -> data.credits.crew
                .filter {
                    it.department.toString().replace("_", " ").equals(currentTab, ignoreCase = true)
                }
                .sortedByDescending { it.releaseDate }
                .map {
                    Triple(it.id, it.title to (it.job ?: "Member"), it.posterPath)
                }
        }
    }

    Spacer(Modifier.padding(top = 4.dp))
    Row(
        Modifier.padding(vertical = 5.dp, horizontal = 16.dp),
    ) {
        Text(
            text = "${displayList.size} Films",
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium,
        )
    }


    displayList.chunked(4).forEach { rowMovies ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowMovies.forEach { (movieId, info, poster) ->
                val (movieTitle, _) = info

                Box(modifier = Modifier.weight(1f)) {
                    CreditItem(
                        id = movieId,
                        title = movieTitle ?: "Untitled",
                        posterPath = poster,
                    )
                }
            }

            if (rowMovies.size < 4) {
                repeat(4 - rowMovies.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun computeAvailableTabs(
    data: PersonDetailsData,
    actingRoles: List<TmdbPersonCredit.Movie>,
    crewJobs: List<TmdbPersonCredit.Movie>,
): List<String> {
    val knownFor = data.details.knownForDepartment?.toString()

    return remember(data.credits, knownFor) {
        val tabs = mutableSetOf<String>()

        if (actingRoles.isNotEmpty()) {
            tabs.add("Acting")
        }
        val crewDepartments = crewJobs
            .map { dept ->
                dept.department.toString()
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
            }

        tabs.addAll(crewDepartments)

        tabs.toList().sortedWith(
            compareBy<String> { tabName ->
                when {
                    tabName.equals(knownFor, ignoreCase = true) -> 0
                    tabName.equals("Acting", ignoreCase = true) -> 1
                    else -> 2
                }
            }.thenBy { it },
        )
    }
}

@Composable
fun CreditItem(
    title: String,
    posterPath: String?,
    modifier: Modifier = Modifier,
    id: Int,
) {
    LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PosterItem(id, posterPath, title, MediaType.MOVIE)
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
        containerColor = Color.Black,
        contentColor = Color.White,
    ) {
        availableTabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
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
            maxLines = 4,
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

