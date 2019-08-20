package com.newegg.ec.redis.util;

import static com.newegg.ec.redis.util.SplitUtil.MINUS;
import static com.newegg.ec.redis.util.SplitUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 8/20/2019
 */
public class CommonUtil {

    public static String replaceSpace(String original) {
        return original.replaceAll(SPACE, MINUS);
    }
}
