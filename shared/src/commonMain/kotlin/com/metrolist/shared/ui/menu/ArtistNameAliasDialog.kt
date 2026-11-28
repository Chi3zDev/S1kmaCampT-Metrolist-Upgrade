@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.artist_name
import com.metrolist.shared.generated.resources.cancel
import com.metrolist.shared.generated.resources.clear_artist_alias
import com.metrolist.shared.generated.resources.edit_artist
import com.metrolist.shared.generated.resources.save
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.settings.ArtistNameAliases
import com.metrolist.shared.settings.displayTitle
import com.metrolist.shared.ui.components.AlertDialog
import com.metrolist.shared.ui.components.LiquidGlassTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistNameAliasDialog(
    artist: ArtistItem,
    onDismiss: () -> Unit,
) {
    var name by remember(artist.id) { mutableStateOf(artist.displayTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.edit_artist)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.artist_name)) },
                    singleLine = true,
                )
                LiquidGlassTextButton(
                    onClick = {
                        ArtistNameAliases.clear(artist.id, artist.channelId, artist.title)
                        onDismiss()
                    },
                ) { Text(stringResource(Res.string.clear_artist_alias)) }
            }
        },
        confirmButton = {
            LiquidGlassTextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    ArtistNameAliases.set(artist.id, artist.channelId, artist.displayTitle, name.trim())
                    onDismiss()
                },
            ) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = { LiquidGlassTextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}
