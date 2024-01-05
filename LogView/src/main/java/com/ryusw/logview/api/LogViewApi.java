package com.ryusw.logview.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.ryusw.logview.callback.LogPermissionCallbackInterface;
import com.ryusw.logview.config.LogConstants;
import com.ryusw.logview.config.LogResultCode;
import com.ryusw.logview.config.LogResultMsg;
import com.ryusw.logview.context.LogViewInitContext;
import com.ryusw.logview.service.LogService;
import com.ryusw.logview.util.LogUtil;
import com.ryusw.logview.view.LogPermissionActivity;

public class LogViewApi {
    private static final String CLASSNAME = "LogViewApi";
    private Context mContext;
    private LogViewInitContext mParams;

    public LogViewApi(Context context, LogViewInitContext params) {
        LogUtil.d(CLASSNAME, "Constructor", "start");
        this.mContext = context;
        this.mParams = params;
        LogUtil.d(CLASSNAME, "Constructor", "end");
    }

    /**
     * 로그뷰 실행
     * */
    public void startLogView() {
        LogUtil.d(CLASSNAME, "startLogView", "start");
        if (checkPermission()) {
            LogUtil.v(CLASSNAME, "startLogView", "Permission Allowed");
            startLogViewService();
            mParams.getLogCallbackInterface().onSuccess();
        } else {
            LogUtil.v(CLASSNAME, "startLogView", "Permission Denied");
            Intent intent = new Intent(mContext, LogPermissionActivity.class);
            LogPermissionActivity.startPermissionActivity(intent, mContext, new LogPermissionCallbackInterface() {
                @Override
                public void onAllowPermission() {
                    LogUtil.d(CLASSNAME, "onAllowPermission", "Permission Allowed");
                    startLogViewService();
                    mParams.getLogCallbackInterface().onSuccess();
                }

                @Override
                public void onDenyPermission() {
                    LogUtil.w(CLASSNAME, "onDenyPermission", "Permission Denied");
                    mParams.getLogCallbackInterface().onFailure(LogResultCode.FAIL_NOT_ALLOW_PERMISSION, LogResultMsg.FAIL_NOT_ALLOW_PERMISSION_MSG);
                }
            });
        }
        LogUtil.d(CLASSNAME, "startLogView", "end");
    }

    private void startLogViewService(){
        LogUtil.d(CLASSNAME, "startLogViewService", "start");
        Intent serviceIntent = new Intent(mContext, LogService.class);
        serviceIntent.putExtra(LogConstants.EXTRATYPE_LOG_FILTER, mParams.getLogFilter());
        serviceIntent.putExtra(LogConstants.EXTRATYPE_APP_PROCESS_ID, mParams.getPid());
        serviceIntent.putExtra(LogConstants.EXTRATYPE_APP_AUTO_SCROLL, mParams.getAutoScroll());
        mContext.startService(serviceIntent);
        LogUtil.d(CLASSNAME, "startLogViewService", "end");
    }

    /**
     * 권한 체크 확인
     * @return 앱 위에 그리기 허용 여부
     * */
    private boolean checkPermission() {
        LogUtil.d(CLASSNAME, "checkPermission", "start");
        // 23이상부터 해당 권한 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mContext);
        }
        LogUtil.d(CLASSNAME, "checkPermission", "end");
        return true;
    }
}
