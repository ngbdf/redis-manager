package com.newegg.ec.redis.util;

import ch.ethz.ssh2.Connection;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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

    public static final String MEMORY_FREE = "memory_free";

    public static final String VERSION = "version";

    private LinuxInfoUtil() {
    }

    public static String getIpAddress() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                continue;
            } else {
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        return "127.0.0.1";
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
