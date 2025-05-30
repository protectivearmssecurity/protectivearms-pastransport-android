# Keep WebView JavaScript Interface classes
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Prevent WebView from being stripped or obfuscated
-keep class android.webkit.WebView { *; }
-dontwarn android.webkit.**

# ==============================
# 🚀 General App Rules
# ==============================

# Keep all model classes
-keep class com.live.pastransport.** { *; }

# Preserve R class to avoid missing resource errors
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep application class
-keep class com.live.pastransport.controller.MyApplication { *; }

# Keep all Activities & Fragments (Prevent issues with obfuscation)
-keep class com.live.pastransport.ui.activities.** { *; }
-keep class com.live.pastransport.ui.fragments.** { *; }

# Preserve line numbers for debugging (Optional)
-keepattributes SourceFile,LineNumberTable

# ==============================
# 🔥 Retrofit + OkHttp (Networking)
# ==============================
-keep class com.squareup.retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep class com.squareup.okhttp3.logging.** { *; }
-dontwarn okhttp3.**

# ==============================
# 💡 Gson (JSON Parsing)
# ==============================
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ==============================
# 🌀 Glide (Image Loading)
# ==============================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.annotation.GlideModule
-keep class com.bumptech.glide.** { *; }
-keep class com.github.bumptech.glide.** { *; }

# ==============================
# ⚡ Kotlin Coroutines
# ==============================
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ==============================
# 📲 AndroidX Libraries
# ==============================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.appcompat.** { *; }
-keep class androidx.constraintlayout.** { *; }
-keep class androidx.recyclerview.widget.** { *; }
-dontwarn androidx.**

# ==============================
# 🏷️ Country Code Picker (CCP)
# ==============================
-keep class com.hbb20.** { *; }
-dontwarn com.hbb20.**

# ==============================
# 🔘 PinView (OTP Input)
# ==============================
-keep class com.chaos.view.PinView { *; }

# ==============================
# 🌀 Blur Layout
# ==============================
-keep class eightbitlab.com.blurview.** { *; }
-dontwarn eightbitlab.com.blurview.**

# ==============================
# 🚀 End of ProGuard Rules
# ==============================
