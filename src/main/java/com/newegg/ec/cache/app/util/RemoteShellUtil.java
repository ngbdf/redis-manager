package com.newegg.ec.cache.app.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 远程Shell脚本执行工具
 *
 * @author Administrator
 */
public class RemoteShellUtil {
    public static final Log logger = LogFactory.getLog(RemoteShellUtil.class);
    private Connection conn;
    private String ipAddr;
    private String userName;
    private String password;

    public RemoteShellUtil (String ipAddr, String userName, String password) {
        this.ipAddr = ipAddr;
        this.userName = userName;
        this.password = password;
    }


    /**
     * 登录远程Linux主机
     *
     * @return
     * @throws IOException
     */
    public boolean login() throws IOException {
        conn = new Connection(ipAddr);
        conn.connect(); // 连接
        return conn.authenticateWithPassword(userName, password); // 认证
    }

    public static String localExec(String cmd){
        String result = "";
        try {
            String[] cmds = { "/bin/sh", "-c", cmd };
            Process ps = Runtime.getRuntime().exec( cmds );
            InputStream in = ps.getInputStream();
            result = processStdout(in);
            InputStream errorIn = ps.getErrorStream();
            result += processStdout(errorIn);
        }
        catch (Exception e) {
            logger.error( e );
        }
        return result;
    }


    /**
     * 执行Shell脚本或命令
     *
     * @param cmds
     *            命令行序列
     * @return
     */
    public String exec(String cmds) {
        String result = "";
        try {
            if (this.login()) {
                Session session = conn.openSession(); // 打开一个会话
                //session.requestPTY("redis-manager");
                session.execCommand(cmds);
                InputStream in = session.getStdout();
                result = processStdout(in);
                InputStream errorIn = session.getStderr();
                result += processStdout(errorIn);
            }
        } catch (IOException e) {
            logger.error( e );
        } finally {
            conn.close();
        }
        return result;
    }

    /**
     * 解析流获取字符串信息
     * @return
     */
    public static String processStdout(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            logger.error( e );
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error( e );
            }
        }
        return sb.toString();

    }

}