package com.ryusw.logview.context;

import com.ryusw.logview.callback.LogResultCallBackInterface;

public class LogViewInitContext {
    private LogResultCallBackInterface logResultCallBackInterface;

    public LogResultCallBackInterface getLogResultCallBackInterface() {
        return logResultCallBackInterface;
    }

    public static class Builder{
        private LogResultCallBackInterface logResultCallBackInterface;
        public Builder(){}

        public Builder setLogResultCallBackInterface(LogResultCallBackInterface logResultCallBackInterface) {
            this.logResultCallBackInterface = logResultCallBackInterface;
            return this;
        }

        public LogViewInitContext build(){
            LogViewInitContext context = new LogViewInitContext();
            context.logResultCallBackInterface = this.logResultCallBackInterface;
            return context;
        }
    }
}
