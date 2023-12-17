package com.ryusw.logview;

import android.os.Build;
import com.ryusw.logview.callback.LogObservingCallBackInterface;
import com.ryusw.logview.config.LogViewResultCode;
import com.ryusw.logview.config.LogViewResultMsg;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * 로그 데이터 관리
 * @author swyu
 * */
public class LogDataManger {
    private static String[] logFilter;
    private static final int logBufferSize = 4 * 1024;
    private static Process logProcess;
    /**
     * 로그 필터 설정
     * @param filter 적용하고자 하는 로그 필터
     * @author swyu
     * */
    public static void SetLogFilter(String[] filter){
        int size = filter.length;
        logFilter = Arrays.copyOf(filter, size);
    }

    /**
     * 로그 필터에 존재한 단어인지 확인
     * @param log 검사하고자 하는 log
     * @return 파라미터의 log가 filter에 존재하는 여부
     * @author swyu
     * */
    public static boolean IsExistLogFilter(String log){
        if(logFilter == null){
            return false;
        }
        for(String filter : logFilter){
            if(log.contains(filter)) return true;
        }
        return false;
    }

    /**
     * 현재까지 기록된 로그 초기화
     * @exception IOException 존재하지 않는 프로세스일 경우 발생
     * @author swyu
     * */
    public static boolean ClearLog() {
        // 로그 초기화 명령 (adb logcat -c)
        String[] clearCommand = {"logcat", "-c"};
        ProcessBuilder clearProcess = new ProcessBuilder(clearCommand);
        // 프로세스 시작

        try {
            Process process = clearProcess.start();

            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();

            // 초기화 후 프로세스 종료
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(process.isAlive()){
                    process.destroyForcibly();
                }
            } else {
                process.destroy();
            }
            return true;
        }catch (IOException e){
            return false;
        }
    }

    /**
     * 로그 기록 시작
     * @param doLogClear 시작시, 로그를 초기화 할지 여부
     * @param callback 로그 기록 중 성공, 실패 인터페이스
     * @author swyu
     * */
    public static void StartLog(boolean doLogClear, LogObservingCallBackInterface callback) {
        if(doLogClear){
            if(!ClearLog()){
                callback.onFailure(LogViewResultCode.FAIL_LOG_CLEAR, LogViewResultMsg.FAIL_LOG_CLEAR_MSG);
                return;
            }
        }
        // 서브 스레드 실행
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 프로세스 아이디를 기준으로 로그 검색
                String currentPid = "--pid=" + android.os.Process.myPid();
                String[] logCommand = {"logcat", "-v", currentPid};
                ProcessBuilder logProcessBuilder = new ProcessBuilder(logCommand);

                BufferedReader br;
                try {
                    logProcess = logProcessBuilder.start();
                    br = new BufferedReader(new InputStreamReader(logProcess.getInputStream()), logBufferSize);
                    String separator = System.getProperty("line.separator");

                    String line = br.readLine();
                    // 로그가 나오지 않음
                    if(line == null){
                        callback.onFailure(LogViewResultCode.FAIL_PROCESS_RESULT_NULL, LogViewResultMsg.FAIL_LOG_PROCESS_NULL_MSG);
                        logProcess.destroy();
                        br.close();
                    }
                    while (line != null) {
                        // 로그가 한줄씩 나올때 callback으로 log전송
                        callback.onSuccess(line + separator);
                        line = br.readLine();
                    }
                    br.close();

                } catch (IOException e){
                    callback.onFailure(LogViewResultCode.FAIL_LOG_OBSERVER, LogViewResultMsg.FAIL_LOG_OBSERVING_ERROR_MSG);
                    if(logProcess != null){
                        logProcess.destroy();
                    }
                } }
        }).start();
    }

    /**
     * 로그 기록 중지
     * @author swyu
     * */
    public static void StopLog(){
        if(logProcess == null){
            return;
        }

        // 실행중인 로그 프로세스 종료
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(logProcess.isAlive()){
                logProcess.destroyForcibly();
            }
        }else{
            logProcess.destroy();
        }
    }
}
