package com.metrolist.android.recap

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.recap_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecapEntry(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = glassCardColor(MaterialTheme.colorScheme.secondaryContainer)),
        border = glassCardBorder(),
    ) {
        Text(
            text = stringResource(Res.string.recap_title),
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.titleMedium,
            color = if (Settings.shared.glassNavigation && Settings.shared.glassCards) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
