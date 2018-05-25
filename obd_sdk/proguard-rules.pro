# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\AndroidDeveloperTools\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-libraryjars <java.home>/lib/rt.jar
#-libraryjars /Users/guomin/Library/Android/sdk/platforms/android-22/android.jar

-dontshrink
-dontoptimize
#-dontobfuscate
-dontwarn
-ignorewarnings
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers public  class com.mapbar.hamster.core.AdasCore{
public *;
private void loadLibraries();
}

-keep public class com.mapbar.hamster.core.AdasCore
-keep public class com.mapbar.hamster.bean.**{*;}
-keep public class com.mapbar.hamster.log.**{*;}
-keepclassmembers class * implements com.mapbar.hamster.bean.Available{
public *;
}
-keep public class com.mapbar.hamster.core.EventType
-keepclassmembers class com.mapbar.hamster.core.EventType{
public *;
}

-keep interface com.mapbar.hamster.core.IAdas
-keep interface com.mapbar.hamster.core.IAdasCallBack

-keep interface com.mapbar.hamster.core.IAdasCallBack{
<methods>;
<fields>;
}
-keepclassmembers enum * {
  *;
}


-keep class com.mapbar.hamster.core.AdasCore$* {
    *;
}
-keepclassmembers class com.mapbar.hamster.core.AdasCore$* {
    *;
}

-keepclassmembers class com.mapbar.hamster.core.HamsterAsyncTask{
public *;
}

-keepclassmembers class com.mapbar.hamster.core.VehicleTrackingTask{
public *;
}

-keepclassmembers class com.mapbar.hamster.core.VehicleTrackingTask$* {
    *;
}

#//////////

# 不混淆采集包
-dontwarn com.mapbar.android.ingest.**
-keep class com.mapbar.android.ingest.** {*;}

# 不混淆GUID
-dontwarn com.mapbar.android.guid.**
-keep class com.mapbar.android.guid.** {*;}


-keep  class com.googlecode.protobuf.format.** {*;}
-dontwarn  com.googlecode.protobuf.format.**

-keep class com.mapbar.android.sc.api.** {*;}
-dontwarn  com.mapbar.android.sc.api.**


-dontwarn  com.google.protobuf.**
-keep  class com.google.protobuf.** {*;}

#//////////
-dontwarn com.mapbar.android.guid.GUIDController
-dontwarn android.support.**
-dontwarn com.google.**

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}

-keep class com.mapbar.hamster.core.LicenseManager$* {
    *;
}
-keep class * implements okhttp3.Callback {
<methods>;
<fields>;
}


# 不混淆Timber
-keep class timber.log.** { *;}
-dontwarn timber.log.**

# 不混淆采集包
-keep class com.mapbar.android.statistics.** {*;}
-dontwarn com.mapbar.android.statistics.**

-keep class com.mapbar.tool.** {*;}
-dontwarn com.mapbar.tool.**

-dontwarn com.mapbar.hamster.**

-dontskipnonpubliclibraryclassmembers