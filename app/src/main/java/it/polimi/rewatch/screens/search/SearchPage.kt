package it.polimi.rewatch.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.headerColor
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.model.PosterInfo

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun SearchPage(
    viewModel: SearchViewModel = viewModel(),
    actions: NavigationActions,
    outerPadding: PaddingValues,
    pagerState: PagerState,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val mediaType by viewModel.mediaType.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }
    // Tracks whether the software keyboard is currently open
    val isImeVisible = WindowInsets.isImeVisible
    // Listen for changes in keyboard visibility
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                query = query,
                mediaType = mediaType,
                onQueryChange = viewModel::onQueryChange,
                onTypeChange = viewModel::onTabChanged,
                onClearQuery = {
                    (viewModel::reset)()
                    focusRequester.requestFocus()
                },
                onClearFocus = {
                    focusManager.clearFocus()
                },
                focusRequester = focusRequester,
                modifier = Modifier.padding(
                    start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                ),
            )
        },
    ) { padding ->
        AnimatedUiState(
            searchResults,
            animationDurationMillis = 500,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = outerPadding.calculateBottomPadding(),
                    start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                )
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            loadingContent = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(60.dp))
                    CircularWavyProgressIndicator(color = Color.White)
                }
            },
        ) { results ->
            val notEmpty = results.results.isNotEmpty()
            if (notEmpty) {
                SearchResultsList(
                    results = results.results,
                    actions = actions,
                    onItemClick = { focusManager.clearFocus() },
                )
            } else {
                val titleText =
                    if (results.query.isEmpty()) "Find ${mediaType.title}"
                    else "No results"

                val subtitleText =
                    if (results.query.isEmpty()) "Search for your favorite movies, Tv shows, and people."
                    else "Nothing matches the given query. Please, try something else"

                val displayIcon =
                    if (results.query.isEmpty()) Icons.Default.SavedSearch
                    else Icons.Default.SearchOff

                EmptySearchState(
                    onSearchClick = { focusRequester.requestFocus() },
                    title = titleText,
                    subtitle = subtitleText,
                    icon = displayIcon,
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    mediaType: SearchType,
    onQueryChange: (String) -> Unit,
    onTypeChange: (SearchType) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onClearFocus: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerColor)
            .then(modifier)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Spacer(Modifier.height(5.dp))
        SearchInputField(
            query = query,
            placeholder = "Search ${mediaType.title.lowercase()}...",
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            onClearFocus = onClearFocus,
            focusRequester = focusRequester,
        )
        Spacer(Modifier.height(4.dp))

        CustomTabRow(
            availableTabs = SearchType.entries,
            tabState = mediaType,
            onClick = onTypeChange,
            title = { it.title },
            divider = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInputField(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onClearFocus: () -> Unit,
    focusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
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
                .focusRequester(focusRequester),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.labelLarge) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // Put your search execution logic here (if needed)
                    // This drops the focus and hides the keyboard
                    onClearFocus()
                },
            ),
            textStyle = MaterialTheme.typography.labelLarge,
        )

        if (query.isNotEmpty()) {
            IconButton(onClick = onClearQuery) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    onSearchClick: () -> Unit,
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(60.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(70.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSearchClick) {
            Text("Start Searching")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultsList(
    results: List<PosterInfo>,
    actions: NavigationActions,
    onItemClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(results) { item ->
            ListItem(
                headlineContent = {
                    Text(
                        text = item.title.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                supportingContent = item.releaseDate?.let { date ->
                    { Text(text = date.year.toString()) }
                },
                leadingContent = {
                    PosterItem(
                        media = item,
                        actions = actions,
                        modifier = Modifier.size(60.dp, 90.dp),
                    )
                },
                modifier = Modifier.clickable {
                    onItemClick()
                    item.id.onClick(actions)
                },
            )
            HorizontalDivider()
        }
    }
}