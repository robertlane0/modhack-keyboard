# ModHack Keyboard (MK)
## Comprehensive Code Specification
**Package:** `io.github.modhack-keyboard`
**Version:** v1.0.0 · App Version Code: 1
**Repository:** github.com/robertlane0/modhack-keyboard

---

## 1. Project Overview
ModHack is a complete, from-scratch Android keyboard. It is designed for power users, developers, and terminal users who require a full 5-row keyboard, modifier chording, and navigation keys on mobile devices. 

MH is built entirely with modern Android paradigms: **Kotlin, Jetpack Compose, Coroutines/Flow, and Material 3**. It has leverages modern APIs like `InputMethodSubtype` for language switching, Compose for rendering, and DataStore for preferences.

### 1.1 Key Features
*   **5-row layout** with a dedicated number row.
*   **Full modifier key set**: Ctrl, Alt, Meta, Fn, Shift — all support true multitouch chording via modern pointer input.
*   **Navigation cluster**: Arrow keys, Home, End, Page Up/Down.
*   **Complete function key row** (F1–F12) available in full 5-row layouts.
*   **Escape and Tab keys** with optional terminal compatibility modes.
*   **Compose key sequence engine** (X11-compatible sequences, modernized in Kotlin).
*   **Dead key / accent sequence engine** for diacritical characters.
*   **Jetpack Compose Rendering**: Hardware-accelerated, vector-based key drawing (no more canvas bitmap invalidation limits).
*   **Modern Language Switching**: Uses standard `InputMethodSubtype` API.
*   **Material 3 Theming**: Including dynamic color (Material You) support, AMOLED black, and legacy tributes.
*   **DataStore Preferences**: Asynchronous, type-safe settings replacing legacy `SharedPreferences`.

### 1.2 Technical Heritage and Status
This project is unrelated to the original AOSP LatinIME codebase. It is written in 100% Kotlin (with a Rust/C++ core for the dictionary engine). It targets modern Android APIs (API 24+) and complies with modern Play Store requirements, including proper window insets, predictive back, and modern IME lifecycle management.

### 1.3 License
Licensed under the Apache License 2.0.

---

## 2. Repository Structure

| Path | Description |
|---|---|
| `mh-master/` | Repository root |
| `app/` | Android application module |
| `app/build.gradle.kts` | Module-level Gradle build script (Kotlin DSL) |
| `app/src/main/AndroidManifest.xml` | Application manifest |
| `app/src/main/cpp/` | Rust/C++ native dictionary library source |
| `app/src/main/kotlin/.../modhack/` | All Kotlin source files |
| `app/src/main/res/xml/` | IME method XML and keyboard layout XMLs |
| `app/src/main/res/drawable/` | Vector assets for icons |
| `build.gradle.kts` | Top-level Gradle build script |
| `gradle/libs.versions.toml` | Gradle version catalog for dependencies |
| `dictionaries/` | Scripts and sample files for building dictionary blobs |

---

## 3. Build System

### 3.1 Gradle Configuration
Uses Android Gradle Plugin 8.x with a single-module layout and Kotlin DSL.

| Setting | Value |
|---|---|
| `compileSdk` | 34 (Android 14) |
| `minSdk` | 24 (Android 7.0 Nougat) |
| `targetSdk` | 34 |
| `applicationId` | `io.github.modhack-keyboard` |
| `versionCode` | 1 |
| `versionName` | v1.0.0 |
| `minifyEnabled` (release) | `true` — R8/ProGuard applied |

**Dependencies** (managed via `libs.versions.toml`):
*   `androidx.core:core-ktx`
*   `androidx.compose:compose-bom` (BOM for Compose UI, Material 3, Foundation)
*   `androidx.lifecycle:lifecycle-runtime-ktx`
*   `androidx.datastore:datastore-preferences` (Replaces SharedPreferences)
*   `org.jetbrains.kotlinx:kotlinx-coroutines-android`

### 3.2 Native Build (CMake / Rust)
The native shared library `libmhdict.so` is built via CMake. The core suggestion engine is implemented in Rust and exposed via JNI bindings (cxx-rs) for memory safety and high performance.
*   `dictionary.rs` — Trie traversal and suggestion engine.
*   `char_utils.rs` — Unicode classification and proximity mappings.
*   `jni_bridge.rs` — JNI entry points.

---

## 4. Android Manifest & Application Configuration

### 4.1 Permissions
| Permission | Purpose |
|---|---|
| `android.permission.VIBRATE` | Haptic feedback on key press |
| `android.permission.READ_USER_DICTIONARY` | Read system user dictionary |
| `android.permission.WRITE_USER_DICTIONARY` | Add user-typed words to system dictionary |
| `android.permission.POST_NOTIFICATIONS` | (API 33+) Notification permission for persistent keyboard toggle |

### 4.2 Hardware Feature Declarations
Microphone, `fake-touch`, and `touchscreen` declared with `required=false`.

### 4.3 Application-Level Settings
| Attribute | Value / Notes |
|---|---|
| `android:label` | `@string/app_name` → "ModHack Keyboard" |
| `android:allowBackup` | `true` |
| `android:supportsRtl` | `true` |
| `android:hardwareAccelerated` | `true` (Required for Jetpack Compose) |
| `android:windowSoftInputMode` | `adjustResize` |

### 4.4 Declared Components

| Component | Type | Class | Description |
|---|---|---|---|
| `MHInputService` | Service | `MHFInputService` | The IME service. Bound with `BIND_INPUT_METHOD`. References `@xml/method`. |
| `MainActivity` | Activity | `MainActivity` | Compose-based launcher activity. Setup wizard for enabling/selecting the keyboard and Subtypes. |
| `SettingsActivity` | Activity | `SettingsActivity` | Hosts Compose `SettingsScreen`. Accessible via intent `io.github.modhack.SETTINGS`. |

---

## 5. Architecture Overview

MH follows a unidirectional data flow (UDF) architecture combined with the Android IME lifecycle. It separates concerns into five modern layers:

| Layer | Responsibilities | Key Classes |
|---|---|---|
| **IME Service Layer** | Android lifecycle, `InputConnection` management, Compose window lifecycle, subtype management | `MHInputService`, `InputConnectionManager` |
| **Input Processing Layer** | Compose pointer parsing, multi-touch chording, modifier state, word composition | `KeyActionHandler`, `ModifierState`, `WordComposer`, `GestureDetector` |
| **Keyboard Model Layer** | Key layout data, layout switching, XML parsing to immutable data | `KeyboardLayout`, `KeyboardLoader`, `LayoutCache` |
| **View/Rendering Layer** | Jetpack Compose UI, key drawing, popups, preview, candidate strip | `KeyboardComposable`, `KeyComposable`, `CandidateStripComposable` |
| **Dictionary/Suggestion Layer** | Rust binary dictionary, Room DB for user/auto/bigrams, Flow-based suggestions | `DictionaryRepository`, `SuggestionEngine`, `UserDictionaryDao` |

### 5.1 Data Flow — Key Press
1. User touches screen → `KeyComposable` detects pointer via `Modifier.pointerInteropFilter` or `detectTapGestures`.
2. Event dispatched to `KeyActionHandler` with `PointerId` for multi-touch tracking.
3. `KeyActionHandler` resolves the `KeyData` and updates `ModifierState` (if modifier) or `WordComposer` (if character).
4. For text characters: `WordComposer` updates, `SuggestionEngine` emits a `Flow<List<Suggestion>>`, UI updates `CandidateStripComposable`.
5. On space/punctuation: `InputConnectionManager.commitText()` is called.

---

## 6. IME Service Layer

### 6.1 MHInputService
File: `MHInputService.kt`

Extends `InputMethodService` and implements `CoroutineScope`. Overridden lifecycle methods manage a Compose `View` tree hosted inside the IME window.

#### 6.1.1 Initialization Sequence
*   `onCreate()`: Initializes `DataStore` preferences, `DictionaryRepository`, `LayoutCache`, and `ModifierState`.
*   `onCreateInputView()`: Inflates a `ComposeView`, sets the `KeyboardUI` composable as content. Returns the `ComposeView`.
*   `onStartInput(EditorInfo, boolean)`: Evaluates `EditorInfo.inputType` to configure prediction mode, recorrection mode, and selects the appropriate layout via `LayoutCache`.

#### 6.1.2 State Flows
| Property | Type | Description |
|---|---|---|
| `keyboardState` | `StateFlow<KeyboardState>` | Emits current layout, mode, shift state, and active modifiers. |
| `suggestions` | `StateFlow<List<Suggestion>>` | Emits ranked suggestion list from `SuggestionEngine`. |
| `preferences` | `StateFlow<Preferences>` | Emits current settings from DataStore. |

#### 6.1.3 Key Handler Methods
| Method | Description |
|---|---|
| `onKey(keyData: KeyData)` | Primary key dispatch. Routes to `handleBackspace()`, `handleShift()`, `sendTab()`, `sendEscape()`, or character commit logic. |
| `handleBackspace()` | Deletes composing or committed text. Accelerates deletion via coroutine timer after 20 consecutive presses. |
| `handleShift()` | Toggles/locks shift. Compose-native multitouch shift is natively supported by `ModifierState`. |
| `sendTab()` | Sends `KeyEvent.KEYCODE_TAB`. Optional ConnectBot hack (sends Ctrl+I) available in settings. |
| `sendEscape()` | Sends `KeyEvent.KEYCODE_ESCAPE`. |
| `sendModifiedKeyEvent(code, mods)` | Constructs a modern `KeyEvent` with modifier flags and dispatches via `InputConnection.sendKeyEvent()`. |

#### 6.1.4 Modifier Key Handling
Modifiers (Ctrl, Alt, Meta, Fn) are tracked in a `ModifierState` singleton using `StateFlow<Set<Modifier>>`. 
When a character key is pressed with an active modifier:
*   **Ctrl+key** → Maps to standard Android KeyCodes with `META_CTRL_ON`.
*   **Alt+key** → `META_ALT_ON`.
*   **Meta+key** → `META_META_ON`.
*   **Fn+arrow** → Maps to Page Up/Down, Home/End.

#### 6.1.5 InputMethodSubtype Integration
MH fully integrates with Android's `InputMethodManager` and `InputMethodSubtype`. Languages are declared in `res/xml/method.xml` as subtypes. Switching languages utilizes `switchToSubtype()` or the system language switcher, correctly updating the system IME indicator.

---

## 7. Keyboard Model Layer

### 7.1 KeyboardLayout
File: `KeyboardLayout.kt`

An immutable data class representing a parsed keyboard layout. Replaces the mutable `Keyboard.java` from AOSP.

```kotlin
data class KeyboardLayout(
    val id: String,
    val rows: List<Row>,
    val width: Int,
    val height: Int,
    val mode: Int
)

data class Row(
    val keys: List<Key>,
    val defaultHeight: Int,
    val defaultWidth: Int,
    val mode: Int,
    val isExtension: Boolean
)

data class Key(
    val codes: List<Int>,       // Primary + nearby for proximity
    val label: String,
    val shiftLabel: String,
    val hint: String?,
    val altHint: String?,
    val icon: ImageVector?,
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
    val isModifier: Boolean,
    val isRepeatable: Boolean,
    val isCursor: Boolean,
    val popupKeys: List<String>?
)
```

### 7.2 LayoutCache (Replaces KeyboardSwitcher)
File: `LayoutCache.kt`

Uses an LRU cache (`LruCache<String, KeyboardLayout>`) keyed by a `LayoutId(mode, locale, orientation, fullMode)`. Loads XML layouts asynchronously via Kotlin Coroutines on a background `Dispatchers.IO` dispatcher.

**Modes:** `TEXT`, `SYMBOLS`, `PHONE`, `URL`, `EMAIL`, `IM`, `WEB`.
**Full Modes:** 4-row QWERTY, 5-row Full (F-keys), 5-row Full (Fn-keys), Compact.

### 7.3 PreferencesRepository (Replaces GlobalKeyboardSettings)
File: `PreferencesRepository.kt`

Utilizes Jetpack DataStore. Exposes a `Flow<KeyboardPreferences>` ensuring thread-safety and asynchronous reads, eliminating the legacy global singleton anti-pattern. UI components observe this Flow via `collectAsState()`.

---

## 8. View / Rendering Layer

### 8.1 KeyboardComposable
File: `KeyboardComposable.kt`

The root Compose function for the keyboard. It observes `keyboardState` and `preferences`, and delegates rendering to `RowComposable` and `KeyComposable`.

*   **Rendering:** Uses Compose `Canvas` and `Text` components. All keys are vector-drawn. Hardware acceleration is inherently supported.
*   **Theming:** Material 3 themes. Supports Dynamic Color (Material You) on Android 12+, as well as custom themes (Material Dark/Light/Black, Neon, ICS Tribute).

### 8.2 KeyComposable
File: `KeyComposable.kt`

Represents a single key. Uses `Modifier.pointerInput` to detect:
*   `detectTapGestures` (Tap, Long Press)
*   `awaitPointerEventScope` (for tracking dragging/sliding to adjacent keys, and multitouch chording).

When pressed, it triggers a `Popup` composable.

### 8.3 CandidateStripComposable
File: `CandidateStripComposable.kt`

A `LazyRow` of suggestions. Observes the `suggestions` Flow. Long-pressing a suggestion triggers a `DropdownMenu` to remove the word from the user dictionary.

---

## 9. Input Processing Layer

### 9.1 PointerTracker (Modernized)
File: `PointerTracker.kt`

Instead of a large state machine, pointer tracking is split into a lightweight class that utilizes Kotlin Coroutines. It tracks multiple pointers via `PointerInputChange` from Compose.
*   **Slide Keys:** Configurable. If enabled, sliding off a key fires it; otherwise, cancels it.
*   **Proximity:** Handled via geometric distance calculation against the current `KeyboardLayout` bounds.

### 9.2 GestureProcessor
File: `GestureProcessor.kt`

Uses Compose's `detectDragGestures` and velocity calculation. If a swipe exceeds the velocity threshold, actions (Next Language, Hide IME, etc.) are triggered based on `KeyboardPreferences`.

### 9.3 WordComposer
File: `WordComposer.kt`

An immutable-resembling class that accumulates key codes. Uses Kotlin `data class` copy-on-write semantics to prevent threading issues. Triggers `SuggestionEngine` updates.

### 9.4 ModifierState
File: `ModifierState.kt`

A thread-safe singleton holding the current active modifiers.
```kotlin
object ModifierState {
    private val _activeModifiers = MutableStateFlow(emptySet<Modifier>())
    val activeModifiers: StateFlow<Set<Modifier>> = _activeModifiers
    
    fun press(mod: Modifier) { ... }
    fun release(mod: Modifier) { ... }
    fun isChording(mod: Modifier): Boolean { ... }
}
```

---

## 10. Dictionary / Suggestion Layer

### 10.1 SuggestionEngine
File: `SuggestionEngine.kt`

Aggregates suggestions from multiple sources asynchronously using Kotlin Coroutines.
*   Sources: `BinaryDictionary` (Rust), `UserDictionaryDao` (Room), `AutoDictionaryDao` (Room).
*   Returns a `Flow<List<Suggestion>>` ordered by frequency and bigram context.

### 10.2 BinaryDictionary (Rust/JNI)
File: `BinaryDictionary.kt`

Delegates to `libmhdict.so`. The Rust implementation handles memory mapping (mmap) safely with explicit error handling for Android SELinux constraints. Exposes suspend functions via JNI bridge:
*   `suspend fun getSuggestions(wordComposer: WordComposer): List<Suggestion>`
*   `suspend fun getBigrams(prevWord: String): List<Suggestion>`

### 10.3 User & Auto Dictionaries (Room)
File: `DictionaryDatabase.kt`, `UserDictionaryDao.kt`

Uses Room Database for app-private learned words and bigrams.
```kotlin
@Entity(tableName = "unigrams")
data class Unigram(
    @PrimaryKey val word: String,
    val frequency: Int,
    val locale: String
)

@Entity(tableName = "bigrams")
data class Bigram(
    @PrimaryKey val word1: String,
    @ColumnInfo(name = "word2") val word2: String,
    val frequency: Int
)
```
ContactsDictionary is intentionally omitted by default for privacy, but can be added as an opt-in plugin.

### 10.4 PluginManager
File: `PluginManager.kt`

Uses modern `PackageInfo` flags and `BroadcastReceiver` (registered via `Context.RECEIVER_NOT_EXPORTED` for Android 13+ compatibility) to detect dictionary plugins. Supports the legacy AnySoftKeyboard protocol and a new MH JSON-based dictionary protocol.

---

## 11. Native Layer (Rust)

### 11.1 Overview
The native code is written in Rust and compiled to `libmhdict.so`. It provides memory-safe dictionary traversal.

### 11.2 dictionary.rs
Implements the trie traversal algorithm. The binary format remains compatible with a 22-bit address space trie (version 200+) for backward compatibility with existing dictionary packs.
Uses `memmap2` crate for safe memory mapping.

### 11.3 char_utils.rs
Implements Unicode lowercasing and keyboard proximity character mapping. Replaces the massive C++ switch statements with static Rust `HashMap` or `match` expressions, compiled with SIMD optimizations where applicable.

---

## 12. Special Character Input: Compose and Dead Keys

### 12.1 ComposeSequence
File: `ComposeSequence.kt`

A Kotlin object holding a `Map<String, String>` of X11 compose sequences. Uses Kotlin's `Normalizer` (java.text) for Unicode normalization.
When `KEYCODE_COMPOSE` is pressed, the `ComposeBuffer` collects subsequent inputs. 

### 12.2 DeadAccentSequence
File: `DeadAccentSequence.kt`

Extends `ComposeSequence`. Uses `java.text.Normalizer.Form.NFC` to combine dead keys with subsequent base characters. Languages without dead keys (Arabic, Thai, Hebrew) are handled via `Locale` checks.

---

## 13. Settings, Activities, and Preference UI

### 13.1 MainActivity
File: `MainActivity.kt`

A `ComponentActivity` hosting Jetpack Compose navigation. Guides the user to enable the IME and grant permissions.

### 13.2 Settings UI
File: `SettingsScreen.kt`, `SettingsViewModel.kt`

Replaces `PreferenceActivity` entirely with a Compose UI. Uses `androidx.datastore` for state persistence.
Sections:
*   **Appearance**: Theme (Material You/Dark/Light/Black), key style, label scale, top row scale.
*   **Layout**: Portrait/Landscape layout modes (4-row vs 5-row), suggestions in landscape.
*   **Behavior**: Auto-cap, shift lock, slide keys, ConnectBot tab hack.
*   **Input**: Swipe actions, hardware volume key actions, long-press duration.
*   **Feedback**: Haptics, vibration length, sound on keypress, popup previews.
*   **Text Correction**: Quick fixes, auto-complete, recorrection.

---

## 14. Keyboard Layout Resources

### 14.1 Keyboard XML Files
All keyboard XML files live in `res/xml/` and `res/xml-{locale}/`. The parsing logic is rewritten in Kotlin to feed the immutable `KeyboardLayout` data class.
*   `kbd_qwerty.xml` (4-row)
*   `kbd_full.xml` (5-row + F-keys)
*   `kbd_full_fn.xml` (5-row + Fn-keys)
*   `kbd_compact.xml`, `kbd_compact_fn.xml`
*   `kbd_symbols.xml`, `kbd_phone.xml`

### 14.2 Key Mapping System
Retains the XKB/X11 key symbol naming convention (`key_ae01`, `key_ad01`, etc.) in `donottranslate-keymap.xml`. This allows easy porting of existing locale layouts.

### 14.3 Supported Locales
MH ships with modernized XML layouts for the same 35+ languages. RTL layouts (Arabic, Hebrew, Persian) leverage Android's modern RTL text rendering and `LayoutDirection.RTL`.

---

## 15. Visual Themes and Resources

### 15.1 Drawables
Fully vector-based. All nine-patch PNGs have been replaced by `VectorDrawable` XMLs, significantly reducing APK size and providing crisp rendering at any display density.

### 15.2 Themes (Material 3)
| Theme Index | Name | Description |
|---|---|---|
| 0 | Material You | Dynamic colors (Android 12+), falls back to baseline. |
| 1 | Material Dark | Static dark Material 3 theme. |
| 2 | Material Light | Static light Material 3 theme. |
| 3 | AMOLED Black | Pure black background for OLED screens. |
| 4 | Neon Tribute | Translucent background with neon accents (legacy tribute). |
| 5 | ICS Tribute | Light blue/white Holo-inspired theme (legacy tribute). |

---

## 16. Complete Preferences Reference (DataStore Keys)

| Preference Key | Type | Default | Description |
|---|---|---|---|
| `height_portrait` | Float | 0.4 | Keyboard height as % of screen (0.15 - 0.75) |
| `height_landscape` | Float | 0.4 | Keyboard height in landscape |
| `mode_portrait` | String | "qwerty" | Layout: `qwerty`, `compact`, `full`, `full_fn` |
| `mode_landscape` | String | "full" | Layout in landscape |
| `theme` | String | "material_you" | Visual theme ID |
| `label_scale` | Float | 1.0 | Key label text scale |
| `vibrate_on` | Boolean | false | Vibrate on key press |
| `vibrate_len` | Int | 50 | Vibration duration in ms |
| `sound_on` | Boolean | false | Play sound on key press |
| `popup_on` | Boolean | true | Show key press preview popup |
| `long_press_duration` | Int | 400 | Long-press delay in ms |
| `auto_cap` | Boolean | true | Auto-capitalize sentences |
| `pref_shift_lock_modifiers`| Boolean | true | Shift lock locks Ctrl/Alt/Meta |
| `connectbot_tab_hack` | Boolean | true | Send Ctrl+I instead of Tab |
| `swipe_up` | String | "none" | Action for swipe up gesture |
| `swipe_down` | String | "hide" | Action for swipe down gesture |

---

## 17. Notifications and Broadcast Receivers

### 17.1 NotificationReceiver
File: `NotificationReceiver.kt`

A modern `BroadcastReceiver` exported with ` RECEIVER_NOT_EXPORTED ` flag for security. Handles `ACTION_SHOW` and `ACTION_SETTINGS` intents.
Notifications use `NotificationCompat.Builder` and require `POST_NOTIFICATIONS` permission on Android 13+.

---

## 18. Development Utility Scripts

| Script | Language | Purpose |
|---|---|---|
| `scripts/CheckMaps.kts` | Kotlin | Validates keyboard layout XML character mappings against the base English keymap. |
| `scripts/BuildDict.kts` | Kotlin | Compiles raw wordlists (XML) into the v200 binary `.dict` format for packaging. |

---

## 19. Keycode Reference

All special key codes are defined as Kotlin constants in `KeyCodes.kt`. They map to modern `android.view.KeyEvent` constants where applicable, and internal negative constants for IME-specific routing:

```kotlin
object KeyCodes {
    const val TAB = 9
    const val ENTER = 10
    const val SPACE = 32
    const val SHIFT = -1
    const val SYMBOL = -2
    const val DELETE = -5
    const val SETTINGS = -100
    const val VOICE = -102
    const val F1 = -103
    const val NEXT_LANGUAGE = -104
    const val PREV_LANGUAGE = -105
    const val COMPOSE = -10024
    const val DPAD_UP = -19
    const val DPAD_DOWN = -20
    const val DPAD_LEFT = -21
    const val DPAD_RIGHT = -22
    const val ESCAPE = -111
    const val CTRL_LEFT = -113
    const val META_LEFT = -117
    const val FN = -119
    const val HOME = -122
    const val END = -123
    const val INSERT = -124
    // ... F-Keys (-131 to -142)
}
```

---

## 20. Dictionary File Format

The binary dictionary format remains compatible with existing specifications to allow reuse of community-built dictionary packs.
*   **Minimum version:** 200
*   **Address width:** 22 bits (~4MB limit)
*   **Word length:** 48 chars (native), 32 chars (Kotlin)
*   **Format:** Flat byte array trie with 22-bit child addresses.

---

---

## Appendix: Key Relationships Diagram

```text
MHInputService (InputMethodService)
├── InputConnectionManager (Coroutines)
├── PreferencesRepository (DataStore Flow)
├── LayoutCache (LruCache)
│   └── KeyboardLayout (Immutable Data)
├── KeyboardComposable (Jetpack Compose)
│   ├── RowComposable
│   │   └── KeyComposable (PointerInput / Gestures)
│   │       └── PopupComposable (Replaces WindowManager Popups)
│   └── CandidateStripComposable (LazyRow)
├── KeyActionHandler
│   ├── ModifierState (StateFlow)
│   ├── WordComposer
│   └── ComposeSequence / DeadAccentSequence
├── SuggestionEngine (Coroutines)
│   ├── BinaryDictionary (Rust via JNI -> libmhdict.so)
│   └── DictionaryDatabase (Room)
│       ├── UserDictionaryDao
│       └── AutoDictionaryDao
└── PluginManager (BroadcastReceiver)
```
