package com.huanchengfly.tieba.post.ui.page.settings.block.dislike

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.DislikeForumInfo
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.rememberPreferenceAsMutableState
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.ClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.SearchBox
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.text.Collator
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Destination
@Composable
fun DislikeForumsPage(
    viewModel: DislikeForumsViewModel = pageViewModel<DislikeForumsUiIntent, DislikeForumsViewModel>(
        listOf(DislikeForumsUiIntent.Refresh)
    ),
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = DislikeForumsUiState::data,
        initial = persistentListOf()
    )
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = DislikeForumsUiState::isLoading,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = DislikeForumsUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = DislikeForumsUiState::hasMore,
        initial = false
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = DislikeForumsUiState::currentPage,
        initial = 1
    )
    val undislikeingIds by viewModel.uiState.collectPartialAsState(
        prop1 = DislikeForumsUiState::undislikeingIds,
        initial = persistentListOf()
    )

    var keyword by rememberSaveable { mutableStateOf("") }
    var sortKey by rememberPreferenceAsMutableState(
        key = stringPreferencesKey("dislikeForumsSort"),
        defaultValue = DislikeForumsSortType.TIME_DESC.key
    )
    val isSearching = remember(keyword) { keyword.isNotBlank() }

    val displayData: ImmutableList<DislikeForumInfo> by remember(
        data,
        keyword,
        sortKey
    ) {
        derivedStateOf {
            val keywordTrimmed = keyword.trim()
            val filtered = if (keywordTrimmed.isEmpty()) {
                data
            } else {
                data.filter { it.forum_name.contains(keywordTrimmed, ignoreCase = true) }
            }
            when (DislikeForumsSortType.fromKey(sortKey)) {
                DislikeForumsSortType.TIME_ASC -> filtered.asReversed()
                DislikeForumsSortType.NAME -> sortByName(filtered)
                DislikeForumsSortType.TIME_DESC -> filtered
            }.toImmutableList()
        }
    }

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_dislike_forums),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        }
    ) { paddingValues ->
        val snackbarHostState = LocalSnackbarHostState.current
        viewModel.onEvent<DislikeForumsUiEvent.UndislikeSuccess> {
            snackbarHostState.showSnackbar(context.getString(R.string.toast_undislike_success))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchBox(
                    keyword = keyword,
                    onKeywordChange = { keyword = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = ExtendedTheme.colors.card,
                    contentColor = ExtendedTheme.colors.text,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.hint_dislike_forums_search),
                            style = MaterialTheme.typography.body2,
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                        )
                    }
                )
                SortTypeMenu(
                    sortKey = sortKey,
                    onSortKeyChange = { sortKey = it }
                )
            }
            LoadAllHintRow(
                visible = isSearching && hasMore,
                text = stringResource(id = R.string.tip_dislike_forums_search_loaded),
                buttonText = stringResource(id = R.string.btn_load_all),
                loading = isLoadingMore,
                onButtonClick = { viewModel.send(DislikeForumsUiIntent.LoadAll(currentPage)) }
            )
            StateScreen(
                isEmpty = displayData.isEmpty(),
                isError = false,
                isLoading = isLoading,
                modifier = Modifier.fillMaxSize()
            ) {
                MyLazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(displayData, key = { it.forum_id }) { item ->
                        DislikeForumItem(
                            name = item.forum_name,
                            memberCount = item.member_count,
                            undislikeing = undislikeingIds.contains(item.forum_id),
                            onUndislikeClick = {
                                viewModel.send(DislikeForumsUiIntent.Undislike(item.forum_id))
                            }
                        )
                    }
                    if (hasMore) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            viewModel.send(
                                                DislikeForumsUiIntent.LoadMore(currentPage + 1)
                                            )
                                        }
                                    ) {
                                        Text(text = stringResource(id = R.string.btn_load_more))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sortByName(
    data: List<DislikeForumInfo>
): List<DislikeForumInfo> {
    val collator = Collator.getInstance(Locale.CHINA)
    return data.sortedWith(Comparator { a, b -> collator.compare(a.forum_name, b.forum_name) })
}

@Composable
private fun LoadAllHintRow(
    visible: Boolean,
    text: String,
    buttonText: String,
    loading: Boolean,
    onButtonClick: () -> Unit,
) {
    if (!visible) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.weight(1f)
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            TextButton(onClick = onButtonClick) {
                Text(text = buttonText)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SortTypeMenu(
    sortKey: String,
    onSortKeyChange: (String) -> Unit,
) {
    ClickMenu(
        menuContent = {
            DislikeForumsSortType.values().forEach { sortType ->
                DropdownMenuItem(
                    onClick = {
                        onSortKeyChange(sortType.key)
                        dismiss()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sortTypeTitle(sortType),
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.weight(1f)
                        )
                        if (sortType.key == sortKey) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = ExtendedTheme.colors.card),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = stringResource(id = R.string.title_dislike_forums_sort),
                modifier = Modifier.size(Sizes.Small)
            )
        }
    }
}

@Composable
private fun sortTypeTitle(sortType: DislikeForumsSortType): String = stringResource(
    id = when (sortType) {
        DislikeForumsSortType.TIME_DESC -> R.string.title_dislike_sort_time_desc
        DislikeForumsSortType.TIME_ASC -> R.string.title_dislike_sort_time_asc
        DislikeForumsSortType.NAME -> R.string.title_dislike_sort_name
    }
)

@Composable
private fun DislikeForumItem(
    name: String,
    memberCount: Int,
    undislikeing: Boolean,
    onUndislikeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Block,
            contentDescription = null,
            modifier = Modifier.size(Sizes.Small)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.subtitle1)
            Text(
                text = stringResource(id = R.string.tip_forum_member_count, memberCount),
                style = MaterialTheme.typography.caption
            )
        }
        if (undislikeing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            TextButton(onClick = onUndislikeClick) {
                Text(text = stringResource(id = R.string.title_undislike_forum))
            }
        }
    }
}
