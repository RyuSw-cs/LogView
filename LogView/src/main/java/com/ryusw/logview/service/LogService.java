package com.ryusw.logview.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
    private View mLogView;
    private DisposableObserver<String> mLogObserver;
    private Observable<String> mLogProcessRecorder;
    private TextView mTvLog;
    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnClose;
    private float mTouchX = 0f;
    private float mTouchY = 0f;
    private int mViewX = 0;
    private int mViewY = 0;
    private String mLogString = "";
    private Boolean mIsRunning = false;
    private static final int logBufferSize = 4 * 1024;

    public LogService() {
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

            while(line != null){
                emitter.onNext(line + separator);
                line = br.readLine();
            }
        });

        mLogObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(@NonNull String s) {
                if(!checkLogFilter(s)){
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
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager.addView(mLogView, mRootViewParams);

        mLogView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
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

                        mWindowManager.updateViewLayout(mLogView, mRootViewParams);
                        return true;
                }
                return false;
            }
        });
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsRunning){
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
                if(mIsRunning){
                    mIsRunning = false;
                    mLogObserver.dispose();
                }
            }
        });

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWindowManager.removeView(mLogView);
                mIsRunning = false;
                stopSelf();
            }
        });
    }

    private void writeLogView(String logString){
        // Todo Change Thread
        mLogString += logString;
        mTvLog.setText(mLogString);
    }

    private void clearLog() throws IOException, InterruptedException {
        String[] clearLogCommand = {"logcat", "-c"};
        ProcessBuilder logProcessBuilder = new ProcessBuilder(clearLogCommand);
        Process logProcess = logProcessBuilder.start();

        logProcess.waitFor();
        logProcess.destroy();
    }

    private boolean checkLogFilter(String log){
        List<String> filterList = new ArrayList<>();
        filterList.add("ViewRoot's Touch Event");
        filterList.add("Accessing hidden");

        for(String filterString : filterList){
            if(log.contains(filterString)){
                return true;
            }
        }
        return false;
    }
}
