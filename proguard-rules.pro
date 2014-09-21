# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /disk1/Apps/android-studio/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Jackson
-libraryjars ../lib_base/libs/jackson-annotations-2.4.2.jar
-libraryjars ../lib_base/libs/jackson-core-2.4.2.jar
-libraryjars ../lib_base/libs/jackson-databind-2.4.2.jar

-dontskipnonpubliclibraryclassmembers

-keepnames class com.fasterxml.jackson.** { *; }

-keepattributes *Annotation*,EnclosingMethod,Signature

-dontwarn javax.xml.**
-dontwarn javax.xml.stream.events.**
-dontwarn com.fasterxml.jackson.databind.**

-keep public class com.micabyte.android.** {
  public void set*(***);
  public *** get*();
}