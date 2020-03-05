package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * @author kz37
 * @date 2018/10/20
 */
public class ListSortUtil {

    /**
     *  List<List<String>>
     * @param sort 排序的List
     * @param index 根据内部List的下标值进行排序
     */
    public static void sortListListStringAsc(List<List<String>> sort, int index) {
        Collections.sort(sort, (l1, l2) -> {
            return compareDouble(l1.get(index),l2.get(index));
        });
    }

    public static void sortListListStringDesc(List<List<String>> sort, int index) {
        Collections.sort(sort, (l1, l2) -> {
            return -compareDouble(l1.get(index),l2.get(index));
        });
    }

    public static int compareDouble(Double d1, Double d2) {
        if(Double.compare(d1, d2) > 0)
        {
            return 1;
        }
        else if(Double.compare(d1, d2) < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public static int compareDouble(String s1, String s2) throws NumberFormatException{
        Double d1 = Double.parseDouble(s1);
        Double d2 = Double.parseDouble(s2);
        return compareDouble(d1, d2);
    }


    public static void sortByKeyValueDesc(List<JSONObject> jsonObjectList, String key) {
        Collections.sort(jsonObjectList, (l1, l2) -> {
            return -compareDouble(l1.getString(key),l2.getString(key));
        });
    }
}
