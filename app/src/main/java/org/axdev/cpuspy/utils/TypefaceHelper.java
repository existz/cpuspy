package org.axdev.cpuspy.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.util.SimpleArrayMap;

/*
    Each call to Typeface.createFromAsset will load a new instance of the typeface into memory,
    and this memory is not consistently get garbage collected
    http://code.google.com/p/android/issues/detail?id=9904
    (It states released but even on Lollipop you can see the typefaces accumulate even after
    multiple GC passes)

    You can detect this by running:
    adb shell dumpsys meminfo com.your.packagenage

    You will see output like:

     Asset Allocations
        zip:/data/app/com.your.packagenage-1.apk:/assets/Roboto-Medium.ttf: 125K
        zip:/data/app/com.your.packagenage-1.apk:/assets/Roboto-Medium.ttf: 125K
        zip:/data/app/com.your.packagenage-1.apk:/assets/Roboto-Medium.ttf: 125K
        zip:/data/app/com.your.packagenage-1.apk:/assets/Roboto-Regular.ttf: 123K
        zip:/data/app/com.your.packagenage-1.apk:/assets/Roboto-Medium.ttf: 125K

*/
public class TypefaceHelper {

    // Roboto-Medium font
    public static final String MEDIUM_FONT = "Roboto-Medium";

    private static final SimpleArrayMap<String, Typeface> cache = new SimpleArrayMap<>();

    public static Typeface mediumTypeface(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Typeface.create("sans-serif-medium", Typeface.NORMAL);
        } else {
            synchronized (cache) {
                if (!cache.containsKey(MEDIUM_FONT)) {
                    final Typeface t = Typeface.createFromAsset(
                            c.getAssets(), String.format("fonts/%s.ttf", MEDIUM_FONT));
                    cache.put(MEDIUM_FONT, t);
                    return t;
                }
                return cache.get(MEDIUM_FONT);
            }
        }
    }
}
