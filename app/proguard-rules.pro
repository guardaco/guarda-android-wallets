# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\AndroidSDK\Android\sdk/tools/proguard/proguard-android.txt
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

#-dontobfuscate

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepnames class * { @butterknife.Bind *;}

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class android.support.v4.app.** { *; }

-keep interface android.support.v4.app.** { *; }

-keep interface javax.** { *; }
-dontwarn com.google.**

-dontwarn autodagger.**
-dontwarn com.fasterxml.jackson.**
-dontwarn com.freshdesk.hotline.**
-dontwarn com.squareup.**
-dontwarn com.thetransactioncompany.**
-dontwarn okio.**
-dontwarn org.spongycastle.**
-dontwarn processorworkflow.**
-dontwarn retrofit2.**
-dontwarn org.web3j.**
-dontwarn org.bitcoinj.store.**
-dontwarn org.bitcoinj.**
-dontwarn org.slf4j.**
-dontwarn org.postgresql.**
-dontwarn org.h2.**
-dontwarn com.mysql.**

-keepclasseswithmembers class org.web3j.** { *; }
-keep class org.spongycastle.**

-keepclasseswithmembers class com.nestlabs.sdk.** { *; }
-keepclasseswithmembers class com.firebase.** { *; }
-keepclasseswithmembers class com.fasterxml.jackson.** { *; }

-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

-keepclasseswithmembers class com.bitshares.** { *; }

-dontwarn javax.**
-dontwarn org.bouncycastle.**

-keepclasseswithmembers class jota.** { *; }

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep class * extends android.webkit.WebChromeClient { *; }
-dontwarn im.delight.android.webview.**

-dontwarn java.awt.*
-keep class com.sun.jna.* { *; }
-keepclassmembers class * extends com.sun.jna.* { public *; }