##---------------Begin: proguard configuration common for all Android apps ----------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-allowaccessmodification
-repackageclasses ''

-libraryjars libs
# 이미 ProGuard를 사용한 라이브러리들을 다시 ProGuard가 검사할 필요 없게 해서 빌드 시간을 줄이는 팁
-keep class com.crashlytics.** { *; }
# 소스 파일의 줄 번호 정보를 유지
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keep class **.R
-keep class **.R$* {
<fields>;
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
  public static <fields>;
}

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class * {
    public protected *;
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

##---------------End: proguard configuration common for all Android apps ----------

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface


-keep public class android.arch.core.internal.** { *; }
-dontwarn android.arch.core.internal.**

-keep public class android.arch.lifecycle.** { *; }
-dontwarn android.arch.lifecycle.**

-keep public class android.support.constraint.** { *; }
-dontwarn android.support.constraint.**

-keep public class android.support.constraint.solver.** { *; }
-dontwarn android.support.constraint.solver.**

-keep public class android.support.test.espresso.** { *; }
-dontwarn android.support.test.espresso.**

-keep public class android.support.test.** { *; }
-dontwarn android.support.test.**

-keep public class android.support.graphics.drawable.** { *; }
-dontwarn android.support.graphics.drawable.**

-keep public class android.support.v7.** { *; }
-dontwarn android.support.v7.**

-keep public class android.support.design.** { *; }
-dontwarn android.support.design.**

-keep public class android.support.multidex.** { *; }
-dontwarn android.support.multidex.**

-keep public class android.support.multidex.instrumentation.** { *; }
-dontwarn android.support.multidex.instrumentation.**

-keep public class android.support.annotation.** { *; }
-dontwarn android.support.annotation.**

-keep public class android.support.** { *; }
-dontwarn android.support.**

-keep public class android.support.v4.** { *; }
-dontwarn android.support.v4.**

-keep public class android.support.graphics.drawable.** { *; }
-dontwarn android.support.graphics.drawable.**

-keep public class android.support.transition.** { *; }
-dontwarn android.support.transition.**

-keep public class com.android.volley.** { *; }
-dontwarn com.android.volley.**

-keep public class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

-keep public class com.bumptech.glide.annotation.** { *; }
-dontwarn com.bumptech.glide.annotation.**

-keep public class com.bumptech.glide.disklrucache.** { *; }
-dontwarn com.bumptech.glide.disklrucache.**

-keep public class com.bumptech.glide.gifdecoder.** { *; }
-dontwarn com.bumptech.glide.gifdecoder.**

-keep public class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

-keep public class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep public class com.google.** { *; }
-dontwarn com.google.**

-keep public class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**

-keep public class com.google.android.gms.internal.measurement.** { *; }
-dontwarn com.google.android.gms.internal.measurement.**

-keep public class com.google.android.gms.stats.** { *; }
-dontwarn com.google.android.gms.stats.**

-keep public class com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.tasks.**

-keep public class com.google.api.client.googleapis.** { *; }
-dontwarn com.google.api.client.googleapis.**

-keep public class com.google.api.client.googleapis.extensions.android.** { *; }
-dontwarn com.google.api.client.googleapis.extensions.android.**

-keep public class com.google.api.services.youtube.** { *; }
-dontwarn com.google.api.services.youtube.**

-keep public class javax.annotation.** { *; }
-dontwarn javax.annotation.**

-keep public class com.google.gson.** { *; }
-dontwarn com.google.gson.**

-keep public class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

-keep public class com.google.firebase.database.** { *; }
-dontwarn com.google.firebase.database.**

-keep public class com.google.firebase.database.collection.** { *; }
-dontwarn com.google.firebase.database.collection.**

-keep public class com.google.firebase.iid.internal.** { *; }
-dontwarn com.google.firebase.iid.internal.**

-keep public class com.google.firebase.analytics.connector.** { *; }
-dontwarn com.google.firebase.analytics.connector.**

-keep public class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

-keep public class com.google.api.client.extensions.android.** { *; }
-dontwarn com.google.api.client.extensions.android.**

-keep public class com.google.api.client.json.jackson2.** { *; }
-dontwarn com.google.api.client.json.jackson2.**

-keep public class com.google.j2objc.annotations.** { *; }
-dontwarn com.google.j2objc.annotations.**

-keep public class com.google.api.client.auth.** { *; }
-dontwarn com.google.api.client.auth.**

-keep public class com.google.i18n.phonenumbers.** { *; }
-dontwarn com.google.i18n.phonenumbers.**

-keep public class okhttp3.** { *; }
-dontwarn okhttp3.**

-keep public class okio.** { *; }
-dontwarn okio.**

-keep public class org.apache.commons.codec.** { *; }
-dontwarn org.apache.commons.codec.**

-keep public class org.apache.commons.logging.** { *; }
-dontwarn org.apache.commons.logging.**

#### -- OkHttp --
-keep public class com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-keep public class com.squareup.javawriter.** { *; }
-dontwarn com.squareup.javawriter.**

-keep class javax.inject.** { *; }
-dontwarn javax.inject.**

-keep class org.junit.** { *; }
-dontwarn org.junit.**

-keep class org.kxml2.** { *; }
-dontwarn org.kxml2.**

-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**

-keep public class org.apache.http.** { *; }
-dontwarn org.apache.http.**

-keep public class org.hamcrest.** { *; }
-dontwarn org.hamcrest.**

-keep public class kotlin.** { *; }
-dontwarn kotlin.**

-keep public class com.enabledaonsoft.thecamp.GlideApp.** { *; }
-dontwarn com.enabledaonsoft.thecamp.GlideApp.**

-keep public class com.enabledaonsoft.thecamp.GlideOptions.** { *; }
-dontwarn com.enabledaonsoft.thecamp.GlideOptions.**

-keep public class com.enabledaonsoft.thecamp.GlideRequest.** { *; }
-dontwarn com.enabledaonsoft.thecamp.GlideRequest.**

-keep public class com.enabledaonsoft.thecamp.GlideRequests.** { *; }
-dontwarn com.enabledaonsoft.thecamp.GlideRequests.**

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

##---------------End: proguard configuration for Gson  ----------


# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class com.nbsoft.tv.model.** {
  *;
}
