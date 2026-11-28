# Liquid Glass implementation audit

This is a work-in-progress checklist, not a visual approval or a claim of complete coverage.

## Renderer and sources

- [ ] **Main-window surfaces:** `QweThemeGlassBackdrop` still uses `QweBackdropSource` containing the wallpaper only. Scrolling page content is not yet captured behind same-window controls. Do not substitute a tint for this requirement.
- [ ] **Separate-window dialogs/popups:** activity-root sampling and dynamic updates have been implemented; compile and run `LiquidGlassBackdropTest.dialogGlassSamplesAndUpdatesActivityForeground` on a supported ARM device. Check nested glass, secure windows, disposal, performance, and scrolling.
- [ ] Verify CPU fallback, Android 12/13+, unsupported ABIs, low-RAM/light mode and renderer failure. Ensure opaque, readable fallbacks, no self-capture, and no input interception.

## Screen frame and layout

- [ ] `GlassScreenFrame` and viewport helpers are in `shared/.../ui/components/GlassScreenFrame.kt`. Their use is partial, not yet a migration of all screens.
- [ ] Recheck Home, Library, Profile and Streak top/status-bar insets and clipping on phones, cutouts, large fonts and landscape; verify the reported overlaps while scrolling.
- [ ] Migrate/audit Search, History, Statistics, Recap, Artist, queue and all settings/detail pages against the shared frame contract. Playlist, Album, automatic playlists, Login, About and settings use viewport helpers but have not been visually validated.
- [ ] Preserve TV focus, accessibility, mini-player/navigation insets, pull-to-refresh, selection bars and non-glass behavior.

## Appearance and behavior

- [ ] Audit every Android route, plus shared components actually used by Android, for opaque cards, bars, chips, switches, sliders, lists, actions, menus, dialogs and sheets. Material calls may be intentional fallbacks or media/safety controls; check context before replacing them.
- [ ] Compare Home chips with Search/Library through `LiquidGlassChip`; selected state must be accent-tinted, with gradient edge/highlight. Verify light and dark contrast with and without user wallpaper.
- [ ] Verify Streak calendar, level colors and seven Canvas flame variants; preserve the flame-level palette. Verify the existing vinyl player changes and music/streak image sharing on device.
- [ ] Treat crash recovery and onboarding separately: their contexts may lack the normal theme renderer. Preserve screenshot protection and warnings for token login; keep destructive actions distinguishable.

## Verification and release

- [ ] Compile the full current branch and run relevant unit/instrumentation tests. Formatting and resource scripts are not compilation.
- [ ] Run the QWE probe and perform visual/interaction checks on a real Android device, both themes and fallback paths. No device validation has been done for these changes.
- [ ] Only after implementation is ready, use authorized ephemeral GitHub credentials for CI/build, never save them, and provide the resulting artifact and exact tested commit.
