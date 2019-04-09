package com.newegg.ec.cache.app.util;

import com.newegg.ec.cache.core.logger.CommonLogger;

import java.util.UUID;

/**
 * Created by lf52 on 2017/12/8.
 */
public class CommonUtil {

    private static final CommonLogger logger = new CommonLogger(CommonUtil.class);

    public static String getUuid() {
        return UUID.randomUUID().toString();
    }

}
