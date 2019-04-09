package com.newegg.ec.cache.app.util;


import com.newegg.ec.cache.app.model.Host;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by lzz on 2018/2/5.
 */
public class NetUtil {

    private static final Log logger = LogFactory.getLog(NetUtil.class);

    public static String getLocalIp() throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        return ip;
    }

    public static String getHostIp() {

        String sIP = "";
        InetAddress ip = null;
        try {
            boolean bFindIP = false;
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                if (bFindIP)
                    break;
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = ips.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip.getHostAddress().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                        bFindIP = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != ip)
            sIP = ip.getHostAddress();
        return sIP;
    }

    public static long pingTime(String ip) throws IOException {
        long startTime = System.currentTimeMillis();
        InetAddress.getByName(ip).isReachable(3000);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public static boolean checkUrl(String address) {
        boolean res = false;
        URL url;
        try {
            url = new URL(address.trim());
            InputStream in = url.openStream();
            res = true;
        } catch (Exception ignore) {
        }
        return res;
    }

    public static boolean checkPort(Integer port) {
        boolean res = false;
        try {
            String ip = getLocalIp();
            res = checkIpAndPort(ip, port);
        } catch (Exception ignore) {

        }
        return res;
    }

    public static boolean checkIp(String ip) {
        boolean res = false;
        try {
            res = InetAddress.getByName(ip).isReachable(5000);
        } catch (Exception ignore) {
        }
        return res;
    }

    public static boolean checkIp(String host, Integer timeOut) {
        try {
            return InetAddress.getByName(host).isReachable(timeOut);
        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    public static Host getHostPassAddress(String address) {
        Host host = null;
        if (!StringUtils.isBlank(address)) {
            String[] addressArr = address.split(",");
            if (addressArr.length > 0) {
                for (String addressStr : addressArr) {
                    String[] tmpArr = addressStr.split(":");
                    String ip = tmpArr[0];
                    int port = Integer.valueOf(tmpArr[1]);
                    if (checkIpAndPort(ip, port)) {
                        host = new Host();
                        host.setIp(ip);
                        host.setPort(port);
                        break;
                    }
                }
            }
        }
        return host;
    }

    public static boolean checkHost(String address) {
        String[] tmpArr = address.split(":");
        if (tmpArr.length == 2) {
            return checkIpAndPort(tmpArr[0], Integer.valueOf(tmpArr[1]));
        }
        return true;
    }

    public static boolean checkIpAndPort(String ip, Integer port) {
        boolean res = false;
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port));
            res = true;
        } catch (IOException ignore) {

        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
        return res;
    }

    public static boolean checkIpAnduserAccess(String ip, String username, String password) {
        RemoteShellUtil remoteShellUtil = new RemoteShellUtil(ip, username, password);
        boolean res = true;
        try {
            res = remoteShellUtil.login();
        } catch (IOException e) {
            res = false;
        }
        return res;
    }

    public static Host getHost(String hostStr) {
        Host host = new Host();

        try{
            String[] tmp = hostStr.split(":");
            String ip = tmp[0];
            int port = Integer.parseInt(tmp[1]);
            host.setIp(ip);
            host.setPort(port);
        }catch (ArrayIndexOutOfBoundsException e){
            logger.error("HostStr: " + hostStr + ", node format error : " + e.getMessage());
        }

        return host;
    }

    public static List<Host> getHostByAddress(String addressStr) {
        String[] addressArr = addressStr.split(",");
        List<Host> listHost = new ArrayList<>();
        for (String address : addressArr) {
            String[] tmpArr = address.split(":");
            if (tmpArr.length == 2) {
                Host host = new Host();
                host.setIp(tmpArr[0]);
                host.setPort(Integer.parseInt(tmpArr[1]));
                listHost.add(host);
            }
        }
        return listHost;
    }
}