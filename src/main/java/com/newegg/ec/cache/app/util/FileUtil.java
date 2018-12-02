package com.newegg.ec.cache.app.util;

import java.net.UnknownHostException;

/**
 * Created by gl49 on 2018/5/29.
 */
public class FileUtil {
    /**
     * "echo redis@newegg | sudo -S sed -i 's/^timeout .*\/timeout 864000/g' redis.conf"
     *
     * @param ip
     * @param port
     * @param username
     * @param password
     * @param filepath
     * @param field
     * @param value
     * @return
     * @throws UnknownHostException
     */
    public static boolean modifyFileContent(String ip, int port, String username, String password, String filepath, String field, String value) {
        boolean res = false;
        String cmd = "echo " + password + " | sudo -S sed -i 's/^" + field + " .*/" + field + " " + value + "/g' " + filepath;
        RemoteShellUtil remoteShellUtil = new RemoteShellUtil(ip, username, password);
        try {
            if (ip.equals(NetUtil.getLocalIp())) {
                RemoteShellUtil.localExec(cmd);
            } else {
                String tmp = remoteShellUtil.exec(cmd);
            }
            res = true;
        } catch (Exception ignore) {

        }
        return res;
    }
}
