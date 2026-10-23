# ======================================================================
# HISAB KHATA - HIGH-PERFORMANCE R8 & PROGUARD OPTIMIZATION SUITE
# ======================================================================

# 1. Aggressive R8 Optimization & Code Shrinking
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# 2. Strip All Android Logging, Println & Trace Overhead in Release Builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int println(...);
}

-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# 3. Strip Jetpack Compose Debug & Tracing Markers
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer, java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer, int, java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
    void traceEventStart(int, int, int, java.lang.String);
    void traceEventEnd();
}

# 4. Room Database Rules (Preserve compile-time generated DAOs and Entities)
-keepattributes *Annotation*
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
    <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Entity and data models
-keep class com.hisabkhata.app.data.local.entity.** { *; }
-keepclassmembers class com.hisabkhata.app.data.local.entity.** { *; }

# 5. Kotlin Coroutines Optimization
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# 6. Google Drive API & Google Play Services Auth
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-keepclassmembers class com.google.api.client.** { *; }
-keep class com.google.android.gms.auth.api.signin.** { *; }

# 7. Suppress Non-Actionable Notes and Warnings from Third-Party Libraries
-dontnote **
-dontwarn **
