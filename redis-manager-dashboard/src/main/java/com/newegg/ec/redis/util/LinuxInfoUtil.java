package com.newegg.ec.redis.util;

import ch.ethz.ssh2.Connection;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
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

    private static final Logger logger = LoggerFactory.getLogger(LinuxInfoUtil.class);

    public static final String MEMORY_FREE = "memory_free";

    public static final String VERSION = "version";

    private LinuxInfoUtil() {
    }

    public static String getIp() {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        //获得IP
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipAddress = inetAddress.getHostAddress().toString();
                            if (!ipAddress.contains("::")
                                    && !ipAddress.contains("0:0:")
                                    && !ipAddress.contains("fe80")
                                    && !"127.0.0.1".equals(ip)) {
                                ip = ipAddress;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("get current ip failed.", e);
            ip = null;
        }
        return ip;
    }

    public static boolean login(Machine machine) throws Exception {
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

    public static Map<String, String> getLinuxInfo(Machine machine) throws Exception {
        String result = SSH2Util.execute(machine, getInfoCommand());
        return formatResult(result);
    }

    private static String getInfoCommand() {
        String command = "export TERM=linux;res=\"\"\n" +
                formatCommand(VERSION, "`cat /proc/version`") +
                formatCommand(MEMORY_FREE, "`free -g | grep Mem | awk '{print $4}'`") +
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
