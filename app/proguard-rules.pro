# Add project specific ProGuard rules here.

# Keep TensorFlow Lite classes
-keep class org.tensorflow.lite.** { *; }
-keep interface org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Retrofit and OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Moshi
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclassmembers class * extends dagger.hilt.android.lifecycle.HiltViewModel {
    <init>(...);
}

# Keep data classes and models
-keep class com.agriedge.link.domain.model.** { *; }
-keep class com.agriedge.link.data.remote.dto.** { *; }
-keep class com.agriedge.link.data.local.database.entity.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
