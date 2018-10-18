package com.newegg.ec.cache.core.logger;

import com.newegg.ec.cache.app.controller.websocket.CreateClusterLogHandler;
import com.newegg.ec.cache.app.model.User;
import com.newegg.ec.cache.app.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gl49 on 2018/4/22.
 */
public class CommonLogger {
    private Logger logger;

    public CommonLogger(Class<?> loggerClazz) {
        this.logger = LoggerFactory.getLogger(loggerClazz);
    }

    public CommonLogger(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    public void error(String msg, Throwable e) {
        this.logger.error(msg, e);
    }

    public void info(String msg) {
        this.logger.info(msg);
    }

    public String websocket(String msg) {
        User user = RequestUtil.getUser();
        if (null == user) {
            return msg;
        }
        CreateClusterLogHandler.appendLog(String.valueOf(RequestUtil.getUser().getId()), msg);
        return msg;
    }
}
