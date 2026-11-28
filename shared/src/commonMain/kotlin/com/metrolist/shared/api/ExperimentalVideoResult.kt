package com.metrolist.shared.api

import com.metrolist.innertubex.models.YouTubeClient
import com.metrolist.shared.api.YouTubeResponseParser.runText
import com.metrolist.shared.model.ArtistTiny
import com.metrolist.shared.model.MUSIC_VIDEO_TYPE_LIVE
import com.metrolist.shared.model.MUSIC_VIDEO_TYPE_VIDEO
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.state.GlobalYouTube
import io.ktor.http.Url
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Experimental WEB search, separate from YouTube Music search. Response formats may change. */
data class ExperimentalVideoResult(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val channel: String?,
    val channelId: String?,
    val isChannel: Boolean,
    val isLive: Boolean,
) {
    fun toSong(): SongItem =
        SongItem(
            id = id,
            title = title,
            thumbnail = thumbnail,
            artists = channel?.let { listOf(ArtistTiny(channelId, it)) } ?: emptyList(),
            album = null,
            isGeneralVideo = true,
            musicVideoType = if (isLive) MUSIC_VIDEO_TYPE_LIVE else MUSIC_VIDEO_TYPE_VIDEO,
        )
}

enum class ExperimentalVideoFilter(
    val params: String?,
) {
    ALL(null),
    VIDEOS("EgIQAQ=="),
    CHANNELS("EgIQAg=="),
    LIVE("EgJAAQ=="),
}

/** Only accept explicit YouTube URLs; ordinary keywords must remain search queries. */
fun experimentalVideoLinkId(input: String): String? {
    val text = input.trim()
    if (text.isEmpty() || text.any(Char::isWhitespace)) return null
    val url = runCatching { Url(if (text.contains("://")) text else "https://$text") }.getOrNull() ?: return null
    if (url.protocol.name !in setOf("http", "https")) return null
    val host = url.host.lowercase().removeSuffix(".")
    val path = url.encodedPath.trim('/')
    val id =
        when {
            host == "youtu.be" -> {
                path.takeIf { '/' !in it }
            }

            host == "youtube.com" || host.endsWith(".youtube.com") -> {
                when {
                    path == "watch" -> url.parameters["v"]
                    path.startsWith("live/") -> path.removePrefix("live/").takeIf { '/' !in it }
                    path.startsWith("embed/") -> path.removePrefix("embed/").takeIf { '/' !in it }
                    else -> null
                }
            }

            else -> {
                null
            }
        }
    return id?.takeIf { it.matches(Regex("^[A-Za-z0-9_-]{11}$")) }
}

fun looksLikeVideoLink(input: String): Boolean {
    val text = input.trim()
    if (text.any(Char::isWhitespace)) return false
    return text.contains("://") || text.startsWith("youtube.com/", true) ||
        text.startsWith("www.youtube.com/", true) || text.startsWith("m.youtube.com/", true) ||
        text.startsWith("music.youtube.com/", true) || text.startsWith("youtu.be/", true)
}

suspend fun searchExperimentalVideos(
    query: String,
    filter: ExperimentalVideoFilter,
): List<ExperimentalVideoResult> {
    val response = GlobalYouTube.innerTube.search(YouTubeClient.WEB, query = query, params = filter.params, setLogin = false)
    check(response.status.isSuccess()) { "Video search HTTP ${response.status.value}" }
    return parseExperimentalVideoSearch(response.bodyLimited<JsonObject>(FEED_RESPONSE_LIMIT))
}

internal fun parseExperimentalVideoSearch(root: JsonElement): List<ExperimentalVideoResult> {
    fun JsonElement?.obj() = this as? JsonObject

    fun JsonElement?.str() = (this as? JsonPrimitive)?.contentOrNull

    fun JsonObject.thumbnail(): String? = ((this["thumbnail"].obj()?.get("thumbnails") as? JsonArray)?.lastOrNull().obj()?.get("url")).str()

    fun JsonObject.channelId(): String? =
        (
            (
                this["navigationEndpoint"]
                    .obj()
                    ?.get("browseEndpoint")
                    .obj()
                    ?.get("browseId")
            )
        ).str()

    fun JsonObject.hasLiveBadge(): Boolean {
        val badges = (this["badges"] as? JsonArray).orEmpty()
        return badges.any { badge ->
            val renderer = badge.obj()?.get("metadataBadgeRenderer").obj()
            renderer?.get("style").str() == "BADGE_STYLE_TYPE_LIVE_NOW"
        }
    }
    val results = mutableListOf<ExperimentalVideoResult>()

    fun visit(node: JsonElement) {
        when (node) {
            is JsonArray -> {
                node.forEach(::visit)
            }

            is JsonObject -> {
                val video = node["videoRenderer"].obj()
                val channel = node["channelRenderer"].obj()
                if (video != null) {
                    val id = video["videoId"].str()
                    val title = video["title"].obj().runText()
                    if (!id.isNullOrBlank() && !title.isNullOrBlank()) {
                        val owner = video["ownerText"].obj() ?: video["longBylineText"].obj()
                        val ownerRun = (owner?.get("runs") as? JsonArray)?.firstOrNull().obj()
                        results +=
                            ExperimentalVideoResult(
                                id,
                                title,
                                video.thumbnail(),
                                owner.runText(),
                                ownerRun?.channelId(),
                                false,
                                video.hasLiveBadge(),
                            )
                    }
                } else if (channel != null) {
                    val id = channel["channelId"].str()
                    val title = channel["title"].obj().runText()
                    if (!id.isNullOrBlank() && !title.isNullOrBlank()) {
                        results += ExperimentalVideoResult(id, title, channel.thumbnail(), null, id, true, false)
                    }
                } else {
                    node.values.forEach(::visit)
                }
            }

            else -> {
                Unit
            }
        }
    }
    visit(root)
    return results.distinctBy { it.isChannel to it.id }
}
