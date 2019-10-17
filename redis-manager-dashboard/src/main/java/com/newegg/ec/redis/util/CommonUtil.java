package com.newegg.ec.redis.util;

import java.util.List;

import static com.newegg.ec.redis.util.SignUtil.MINUS;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 8/20/2019
 */
public class CommonUtil {

    public static String replaceSpace(String original) {
        return original.replaceAll(SPACE, MINUS);
    }

}
