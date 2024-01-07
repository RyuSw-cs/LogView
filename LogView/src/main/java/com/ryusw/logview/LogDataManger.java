package com.ryusw.logview;

import android.os.Build;

import com.ryusw.logview.callback.LogObservingCallBackInterface;
import com.ryusw.logview.config.LogResultCode;
import com.ryusw.logview.config.LogResultMsg;
import com.ryusw.logview.util.LogUtil;

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
    private static final String CLASSNAME = "LogDataManger";
    private String[] logFilter = new String[0];
    private final int logBufferSize = 2048 * 12;
    private Process logProcess;
    private BufferedReader br;
    private Thread logThread;
    private String pid;
    private boolean isRunning = false;

    private LogDataManger() {
    }

    /**
     * 싱글톤 구현
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
     * @param filter 적용하고자 하는 로그 필터
     * @author swyu
     */
    public void setLogFilter(String[] filter) {
        int size = filter.length;
        logFilter = Arrays.copyOf(filter, size);
    }

    /**
     * 로그를 출력하고 싶은 프로세스 아이디 설정
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
    public synchronized void clearLog() {
        LogUtil.d(CLASSNAME, "clearLog", "start");
        // 로그 초기화 명령 (adb logcat -c)
        String[] clearCommand = {"logcat", "-c"};
        ProcessBuilder clearProcess = new ProcessBuilder(clearCommand);
        // 프로세스 시작

        try {
            Process process = clearProcess.start();
            process.waitFor();
            // 초기화 후 프로세스 종료
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            } else {
                process.destroy();
            }
            LogUtil.d(CLASSNAME, "clearLog", "end");
        } catch (Exception e) {
            LogUtil.e(CLASSNAME, "clearLog", "exception = " + e.getMessage());
        }
    }

    /**
     * 로그 기록 시작
     * 기록을 시작하기전 앞의 Java Runtime에서 기록된 로그는 삭제
     * LogView에서의 logString은 기록하지 않음
     * @param callback   로그 기록 중 성공, 실패 인터페이스
     * @exception InterruptedIOException 스레드 실행 중 오류 발생
     * @exception Exception 로그를 옵저빙하면서 오류 발생
     * @author swyu
     */
    public void startLog(LogObservingCallBackInterface callback) {
        LogUtil.d(CLASSNAME, "startLog", "start");
        // 일시정지, 초기화 실행 중일 때 쌓여있는 로그를 초기화하고 기록 시작
        clearLog();
        logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 프로세스 아이디를 기준으로 로그 검색
                String[] logCommand = {"logcat", "-v", "brief", pid};
                ProcessBuilder logProcessBuilder = new ProcessBuilder(logCommand);
                isRunning = true;
                try {
                    // 위의 커맨드로 실행 (adb logcat -v brief pid=="input pid")
                    logProcess = logProcessBuilder.start();
                    br = new BufferedReader(new InputStreamReader(logProcess.getInputStream()), logBufferSize);
                    // 줄바꿈 단어
                    String separator = System.getProperty("line.separator");

                    String line = br.readLine();
                    // 로그가 나오지 않음
                    if (line == null) {
                        callback.onFailure(LogResultCode.FAIL_PROCESS_RESULT_NULL, LogResultMsg.FAIL_LOG_PROCESS_NULL_MSG);
                        logProcess.destroy();
                        br.close();
                    }

                    // 해당 부분을 통해서 로그를 받아옴
                    while (line != null) {
                        callback.onSuccess(line);
                        line = separator + br.readLine();
                    }

                    br.close();
                    LogUtil.d(CLASSNAME, "startLog", "end");
                } catch (InterruptedIOException interruptedException){
                    if(isRunning){ // 실행중일때 해당 예외가 발생하면 오류
                        LogUtil.e(CLASSNAME, "startLog", "Thread Exception = " + interruptedException.getMessage());
                        callback.onFailure(LogResultCode.FAIL_LOG_OBSERVER, LogResultMsg.FAIL_LOG_OBSERVING_ERROR_MSG);
                    }
                } catch(Exception e) {
                    LogUtil.e(CLASSNAME, "startLog", "Observing Exception = " + e.getMessage());
                    callback.onFailure(LogResultCode.FAIL_LOG_OBSERVER, LogResultMsg.FAIL_LOG_OBSERVING_ERROR_MSG);
                    release();
                }
            }
        });
        logThread.start();
    }

    /**
     * 로그 기록 중지
     * 프로세스와 스레드 모두 초기화
     * @author swyu
     */
    public void release() {
        LogUtil.d(CLASSNAME, "release", "start");

        if(logProcess == null){
            LogUtil.w(CLASSNAME, "release", "log process is null");
            return;
        }

        if(logThread == null){
            LogUtil.w(CLASSNAME, "release", "log thread is null");
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
            logThread = null;
            logProcess = null;
            isRunning = false;
            LogUtil.d(CLASSNAME, "release", "end");
        }catch (Exception e){
            LogUtil.e(CLASSNAME, "release", "exception = " + e.getMessage());
        }
    }
    public void setStatus(boolean status){
        this.isRunning = status;
    }
    public boolean currentStatus(){
        return isRunning;
    }
}

