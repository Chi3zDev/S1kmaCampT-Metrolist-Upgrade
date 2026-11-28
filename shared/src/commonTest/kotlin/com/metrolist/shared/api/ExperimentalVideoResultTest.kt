package com.metrolist.shared.api

import com.metrolist.shared.model.MUSIC_VIDEO_TYPE_LIVE
import com.metrolist.shared.model.SongItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExperimentalVideoResultTest {
    @Test
    fun parsesVideoChannelAndLiveWithoutConfusingNestedObjects() {
        val response =
            Json.parseToJsonElement(
                """
                {"contents":[
                  {"videoRenderer":{"videoId":"video1","title":{"runs":[{"text":"Live music"}]},
                    "ownerText":{"runs":[{"text":"Example", "navigationEndpoint":{"browseEndpoint":{"browseId":"UC1"}}}]},
                    "thumbnail":{"thumbnails":[{"url":"https://example.com/a"},{"url":"https://example.com/b"}]},
                    "badges":[{"metadataBadgeRenderer":{"style":"BADGE_STYLE_TYPE_LIVE_NOW"}}]}},
                  {"channelRenderer":{"channelId":"UC1","title":{"simpleText":"Example"},"thumbnail":{"thumbnails":[]}}},
                  {"videoRenderer":{"videoId":"video1","title":{"simpleText":"Duplicate"}}},
                  {"videoRenderer":{"videoId":"missing-title"}}
                ]}
                """.trimIndent(),
            )
        val results = parseExperimentalVideoSearch(response)
        assertEquals(2, results.size)
        assertEquals("Live music", results[0].title)
        assertEquals("https://example.com/b", results[0].thumbnail)
        assertEquals("UC1", results[0].channelId)
        assertTrue(results[0].isLive)
        assertEquals(MUSIC_VIDEO_TYPE_LIVE, results[0].toSong().musicVideoType)
        assertTrue(results[0].toSong().isGeneralVideo)
        assertTrue(results[1].isChannel)
        assertFalse(results[1].isLive)
    }

    @Test
    fun videoClassificationSurvivesQueueSerializationAndLegacySongsDefaultToMusic() {
        val song = ExperimentalVideoResult("video", "Title", null, null, null, false, false).toSong()
        val restored = Json.decodeFromString<SongItem>(Json.encodeToString(song))
        assertTrue(restored.isGeneralVideo)
        val legacy = Json.decodeFromString<SongItem>("""{"id":"music","title":"Music","thumbnail":null,"artists":[],"album":null}""")
        assertFalse(legacy.isGeneralVideo)
    }

    @Test
    fun emptyAndUnrelatedResponseHaveNoResults() {
        assertTrue(parseExperimentalVideoSearch(Json.parseToJsonElement("{\"contents\":[]}")).isEmpty())
    }
}
