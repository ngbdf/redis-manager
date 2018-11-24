package com.newegg.ec.cache.app.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jn50 on 2018/3/29.
 * 支持 * / + - ( ) < <= == >= > 与 数字
 * 表达式每个元素之间使用空格隔开
 * 例如: 3 * ( 2 + 2 )
 * 变量使用@{v1} 来表示
 */
public class MathExpressionCalculateUtil {

    public static final Pattern varPattern = Pattern.compile("(\\@\\{[\\w:]+\\})");
    private static Map<String, Object> params = new HashMap<>();

    static {
        params.put("connectedClients", 1);
        params.put("blockedClients", 1);
        params.put("rejectedConnections", 1);
        params.put("used_memory", 1);
        params.put("mem_fragmentation_ratio", 1);
        params.put("usedCpuSys", 1);
        params.put("totalKeys", 1);
        params.put("expireKeys", 1);
        params.put("instantaneous_input_kbps", 1);
        params.put("instantaneous_output_kbps", 1);
        params.put("responseTime", 1);

    }

    //-------------------------------------------------calculate-------------------------//

    public static boolean checkRule(String formula) {
        try {
            String result = String.valueOf(calculate(format(formula), params));
            if ("true".equals(result) || "false".equals(result)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getRuleDataStr(String calculateStr, Map<String, Object> map) {
        Matcher matcher = varPattern.matcher(calculateStr);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String matcherStr = calculateStr.substring(matcher.start(), matcher.end());
            String field = matcherStr.substring(2, matcherStr.length() - 1);
            stringBuffer.append(field + "=" + map.get(field) + ",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }

    public static String fillVarWithValue(String calculateStr, Map<String, Object> map) {
        String strTemp = calculateStr;
        Matcher matcher = varPattern.matcher(strTemp);
        while (matcher.find()) {
            String matcherStr = calculateStr.substring(matcher.start(), matcher.end());
            String field = matcherStr.substring(2, matcherStr.length() - 1);
            if (map.get(field) != null) {
                strTemp = strTemp.replace(matcherStr, map.get(field).toString());
            } else {
                return null;
            }
        }
        return strTemp;
    }

    private static String format(String calculateStr) {
        StringBuilder sb = new StringBuilder();
        calculateStr = calculateStr.replaceAll(" ", "");
        for (int i = 0; i < calculateStr.length(); i++) {
            char c = calculateStr.charAt(i);
            if (i != 0 && i != calculateStr.length() - 1) {
                if (c == '(' || c == ')' || c == '*' || c == '/' || c == '+' || c == '-') {
                    sb.append(" ");
                    sb.append(c);
                    sb.append(" ");
                } else if (c == '>' || c == '=' || c == '<') {
                    if (calculateStr.charAt(i - 1) != '>' && calculateStr.charAt(i - 1) != '=' && calculateStr.charAt(i - 1) != '<') {
                        sb.append(" ");
                    }
                    sb.append(c);
                    if (calculateStr.charAt(i + 1) != '=') {
                        sb.append(" ");
                    }
                } else {
                    sb.append(c);
                }
            } else if (i == 0) {
                sb.append(c);
                if (c == '(' || c == ')' || c == '*' || c == '/' || c == '+' || c == '-') {
                    sb.append(" ");
                }
                if (c == '>' || c == '=' || c == '<') {
                    if (calculateStr.charAt(i + 1) != '=') {
                        sb.append(" ");
                    }
                }
            } else if (i == calculateStr.length() - 1) {
                if (c == '(' || c == ')' || c == '*' || c == '/' || c == '+' || c == '-') {
                    sb.append(" ");
                }
                if (c == '>' || c == '=' || c == '<') {
                    if (calculateStr.charAt(i - 1) != '>' && calculateStr.charAt(i - 1) != '=' && calculateStr.charAt(i - 1) != '<') {
                        sb.append(" ");
                    }
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Object calculate(String expressionStr, Map<String, Object> data) throws Exception {

        String tempStr = format(expressionStr).replaceAll("\\s{1,}", " ");
        String calculateStr = fillVarWithValue(tempStr, data);
        String[] elements = calculateStr.split(" ");
        return calculateWithBracket(elements);
    }

    public static String calculateWithBracket(String[] elements) throws Exception {
        boolean findBracket = false;
        int start = 0;
        int end = 0;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].equals("(")) {
                findBracket = true;
                start = i;
            }
            if (elements[i].equals(")") && findBracket) {
                end = i;
                break;
            }
        }
        if (findBracket) {
            if (end != 0) {
                String[] partElements = new String[end - (start + 1)];
                int pIndex = 0;
                for (int i = start + 1; i < end; i++) {
                    partElements[pIndex] = elements[i];
                    pIndex++;
                }
                String tempResult = computeWithNoBracket(partElements).toString();
                String[] tempElements = new String[elements.length - (end - start)];
                fillTempElements(tempElements, elements, start, end, tempResult);
                return calculateWithBracket(tempElements);
            } else {
                throw new Exception();
            }
        } else {
            return computeWithNoBracket(elements).toString();
        }
    }

    //先乘除 再加减 最后等式判断
    private static Object computeWithNoBracket(String[] elements) throws Exception {
        String[] tempElements;
        if (elements.length > 2) {
            tempElements = new String[elements.length - 2];
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals("*") || elements[i].equals("/")) {
                    Object tempStr = mathCompute(elements[i - 1], elements[i], elements[i + 1]);
                    fillTempElements(tempElements, elements, i - 1, i + 1, tempStr.toString());
                    return computeWithNoBracket(tempElements);
                }
            }
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals("+") || elements[i].equals("-")) {
                    Object tempStr = mathCompute(elements[i - 1], elements[i], elements[i + 1]);
                    fillTempElements(tempElements, elements, i - 1, i + 1, tempStr.toString());
                    return computeWithNoBracket(tempElements);
                }
            }
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals("<") || elements[i].equals("<=") || elements[i].equals("==") || elements[i].equals(">=") || elements[i].equals(">")) {
                    Object tempStr = equalsCompute(elements[i - 1], elements[i], elements[i + 1]);
                    fillTempElements(tempElements, elements, i - 1, i + 1, tempStr.toString());
                    return computeWithNoBracket(tempElements);
                }
            }
            return null;
        } else if (elements.length == 2) {
            throw new Exception();
        } else {
            return elements[0];
        }
    }

    //中间结果保存
    private static void fillTempElements(String[] tempElements, String[] elements, int start, int end, String tempResult) {

        int nIndex = 0;
        for (int i = 0; i < start; i++) {
            tempElements[nIndex] = elements[i];
            nIndex++;
        }
        tempElements[nIndex] = tempResult;
        nIndex++;
        for (int i = end + 1; i < elements.length; i++) {
            tempElements[nIndex] = elements[i];
            nIndex++;
        }

    }

    //equals 计算
    private static Object equalsCompute(String value1, String equalsSymbol, String value2) throws Exception {
        boolean result = true;
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        int intResult = b1.compareTo(b2);
        switch (equalsSymbol) {
            case "<":
                result = (intResult == -1);
                break;
            case "<=":
                result = (intResult == -1 || intResult == 0);
                break;
            case "==":
                result = (intResult == 0);
                break;
            case ">=":
                result = (intResult == 0 || intResult == 1);
                break;
            case ">":
                result = (intResult == 1);
                break;
            default:
                throw new Exception();
        }
        return result;
    }

    //加减乘除 计算
    private static Object mathCompute(String value1, String mathSymbol, String value2) throws Exception {
        double result = 0;
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        switch (mathSymbol) {
            case "+":
                result = b1.add(b2).doubleValue();
                break;
            case "-":
                result = b1.subtract(b2).doubleValue();
                break;
            case "*":
                result = b1.multiply(b2).doubleValue();
                break;
            case "/":
                result = b1.divide(b2, 2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
                break;
            default:
                throw new Exception();
        }
        return result;
    }

}
