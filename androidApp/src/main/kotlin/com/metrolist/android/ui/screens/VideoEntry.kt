package com.metrolist.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.video_home_description
import com.metrolist.shared.generated.resources.video_home_title
import com.metrolist.shared.ui.components.LocalThemeGlassBackdrop
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.ui.icons.MetrolistIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun VideoEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        LocalThemeGlassBackdrop.current?.Background(radius = 12.dp, modifier = Modifier.matchParentSize())
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.secondaryContainer)),
            border = glassCardBorder(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(MetrolistIcons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.video_home_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(Res.string.video_home_description), style = MaterialTheme.typography.bodySmall)
                }
                Icon(MetrolistIcons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}
