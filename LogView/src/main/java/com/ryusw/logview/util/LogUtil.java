package com.ryusw.logview.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 로그뷰에서 사용되는 로그 함수
 * @author swyu
 * */
public class LogUtil {
    private static final String TAG = "LOGVIEW";

    /**
     * Log Level Debug
     * 클래스나 메소드의 start, end에서 활용
     * */
    public static void d(@NonNull String className, @NonNull String methodName, @Nullable String message){
        String log = createLogString(className, methodName, message);
        Log.d(TAG, log);
    }

    /**
     * Log Level Error
     * 잘못된 값이나 Exception이 발생되는 경우
     * @author swyu
     * */
    public static void e(@NonNull String className, @NonNull String methodName, @Nullable String message){
        String log = createLogString(className, methodName, message);
        Log.e(TAG, log);
    }

    /**
     * Log Level Warning
     * 값이 잘못됐지만 exception이 발생되지 않는 경우
     * */
    public static void w(@NonNull String className, @NonNull String methodName, @Nullable String message){
        String log = createLogString(className, methodName, message);
        Log.w(TAG, log);
    }

    /**
     * Log Level Info
     * 파라미터 혹은 필요한 데이터 출력
     * */
    public static void i(@NonNull String className, @NonNull String methodName, @Nullable String message){
        String log = createLogString(className, methodName, message);
        Log.i(TAG, log);
    }

    /**
     * Log Level Verbose
     * 메소드의 동작 상태를 확인하기 위함
     * */
    public static void v(@NonNull String className, @NonNull String methodName, @Nullable String message){
        String log = createLogString(className, methodName, message);
        Log.v(TAG, log);
    }

    /**
     * Log String 생성
     * */
    private static String createLogString(String className, String methodName, String message){
        return className + " > " + methodName + "() : " + message;
    }
}
