package com.ryusw.logview.callback;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public interface LogCallbackInterface {
    void onSuccess();
    void onFailure(int errorCode, String errorMsg);
}
