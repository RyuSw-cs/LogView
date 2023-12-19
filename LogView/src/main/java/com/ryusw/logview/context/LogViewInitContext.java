package com.ryusw.logview.context;

import com.ryusw.logview.callback.LogCallbackInterface;
import com.ryusw.logview.exception.InvalidLogViewArgumentException;

import java.util.Arrays;

public class LogViewInitContext {
    private LogCallbackInterface logCallbackInterface;
    private String[] logFilter;
    private int pid;
    private boolean autoScrollFlag;

    public LogCallbackInterface getLogCallbackInterface() {
        return logCallbackInterface;
    }

    public String[] getLogFilter() {
        return logFilter;
    }

    public int getPid() {
        return pid;
    }

    public boolean getAutoScroll(){
        return autoScrollFlag;
    }

    public static final class Builder{
        private LogCallbackInterface logCallbackInterface;
        private String[] logFilter = new String[0];
        private int pid = -1;
        private boolean autoScroll;
        public Builder(){}

        public Builder setAutoScroll(boolean flag){
            this.autoScroll = flag;
            return this;
        }

        public Builder setPid(int pid) {
            this.pid = pid;
            return this;
        }

        public Builder setLogResultCallBackInterface(LogCallbackInterface logCallbackInterface) {
            this.logCallbackInterface = logCallbackInterface;
            return this;
        }

        public Builder setLogFilter(String[] logFilter) {
            int size = logFilter.length;
            this.logFilter = Arrays.copyOf(logFilter, size);
            return this;
        }


        public LogViewInitContext build() throws InvalidLogViewArgumentException {
            LogViewInitContext context = new LogViewInitContext();

            if(this.logCallbackInterface == null) {
                throw new InvalidLogViewArgumentException("callback interface is null");
            }

            context.logCallbackInterface = this.logCallbackInterface;
            context.logFilter = this.logFilter;
            context.pid = this.pid;
            context.autoScrollFlag = this.autoScroll;
            return context;
        }
    }
}
