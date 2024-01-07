package com.ryusw.logview.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Util {
    /**
     * 다른 창 띄우기 권한 확인
     * 마시멜로우 이상에서 확인해야함
     * @return 권한 설정 여부
     * */
    public static boolean checkPermission(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
}
