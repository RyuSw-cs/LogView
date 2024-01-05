package com.ryusw.logview.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
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
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

/**
 * UI 관련 로직 구현
 * */
public class LogView extends ConstraintLayout {

    private ViewGroup mMainViewGroup;
    private ViewGroup mAppbarViewGroup;
    private ViewGroup mErrorViewGroup;
    private ImageButton mBtnSetting;
    private ImageButton mBtnControl;
    private ImageButton mBtnStop;
    private ImageButton mBtnClose;
    private TextView mTvLog;
    private NestedScrollView mScrollView;
    private String mLog = "";
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mAutoScrollMode = false;
    private SpannableStringBuilder spBuilder = new SpannableStringBuilder();
    private int mCurrentLogColor = Color.WHITE;

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
        // main
        mMainViewGroup = (ViewGroup) LayoutInflater.from(context).inflate(getResourceId("layout", "view_log"), this, false);
        mScrollView = mMainViewGroup.findViewById(getResourceId("id", "sv_log"));
        mTvLog = mMainViewGroup.findViewById(getResourceId("id", "tv_log"));

        // appbar
        mAppbarViewGroup = mMainViewGroup.findViewById(getResourceId("id", "layout_appbar"));
        mBtnControl = mAppbarViewGroup.findViewById(getResourceId("id", "btn_control"));
        mBtnStop = mAppbarViewGroup.findViewById(getResourceId("id", "btn_stop"));
        mBtnClose = mAppbarViewGroup.findViewById(getResourceId("id", "btn_close"));
        mBtnSetting = mAppbarViewGroup.findViewById(getResourceId("id", "btn_setting"));

        // error
        mErrorViewGroup = mMainViewGroup.findViewById(getResourceId("id", "layout_error"));

        addView(mMainViewGroup);
    }

    public void setAutoScrollMode(boolean flag){
        mAutoScrollMode = flag;
    }

    public void setErrorViewVisibility(boolean visibility, int errorCode, String errorMsg){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(visibility){
                    mErrorViewGroup.bringToFront();
                    mErrorViewGroup.setVisibility(VISIBLE);
                    TextView contentView = mErrorViewGroup.findViewById(getResourceId("id", "tv_error_text"));
                    String content = "오류" + "(" + errorCode + ")" + " : " + errorMsg;
                    contentView.setText(content);
                } else {
                    mErrorViewGroup.setVisibility(GONE);
                }
            }
        });
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
    public synchronized void setLogText(String log) {
        // Sub Thread에서 텍스트 추가
        String logLevel = log.substring(1, 3);
        setLogColor(logLevel, log);
        // Main Thread에서 View Update
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTvLog.setText(spBuilder);

                if(mAutoScrollMode){
                    mScrollView.smoothScrollTo(0, mScrollView.getChildAt(0).getHeight());
                }
            }
        });
    }

    /**
     * 로그색상 지정
     * */
    private void setLogColor(String logType, String log){
        String colorResName = "";
        // 줄바꿈 상태라면 기존 선택된 색상으로 설정
        switch (logType){
            case "D/":
                colorResName = "log_debug_color";
                break;
            case "I/":
                colorResName = "log_info_color";
                break;
            case "W/" :
                colorResName = "log_warn_color";
                break;
            case "E/" :
                colorResName = "log_error_color";
                break;
            default:
                break;
        }

        if(!colorResName.isEmpty()){
            mCurrentLogColor = ContextCompat.getColor(getContext(), getResourceId("color", colorResName));
        }

        spBuilder.insert(mLog.length(), log);
        spBuilder.setSpan(new ForegroundColorSpan(mCurrentLogColor), mLog.length(), mLog.length() + log.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mLog += log;
    }


    /**
     * 현재까지 기록된 로그 초기화
     * @author swyu
     * */
    public void clearLogText(){
        this.mLog = "";
        mTvLog.setText(this.mLog);
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
