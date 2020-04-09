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

    public static boolean checkFreePort(String ip, int port) {
        try {
            return !connect(ip, port);
        } catch (Exception e) {
            logger.info(ip + ":" + port + " is free.");
            return true;
        }
    }

    public static boolean telnet(String ip, int port) {
        try {
            return connect(ip, port);
        } catch (Exception e) {
            logger.warn(ip + ":" + port + " can't access.");
            return false;
        }
    }

    private static boolean connect(String ip, int port) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            return socket.isConnected();
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
    }

}
