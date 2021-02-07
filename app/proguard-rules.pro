# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

-keep public class * extends com.duzhaokun123.bilibilihd.bases*
-keep public class * extends androidx.fragment.app.Fragment
-keep public class com.hiczp.bilibili.api** {*;}
-keepnames public class * extends java.lang.Throwable
-keep class bilibili.app** {*;}
-keep class com.duzhaokun123.bilibilihd.proto** {*;}
-keep public class com.google.protobuf** {*;}
