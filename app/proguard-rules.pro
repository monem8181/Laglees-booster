# ── LagLess ProGuard / R8 rules ──────────────────────────────────────────────

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# ── Kotlinx Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
# Composable functions are looked up by name via reflection in some tooling paths
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
# Compose compiler plugin generates stability classes
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ── Compose Navigation ────────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }
-keepnames class * extends androidx.navigation.NavGraph
-dontwarn androidx.navigation.**

# ── Lifecycle / ViewModel ─────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class * implements androidx.datastore.core.Serializer { *; }
-dontwarn androidx.datastore.**

# ── Accompanist Permissions ───────────────────────────────────────────────────
-keep class com.google.accompanist.permissions.** { *; }
-dontwarn com.google.accompanist.**

# ── Core SplashScreen ─────────────────────────────────────────────────────────
-keep class androidx.core.splashscreen.** { *; }
-dontwarn androidx.core.splashscreen.**

# ── App models & data layer ───────────────────────────────────────────────────
# Keep all data/model classes so R8 doesn't strip fields used by DataStore
-keep class com.lagless.booster.data.** { *; }
-keep class com.lagless.booster.ui.screens.**.HomeUiState { *; }

# ── Serialization (future-proofing) ──────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ── Android / System ──────────────────────────────────────────────────────────
-keep class android.os.** { *; }
-keep class android.app.ActivityManager$MemoryInfo { *; }
-keep class android.os.BatteryManager { *; }

# ── Shizuku ───────────────────────────────────────────────────────────────────
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keepclassmembers class * {
    @rikka.shizuku.** *;
}
-dontwarn rikka.shizuku.**
-dontwarn moe.shizuku.**

# ── Suppress noisy warnings from third-party libs ────────────────────────────
-dontwarn org.jetbrains.**
-dontwarn javax.annotation.**
