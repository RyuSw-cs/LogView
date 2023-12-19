package com.ryusw.logview.config;

public class LogResultCode {
    public static int BASE_APP_CODE                 = 100000;
    public static int SUCCESS_RESULT_OK             = BASE_APP_CODE + 100;      // 성공
    public static int FAIL_NOT_ALLOW_PERMISSION     = BASE_APP_CODE + 200;      // 권한 없음
    public static int FAIL_LOG_OBSERVER             = BASE_APP_CODE + 202;      // Observing 중 오류 발생
    public static int FAIL_LOG_CLEAR                = BASE_APP_CODE + 203;      // Log clear 실패
    public static int FAIL_PROCESS_RESULT_NULL      = BASE_APP_CODE + 204;      // Log 실행결과가 null
}
