package com.ryusw.logview.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ryusw.logview.LogDataManger;
import com.ryusw.logview.callback.LogObservingCallBackInterface;
import com.ryusw.logview.config.LogConstants;
import com.ryusw.logview.config.LogViewStateCode;
import com.ryusw.logview.view.LogView;

public class LogService extends Service {
    private static final String NOTIFICATION_CHANNEL = "LOGVIEW_NOTIFICATION_CHANNEL";
    private WindowManager.LayoutParams mRootViewParams;
    private WindowManager mWindowManager;
    private LogView mViewLog;
    private float mTouchX = 0f;
    private float mTouchY = 0f;
    private int mViewX = 0;
    private int mViewY = 0;
    private Boolean mIsRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 프로세스가 다르기때문에 Service에서 데이터를 초기화 해야함
        if (intent != null) {
            // 로그 필터 설정
            String[] logFilter = intent.getStringArrayExtra(LogConstants.EXTRATYPE_LOG_FILTER);
            if (logFilter != null) {
                LogDataManger.getInstance().setLogFilter(logFilter);
            }

            // 프로세스 id 설정
            int appPid = intent.getIntExtra(LogConstants.EXTRATYPE_APP_PROCESS_ID, -1);
            LogDataManger.getInstance().setAppPid(appPid);

            // 자동 스크롤 모드 설정
            boolean autoScrollMode = intent.getBooleanExtra(LogConstants.EXTRATYPE_APP_AUTO_SCROLL, false);
            mViewLog.setAutoScrollMode(autoScrollMode);

            return super.onStartCommand(intent, flags, startId);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 오레오 이상에서 Notification 생성해야 Foreground Service 사용 가능
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification();
        }

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

        mViewLog = new LogView(this);

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

        mViewLog.setControlBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 실행
                if (!mIsRunning) {
                    mIsRunning = true;
                    mViewLog.setControlBtnImg(true);

                    mViewLog.clearLogText();
                    // 프로세스가 다르므로 pid를 설정해야함.
                    LogDataManger.getInstance().startLog(true, new LogObservingCallBackInterface() {
                        @Override
                        public void onSuccess(String log) {
                            if(mIsRunning){
                                mViewLog.setErrorViewVisibility(false, LogViewStateCode.NO_ERROR_CODE, null);
                            }

                            if (!LogDataManger.getInstance().isExistLogFilter(log)) {
                                mViewLog.setLogText(log);
                            }
                        }
                        @Override
                        public void onFailure(int errorCode, String errorMsg) {
                            mViewLog.setErrorViewVisibility(true, errorCode, errorMsg);
                            mViewLog.setControlBtnImg(false);
                            mIsRunning = false;
                            LogDataManger.getInstance().stopLog();
                        }
                    });
                } else {
                    mViewLog.setControlBtnImg(false);
                    mIsRunning = false;
                    LogDataManger.getInstance().stopLog();
                }
            }
        });
        mViewLog.setStopBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogDataManger.getInstance().stopLog();
            }
        });

        mViewLog.setSettingBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 세팅버튼 레이아웃 추가
            }
        });

        mViewLog.setCloseBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogDataManger.getInstance().stopLog();
                stopSelf();
            }
        });

        mWindowManager.addView(mViewLog, mRootViewParams);
    }

    /**
     * Notification 생성
     *
     * @author swyu
     */
    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String title = getString(getResourceId("string", "notification_title"));
            String content = getString(getResourceId("string", "notification_content"));
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, title, importance);
            notificationChannel.setDescription(content);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(getResourceId("drawable", "icon_setting"))
                    .build();

            // Android 14이상은 Foreground Service Type을 명시해야함
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(1, notification);
            }
        }
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

    /**
     * 난독화 설정으로 인해 resId를 못가져오는 현상을 막기 위함
     *
     * @param type 리소스 타입 (id, drawable, layout...)
     * @param name 가져오려는 리소스 이름
     * @return 라이브러리에 실제 할당된 resId
     * @author swyu
     */
    private int getResourceId(String type, String name) {
        return this.getResources().getIdentifier(name, type, getPackageName());
    }
}
