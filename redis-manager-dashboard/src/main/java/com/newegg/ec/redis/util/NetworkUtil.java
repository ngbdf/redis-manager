package com.newegg.ec.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/**
 * Check port
 * Ping
 *
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
public class NetworkUtil {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    private static final int TIMEOUT = 2000;

    private NetworkUtil() {
    }

    /**
     * 监测端口是否在使用
     *
     * @param ip
     * @param port
     * @return
     */
    public static final boolean telnet(String ip, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            return socket.isConnected();
        } catch (Exception e) {
            logger.error(ip + ":" + port + " can't access.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
        return false;
    }

}
