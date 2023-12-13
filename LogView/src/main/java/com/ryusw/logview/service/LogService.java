package com.ryusw.logview.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LogService extends Service {

    private WindowManager.LayoutParams mRootViewParams;
    private WindowManager mWindowManager;
    private ViewGroup mViewLog;
    private DisposableObserver<String> mLogObserver;
    private Observable<String> mLogProcessRecorder;
    private TextView mTvLog;
    private Button mBtnStart;
    private Button mBtnStop;
    private ImageButton mBtnClose;
    private float mTouchX = 0f;
    private float mTouchY = 0f;
    private int mViewX = 0;
    private int mViewY = 0;
    private String mLogString = "";
    private Boolean mIsRunning = false;
    private static final int logBufferSize = 4 * 1024;
    private List<String> filterList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewLog = (ViewGroup) layoutInflater.inflate(getResourceId("layout", "view_log"), null);

        mBtnClose = (ImageButton) mViewLog.findViewById(getResourceId("id", "btn_close"));
        mBtnStop = (Button) mViewLog.findViewById(getResourceId("id", "btn_stop"));
        mBtnStart = (Button) mViewLog.findViewById(getResourceId("id", "btn_start"));
        mTvLog = (TextView) mViewLog.findViewById(getResourceId("id", "tv_log"));

        mBtnClose.setImageResource(getResourceId("drawable", "icon_close"));

        int managerType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            managerType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            managerType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mRootViewParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                managerType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mLogProcessRecorder = Observable.create(emitter -> {
            clearLog();

            String currentPid = "--pid=" + android.os.Process.myPid();
            String[] logCommand = {"logcat", "-v", currentPid};
            ProcessBuilder logProcessBuilder = new ProcessBuilder(logCommand);

            Process logProcess = logProcessBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(logProcess.getInputStream()), logBufferSize);
            String separator = System.getProperty("line.separator");

            String line = br.readLine();

            while (line != null) {
                emitter.onNext(line + separator);
                line = br.readLine();
            }
        });

        mLogObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(@NonNull String s) {
                if (!checkLogFilter(s)) {
                    writeLogView(s);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                stopSelf();
                mIsRunning = false;
            }

            @Override
            public void onComplete() {
                mIsRunning = false;
            }
        };

        filterList.add("ViewRoot's Touch Event");
        filterList.add("Accessing hidden");

        mViewLog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchX = motionEvent.getRawX();
                        mTouchY = motionEvent.getRawY();
                        mViewX = mRootViewParams.x;
                        mViewY = mRootViewParams.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) (motionEvent.getRawX() - mTouchX);
                        int y = (int) (motionEvent.getRawY() - mTouchY);

                        mRootViewParams.x = mViewX + x;
                        mRootViewParams.y = mViewY + y;

                        mWindowManager.updateViewLayout(mViewLog, mRootViewParams);
                        return true;
                }
                return false;
            }
        });
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsRunning) {
                    mLogProcessRecorder.subscribeOn(Schedulers.io()).subscribe(mLogObserver);
                    mIsRunning = true;
                } else {
                    mLogString = "";
                    mTvLog.setText(mLogString);
                }
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsRunning) {
                    mIsRunning = false;
                    mLogObserver.dispose();
                }
            }
        });

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsRunning = false;
                stopSelf();
            }
        });

        mWindowManager.addView(mViewLog, mRootViewParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViewLog != null && mWindowManager != null) {
            mWindowManager.removeView(mViewLog);
            mViewLog = null;
            mWindowManager = null;
        }
    }

    private void writeLogView(String logString) {
        mLogString += logString;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mLogString != null) {
                    mTvLog.setText(mLogString);
                }
            }
        });
    }

    private void clearLog() throws IOException, InterruptedException {
        String[] clearLogCommand = {"logcat", "-c"};
        ProcessBuilder logProcessBuilder = new ProcessBuilder(clearLogCommand);
        Process logProcess = logProcessBuilder.start();

        logProcess.waitFor();
        logProcess.destroy();
    }

    private boolean checkLogFilter(String log) {
        for (String filterString : filterList) {
            if (log.contains(filterString)) {
                return true;
            }
        }
        return false;
    }

    private int getResourceId(String type, String name) {
        return this.getResources().getIdentifier(name, type, getPackageName());
    }
}
