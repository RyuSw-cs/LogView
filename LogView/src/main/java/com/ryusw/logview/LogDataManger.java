package com.ryusw.logview;

import android.os.Build;

import com.ryusw.logview.callback.LogObservingCallBackInterface;
import com.ryusw.logview.config.LogResultCode;
import com.ryusw.logview.config.LogViewResultMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Arrays;

/**
 * 로그 데이터 관리
 *
 * @author swyu
 */
public class LogDataManger {
    private String[] logFilter = new String[0];
    private final int logBufferSize = 2048 * 12;
    private Process logProcess;
    private BufferedReader br;
    private Thread logThread;
    private String pid;

    private LogDataManger() {
    }

    /**
     * 싱글톤 구현
     *
     * @author swyu
     */
    public static LogDataManger getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final LogDataManger INSTANCE = new LogDataManger();
    }

    /**
     * 로그 필터 설정
     *
     * @param filter 적용하고자 하는 로그 필터
     * @author swyu
     */
    public void setLogFilter(String[] filter) {
        int size = filter.length;
        logFilter = Arrays.copyOf(filter, size);
    }

    /**
     * 로그를 출력하고 싶은 프로세스 아이디 설정
     *
     * @param pid 적용하고자 하는 앱의 process id
     * @author swyu
     */
    public void setAppPid(int pid) {
        if (pid == -1) {
            this.pid = "";
        } else {
            this.pid = "--pid=" + pid;
        }
    }

    /**
     * 로그 필터에 존재한 단어인지 확인
     *
     * @param log 검사하고자 하는 log
     * @return 파라미터의 log가 filter에 존재하는 여부
     * @author swyu
     */
    public boolean isExistLogFilter(String log) {
        if (logFilter == null) {
            return false;
        }
        for (String filter : logFilter) {
            if (log.contains(filter))
                return true;
        }
        return false;
    }

    /**
     * 현재까지 기록된 로그 초기화
     *
     * @throws IOException 존재하지 않는 프로세스일 경우 발생
     * @author swyu
     */
    public boolean clearLog() {
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
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            } else {
                process.destroy();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 로그 기록 시작
     *
     * @param doLogClear 시작시, 로그를 초기화 할지 여부
     * @param callback   로그 기록 중 성공, 실패 인터페이스
     * @author swyu
     */
    public void startLog(boolean doLogClear, LogObservingCallBackInterface callback) {
        if (doLogClear) {
            if (!clearLog()) {
                callback.onFailure(LogResultCode.FAIL_LOG_CLEAR, LogViewResultMsg.FAIL_LOG_CLEAR_MSG);
                return;
            }
        }
        // 서브 스레드 실행
        logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 프로세스 아이디를 기준으로 로그 검색
                String[] logCommand = {"logcat", "-v", "brief", pid};
                ProcessBuilder logProcessBuilder = new ProcessBuilder(logCommand);

                try {
                    logProcess = logProcessBuilder.start();
                    br = new BufferedReader(new InputStreamReader(logProcess.getInputStream()), logBufferSize);
                    String separator = System.getProperty("line.separator");

                    String line = br.readLine();
                    // 로그가 나오지 않음
                    if (line == null) {
                        callback.onFailure(LogResultCode.FAIL_PROCESS_RESULT_NULL, LogViewResultMsg.FAIL_LOG_PROCESS_NULL_MSG);
                        logProcess.destroy();
                        br.close();
                    }

                    while (line != null) {
                        callback.onSuccess(line);
                        line = separator + br.readLine();
                    }

                    br.close();
                } catch (InterruptedIOException interruptedException){
                    interruptedException.printStackTrace();
                    stopLog();
                } catch(Exception e) {
                    e.printStackTrace();
                    callback.onFailure(LogResultCode.FAIL_LOG_OBSERVER, LogViewResultMsg.FAIL_LOG_OBSERVING_ERROR_MSG);
                    if (logProcess != null) {
                        logProcess.destroy();
                    }
                }
            }
        });
        logThread.start();
    }

    /**
     * 로그 기록 중지
     *
     * @author swyu
     */
    public void stopLog() {
        if (logProcess == null) {
            return;
        }

        if(!logThread.isAlive()){
            return;
        }

        try {
            if(logThread.isAlive()){
                // 실행중인 로그 프로세스 종료
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (logProcess.isAlive()) {
                        logProcess.destroyForcibly();
                    }
                } else {
                    logProcess.destroy();
                }
                logThread.interrupt();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

