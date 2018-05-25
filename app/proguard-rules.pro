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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontshrink
-dontoptimize
#-dontobfuscate
-dontwarn
-ignorewarnings

##不混淆gson
-keep class com.google.gson.**{*;}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
##不混淆序列化相关对象
-keep public class * implements java.io.Serializable {*;}


#### 不能混淆的类
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep public class * extends com.mapbar.android.page.MainFragmentPage{*;}
-keep public class * extends com.mapbar.android.mapbarmap.core.page.BasePage{*;}
-keep public class * extends com.mapbar.android.mapbarmap.core.page.FragmentPage{*;}
-keep public class * extends com.mapbar.android.page.search.AbsSearchPage{*;}
-keep class com.umeng.* {*;}


-dontwarn com.mapbar.android.collector.**
-keep class com.mapbar.android.collector.** { *;}
-dontwarn com.mapbar.android.statistics.**
-keep class com.mapbar.android.statistics.** { *;}



-dontwarn com.umeng.socialize.sso.**
-keep class com.umeng.socialize.sso.** { *;}

-dontwarn okhttp3.**

# 不混淆采集包
-dontwarn com.mapbar.android.ingest.**
-keep class com.mapbar.android.ingest.** {*;}

# 不混淆GUID
-dontwarn com.mapbar.android.guid.**
-keep class com.mapbar.android.guid.** {*;}





-dontwarn  com.googlecode.protobuf.format.**
-keep  class com.googlecode.protobuf.format.** {*;}

# 不混淆统计包
-dontwarn com.mapbar.android.statistics.**
-keep class com.mapbar.android.statistics.** {*;}


# 不混淆...
-dontwarn org.apache.http.entity.mime.**
-keep class org.apache.http.entity.mime.** {*;}






-keep class **.R$* {
*;
}


#保护注解
-keepattributes Annotation,Signature
-keepattributes SourceFile,LineNumberTable
-keepclassmembers class *.R$ {public static <fields>;}
-keepclasseswithmembernames class * {native <methods>;}
-keepattributes *JavascriptInterface*
# https://stackoverflow.com/questions/31703303/newrelic-causing-build-errors
-keepattributes Exceptions, Signature, InnerClasses


#保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

#避免混淆泛型
-keepattributes Signature

#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

#保留本地native方法不被混淆kkkkkkkkkkkkkkkkkkkkkk
-keepclasseswithmembernames class * {
    native <methods>;
}

#保留枚举类不被混淆
#  public static **[] values();
#  public static ** valueOf(java.lang.String);

-keepclassmembers enum * {
  *;
}

#保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}



# 不混淆GUID
-dontwarn com.android.view.**
-keep class com.android.view.** {*;}

# 不混淆GUID
-dontwarn com.ankai.**
-keep class com.ankai.** {*;}

-keep class com.mapbar.hamster** {*;}
-keep class com.mapbar.log** {*;}



-dontskipnonpubliclibraryclassmembers

