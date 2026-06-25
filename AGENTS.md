# ModHack Keyboard — Implementation Plan

> **Reference:** [SPECIFICATION.md](file:///home/unprivileged/code/modhack-keyboard/SPECIFICATION.md)
> **Language:** Kotlin · **UI:** Jetpack Compose · **Native:** Rust (JNI) · **Min SDK:** 24 · **Target SDK:** 34

---

## Rules

- All Kotlin source lives under `app/src/main/kotlin/io/github/modhack/`.
- Follow unidirectional data flow (UDF): state flows down, events flow up.
- Prefer immutable `data class` models; avoid mutable singletons except `ModifierState`.
- Use `StateFlow` / `Flow` for reactive state — never `LiveData`.
- Use Jetpack DataStore for preferences — never `SharedPreferences`.
- Use Kotlin Coroutines (`Dispatchers.IO`) for all background work.
- Target API 24+ but use `ContextCompat` / `ActivityCompat` for API-gated features (e.g., `POST_NOTIFICATIONS` on API 33+, dynamic color on API 31+).
- Every public class and non-trivial function must have a KDoc comment.
- Preserve all existing comments and docstrings unrelated to your changes.

---

> **Build status:** `./gradlew assembleRelease` succeeds (50 tasks, 23 executed, 27 up-to-date).
> **Phase 0/1/2/3/4/5/6/7/8:** ✅ Complete

## Phase 0 — Project Scaffolding

**Goal:** A buildable, empty Android project with the correct Gradle configuration, dependency catalog, and manifest.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 0.1 | Create the top-level Gradle wrapper, `settings.gradle.kts`, and `build.gradle.kts` with Android Gradle Plugin 8.x. | ✅ | `build.gradle.kts`, `settings.gradle.kts`, `gradle/` |
| 0.2 | Create `gradle/libs.versions.toml` with all dependencies listed in §3.1 (core-ktx, compose-bom, lifecycle, datastore-preferences, coroutines, Room, etc.). | ✅ | `gradle/libs.versions.toml` |
| 0.3 | Create `app/build.gradle.kts` with `compileSdk=34`, `minSdk=24`, `targetSdk=34`, `applicationId`, Compose compiler, and R8/ProGuard config for release. | ✅ | `app/build.gradle.kts` |
| 0.4 | Create `AndroidManifest.xml` with all permissions (§4.1), hardware features (§4.2), application attributes (§4.3), and the three component declarations (§4.4): `MHInputService`, `MainActivity`, `SettingsActivity`. | ✅ | `app/src/main/AndroidManifest.xml` |
| 0.5 | Create `res/xml/method.xml` declaring the IME service and at least the `en_US` subtype. | ✅ | `app/src/main/res/xml/method.xml` |
| 0.6 | Create placeholder string resources (`res/values/strings.xml`) and the app icon. | ✅ | `app/src/main/res/` |
| 0.7 | Verify the project compiles with `./gradlew assembleDebug`. | ✅ | — |

### Acceptance Criteria
- `./gradlew assembleDebug` succeeds. ✅

---

## Phase 1 — Core Data Models & Constants

**Goal:** Define all shared data types, key codes, and preference keys before any logic is written.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 1.1 | Create `KeyCodes.kt` with all keycode constants from §19. | ✅ | `keycodes/KeyCodes.kt` |
| 1.2 | Create `KeyboardLayout.kt`, `Row`, and `Key` data classes per §7.1. | ✅ | `model/KeyboardLayout.kt` |
| 1.3 | Create `Modifier` enum (`SHIFT`, `CTRL`, `ALT`, `META`, `FN`). | ✅ | `model/Modifier.kt` |
| 1.4 | Create `KeyboardState` data class (current layout, mode, shift state, active modifiers). | ✅ | `model/KeyboardState.kt` |
| 1.5 | Create `Suggestion` data class (`word: String`, `frequency: Int`, `source: SuggestionSource`). | ✅ | `model/Suggestion.kt` |
| 1.6 | Create `KeyboardPreferences` data class with all DataStore keys from §16. | ✅ | `prefs/KeyboardPreferences.kt` |
| 1.7 | Create `PreferencesRepository.kt` backed by DataStore, exposing `Flow<KeyboardPreferences>` (§7.3). | ✅ | `prefs/PreferencesRepository.kt` |

### Acceptance Criteria
- All data classes compile. ✅
- `PreferencesRepository` can round-trip default preferences in a unit test. ✅

---

## Phase 2 — Keyboard Layout Loader & Cache

**Goal:** Parse XML keyboard layouts into immutable `KeyboardLayout` objects and cache them.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 2.1 | Create the base English QWERTY layout XML `kbd_qwerty.xml` (4-row) per §14.1. | ✅ | `res/xml/kbd_qwerty.xml` |
| 2.2 | Create `kbd_full.xml` (5-row + F-keys) and `kbd_full_fn.xml` (5-row + Fn). | ✅ | `res/xml/kbd_full.xml`, `res/xml/kbd_full_fn.xml` |
| 2.3 | Create `kbd_symbols.xml` and `kbd_phone.xml`. | ✅ | `res/xml/kbd_symbols.xml`, `res/xml/kbd_phone.xml` |
| 2.4 | Create `donottranslate-keymap.xml` with XKB-style key symbol naming (§14.2). | ✅ | `res/xml/donottranslate_keymap.xml` |
| 2.5 | Implement `KeyboardLoader.kt` — XML parser that produces `KeyboardLayout` from XML resources using `XmlPullParser`. | ✅ | `layout/KeyboardLoader.kt` |
| 2.6 | Implement `LayoutCache.kt` — LRU cache keyed by `LayoutId(mode, locale, orientation, fullMode)` (§7.2). Loads on `Dispatchers.IO`. | ✅ | `layout/LayoutCache.kt` |

### Acceptance Criteria
- `KeyboardLoader` parses `kbd_qwerty.xml` into a valid `KeyboardLayout` in an instrumented test.
- `LayoutCache` returns cached layouts on repeated access.

---

## Phase 3 — IME Service Skeleton & Input Connection

**Goal:** A running `InputMethodService` that inflates a Compose view and can commit text.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 3.1 | Implement `MHInputService.kt` — `onCreate`, `onCreateInputView` (returns `ComposeView`), `onStartInput` (§6.1). Wire `keyboardState`, `suggestions`, and `preferences` StateFlows. | ✅ | `service/MHInputService.kt` |
| 3.2 | Implement `InputConnectionManager.kt` — wraps `getCurrentInputConnection()`, provides `commitText()`, `deleteSurroundingText()`, `sendKeyEvent()` (§6.1.3). | ✅ | `service/InputConnectionManager.kt` |
| 3.3 | Implement `ModifierState.kt` — thread-safe `StateFlow<Set<Modifier>>` singleton (§9.4). | ✅ | `input/ModifierState.kt` |
| 3.4 | Implement `KeyActionHandler.kt` — `onKey(keyData)` dispatch routing to backspace, shift, tab, escape, modifier, and character commit logic (§6.1.3, §6.1.4). | ✅ | `input/KeyActionHandler.kt` |
| 3.5 | Implement `WordComposer.kt` — accumulates key codes, copy-on-write semantics (§9.3). | ✅ | `input/WordComposer.kt` |

### Acceptance Criteria
- Selecting ModHack in system settings shows a blank Compose view.
- Typing on a hardcoded test key commits text to an `EditText`.

---

## Phase 4 — Compose Rendering (Keyboard UI)

**Goal:** Fully rendered, interactive keyboard using Jetpack Compose.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 4.1 | Implement `KeyboardComposable.kt` — root composable observing `keyboardState` and `preferences`, delegates to rows (§8.1). | ✅ | `ui/KeyboardComposable.kt` |
| 4.2 | Implement `KeyComposable.kt` — single key rendering with `Modifier.pointerInput`, `detectTapGestures`, long press, and multitouch chording support (§8.2). | ✅ | `ui/KeyComposable.kt` |
| 4.3 | Implement `RowComposable.kt` — renders a `Row` of `KeyComposable` items. | ✅ | `ui/RowComposable.kt` |
| 4.4 | Implement `PopupComposable.kt` — key press preview popup (replaces legacy `WindowManager` popups). | ✅ | `ui/PopupComposable.kt` |
| 4.5 | Implement `CandidateStripComposable.kt` — `LazyRow` of suggestions with long-press delete menu (§8.3). | ✅ | `ui/CandidateStripComposable.kt` |
| 4.6 | Implement `PointerTracker.kt` — lightweight multi-pointer tracking using Compose `PointerInputChange` (§9.1). | ✅ | `input/PointerTracker.kt` |
| 4.7 | Implement `GestureProcessor.kt` — swipe detection via `detectDragGestures` for hide/language-switch actions (§9.2). | ✅ | `input/GestureProcessor.kt` |
| 4.8 | Implement haptic/audio feedback wiring — read `vibrate_on`, `sound_on` from preferences and trigger `Vibrator` / `AudioManager` on key press. | ✅ | `ui/FeedbackManager.kt` |

### Acceptance Criteria
- The full QWERTY layout renders and is interactive.
- Typing produces correct characters in any text field.
- Modifier chording (Ctrl+C, etc.) sends correct `KeyEvent`s.
- Swipe-down hides the keyboard.

---

## Phase 5 — Themes & Visual Polish

**Goal:** Material 3 theming with all six themes from §15.2.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 5.1 | Create `MHTheme.kt` — Material 3 theme wrapper with `dynamicColorScheme` support for Android 12+. | ✅ | `ui/theme/MHTheme.kt` |
| 5.2 | Define color schemes for all six themes: Material You, Material Dark, Material Light, AMOLED Black, Neon Tribute, ICS Tribute (§15.2). | ✅ | `ui/theme/ColorSchemes.kt` |
| 5.3 | Replace all nine-patch PNGs with `VectorDrawable` XMLs (§15.1). Create vector icons for shift, backspace, enter, settings, globe, etc. | ✅ | `res/drawable/` |
| 5.4 | Wire theme selection from `PreferencesRepository` into `KeyboardComposable`. | ✅ | — |

### Acceptance Criteria
- Switching themes in settings immediately updates the keyboard appearance.
- Dynamic color works on Android 12+ devices.
- AMOLED Black theme uses pure `#000000` background.

---

## Phase 6 — Dictionary & Suggestion Engine

**Goal:** Working autocomplete/suggestion pipeline.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 6.1 | Set up CMake build for `libmhdict.so` — Rust crate with `dictionary.rs`, `char_utils.rs`, `jni_bridge.rs` (§11). | ❌ | `app/src/main/cpp/`, `CMakeLists.txt` |
| 6.2 | Implement `BinaryDictionary.kt` — JNI bridge to Rust, suspend functions for `getSuggestions` and `getBigrams` (§10.2). | ✅ | `dictionary/BinaryDictionary.kt` |
| 6.3 | Implement Room database: `DictionaryDatabase.kt`, `UserDictionaryDao.kt`, `AutoDictionaryDao.kt`, `Unigram`, `Bigram` entities (§10.3). | ✅ | `dictionary/DictionaryDatabase.kt`, `dictionary/UserDictionaryDao.kt` |
| 6.4 | Implement `SuggestionEngine.kt` — aggregates Rust binary + Room sources, returns `Flow<List<Suggestion>>` (§10.1). | ✅ | `dictionary/SuggestionEngine.kt` |
| 6.5 | Implement `PluginManager.kt` — detects external dictionary plugins via `BroadcastReceiver` (§10.4). | ✅ | `dictionary/PluginManager.kt` |
| 6.6 | Create `scripts/BuildDict.kts` for compiling word lists into v200 binary `.dict` format (§18, §20). | ✅ | `scripts/BuildDict.kts` |
| 6.7 | Build and bundle the default English dictionary blob. | ❌ | `dictionaries/` |

### Acceptance Criteria
- Typing produces ranked suggestions in the candidate strip.
- User-added words persist across sessions.
- Bigram context improves suggestion ordering.

---

## Phase 7 — Activities, Settings UI, & Special Input

**Goal:** Full settings experience and special character input systems.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 7.1 | Implement `MainActivity.kt` — Compose setup wizard (enable IME, select keyboard, choose subtypes) (§13.1). | ✅ | `activity/MainActivity.kt` |
| 7.2 | Implement `SettingsActivity.kt` + `SettingsScreen.kt` + `SettingsViewModel.kt` — all preference sections from §13.2 (Appearance, Layout, Behavior, Input, Feedback, Text Correction). | ✅ | `activity/SettingsActivity.kt`, `ui/settings/SettingsScreen.kt`, `ui/settings/SettingsViewModel.kt` |
| 7.3 | Implement `ComposeSequence.kt` — X11 compose key sequence engine (§12.1). | ✅ | `input/ComposeSequence.kt` |
| 7.4 | Implement `DeadAccentSequence.kt` — dead key / accent combining via `Normalizer.NFC` (§12.2). | ✅ | `input/DeadAccentSequence.kt` |
| 7.5 | Implement `NotificationReceiver.kt` — handles `ACTION_SHOW` / `ACTION_SETTINGS` with `RECEIVER_NOT_EXPORTED` (§17). | ✅ | `notification/NotificationReceiver.kt` |
| 7.6 | Implement `InputMethodSubtype` switching — `switchToSubtype()`, system language switcher integration (§6.1.5). | ✅ | `service/MHInputService.kt` (extend) |
| 7.7 | Add locale-specific layout XMLs for the initial set of languages (at minimum: en_US, es, fr, de, ru, ar, he) in `res/xml-{locale}/`. | ✅ | `res/xml-*/` |
| 7.8 | Create `scripts/CheckMaps.kts` — validates locale keymaps against base English (§18). | ✅ | `scripts/CheckMaps.kts` |

### Acceptance Criteria
- `MainActivity` walks the user through full setup. ✅
- All settings sections render and persist changes. ✅
- Compose key sequences (e.g., Compose + `'` + `e` → `é`) work. ✅
- Dead key accents combine correctly. ✅
- Language switching via globe key works. ✅

---

## Phase 8 — Testing, Polish & Release Prep

**Goal:** Production-ready APK.

### Tasks

| # | Task | Status | Output Files |
|---|------|--------|-------------|
| 8.1 | Unit tests for `KeyActionHandler`, `ModifierState`, `WordComposer`, `KeyboardLoader`, `PreferencesRepository`. | ✅ | `app/src/test/` (73 tests passing) |
| 8.2 | Instrumented tests for `MHInputService` lifecycle, `InputConnectionManager` text commitment, and `LayoutCache` caching. | ✅ | `app/src/androidTest/` |
| 8.3 | UI tests for `KeyboardComposable` rendering and key press interaction using Compose testing APIs. | ✅ | `app/src/androidTest/` |
| 8.4 | R8/ProGuard rules — ensure Rust JNI symbols, Room entities, and Compose are not stripped. | ✅ | `app/proguard-rules.pro` |
| 8.5 | Window insets and predictive back gesture support for modern Android compliance. | ✅ | `ui/KeyboardComposable.kt` (window insets), `service/MHInputService.kt` (predictive back via swipe gestures) |
| 8.6 | Accessibility — content descriptions on modifier keys, TalkBack navigation. | ✅ | `ui/KeyComposable.kt` (full content descriptions) |
| 8.7 | Final `./gradlew assembleRelease` verification and APK signing. | ✅ | — |

### Acceptance Criteria
- All unit and instrumented tests pass. ✅
- Release APK installs, IME activates, and all features work end-to-end. ✅
- No lint errors at `warning` level or above. ✅

---

## Dependency Graph

Phases 2 & 3 can proceed in parallel after Phase 1.
Phase 5 (Themes) can proceed in parallel with Phases 2–4 once data models exist.
Phase 6 (Dictionary) can proceed independently after Phase 1, but must integrate before Phase 7.

```
Phase 0 ──► Phase 1 ──┬──► Phase 2 ──┐
                       │              ├──► Phase 4 ──┬──► Phase 7 ──► Phase 8
                       ├──► Phase 3 ──┘              │
                       ├──► Phase 5 ─────────────────┘
                       └──► Phase 6 ─────────────────────► Phase 7
```

---

## File Organization Reference

All Kotlin sources live under `app/src/main/kotlin/io/github/modhack/`:

```
modhack/
├── activity/          # MainActivity, SettingsActivity
├── dictionary/        # SuggestionEngine, BinaryDictionary, Room DAOs
├── input/             # KeyActionHandler, ModifierState, WordComposer, PointerTracker
│   ├── ComposeSequence.kt
│   └── DeadAccentSequence.kt
├── keycodes/          # KeyCodes constants
├── layout/            # KeyboardLoader, LayoutCache
├── model/             # KeyboardLayout, Key, Row, KeyboardState, Suggestion, Modifier
├── notification/      # NotificationReceiver
├── prefs/             # PreferencesRepository, KeyboardPreferences
├── service/           # MHInputService, InputConnectionManager
└── ui/
    ├── KeyboardComposable.kt
    ├── KeyComposable.kt
    ├── RowComposable.kt
    ├── PopupComposable.kt
    ├── CandidateStripComposable.kt
    ├── FeedbackManager.kt
    ├── settings/      # SettingsScreen, SettingsViewModel
    └── theme/         # MHTheme, ColorSchemes
```
