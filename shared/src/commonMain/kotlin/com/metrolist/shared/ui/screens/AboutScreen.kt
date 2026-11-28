/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.metrolist.shared.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.BuildConfig
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.about_glasmio_credit
import com.metrolist.shared.generated.resources.about_liquid_glass_android_credit
import com.metrolist.shared.generated.resources.about_nunito_credit
import com.metrolist.shared.generated.resources.about_play_favorite_song
import com.metrolist.shared.generated.resources.about_upgrade_candidate_five
import com.metrolist.shared.generated.resources.about_upgrade_candidate_four
import com.metrolist.shared.generated.resources.about_upgrade_candidate_six
import com.metrolist.shared.generated.resources.about_upgrade_candidate_three
import com.metrolist.shared.generated.resources.about_upgrade_candidate_two
import com.metrolist.shared.generated.resources.about_upgrade_developer
import com.metrolist.shared.generated.resources.about_upgrade_note_about
import com.metrolist.shared.generated.resources.about_upgrade_note_defaults
import com.metrolist.shared.generated.resources.about_upgrade_note_fonts
import com.metrolist.shared.generated.resources.about_upgrade_note_glass
import com.metrolist.shared.generated.resources.about_upgrade_note_poster
import com.metrolist.shared.generated.resources.about_upgrade_note_profile
import com.metrolist.shared.generated.resources.about_upgrade_note_recap
import com.metrolist.shared.generated.resources.about_upgrade_note_streak
import com.metrolist.shared.generated.resources.about_upgrade_two_interface
import com.metrolist.shared.generated.resources.about_upgrade_two_motion
import com.metrolist.shared.generated.resources.about_upgrade_two_player_poster
import com.metrolist.shared.generated.resources.about_upgrade_version
import com.metrolist.shared.generated.resources.about_upgrade_whats_new
import com.metrolist.shared.generated.resources.app_name
import com.metrolist.shared.generated.resources.back
import com.metrolist.shared.generated.resources.build_type_debug
import com.metrolist.shared.generated.resources.build_type_release
import com.metrolist.shared.generated.resources.chi3zdev_avatar
import com.metrolist.shared.generated.resources.devmode_already_active
import com.metrolist.shared.generated.resources.devtools_enabled
import com.metrolist.shared.generated.resources.play
import com.metrolist.shared.generated.resources.stands_with_palestine
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.GlassScreenFrame
import com.metrolist.shared.ui.components.LiquidGlassIconButton
import com.metrolist.shared.ui.components.LiquidGlassSurface
import com.metrolist.shared.ui.components.LocalThemeGlassBackdrop
import com.metrolist.shared.ui.components.appPageBackground
import com.metrolist.shared.ui.components.glassCardBorder
import com.metrolist.shared.ui.components.glassCardColor
import com.metrolist.shared.ui.components.glassScaffoldViewport
import com.metrolist.shared.ui.components.images.AsyncImage
import com.metrolist.shared.ui.components.liquidGlassBackdropReady
import com.metrolist.shared.ui.icons.MetrolistIcons
import com.metrolist.shared.ui.navigation.NavigationBackHandler
import com.metrolist.shared.ui.utils.toPolygonShape
import com.metrolist.shared.ui.utils.verticalScrollWithDrag
import com.metrolist.shared.utils.getAppArchitecture
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    logo: Painter,
    modifier: Modifier = Modifier,
    appName: String = "",
    version: String = BuildConfig.APP_VERSION,
    architecture: String = remember { getAppArchitecture().uppercase() },
    isDebug: Boolean = BuildConfig.DEBUG,
    upgradeNumber: Int? = null,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    bottomInset: Dp = 0.dp,
    onNavigateBack: (() -> Unit)? = null,
    onOpenCommunityLink: ((AboutCommunityLink) -> Unit)? = null,
    onDevtoolsUnlocked: () -> Unit = {},
    onPlayFavoriteSong: ((videoId: String) -> Unit)? = null,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val glass = liquidGlassBackdropReady()
    var logoClickCount by rememberSaveable { mutableIntStateOf(0) }
    var devtoolsUnlocked by remember { mutableStateOf(Settings.shared.devtoolsUnlocked) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val text = rememberAboutScreenText()
    val devtoolsEnabledMessage = stringResource(Res.string.devtools_enabled)
    val devmodeAlreadyActiveMessage = stringResource(Res.string.devmode_already_active)
    val playFavoriteMessage = stringResource(Res.string.about_play_favorite_song)
    val playLabel = stringResource(Res.string.play)
    val uriHandler = LocalUriHandler.current
    var selectedContributor by remember { mutableStateOf<AboutContributor?>(null) }
    var showUpgradeNotes by rememberSaveable { mutableStateOf(false) }
    if (showUpgradeNotes && upgradeNumber != null) {
        NavigationBackHandler(onBack = { showUpgradeNotes = false })
        UpgradeNotesScreen(bottomInset, upgradeNumber) { showUpgradeNotes = false }
        return
    }
    val openCommunityLink =
        onOpenCommunityLink ?: { link: AboutCommunityLink ->
            runCatching { uriHandler.openUri(link.url) }
            Unit
        }

    val onContributorLongClick: ((AboutContributor) -> Unit)? =
        onPlayFavoriteSong?.let { play ->
            { contributor ->
                contributor.favoriteSongVideoId?.let { videoId ->
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        if (snackbarHostState.showSnackbar(playFavoriteMessage, playLabel) == SnackbarResult.ActionPerformed) play(videoId)
                    }
                }
            }
        }

    fun handleLogoClick() {
        if (devtoolsUnlocked) {
            coroutineScope.launch { snackbarHostState.showSnackbar(devmodeAlreadyActiveMessage) }
        } else if (++logoClickCount >= 3) {
            logoClickCount = 0
            devtoolsUnlocked = true
            Settings.shared.devtoolsUnlocked = true
            onDevtoolsUnlocked()
            coroutineScope.launch { snackbarHostState.showSnackbar(devtoolsEnabledMessage) }
        }
    }

    Scaffold(
        modifier =
            if (onNavigateBack != null) {
                modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            } else {
                modifier
            },
        containerColor = if (onNavigateBack == null) Color.Transparent else appPageBackground(colorScheme.surface),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (onNavigateBack != null) {
                LiquidGlassSurface(radius = 0.dp, modifier = Modifier.fillMaxWidth()) {
                    LargeFlexibleTopAppBar(
                        title = { Text(text.title) },
                        navigationIcon = {
                            LiquidGlassIconButton(onClick = onNavigateBack) {
                                Icon(
                                    MetrolistIcons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(Res.string.back),
                                )
                            }
                        },
                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor =
                                    if (Settings.shared.liquidGlassTheme) {
                                        glassCardColor(
                                            colorScheme.surface,
                                        )
                                    } else {
                                        colorScheme.surface
                                    },
                                scrolledContainerColor =
                                    if (Settings.shared.liquidGlassTheme) {
                                        glassCardColor(
                                            colorScheme.surfaceContainerHigh,
                                        )
                                    } else {
                                        colorScheme.surfaceContainerHigh
                                    },
                            ),
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = bottomInset),
            )
        },
    ) { paddingValues ->
        val viewport = if (glass) Modifier.glassScaffoldViewport(paddingValues) else Modifier.fillMaxSize().padding(paddingValues)
        Column(
            modifier =
                viewport
                    .verticalScrollWithDrag(rememberScrollState())
                    .padding(contentPadding)
                    .padding(bottom = bottomInset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppHeaderCard(
                logo = logo,
                appName = appName,
                version = version,
                architecture = architecture,
                isDebug = isDebug,
                upgradeNumber = upgradeNumber,
                colorScheme = colorScheme,
                onLogoClick = ::handleLogoClick,
            )

            CommunitySection(
                text = text,
                onOpenLink = openCommunityLink,
            )

            if (upgradeNumber != null) {
                UpgradeDeveloperCard(upgradeNumber, colorScheme) {
                    runCatching { uriHandler.openUri("https://github.com/Chi3zDev") }
                }
            }

            if (upgradeNumber != null) {
                Box(Modifier.fillMaxWidth()) {
                    if (Settings.shared.liquidGlassTheme) {
                        LocalThemeGlassBackdrop.current?.Background(radius = 20.dp, modifier = Modifier.matchParentSize())
                    }
                    Surface(
                        onClick = { showUpgradeNotes = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = glassCardColor(colorScheme.surfaceContainer),
                        border = glassCardBorder(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(Res.string.about_upgrade_whats_new),
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Icon(MetrolistIcons.Outlined.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            CollaboratorsList(
                contributors = listOf(AboutContributors.lead),
                text = text,
                title = text.creatorSection,
                avatarSize = 64.dp,
                colorScheme = colorScheme,
                onContributorClick = { selectedContributor = it },
                onContributorLongClick = onContributorLongClick,
            )

            CollaboratorsList(
                contributors = AboutContributors.leads,
                text = text,
                title = text.leadsSection,
                avatarSize = 64.dp,
                colorScheme = colorScheme,
                onContributorClick = { selectedContributor = it },
                onContributorLongClick = onContributorLongClick,
            )

            CollaboratorsList(
                contributors = AboutContributors.collaborators,
                text = text,
                colorScheme = colorScheme,
                onContributorClick = { selectedContributor = it },
                onContributorLongClick = onContributorLongClick,
            )

            Text(
                text = text.licenseDescription,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.Button, onClickLabel = text.license) { openCommunityLink(aboutLicenseLink) }
                        .padding(16.dp),
            )

            if (upgradeNumber != null) {
                Text(
                    text = stringResource(Res.string.about_nunito_credit),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.about_glasmio_credit),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { uriHandler.openUri("https://github.com/NEORUAA/Glasmio") }.padding(8.dp),
                )
                Text(
                    text = stringResource(Res.string.about_liquid_glass_android_credit),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { uriHandler.openUri("https://github.com/QWEA0/Liquid-Glass-Android") }.padding(8.dp),
                )
            }

            Text(
                text = stringResource(Res.string.stands_with_palestine),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }

    selectedContributor?.let { contributor ->
        AuthorInfoSheet(
            contributor = contributor,
            colorScheme = colorScheme,
            onDismiss = { selectedContributor = null },
            onOpenUrl = { url -> runCatching { uriHandler.openUri(url) } },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun contributorShape(contributor: AboutContributor): Shape =
    when (contributor.avatarShape) {
        AboutAvatarShape.COOKIE_4_SIDED -> MaterialShapes.Cookie4Sided.toPolygonShape()
        AboutAvatarShape.COOKIE_6_SIDED -> MaterialShapes.Cookie6Sided.toPolygonShape()
        AboutAvatarShape.COOKIE_9_SIDED -> MaterialShapes.Cookie9Sided.toPolygonShape()
        AboutAvatarShape.BURST -> MaterialShapes.Cookie12Sided.toPolygonShape()
        AboutAvatarShape.PUFFY_DIAMOND -> MaterialShapes.Cookie4Sided.toPolygonShape()
        AboutAvatarShape.SUNNY -> MaterialShapes.Cookie9Sided.toPolygonShape()
        AboutAvatarShape.GEM -> MaterialShapes.Cookie12Sided.toPolygonShape()
    }

@Composable
private fun AppHeaderCard(
    logo: Painter,
    appName: String,
    version: String,
    architecture: String,
    isDebug: Boolean,
    upgradeNumber: Int?,
    colorScheme: ColorScheme,
    onLogoClick: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        if (Settings.shared.liquidGlassTheme) {
            LocalThemeGlassBackdrop.current?.Background(radius = 28.dp, modifier = Modifier.matchParentSize())
        }
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = glassCardColor(colorScheme.surfaceContainer)),
            border = glassCardBorder(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .logoStripes(logo, colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 24.dp, vertical = 28.dp),
            ) {
                Surface(
                    onClick = onLogoClick,
                    shape = RoundedCornerShape(28.dp),
                    color = colorScheme.primaryContainer,
                    modifier = Modifier.size(112.dp),
                ) {
                    Image(
                        painter = logo,
                        contentDescription = stringResource(Res.string.app_name),
                        colorFilter = ColorFilter.tint(colorScheme.onPrimaryContainer, BlendMode.SrcIn),
                        modifier = Modifier.padding(12.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = appName.ifBlank { stringResource(Res.string.app_name) },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    InfoBadge(
                        version,
                        colorScheme.surfaceContainerHighest,
                        colorScheme.onSurfaceVariant,
                    )
                    InfoBadge(architecture, colorScheme.surfaceContainerHighest, colorScheme.onSurfaceVariant)
                    if (upgradeNumber != null) {
                        InfoBadge(
                            stringResource(
                                if (isUpgradeCandidateSix(version)) {
                                    Res.string.about_upgrade_candidate_six
                                } else if (isUpgradeCandidateFive(version)) {
                                    Res.string.about_upgrade_candidate_five
                                } else if (isUpgradeCandidateFour(version)) {
                                    Res.string.about_upgrade_candidate_four
                                } else if (isUpgradeCandidateThree(version)) {
                                    Res.string.about_upgrade_candidate_three
                                } else if (isUpgradeCandidateTwo(
                                        version,
                                    )
                                ) {
                                    Res.string.about_upgrade_candidate_two
                                } else {
                                    Res.string.about_upgrade_version
                                },
                                upgradeNumber,
                            ),
                            colorScheme.secondaryContainer,
                            colorScheme.onSecondaryContainer,
                        )
                    }
                    InfoBadge(
                        stringResource(if (isDebug) Res.string.build_type_debug else Res.string.build_type_release),
                        colorScheme.secondaryContainer,
                        colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

private fun Modifier.logoStripes(
    logo: Painter,
    tint: Color,
): Modifier =
    drawWithCache {
        val iconSize = 20.dp.toPx()
        val columnSpacing = 36.dp.toPx()
        val rowSpacing = 56.dp.toPx()
        val colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn)
        val reach = size.width + size.height
        onDrawBehind {
            clipRect {
                rotate(-45f) {
                    var row = 0
                    var y = center.y - reach
                    while (y < center.y + reach) {
                        var x = center.x - reach + if (row % 2 == 0) 0f else columnSpacing / 2
                        while (x < center.x + reach) {
                            translate(x, y) { with(logo) { draw(Size(iconSize, iconSize), colorFilter = colorFilter) } }
                            x += columnSpacing
                        }
                        y += rowSpacing
                        row++
                    }
                }
            }
        }
    }

@Composable
private fun InfoBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(shape = RoundedCornerShape(20.dp), color = containerColor, contentColor = contentColor) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

// Add new version entries here; descriptions are localized resources.
private val upgradeOneNotes =
    listOf(
        Res.string.about_upgrade_note_streak,
        Res.string.about_upgrade_note_recap,
        Res.string.about_upgrade_note_poster,
        Res.string.about_upgrade_note_glass,
        Res.string.about_upgrade_note_profile,
        Res.string.about_upgrade_note_about,
        Res.string.about_upgrade_note_fonts,
        Res.string.about_upgrade_note_defaults,
    )

private val upgradeTwoNotes =
    listOf(
        Res.string.about_upgrade_two_motion,
        Res.string.about_upgrade_two_player_poster,
        Res.string.about_upgrade_two_interface,
    )

internal fun isUpgradeCandidateTwo(version: String): Boolean = version.endsWith("-upgrade.2-rc.2")

internal fun isUpgradeCandidateThree(version: String): Boolean = version.endsWith("-upgrade.2-rc.3")

internal fun upgradeNoteVersions(upgradeNumber: Int): List<Int> = if (upgradeNumber >= 2) listOf(2, 1) else listOf(1)

@Composable
private fun UpgradeNotesScreen(
    bottomInset: Dp,
    upgradeNumber: Int,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        GlassScreenFrame(topInset = 0.dp, barHeight = 64.dp, topBar = {
            LiquidGlassSurface(radius = 0.dp, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = glassCardColor(MaterialTheme.colorScheme.surface),
                    border = glassCardBorder(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LiquidGlassIconButton(onClick = onBack) {
                            Icon(MetrolistIcons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                        }
                        Text(
                            stringResource(Res.string.about_upgrade_whats_new),
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }) { viewport ->
            Column(
                modifier =
                    viewport
                        .verticalScrollWithDrag(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .padding(bottom = bottomInset),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                upgradeNoteVersions(upgradeNumber).forEach { version ->
                    Text(
                        stringResource(Res.string.about_upgrade_version, version),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    val notes = if (version == 2) upgradeTwoNotes else upgradeOneNotes
                    notes.forEach { note ->
                        LiquidGlassSurface(radius = 16.dp, modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = glassCardColor(MaterialTheme.colorScheme.surfaceContainer),
                                border = glassCardBorder(),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(note), Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeDeveloperCard(
    upgradeNumber: Int,
    colorScheme: ColorScheme,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.about_upgrade_developer),
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).semantics { heading() },
        )
        Box(Modifier.fillMaxWidth()) {
            if (Settings.shared.liquidGlassTheme) {
                LocalThemeGlassBackdrop.current?.Background(radius = 20.dp, modifier = Modifier.matchParentSize())
            }
            Surface(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = glassCardColor(colorScheme.surfaceContainer),
                border = glassCardBorder(),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.chi3zdev_avatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Chi3zDev", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(Res.string.about_upgrade_developer), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(Res.string.about_upgrade_version, upgradeNumber), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorAvatar(
    contributor: AboutContributor,
    size: Dp,
    colorScheme: ColorScheme,
) {
    val shape = CircleShape
    Surface(
        modifier = Modifier.size(size),
        shape = shape,
        color = colorScheme.surfaceContainerHighest,
        tonalElevation = 4.dp,
    ) {
        AsyncImage(
            url = contributor.avatarUrl,
            contentDescription = null,
            shape = shape,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CollaboratorsList(
    contributors: List<AboutContributor>,
    text: AboutScreenText,
    colorScheme: ColorScheme,
    onContributorClick: (AboutContributor) -> Unit,
    onContributorLongClick: ((AboutContributor) -> Unit)?,
    title: String? = text.collaborators,
    avatarSize: Dp = 48.dp,
) {
    Column(Modifier.fillMaxWidth()) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).semantics { heading() },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val columns = if (contributors.size == 1) 1 else 2
            contributors.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { contributor ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = glassCardColor(colorScheme.surfaceContainer),
                            border = glassCardBorder(),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onLongClick =
                                                onContributorLongClick
                                                    ?.takeIf { contributor.favoriteSongVideoId != null }
                                                    ?.let { { it(contributor) } },
                                            onClick = { onContributorClick(contributor) },
                                        ).padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                ContributorAvatar(contributor = contributor, size = avatarSize, colorScheme = colorScheme)
                                Text(
                                    contributor.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                )
                                Text(
                                    text.roleLabel(contributor.role),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    if (row.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

internal fun isUpgradeCandidateFour(version: String): Boolean = version.endsWith("-upgrade.2-rc.4")

internal fun isUpgradeCandidateFive(version: String): Boolean = version.endsWith("-upgrade.2-rc.5")

internal fun isUpgradeCandidateSix(version: String): Boolean = version.endsWith("-upgrade.2-rc.6")
