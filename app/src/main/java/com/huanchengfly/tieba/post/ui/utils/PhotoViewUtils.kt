package com.huanchengfly.tieba.post.ui.utils

import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.models.LoadPicPageData
import com.huanchengfly.tieba.post.models.PhotoViewData
import com.huanchengfly.tieba.post.models.PicItem
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.PicContentRender
import com.huanchengfly.tieba.post.utils.ImageUtil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

fun getPhotoViewData(
    post: Post,
    picId: String,
    picUrl: String,
    originUrl: String,
    showOriginBtn: Boolean,
    originSize: Int,
    seeLz: Boolean = false
): PhotoViewData? {
    if (post.from_forum == null) return null
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = post.from_forum.id,
            forumName = post.from_forum.name,
            threadId = post.tid,
            postId = post.id,
            objType = "pb",
            picId = picId,
            picIndex = 1,
            seeLz = seeLz,
            originUrl = originUrl,
        ),
        picItems = persistentListOf(
            PicItem(
                picId = picId,
                picIndex = 1,
                url = picUrl,
                originUrl = originUrl,
                showOriginBtn = showOriginBtn,
                originSize = originSize,
                postId = post.id
            )
        ),
        threadId = post.tid,
        forumId = post.from_forum.id,
    )
}

// 楼中楼图片不走 picPage 接口（接口不收录楼中楼图片，且 from_forum 可能为空），
// 直接用已有图片列表本地浏览
fun getLocalPhotoViewData(
    images: List<PicContentRender>,
    index: Int,
    postId: Long? = null,
    threadId: Long? = null,
    forumId: Long? = null,
): PhotoViewData {
    return PhotoViewData(
        data = null,
        picItems = images.mapIndexed { imageIndex, image ->
            // 楼中楼图片的 originSrc 常为空，而图片查看器只按 originUrl 加载
            val originUrl = image.originUrl.ifBlank { image.picUrl }
            PicItem(
                picId = image.picId.ifBlank { ImageUtil.getPicId(originUrl) },
                picIndex = imageIndex + 1,
                url = image.picUrl.ifBlank { originUrl },
                originUrl = originUrl,
                showOriginBtn = image.showOriginBtn,
                originSize = image.originSize,
                postId = postId
            )
        }.toImmutableList(),
        index = index,
        threadId = threadId,
        forumId = forumId,
    )
}

fun List<PbContentRender>.withLocalPhotoViewData(
    postId: Long? = null,
    threadId: Long? = null,
    forumId: Long? = null,
): ImmutableList<PbContentRender> {
    val pics = filterIsInstance<PicContentRender>()
    if (pics.isEmpty() || pics.all { it.photoViewData != null }) return this.toImmutableList()
    var picIndex = 0
    return map { render ->
        if (render is PicContentRender) {
            val index = picIndex++
            if (render.photoViewData == null) {
                render.copy(
                    photoViewData = getLocalPhotoViewData(pics, index, postId, threadId, forumId)
                )
            } else render
        } else render
    }.toImmutableList()
}

fun getPhotoViewData(
    threadInfo: ThreadInfo,
    index: Int
): PhotoViewData {
    return getPhotoViewData(
        medias = threadInfo.media,
        forumId = threadInfo.forumId,
        forumName = threadInfo.forumName,
        threadId = threadInfo.threadId,
        index = index
    )
}

/**
 * 卡片点图开关开启时使用：直接用贴内携带的全部图片本地浏览，不走 picPage 接口。
 * objType 为 "index" 时 picPage 只返回部分图，本地列表能完整展示贴内图片。
 */
fun getLocalPhotoViewData(
    medias: List<Media>,
    forumId: Long,
    threadId: Long,
    index: Int
): PhotoViewData {
    return PhotoViewData(
        data = null,
        picItems = medias.mapIndexed { mediaIndex, media ->
            PicItem(
                picId = ImageUtil.getPicId(media.originPic),
                picIndex = mediaIndex + 1,
                url = media.bigPic,
                originUrl = media.originPic,
                showOriginBtn = media.showOriginalBtn == 1,
                originSize = media.originSize,
                postId = media.postId
            )
        }.toImmutableList(),
        index = index,
        threadId = threadId,
        forumId = forumId,
    )
}

fun getPhotoViewData(
    medias: List<Media>,
    forumId: Long,
    forumName: String,
    threadId: Long,
    index: Int
): PhotoViewData {
    val media = medias[index]
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = forumId,
            forumName = forumName,
            threadId = threadId,
            postId = media.postId,
            seeLz = false,
            objType = "index",
            picId = ImageUtil.getPicId(media.originPic),
            picIndex = index + 1,
            originUrl = media.originPic
        ),
        picItems = medias.mapIndexed { mediaIndex, mediaItem ->
            PicItem(
                picId = ImageUtil.getPicId(mediaItem.originPic),
                picIndex = mediaIndex + 1,
                url = mediaItem.bigPic,
                originUrl = mediaItem.originPic,
                showOriginBtn = mediaItem.showOriginalBtn == 1,
                originSize = mediaItem.originSize,
                postId = mediaItem.postId
            )
        }.toImmutableList(),
        index = index,
        threadId = threadId,
        forumId = forumId,
    )
}