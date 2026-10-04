package com.huanchengfly.tieba.post.ui.page.settings.block.dislike

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.DislikeForumInfo
import com.huanchengfly.tieba.post.api.models.protos.getDislikeList.GetDislikeListResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

class DislikeForumsViewModel :
    BaseViewModel<DislikeForumsUiIntent, DislikeForumsPartialChange, DislikeForumsUiState, DislikeForumsUiEvent>() {
    override fun createInitialState(): DislikeForumsUiState = DislikeForumsUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<DislikeForumsUiIntent, DislikeForumsPartialChange, DislikeForumsUiState> =
        DislikeForumsPartialChangeProducer

    override fun dispatchEvent(partialChange: DislikeForumsPartialChange): UiEvent? =
        when (partialChange) {
            is DislikeForumsPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is DislikeForumsPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is DislikeForumsPartialChange.Undislike.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is DislikeForumsPartialChange.Undislike.Success -> DislikeForumsUiEvent.UndislikeSuccess
            else -> null
        }

    private object DislikeForumsPartialChangeProducer :
        PartialChangeProducer<DislikeForumsUiIntent, DislikeForumsPartialChange, DislikeForumsUiState> {
        private const val MAX_PAGES_PER_LOAD_ALL = 50

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<DislikeForumsUiIntent>): Flow<DislikeForumsPartialChange> =
            merge(
                intentFlow.filterIsInstance<DislikeForumsUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<DislikeForumsUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<DislikeForumsUiIntent.LoadAll>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<DislikeForumsUiIntent.Undislike>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<DislikeForumsPartialChange.Refresh> =
            TiebaApi.getInstance()
                .getDislikeListFlow(page = 1)
                .map<GetDislikeListResponse, DislikeForumsPartialChange.Refresh> { response ->
                    DislikeForumsPartialChange.Refresh.Success(
                        data = response.data_?.forum_list ?: emptyList(),
                        hasMore = response.data_?.has_more == 1,
                    )
                }
                .onStart { emit(DislikeForumsPartialChange.Refresh.Start) }
                .catch { emit(DislikeForumsPartialChange.Refresh.Failure(it)) }

        private fun DislikeForumsUiIntent.LoadMore.producePartialChange(): Flow<DislikeForumsPartialChange.LoadMore> =
            TiebaApi.getInstance()
                .getDislikeListFlow(page = page)
                .map<GetDislikeListResponse, DislikeForumsPartialChange.LoadMore> { response ->
                    DislikeForumsPartialChange.LoadMore.Success(
                        currentPage = page,
                        data = response.data_?.forum_list ?: emptyList(),
                        hasMore = response.data_?.has_more == 1,
                    )
                }
                .onStart { emit(DislikeForumsPartialChange.LoadMore.Start) }
                .catch { emit(DislikeForumsPartialChange.LoadMore.Failure(it)) }

        /**
         * 依次拉取剩余页面，用于让搜索覆盖全部屏蔽的贴吧
         *
         * @param fromPage 已经加载到的页码
         */
        private fun DislikeForumsUiIntent.LoadAll.producePartialChange(): Flow<DislikeForumsPartialChange.LoadMore> =
            flow {
                var page = fromPage
                var loadedPages = 0
                while (loadedPages < MAX_PAGES_PER_LOAD_ALL) {
                    emit(DislikeForumsPartialChange.LoadMore.Start)
                    val response = runCatching {
                        TiebaApi.getInstance().getDislikeListFlow(page = page + 1).first()
                    }.getOrElse {
                        emit(DislikeForumsPartialChange.LoadMore.Failure(it))
                        return@flow
                    }
                    val data = response.data_?.forum_list.orEmpty()
                    val hasMore = response.data_?.has_more == 1 && data.isNotEmpty()
                    page += 1
                    loadedPages += 1
                    emit(
                        DislikeForumsPartialChange.LoadMore.Success(
                            currentPage = page,
                            data = data,
                            hasMore = hasMore,
                        )
                    )
                    if (!hasMore) break
                }
            }

        private fun DislikeForumsUiIntent.Undislike.producePartialChange(): Flow<DislikeForumsPartialChange.Undislike> =
            TiebaApi.getInstance()
                .undislikeForumFlow(forumId)
                .map<CommonResponse, DislikeForumsPartialChange.Undislike> {
                    DislikeForumsPartialChange.Undislike.Success(forumId)
                }
                .onStart { emit(DislikeForumsPartialChange.Undislike.Start(forumId)) }
                .catch { emit(DislikeForumsPartialChange.Undislike.Failure(forumId, it)) }
    }
}

sealed interface DislikeForumsUiIntent : UiIntent {
    data object Refresh : DislikeForumsUiIntent

    data class LoadMore(val page: Int) : DislikeForumsUiIntent

    data class LoadAll(val fromPage: Int) : DislikeForumsUiIntent

    data class Undislike(val forumId: Long) : DislikeForumsUiIntent
}

sealed interface DislikeForumsPartialChange : PartialChange<DislikeForumsUiState> {
    sealed class Refresh : DislikeForumsPartialChange {
        override fun reduce(oldState: DislikeForumsUiState): DislikeForumsUiState =
            when (this) {
                Start -> oldState.copy(isLoading = true, isRefreshing = true)
                is Success -> oldState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    currentPage = 1,
                    hasMore = hasMore,
                    data = data.toImmutableList(),
                )

                is Failure -> oldState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : Refresh()

        data class Success(
            val data: List<DislikeForumInfo>,
            val hasMore: Boolean,
        ) : Refresh()

        data class Failure(val error: Throwable) : Refresh()
    }

    sealed class LoadMore : DislikeForumsPartialChange {
        override fun reduce(oldState: DislikeForumsUiState): DislikeForumsUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> oldState.copy(
                    isLoadingMore = false,
                    currentPage = currentPage,
                    hasMore = hasMore,
                    data = (oldState.data + data).distinctBy { it.forum_id }.toImmutableList(),
                )

                is Failure -> oldState.copy(
                    isLoadingMore = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : LoadMore()

        data class Success(
            val currentPage: Int,
            val data: List<DislikeForumInfo>,
            val hasMore: Boolean,
        ) : LoadMore()

        data class Failure(val error: Throwable) : LoadMore()
    }

    sealed class Undislike : DislikeForumsPartialChange {
        override fun reduce(oldState: DislikeForumsUiState): DislikeForumsUiState =
            when (this) {
                is Start -> oldState.copy(undislikeingIds = (oldState.undislikeingIds + forumId).toImmutableList())
                is Success -> oldState.copy(
                    undislikeingIds = (oldState.undislikeingIds - forumId).toImmutableList(),
                    data = oldState.data.filterNot { it.forum_id == forumId }.toImmutableList(),
                )

                is Failure -> oldState.copy(
                    undislikeingIds = (oldState.undislikeingIds - forumId).toImmutableList(),
                    error = error.wrapImmutable()
                )
            }

        data class Start(val forumId: Long) : Undislike()

        data class Success(val forumId: Long) : Undislike()

        data class Failure(
            val forumId: Long,
            val error: Throwable,
        ) : Undislike()
    }
}

data class DislikeForumsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val data: ImmutableList<DislikeForumInfo> = persistentListOf(),
    val undislikeingIds: ImmutableList<Long> = persistentListOf(),
) : UiState

sealed interface DislikeForumsUiEvent : UiEvent {
    data object UndislikeSuccess : DislikeForumsUiEvent
}

/**
 * 屏蔽的贴吧列表的排序方式
 *
 * @param key 存储在 DataStore 中的值
 */
enum class DislikeForumsSortType(val key: String) {
    /** 屏蔽时间，最近屏蔽的在前（服务端返回顺序） */
    TIME_DESC("time_desc"),

    /** 屏蔽时间，最早屏蔽的在前 */
    TIME_ASC("time_asc"),

    /** 贴吧名称首字母升序 */
    NAME("name"),
    ;

    companion object {
        fun fromKey(key: String?): DislikeForumsSortType =
            values().firstOrNull { it.key == key } ?: TIME_DESC
    }
}
