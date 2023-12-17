package com.ryusw.logview.callback;

public interface LogObservingCallBackInterface {
    void onSuccess(String log);
    void onFailure(int errorCode, String errorMsg);
}
