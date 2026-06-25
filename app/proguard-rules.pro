# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

-keep class io.github.modhack.dictionary.BinaryDictionary { *; }
-keepclassmembers class io.github.modhack.dictionary.BinaryDictionary {
    native <methods>;
}

-keep class io.github.modhack.dictionary.** { *; }

# Compose rules are usually handled by AAPT/AGP but good to ensure
-keep class androidx.compose.** { *; }
