# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Icy Fishing Winter — a Compose-based Android fishing game with level progression, scoring, and audio. Single `:app` module, package `dj.music.mixer.sound.effe`.

## Build Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build (R8 minification enabled)
./gradlew bundleRelease          # AAB for Play Store
./gradlew test                   # Unit tests (JUnit 4)
./gradlew connectedAndroidTest   # Instrumented tests (Espresso + Compose UI tests)
./gradlew clean                  # Clean build
```

Minification is enabled for **both** debug and release builds. A custom `removeProguardMap` task strips mapping files from AAB bundles.

## Build Targets

- compileSdk/targetSdk: 36, minSdk: 28
- Kotlin 2.3.20, Java 11 compatibility
- Compose BOM 2026.03.01, Material 3

## Architecture

No formal MVVM/MVI — uses composable function composition with parameter-based dependency passing.

**Two entry points:**
- `LoadingActivity` (launcher) — splash screen, network check, then navigates to `MainActivity`
- `MainActivity` — hosts main navigation graph, manages `SoundManager` lifecycle

**Key packages under `dj.music.mixer.sound.effe`:**

| Package | Purpose |
|---------|---------|
| `screens/` | 9 stateless Compose screen functions (Menu, Game, Levels, Settings, Leaderboard, HowToPlay, PrivacyPolicy, Loading, Connect) |
| `navigation/` | `MainNav` and `LoadingNav` navigation graphs, `Routes` constants |
| `storage/` | `GamePreferences` — SharedPreferences wrapper (`icce_fishing_prefs`) for level progress, scores, audio toggles |
| `audio/` | `SoundManager` — MediaPlayer-based background music and SFX with lifecycle-aware pause/resume |
| `ui/componets/` | Reusable Compose components (`MenuButton`, `SquareButton`, custom modifiers) |
| `ui/theme/` | Material 3 theme, colors, typography with custom game font |

**Note:** The `ui/componets/` directory is intentionally spelled that way (not "components").

## Key Integrations

- **Firebase**: Analytics, Crashlytics, Cloud Messaging (configured via `google-services.json`)
- **AdMob**: Google Mobile Ads (`play-services-ads`)
- **Splash Screen**: AndroidX Core Splashscreen API

## Dependencies

Version catalog at `gradle/libs.versions.toml`. Key versions managed there — update versions in the catalog, not in `build.gradle.kts`.
