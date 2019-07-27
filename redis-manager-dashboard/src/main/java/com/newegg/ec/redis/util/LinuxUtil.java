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
public class LinuxUtil {

    private static final Logger logger = LoggerFactory.getLogger(LinuxUtil.class);

    public static final String MEMORY_FREE = "memory_free";

    public static final String VERSION = "version";

    private static final int TIMEOUT = 2000;


    private LinuxUtil() {
    }

    public static final boolean login(Machine machine) {
        Connection connection = new Connection(machine.getHost());
        try {
            connection.connect();
            connection.authenticateWithPassword(machine.getUserName(), machine.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return true;
    }

    /**
     * 执行Shell脚本或命令
     *
     * @param machine
     * @param commands
     * @return
     */
    public static String execute(Machine machine, String commands) {
        String result = "";
        Connection connection = new Connection(machine.getHost());
        try {
            connection.connect();
            connection.authenticateWithPassword(machine.getUserName(), machine.getPassword());
            // 打开一个会话
            Session session = connection.openSession();
            session.execCommand(commands);
            InputStream in = session.getStdout();
            result = processStandardOutput(in);
            InputStream errorIn = session.getStderr();
            result += processStandardOutput(errorIn);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return result;
    }

    public static String localExecute(String command) {
        String result = "";
        try {
            String[] cmds = {"/bin/sh", "-c", command};
            Process ps = Runtime.getRuntime().exec(cmds);
            InputStream in = ps.getInputStream();
            result = processStandardOutput(in);
            InputStream errorIn = ps.getErrorStream();
            result += processStandardOutput(errorIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static final Map<String, String> getLinuxInfo(Machine machine) {
        String result = execute(machine, getInfoCommand());
        Map<String, String> machineResourceInfoMap = formatResult(result);
        return machineResourceInfoMap;
    }

    /**
     * 解析流获取字符串信息
     *
     * @return
     */
    private static String processStandardOutput(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
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
        Map<String, String> infoMap = new HashMap();
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
