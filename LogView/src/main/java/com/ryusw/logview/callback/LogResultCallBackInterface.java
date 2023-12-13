package com.ryusw.logview.callback;

public interface LogResultCallBackInterface {
    void onSuccess();
    void onFailure(int errorCode, String errorMsg);
}
