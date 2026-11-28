package com.metrolist.android.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.android.LocalMiniPlayerBottomInset
import com.metrolist.android.playback.LocalPlayerConnection
import com.metrolist.android.ui.component.GlassScreenFrame
import com.metrolist.android.ui.component.tabContentTopPadding
import com.metrolist.android.ui.utils.playSong
import com.metrolist.shared.api.ExperimentalVideoFilter
import com.metrolist.shared.api.ExperimentalVideoResult
import com.metrolist.shared.api.searchExperimentalVideos
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.back
import com.metrolist.shared.generated.resources.experimental_video_all
import com.metrolist.shared.generated.resources.experimental_video_channels
import com.metrolist.shared.generated.resources.experimental_video_empty
import com.metrolist.shared.generated.resources.experimental_video_error
import com.metrolist.shared.generated.resources.experimental_video_hint
import com.metrolist.shared.generated.resources.experimental_video_live
import com.metrolist.shared.generated.resources.experimental_video_not_ready
import com.metrolist.shared.generated.resources.experimental_video_search
import com.metrolist.shared.generated.resources.experimental_video_title
import com.metrolist.shared.generated.resources.experimental_video_videos
import com.metrolist.shared.generated.resources.search
import com.metrolist.shared.ui.components.LiquidGlassChip
import com.metrolist.shared.ui.components.LiquidGlassCompactButton
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExperimentalVideoScreen(
    onBack: () -> Unit,
    onVideoSelected: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf("") }
    var requestNumber by remember { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf(ExperimentalVideoFilter.ALL) }
    var results by remember { mutableStateOf<List<ExperimentalVideoResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val player = LocalPlayerConnection.current
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val playerNotReady = stringResource(Res.string.experimental_video_not_ready)
    LaunchedEffect(submitted, filter, requestNumber) {
        results = emptyList()
        failed = false
        if (submitted.isNotBlank()) {
            loading = true
            try {
                results = searchExperimentalVideos(submitted, filter)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                failed = true
            } finally {
                loading = false
            }
        }
    }
    GlassScreenFrame(
        topInset = tabContentTopPadding(),
        barHeight = 64.dp,
        topBar = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) }
                Text(
                    stringResource(Res.string.experimental_video_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
    ) { viewport ->
        Column(viewport.padding(horizontal = 16.dp)) {
            Text(stringResource(Res.string.experimental_video_hint), style = MaterialTheme.typography.bodySmall)

            fun submitSearch() {
                if (input.isNotBlank()) {
                    submitted = input.trim()
                    requestNumber++
                    keyboard?.hide()
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(stringResource(Res.string.experimental_video_search)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f),
                )
                LiquidGlassCompactButton(onClick = { submitSearch() }, enabled = input.isNotBlank()) {
                    Text(stringResource(Res.string.search))
                }
            }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ExperimentalVideoFilter.entries.forEach { item ->
                    val label =
                        when (item) {
                            ExperimentalVideoFilter.ALL -> Res.string.experimental_video_all
                            ExperimentalVideoFilter.VIDEOS -> Res.string.experimental_video_videos
                            ExperimentalVideoFilter.CHANNELS -> Res.string.experimental_video_channels
                            ExperimentalVideoFilter.LIVE -> Res.string.experimental_video_live
                        }
                    LiquidGlassChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(stringResource(label)) },
                    )
                }
            }
            if (loading) CircularProgressIndicator(Modifier.size(32.dp))
            if (failed) Text(stringResource(Res.string.experimental_video_error))
            if (!loading && !failed && submitted.isNotBlank() &&
                results.isEmpty()
            ) {
                Text(stringResource(Res.string.experimental_video_empty))
            }
            LazyColumn(contentPadding = PaddingValues(bottom = LocalMiniPlayerBottomInset.current + 16.dp)) {
                items(results, key = { "${it.isChannel}:${it.id}" }) { result ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (result.isChannel) {
                                    val intent =
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri
                                                .Builder()
                                                .scheme(
                                                    "https",
                                                ).authority("www.youtube.com")
                                                .appendPath("channel")
                                                .appendPath(result.id)
                                                .build(),
                                        )
                                    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                                } else if (player == null) {
                                    Toast.makeText(context, playerNotReady, Toast.LENGTH_SHORT).show()
                                } else {
                                    onVideoSelected(result.id)
                                    playSong(result.toSong(), player, scope, startVideo = true)
                                }
                            }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AsyncImage(
                            model = result.thumbnail,
                            contentDescription = null,
                            modifier = Modifier.size(width = 120.dp, height = 68.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Column(Modifier.height(68.dp)) {
                            Text(result.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                            result.channel?.let {
                                Text(
                                    it,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            if (result.isLive) {
                                Text(
                                    stringResource(Res.string.experimental_video_live),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
