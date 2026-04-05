package it.polimi.rewatch.screens.person

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageUrlBuilder.build
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.ExpandableText
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.itemFullSpan
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.MediaScaffold
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.tracking.SortFilterChips
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

private fun String.cleanAndCapitalize(): String = this.replace("_", " ").lowercase().replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}

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
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isAscending by viewModel.isAscending.collectAsStateWithLifecycle()
    val availableTabs by viewModel.availableTabs.collectAsStateWithLifecycle()
    val displayList by viewModel.displayList.collectAsStateWithLifecycle()

    AnimatedUiState(
        state = viewModel.uiState,
        loadingContent = { PersonSkeleton() },
    ) { data ->
        PersonContent(
            data = data,
            outerPadding = outerPadding,
            actions = actions,
            currentTab = currentTab ?: "",
            availableTabs = availableTabs,
            sortOrder = sortOrder,
            isAscending = isAscending,
            displayList = displayList,
            onTabSelected = viewModel::onTabSelected,
            onSortSelected = viewModel::onSortSelected,
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PersonContent(
    data: PersonDetailsData,
    outerPadding: PaddingValues,
    actions: NavigationActions,
    currentTab: String,
    availableTabs: List<String>,
    sortOrder: PersonSortOrder,
    isAscending: Boolean,
    displayList: List<PosterInfo>,
    onTabSelected: (String) -> Unit,
    onSortSelected: (PersonSortOrder) -> Unit,
) {
    val details = data.details
    val scrollState = rememberLazyGridState()

    MediaScaffold(
        details.name,
        actions.onNavigateBack,
        scrollState,
    ) { _ ->
        CustomLazyGrid(
            state = scrollState,
            contentPadding = PaddingValues(
                top = outerPadding.calculateTopPadding(),
                bottom = outerPadding.calculateBottomPadding() + 5.dp,
                start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
            ),
        ) {
            itemFullSpan {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .layoutEvade(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfilePicture(
                        details.profileImage,
                        modifier = Modifier.clickable {
                            details.profileImage?.let { actions.onFullscreenImage(build(it, width = 500, height = 500)) }
                        },
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
                                onClick = onTabSelected,
                                title = { it.cleanAndCapitalize() },
                                tabState = currentTab,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 3.dp)
                            .padding(horizontal = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (displayList.isNotEmpty()) {
                            SortFilterChips(
                                options = PersonSortOrder.entries,
                                selectedSort = sortOrder,
                                onSortSelected = onSortSelected,
                                isAscending = isAscending,
                                label = { it.label },
                            )
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
                key = { it.id.toString() },
            ) {
                PosterItem(it, actions, Modifier.animateItem())
            }
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
            text = job.cleanAndCapitalize(),
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
            modifier = Modifier
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .then(modifier),
        )
    }
}

fun calculatePersonAge(birthday: String?): String {
    if (birthday.isNullOrBlank()) return " - "
    return try {
        val age = Period.between(LocalDate.parse(birthday), LocalDate.now()).years
        "$age years"
    } catch (_: DateTimeParseException) {
        "Wrong date format"
    }
}
