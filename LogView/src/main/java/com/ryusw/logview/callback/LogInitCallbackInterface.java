package com.ryusw.logview.callback;

public interface LogInitCallbackInterface {
    void onSuccess();
    void onFailure(int errorCode, String errorMsg);
}
