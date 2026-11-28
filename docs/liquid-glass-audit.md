# Liquid Glass implementation audit

This is a work-in-progress checklist, not a visual approval or a claim of complete coverage.

## Renderer and sources

- [ ] **Main-window surfaces:** app chrome and the player now request a separate live page source, with a debug probe for a page containing its own glass. Page-local controls still use the wallpaper source to avoid self-capture. The live-source integration and probe have not been built or run; same-window page controls still need a safe moving-content solution. Do not substitute a tint for this requirement.
- [ ] Validate the separate page ComposeView with navigation/shared transitions, keyboard and focus, scroll, accessibility, lifecycle/recomposition, AndroidView rendering and performance before relying on it.
- [ ] Player-close flash: the scaffold now keeps live glass sources drawable and keeps Glasmio's source attached while a player covers it, rather than hiding/recreating the source at transition boundaries. Verify opening/closing repeatedly, background/foreground and PiP, QWE/Glasmio/Haze, both themes, without artwork/wallpaper, and measure GPU/battery cost on-device; this has not been built or visually validated.
- [ ] `LiquidGlassAncestorProbeActivity` deliberately bypasses the production ancestor guard in debug only. Run `upstreamAncestorProbeUpdatesWithoutReenteringCompose` on a supported ARM device to investigate live same-page capture, then additionally test overlapping controls, z-order, multiple simultaneous glasses, clipping and frame cost. A passing color/stripe probe alone does not establish correct z-order or safety. Production must retain its guard until those checks support a safe approach.
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
