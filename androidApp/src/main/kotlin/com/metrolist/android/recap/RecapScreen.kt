package com.metrolist.android.recap

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.shared.db.MusicDatabase
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.back
import com.metrolist.shared.generated.resources.recap_all_filter
import com.metrolist.shared.generated.resources.recap_artists_count
import com.metrolist.shared.generated.resources.recap_empty
import com.metrolist.shared.generated.resources.recap_error
import com.metrolist.shared.generated.resources.recap_minutes
import com.metrolist.shared.generated.resources.recap_month_filter
import com.metrolist.shared.generated.resources.recap_songs_count
import com.metrolist.shared.generated.resources.recap_thirty_filter
import com.metrolist.shared.generated.resources.recap_title
import com.metrolist.shared.generated.resources.recap_top_albums
import com.metrolist.shared.generated.resources.recap_top_artists
import com.metrolist.shared.generated.resources.recap_top_songs
import com.metrolist.shared.generated.resources.recap_year
import com.metrolist.shared.generated.resources.recap_year_filter
import com.metrolist.shared.ui.icons.MetrolistIcons
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(onBack: () -> Unit) {
    val database: MusicDatabase = koinInject()
    var period by remember { mutableStateOf(RecapPeriod.YEAR) }
    var data by remember { mutableStateOf<RecapData?>(null) }
    var failed by remember { mutableStateOf(false) }
    val year = remember { Instant.now().atZone(ZoneId.systemDefault()).year }
    LaunchedEffect(database, period) {
        data = null
        failed = false
        try {
            data = database.loadRecap(period, Instant.now(), ZoneId.systemDefault())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            failed = true
        }
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(Res.string.recap_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(MetrolistIcons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                }
            },
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.recap_year, year), style = MaterialTheme.typography.headlineSmall)
                        Text((data?.minutes ?: 0).toString(), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(Res.string.recap_minutes), style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(Res.string.recap_songs_count, data?.songCount ?: 0), style = MaterialTheme.typography.labelLarge)
                            Text(stringResource(Res.string.recap_artists_count, data?.artistCount ?: 0), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecapPeriod.entries.forEach { choice ->
                        val label = when (choice) {
                            RecapPeriod.YEAR -> Res.string.recap_year_filter
                            RecapPeriod.LAST_30_DAYS -> Res.string.recap_thirty_filter
                            RecapPeriod.MONTH -> Res.string.recap_month_filter
                            RecapPeriod.ALL -> Res.string.recap_all_filter
                        }
                        FilterChip(selected = period == choice, onClick = { period = choice }, label = { Text(stringResource(label)) })
                    }
                }
            }
            if (failed) item { Text(stringResource(Res.string.recap_error), Modifier.padding(16.dp)) }
            else if (data == null) item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            else {
                val recap = data!!
                if (recap.songs.isEmpty()) item { Text(stringResource(Res.string.recap_empty), Modifier.padding(16.dp)) }
                item { RecapList(stringResource(Res.string.recap_top_songs), recap.songs.take(10).map { it.title }) }
                item { RecapList(stringResource(Res.string.recap_top_artists), recap.artists.take(10).map { it.name }) }
                item { RecapList(stringResource(Res.string.recap_top_albums), recap.albums.take(10).map { it.name }) }
            }
        }
    }
}

@Composable
private fun RecapList(title: String, names: List<String>) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        names.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
    }
}
