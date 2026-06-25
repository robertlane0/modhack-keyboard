# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ── JNI / Native Methods ──────────────────────────────────────────────
-keep class io.github.modhack.dictionary.BinaryDictionary { *; }
-keepclassmembers class io.github.modhack.dictionary.BinaryDictionary {
    native <methods>;
}

# ── Dictionary classes ────────────────────────────────────────────────
-keep class io.github.modhack.dictionary.** { *; }

# ── Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }

# ── Room entities (required for reflection-based DAOs) ────────────────
-keep class io.github.modhack.dictionary.Unigram { *; }
-keep class io.github.modhack.dictionary.Bigram { *; }

# ── Activities (referenced from manifest) ─────────────────────────────
-keep class io.github.modhack.activity.MainActivity { *; }
-keep class io.github.modhack.activity.SettingsActivity { *; }

# ── InputMethodService (referenced from manifest) ─────────────────────
-keep class io.github.modhack.service.MHInputService { *; }

# ── BroadcastReceiver (referenced from manifest) ─────────────────────
-keep class io.github.modhack.notification.NotificationReceiver { *; }

# ── DataStore preferences (uses serialization) ────────────────────────
-keep class io.github.modhack.prefs.KeyboardPreferences { *; }

# ── ViewModel (used by SettingsViewModel) ─────────────────────────────
-keep class io.github.modhack.ui.settings.SettingsViewModel { *; }
