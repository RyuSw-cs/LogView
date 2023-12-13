package com.ryusw.logview.api;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.ryusw.logview.config.LogViewResultCode;
import com.ryusw.logview.context.LogViewInitContext;
import com.ryusw.logview.service.LogService;

public class LogViewApi {
    private static final String CLASSNAME = "LogViewApi";
    private Context mContext;
    private LogViewInitContext mParams;

    public LogViewApi(Context context, LogViewInitContext params) {
        this.mContext = context;
        this.mParams = params;
    }

    public void startLogView() {
        if (checkPermission()) {
            Intent serviceIntent = new Intent(mContext, LogService.class);

            ComponentName componentName;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                componentName = mContext.startForegroundService(serviceIntent);
            } else {
                componentName = mContext.startService(serviceIntent);
            }
            if (componentName == null) {
                Log.d(CLASSNAME, "startLogView : not registered manifest");
                mParams.getLogResultCallBackInterface().onFailure(LogViewResultCode.FAIL_NOT_REGISTERED_MANIFEST, createErrorMessage(LogViewResultCode.FAIL_NOT_REGISTERED_MANIFEST));
            } else {
                Log.d(CLASSNAME, "startLogView : log view init");
                mParams.getLogResultCallBackInterface().onSuccess();
            }
        } else {
            Log.d(CLASSNAME, "startLogView : not allowed permission");
            mParams.getLogResultCallBackInterface().onFailure(LogViewResultCode.FAIL_NOT_ALLOW_PERMISSION, createErrorMessage(LogViewResultCode.FAIL_NOT_ALLOW_PERMISSION));
        }
    }

    private String createErrorMessage(int errorCode) {
        switch (errorCode) {
            case 100200:
                return "권한 설정이 필요합니다";
            case 100201:
                return "매니페스트 설정이 필요합니다";
            default:
                return "존재하지 않는 에러코드 입니다";
        }
    }

    private boolean checkPermission() {
        // 23이상부터 해당 권한 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mContext);
        }
        return true;
    }
}
