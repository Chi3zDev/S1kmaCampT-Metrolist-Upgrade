package com.metrolist.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.metrolist.android.LocalMiniPlayerBottomInset
import com.metrolist.android.ui.component.tabContentTopPadding
import com.metrolist.shared.BuildConfig
import com.metrolist.shared.auth.AuthManager
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.account_login_with_token
import com.metrolist.shared.generated.resources.account_logout_confirm
import com.metrolist.shared.generated.resources.account_menu_license
import com.metrolist.shared.generated.resources.account_menu_title
import com.metrolist.shared.generated.resources.account_sign_in
import com.metrolist.shared.generated.resources.action_logout
import com.metrolist.shared.generated.resources.cancel
import com.metrolist.shared.generated.resources.glass_profile_signed_out
import com.metrolist.shared.generated.resources.integrations
import com.metrolist.shared.generated.resources.menu_history
import com.metrolist.shared.generated.resources.menu_switch_account
import com.metrolist.shared.generated.resources.settings
import com.metrolist.shared.generated.resources.statistics
import com.metrolist.shared.generated.resources.streak_page_title
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.images.AsyncImage
import com.metrolist.shared.ui.icons.MetrolistIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(
    authManager: AuthManager,
    onLogin: () -> Unit,
    onTokenLogin: () -> Unit,
    onSwitchAccount: () -> Unit,
    onLogout: () -> Unit,
    onHistory: () -> Unit,
    onStatistics: () -> Unit,
    onStreak: () -> Unit,
    onIntegrations: () -> Unit,
    onSettings: () -> Unit,
) {
    val glass = Settings.shared.glassNavigation && Settings.shared.glassProfile
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    val tintMultiplier =
        when (Settings.shared.glassStyle) {
            "FROSTED" -> 0.9f
            "CLEAR" -> 0.45f
            else -> 0.7f
        }
    val color =
        if (glass) {
            MaterialTheme.colorScheme.surfaceContainer.copy(
                alpha =
                    (
                        Settings.shared.glassTintStrength / 100f *
                            tintMultiplier
                    ).coerceIn(0.22f, 0.9f),
            )
        } else {
            surface
        }
    val high = MaterialTheme.colorScheme.onSurface.copy(alpha = if (Settings.shared.glassStyle == "CLEAR") 0.38f else 0.25f)
    val low = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
    val border = remember(high, low, glass) { if (glass) BorderStroke(1.dp, Brush.verticalGradient(listOf(high, low))) else null }
    val shape = RoundedCornerShape(24.dp)
    var confirmLogout by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = tabContentTopPadding() + 16.dp,
                bottom =
                    LocalMiniPlayerBottomInset.current + 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                border = border,
                shape = shape,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (authManager.accountThumbnail.isNotBlank()) {
                            AsyncImage(
                                url = authManager.accountThumbnail,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(CircleShape),
                            )
                        } else {
                            Icon(MetrolistIcons.Outlined.AccountCircle, contentDescription = null, modifier = Modifier.size(56.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (authManager.isLoggedIn) {
                                    authManager.accountName
                                } else {
                                    stringResource(
                                        Res.string.glass_profile_signed_out,
                                    )
                                },
                                style = MaterialTheme.typography.titleLarge,
                            )
                            if (authManager.isLoggedIn) {
                                Text(
                                    authManager.accountChannelHandle.ifBlank {
                                        authManager.accountEmail
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = if (authManager.isLoggedIn) onSwitchAccount else onLogin, modifier = Modifier.weight(1f)) {
                            Text(stringResource(if (authManager.isLoggedIn) Res.string.menu_switch_account else Res.string.account_sign_in))
                        }
                        TextButton(
                            onClick = onTokenLogin,
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(Res.string.account_login_with_token)) }
                    }
                    if (authManager.isLoggedIn) {
                        TextButton(
                            onClick = { confirmLogout = true },
                        ) { Text(stringResource(Res.string.action_logout)) }
                    }
                }
            }
        }
        item { ProfileRow(stringResource(Res.string.menu_history), onHistory, color, border) }
        item { ProfileRow(stringResource(Res.string.statistics), onStatistics, color, border) }
        item { ProfileRow(stringResource(Res.string.streak_page_title), onStreak, color, border) }
        item { ProfileRow(stringResource(Res.string.integrations), onIntegrations, color, border) }
        item { ProfileRow(stringResource(Res.string.settings), onSettings, color, border) }
        item {
            Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.account_menu_title, BuildConfig.APP_VERSION), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(Res.string.account_menu_license), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(Res.string.action_logout)) },
            text = { Text(stringResource(Res.string.account_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onLogout()
                }) { Text(stringResource(Res.string.action_logout)) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun ProfileRow(
    title: String,
    onClick: () -> Unit,
    color: Color,
    border: BorderStroke?,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
        border = border,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(title, modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.titleMedium)
    }
}
