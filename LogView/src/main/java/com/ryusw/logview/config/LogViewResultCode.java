package com.ryusw.logview.config;

public class LogViewResultCode {
    public static int BASE_APP_CODE                 = 100000;
    public static int SUCCESS_INIT_CODE             = BASE_APP_CODE + 100;      // 로그뷰 초기화 성공
    public static int FAIL_NOT_ALLOW_PERMISSION     = BASE_APP_CODE + 200;      // 권한 없음
    public static int FAIL_NOT_REGISTERED_MANIFEST  = BASE_APP_CODE + 201;      // 매니페스트 등록 안된
    public static int FAIL_LOG_OBSERVER             = BASE_APP_CODE + 202;      // Observing 중 오류 발생
}
