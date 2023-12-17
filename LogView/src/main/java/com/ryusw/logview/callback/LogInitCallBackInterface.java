package com.ryusw.logview.callback;

public interface LogInitCallBackInterface {
    void onSuccess();
    void onFailure(int errorCode, String errorMsg);
}
