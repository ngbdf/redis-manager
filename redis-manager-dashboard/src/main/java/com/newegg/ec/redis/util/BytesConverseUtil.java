package com.newegg.ec.redis.util;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author kz37
 * @date 2018/10/25
 */
public class BytesConverseUtil {

    public static String getSize(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    public static void converseBytes(List<List<String>> datas, int index){
        for(int i = 0; i < datas.size(); i++){
            datas.get(i).set(index, getSize(Long.parseLong(datas.get(i).get(index))));
        }
    }
}
