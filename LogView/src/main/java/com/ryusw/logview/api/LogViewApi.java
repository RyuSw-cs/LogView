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

import com.ryusw.logview.LogDataManger;
import com.ryusw.logview.config.LogViewResultCode;
import com.ryusw.logview.config.LogViewResultMsg;
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

    /**
     * 로그뷰 실행
     * */
    public void startLogView() {
        if (checkPermission()) {
            Intent serviceIntent = new Intent(mContext, LogService.class);
            mContext.startService(serviceIntent);

            // LogFilter 적용
            LogDataManger.SetLogFilter(mParams.getLogFilter());
            mParams.getLogResultCallBackInterface().onSuccess();
        } else {
            Log.d(CLASSNAME, "startLogView : not allowed permission");
            mParams.getLogResultCallBackInterface().onFailure(LogViewResultCode.FAIL_NOT_ALLOW_PERMISSION, LogViewResultMsg.FAIL_NOT_ALLOW_PERMISSION_MSG);
        }
    }

    /**
     * 권한 체크 확인
     * @return 앱 위에 그리기 허용 여부
     * */
    private boolean checkPermission() {
        // 23이상부터 해당 권한 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mContext);
        }
        return true;
    }
}
