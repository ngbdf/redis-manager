package com.newegg.ec.redis.util;

/**
 * @author Jay.H.Zou
 * @date 8/7/2019
 */
public class SignUtil {

    public static final String COMMAS = ",";

    public static final String MINUS = "-";

    public static final String EQUAL_SIGN = "=";

    public static final String COLON = ":";

    public static final String SEMICOLON = ";";

    public static final String AITE = "@";

    public static final String SPACE = " ";

    public static final String SLASH = "/";

    private SignUtil() {
    }

    public static final String replaceSpaceToMinus(String str) {
        return str.replace(SPACE, MINUS);
    }

    public static final String[] splitByMinus(String str) {
        return splitBySign(str, MINUS);
    }

    public static final String[] splitByCommas(String str) {
        return splitBySign(str, COMMAS);
    }

    public static final String[] splitByEqualSign(String str) {
        return splitBySign(str, EQUAL_SIGN);
    }

    public static final String[] splitByColon(String str) {
        return splitBySign(str, COLON);
    }

    public static final String[] splitBySemicolon(String str) {
        return splitBySign(str, SEMICOLON);
    }

    public static final String[] splitByAite(String str) {
        return splitBySign(str, AITE);
    }

    public static final String[] splitBySpace(String str) {
        return splitBySign(str, SPACE);
    }

    public static final String[] splitBySign(String str, String sign) {
        String[] split = str.split(sign);
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }
}
