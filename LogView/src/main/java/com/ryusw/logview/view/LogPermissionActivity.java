package com.ryusw.logview.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.ryusw.logview.R;
import com.ryusw.logview.callback.LogPermissionCallbackInterface;
import com.ryusw.logview.config.LogViewStateCode;
import com.ryusw.logview.util.LogUtil;
import com.ryusw.logview.util.Util;

public class LogPermissionActivity extends AppCompatActivity {
    private static String CLASSNAME = "LogPermissionActivity";
    private static LogPermissionCallbackInterface logPermissionCallbackInterface;
    private static String callPackageName;

    public static void startPermissionActivity(Intent intent, Context context, LogPermissionCallbackInterface callbackInterface) {
        LogUtil.d(CLASSNAME, "startPermissionActivity", "start");
        logPermissionCallbackInterface = callbackInterface;
        callPackageName = context.getPackageName();
        LogUtil.i(CLASSNAME, "startPermissionActivity", "calling package name = " + callPackageName);
        context.startActivity(intent);
        LogUtil.d(CLASSNAME, "startPermissionActivity", "end");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.d(CLASSNAME, "onCreate", "start");
        overridePendingTransition(0,0);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        doCreateLayout();
        LogUtil.d(CLASSNAME, "onCreate", "end");
    }

    private void doCreateLayout() {
        LogUtil.d(CLASSNAME, "doCreateLayout", "start");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View permissionView = layoutInflater.inflate(getResourceId("layout", "view_permission"), null);

        permissionView.findViewById(getResourceId("id", "btn_request_permission")).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.fromParts("package", callPackageName, null));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, LogViewStateCode.REQ_PERMISSION_CODE);
            }
        });

        permissionView.findViewById(getResourceId("id", "btn_denied_permission")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logPermissionCallbackInterface.onDenyPermission();
                finish();
            }
        });

        new AlertDialog.Builder(this, getResourceId("style", "permissionDialogStyle"))
                .setCancelable(false)
                .setView(permissionView)
                .show();

        LogUtil.d(CLASSNAME, "doCreateLayout", "end");
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(CLASSNAME, "onDestroy", "start");
        super.onDestroy();
        overridePendingTransition(0,0);
        LogUtil.d(CLASSNAME, "onDestroy", "end");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        LogUtil.d(CLASSNAME, "onActivityResult", "start");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != LogViewStateCode.REQ_PERMISSION_CODE) {
            return;
        }

        if (Util.checkPermission(this)) {
            logPermissionCallbackInterface.onAllowPermission();
        } else {
            logPermissionCallbackInterface.onDenyPermission();
        }

        finish();
        LogUtil.d(CLASSNAME, "onActivityResult", "end");
    }

    private int getResourceId(String type, String name) {
        return getResources().getIdentifier(name, type, getPackageName());
    }
}
