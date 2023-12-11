package com.ryusw.logview.exception;

/**
 * Rx를 사용해서 로그 프로세스를 Observing 중 오류 발생
 * @author swyu
 * */
public class LogObservingException extends Exception{
    public LogObservingException(String message){
        super(message);
    }
}
