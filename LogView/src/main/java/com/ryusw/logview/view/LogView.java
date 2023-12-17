package com.ryusw.logview.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * UI 관련 로직 구현
 * */
public class LogView extends ConstraintLayout {

    private ViewGroup mViewGroup;
    private ImageButton mBtnSetting;
    private ImageButton mBtnControl;
    private ImageButton mBtnStop;
    private ImageButton mBtnClose;
    private TextView mTvLog;
    private String mLog;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public LogView(@NonNull Context context) {
        super(context);
        doCreateLayout(context);
    }

    public LogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        doCreateLayout(context);
    }

    public LogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        doCreateLayout(context);
    }

    /**
     * 레이아웃 생성
     * @param context 생성자에서 받아온 context
     * @author swyu
     * */
    private void doCreateLayout(Context context) {
        mViewGroup = (ViewGroup) LayoutInflater.from(context).inflate(getResourceId("layout", "view_log"), this, false);

        mBtnControl = mViewGroup.findViewById(getResourceId("id", "btn_control"));
        mBtnStop = mViewGroup.findViewById(getResourceId("id", "btn_stop"));
        mBtnClose = mViewGroup.findViewById(getResourceId("id", "btn_close"));
        mBtnSetting = mViewGroup.findViewById(getResourceId("id", "btn_setting"));

        mTvLog = mViewGroup.findViewById(getResourceId("id", "tv_log"));

        addView(mViewGroup);
    }

    /**
     * 세팅 버튼 클릭 리스너 설정
     *
     * @param listener 클릭 리스너
     * @author swyu
     */
    public void setSettingBtnClickListener(View.OnClickListener listener) {
        mBtnSetting.setOnClickListener(listener);
    }

    /**
     * 컨트롤 버튼 클릭 리스너 설정
     *
     * @param listener 클릭 리스너
     * @author swyu
     */
    public void setControlBtnClickListener(View.OnClickListener listener) {
        mBtnControl.setOnClickListener(listener);
    }

    /**
     * 정지 버튼 클릭 리스너 설정
     *
     * @param listener 클릭 리스너
     * @author swyu
     */
    public void setStopBtnClickListener(View.OnClickListener listener) {
        mBtnStop.setOnClickListener(listener);
    }

    /**
     * 닫기 버튼 클릭 리스터
     *
     * @param listener 클릭 리스너
     * @author swyu
     */
    public void setCloseBtnClickListener(View.OnClickListener listener) {
        mBtnClose.setOnClickListener(listener);
    }

    public void setControlBtnImg(boolean isRunning){
        // 실행중이라면 일시정지 버튼
        if(isRunning) mBtnControl.setImageResource(getResourceId("drawable", "icon_pause"));
        // 실행중이 아니라면 재생 버튼
        else mBtnControl.setImageResource(getResourceId("drawable", "icon_play"));
    }

    /**
     * 로그 Text 설정
     *
     * @param log Process에서 받은 Log String
     * @author swyu
     */
    public void setLogText(String log) {
        // Sub Thread에서 텍스트 추가
        this.mLog += log;
        // Main Thread에서 View Update
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTvLog.setText(mLog);
            }
        });
    }

    /**
     * 난독화 설정으로 인해 resId를 못가져오는 현상을 막기 위함
     * @param type 리소스 타입 (id, drawable, layout...)
     * @param name 가져오려는 리소스 이름
     * @return 라이브러리에 실제 할당된 resId
     * @author swyu
     */
    private int getResourceId(String type, String name) {
        return getContext().getResources().getIdentifier(name, type, getContext().getPackageName());
    }
}