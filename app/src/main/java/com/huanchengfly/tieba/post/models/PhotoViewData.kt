package com.huanchengfly.tieba.post.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@Parcelize
data class PhotoViewData(
    val data: LoadPicPageData? = null,
    val picItems: List<PicItem> = persistentListOf(),
    val index: Int = 0,
    // 图片所在贴，供图片查看器跳回楼层使用；data 为 null 的本地浏览也需要
    val threadId: Long? = null,
    val forumId: Long? = null,
) : Parcelable

@Immutable
@Serializable
@Parcelize
data class PicItem(
    val picId: String,
    val picIndex: Int,
    val url: String,
    val originUrl: String,
    val showOriginBtn: Boolean,
    val originSize: Int,
    val postId: Long? = null,
) : Parcelable

@Immutable
@Serializable
@Parcelize
data class LoadPicPageData(
    val forumId: Long,
    val forumName: String,
    val seeLz: Boolean,
    val objType: String,
    val picId: String,
    val picIndex: Int,
    val threadId: Long,
    val postId: Long,
    val originUrl: String?,
) : Parcelable