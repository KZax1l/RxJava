# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\AndroidIDE\sdk/tools/proguard/proguard-android.txt
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

-keep class com.zxy.recovery.core.ActivityStackCompat
-keep interface com.zxy.recovery.callback.RecoveryCallback
-keepclassmembers class com.zxy.recovery.core.ActivityStackCompat{
    *;
}

-keep class com.zxy.recovery.callback.RecoveryCallback{
    *;
}

# 不混淆接口及其实现类
-keep class * implements com.zxy.recovery.callback.RecoveryCallback{
    *;
}

# 不混淆内部公共成员
-keepclassmembers class com.zxy.recovery.core.Recovery{
    public static ** getInstance();
    public *;
}

# 不混淆内部类
-keep class com.zxy.recovery.core.Recovery$SilentMode{
    *;
}