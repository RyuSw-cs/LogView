package com.ryusw.logview.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Util {
    public static boolean checkPermission(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
}
