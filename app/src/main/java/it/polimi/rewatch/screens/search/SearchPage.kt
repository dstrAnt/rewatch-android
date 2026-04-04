package it.polimi.rewatch.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.capitalize
import it.polimi.rewatch.common.headerColor
import it.polimi.rewatch.components.PosterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    viewModel: SearchViewModel = viewModel(),
    actions: NavigationActions,
    outerPadding: PaddingValues,
    pagerState: PagerState,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val movies by viewModel.media.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val type = viewModel.mediaType.collectAsStateWithLifecycle().value
    val string = when (type) {
        SearchType.ALL -> "anything"
        SearchType.MOVIES -> "movies"
        SearchType.TV -> "tv shows"
        SearchType.CAST -> "people"
    }
    // Intercept the back button BEFORE the SearchBar catches it,
    // and forcefully navigate back out of the page.
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
    // 1. Clear focus when the Pager starts moving
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.reset()
        }
    }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }
    Scaffold(
        topBar = {
            SearchTopBar(
                string, query, viewModel, focusRequester,
                modifier = Modifier.padding(
                    start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                ),
            )
        },
    ) { padding ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = outerPadding.calculateBottomPadding(),
                        start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                focusManager.clearFocus()
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            // dragAmount > 0 means the user is swiping DOWN.
                            // We use a small threshold (e.g., 10f) so a tiny jitter
                            // doesn't accidentally trigger the keyboard.
                            if (dragAmount > 10f) {
                                focusRequester.requestFocus()
                            }
                        }
                    },
            ) {
                // This is the 'content' slot
                val focusManager = LocalFocusManager.current
                val listState = rememberLazyListState()

                LaunchedEffect(listState.isScrollInProgress) {
                    if (listState.isScrollInProgress) {
                        focusManager.clearFocus()
                    }
                }
                // 2. The Results Area
                if (query.isEmpty())
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 60.dp)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.SavedSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(70.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Find ${string.capitalize()}",
                            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                        )
                        Text(
                            "Type a query to start searching for $string",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { focusRequester.requestFocus() },
                        ) {
                            Text("Search")
                        }
                        Spacer(Modifier.height(50.dp))
                    }
                else
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
                                    PosterItem(it, actions, modifier = Modifier.size(60.dp, 90.dp))
                                },
                                modifier = Modifier.clickable {
                                    // 2. Trigger callback with the movie ID
                                    focusManager.clearFocus()
                                    it.id.onClick(actions)
                                },
                            )

                            HorizontalDivider()
                        }
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
        color = headerColor,
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
            Spacer(Modifier.height(5.dp))
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
//                        .height(48.dp)
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

            Spacer(Modifier.height(4.dp))
            val availableTabs = SearchType.entries
            var tabState by rememberSaveable { mutableStateOf(availableTabs.getOrNull(0)) }

            CustomTabRow(
                availableTabs = availableTabs,
                onClick = {
                    tabState = it
                    onTabChange(it)
                },
                title = { it.title },
                tabState = tabState,
                divider = {},
            )
        }
    }
}

@Composable
private fun SearchTopBar(
    string: String,
    query: String,
    viewModel: SearchViewModel,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(headerColor)
            .then(modifier)
            .windowInsetsPadding(WindowInsets.statusBars), // Keeps it below the system status bar
    ) {
        CustomExpandedSearchBar(
            placeholder = "Search $string...",
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onClearQuery = { viewModel.onQueryChange("") },
            onTabChange = viewModel::onTabChanged,
            focusRequester = focusRequester,
        )
    }
}

enum class SearchType(val title: String) {
    ALL("All"),
    MOVIES("Movies"),
    TV("Tv"),
    CAST("People"),
}