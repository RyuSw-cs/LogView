package com.ryusw.logview.api;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.ryusw.logview.config.LogViewResultCode;
import com.ryusw.logview.context.LogViewInitContext;
import com.ryusw.logview.service.LogService;

public class LogViewApi {
    private Context mContext;
    private LogViewInitContext mParams;
    public LogViewApi(Context context, LogViewInitContext params){
        this.mContext = context;
        this.mParams = params;
    }

    public void startLogView(){
        if(checkPermission()){
            Intent serviceIntent = new Intent(mContext, LogService.class);
            ComponentName componentName = mContext.startService(serviceIntent);
            if(componentName == null){
                mParams.getLogResultCallBackInterface().onFailure(LogViewResultCode.FAIL_NOT_REGISTERED_MANIFEST);
            } else {
                mParams.getLogResultCallBackInterface().onSuccess();
            }
        } else {
            mParams.getLogResultCallBackInterface().onFailure(LogViewResultCode.FAIL_NOT_ALLOW_PERMISSION);
        }
    }

    private boolean checkPermission(){
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED
    }
}
