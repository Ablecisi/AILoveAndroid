# 保留类名和字段名
-keep class com.ailianlian.** { *; }

# 保留注解
-keepattributes *Annotation*

# 保留Gson注解
-keep class com.google.gson.annotations.SerializedName

# 保留Room注解
-keep class androidx.room.** { *; }

# 保留Retrofit接口
-keep interface com.ailianlian.** { *; }

# 保留Firebase
-keep class com.google.firebase.** { *; }

# 保留Lottie动画
-keep class com.airbnb.lottie.** { *; }

# 优化代码
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 忽略警告
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.firebase.**
-dontwarn com.airbnb.lottie.** 