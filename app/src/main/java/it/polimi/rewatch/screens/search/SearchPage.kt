package it.polimi.rewatch.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.PosterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    viewModel: SearchViewModel = viewModel(),
    actions: NavigationActions,
    outerPadding: PaddingValues,
) {
    val query by viewModel.query.collectAsState()
    val movies by viewModel.media.collectAsState()
    val focusRequester = remember { FocusRequester() }
    // Intercept the back button BEFORE the SearchBar catches it,
    // and forcefully navigate back out of the page.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = outerPadding.calculateBottomPadding()),
        ) {
            CustomExpandedSearchBar(
                placeholder = "Search movies...",
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onClearQuery = { viewModel.onQueryChange("") },
                onTabChange = viewModel::onTabChanged,
                focusRequester = focusRequester,
            )
            // This is the 'content' slot
            val focusManager = LocalFocusManager.current
            val listState = rememberLazyListState()

            LaunchedEffect(listState.isScrollInProgress) {
                if (listState.isScrollInProgress) {
                    focusManager.clearFocus()
                }
            }
            // 2. The Results Area
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                state = listState,
            ) {
                items(movies) {
                    ListItem(
                        headlineContent = {
                            Text(it.title.orEmpty() + it.releaseDate?.let { ", ${it.year}" }.orEmpty())
                        },
                        leadingContent = {
                            PosterItem(
                                it, actions.onNavigateToMedia, modifier = Modifier.size(60.dp, 90.dp),
                            )
                        },
                        modifier = Modifier.clickable {
                            // 2. Trigger callback with the movie ID
                            focusManager.clearFocus()
                            actions.onNavigateToMedia(it.id.toString(), it.type)
                        },
                    )

                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExpandedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    placeholder: String = "Search movies...",
    onTabChange: (SearchType) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = SearchBarDefaults.TonalElevation, // Gives it that slight M3 tinted look
    ) {
        Column(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        },
                    )
                },
        ) {
            // 1. The Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp) // Standard M3 SearchBar height
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        //  .height(50.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text(placeholder) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                )

                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Clear, contentDescription = "Clear search",
                        )
                    }
                }
            }
            val availableTabs = SearchType.entries
            var tabState by remember { mutableStateOf(availableTabs.getOrNull(0)) }

            CustomTabRow(
                availableTabs = availableTabs,
                onClick = {
                    tabState = it
                    onTabChange(it)
                },
                title = { it.title },
                tabState = tabState,
            )
        }
    }
}
enum class SearchType(val title: String) {
    MOVIES("Films"), TV("Tv"), CAST("Cast, Crew, Studios"),
}