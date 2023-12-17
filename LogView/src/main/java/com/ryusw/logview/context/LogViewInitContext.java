package com.ryusw.logview.context;

import com.ryusw.logview.callback.LogInitCallBackInterface;
import com.ryusw.logview.exception.InvalidLogViewArgumentException;

import java.util.Arrays;

public class LogViewInitContext {
    private LogInitCallBackInterface logInitCallBackInterface;
    private String[] logFilter;

    public LogInitCallBackInterface getLogResultCallBackInterface() {
        return logInitCallBackInterface;
    }

    public String[] getLogFilter() {
        return logFilter;
    }

    public static class Builder{
        private LogInitCallBackInterface logInitCallBackInterface;
        private String[] logFilter = new String[0];
        public Builder(){}

        public Builder setLogResultCallBackInterface(LogInitCallBackInterface logInitCallBackInterface) {
            this.logInitCallBackInterface = logInitCallBackInterface;
            return this;
        }

        public Builder setLogFilter(String[] logFilter) {
            int size = logFilter.length;
            this.logFilter = Arrays.copyOf(logFilter, size);
            return this;
        }

        public LogViewInitContext build() throws InvalidLogViewArgumentException {
            LogViewInitContext context = new LogViewInitContext();

            if(this.logInitCallBackInterface == null) {
                throw new InvalidLogViewArgumentException("callback interface is null");
            }

            context.logInitCallBackInterface = this.logInitCallBackInterface;
            context.logFilter = this.logFilter;
            return context;
        }
    }
}
