package com.ryusw.logview.context;

import com.ryusw.logview.callback.LogInitCallbackInterface;
import com.ryusw.logview.exception.InvalidLogViewArgumentException;
import com.ryusw.logview.util.LogUtil;

import java.util.Arrays;

public class LogViewInitContext {
    private static String CLASSNAME = "LogViewInitContext";
    private LogInitCallbackInterface logInitCallbackInterface;
    private String[] logFilter;
    private int pid;
    private boolean autoScrollFlag;

    public LogInitCallbackInterface getLogCallbackInterface() {
        return logInitCallbackInterface;
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
        private LogInitCallbackInterface logInitCallbackInterface;
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

        public Builder setLogResultCallBackInterface(LogInitCallbackInterface logInitCallbackInterface) {
            this.logInitCallbackInterface = logInitCallbackInterface;
            return this;
        }

        public Builder setLogFilter(String[] logFilter) {
            int size = logFilter.length;
            this.logFilter = Arrays.copyOf(logFilter, size);
            return this;
        }


        public LogViewInitContext build() throws InvalidLogViewArgumentException {
            LogUtil.d(CLASSNAME, "build", "start");
            LogViewInitContext context = new LogViewInitContext();

            if(this.logInitCallbackInterface == null) {
                throw new InvalidLogViewArgumentException("callback interface is null");
            }

            context.logInitCallbackInterface = this.logInitCallbackInterface;
            context.logFilter = this.logFilter;
            context.pid = this.pid;
            context.autoScrollFlag = this.autoScroll;

            LogUtil.i(CLASSNAME, "build", "pid = " + context.pid);
            LogUtil.i(CLASSNAME, "build", "log filter = " + Arrays.toString(context.logFilter));
            LogUtil.i(CLASSNAME, "build", "autoScrollFlag = " + context.autoScrollFlag);

            LogUtil.d(CLASSNAME, "build", "end");
            return context;
        }
    }
}
