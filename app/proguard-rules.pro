# Rules for Firebase Realtime Database and Auth
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Firebase Database classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.firebase_ml_** { *; }

# Keep data models used with Firebase Database serialisation/deserialisation
-keepclassmembers class com.bagomri.fajrloop.data.** {
    *** get*();
    *** set*(*);
    public <init>(...);
}
-keep class com.bagomri.fajrloop.data.** { *; }

# Keep Google Play Services and Sign-In / Credential Manager classes
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Keep Adhan calculations library
-keep class com.batoulapps.adhan.** { *; }
-keepclassmembers class com.batoulapps.adhan.** { *; }

# Keep Jetpack WorkManager, Room, and Startup classes to prevent release startup crash
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.startup.** { *; }
-keep class androidx.sqlite.** { *; }

