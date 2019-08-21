package com.newegg.ec.redis.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Check Memory
 * Check CPU
 * File Operation
 *
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
public class LinuxInfoUtil {

    private static final Logger logger = LoggerFactory.getLogger(LinuxInfoUtil.class);

    public static final String MEMORY_FREE = "memory_free";

    public static final String VERSION = "version";

    private LinuxInfoUtil() {
    }

    public static final boolean login(Machine machine) throws Exception {
        Connection connection = null;
        try {
            connection = SSH2Util.getConnection(machine);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return true;
    }

    public static final Map<String, String> getLinuxInfo(Machine machine) throws Exception {
        String result = SSH2Util.execute(machine, getInfoCommand());
        Map<String, String> machineResourceInfoMap = formatResult(result);
        return machineResourceInfoMap;
    }

    private static String getInfoCommand() {
        String command = "export TERM=linux;res=\"\"\n" +
                // Version
                formatCommand(VERSION, "`cat /proc/version`") +
                // CPU 使用率
                // formatCommand("processor", "`sar -u 1 1 |grep 'Average'|awk '{print $3}'`") +
                // loadaverage
                // formatCommand("load_average", "`uptime |awk '{print $(NF-2) $(NF-1) $NF}'`") +
                // Memory
                // formatCommand("memory_total", "`free -g | grep Mem | awk '{print $2}'`") +
                formatCommand(MEMORY_FREE, "`free -g | grep Mem | awk '{print $4}'`") +
                // formatCommand("memory_available", "`free -g | grep Mem | awk '{print $7}'`") +
                "echo -e $res";
        return command;
    }

    /**
     * @param field
     * @param command
     * @return
     */
    private static String formatCommand(String field, String command) {
        return "res=${res}\"" + field + "'" + command + "\\n" + "\"\n";
    }

    private static Map<String, String> formatResult(String result) {
        Map<String, String> infoMap = new HashMap(16);
        String[] lines = result.split("\n");
        for (String line : lines) {
            if (Strings.isNullOrEmpty(line)) {
                continue;
            }
            String[] tmp = line.split("'");
            if (tmp.length > 1) {
                infoMap.put(tmp[0].trim(), tmp[1].trim());
            } else {
                infoMap.put(tmp[0].trim(), "");
            }
        }
        return infoMap;
    }

}
