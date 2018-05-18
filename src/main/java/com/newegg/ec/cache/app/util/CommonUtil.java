package com.newegg.ec.cache.app.util;

import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Created by lf52 on 2017/12/8.
 */
public class CommonUtil {

    private static Logger logger= Logger.getLogger(CommonUtil.class);

    public static String getUuid(){
        return UUID.randomUUID().toString();
    }


}
