package com.newegg.ec.redis.util;

import ch.ethz.ssh2.*;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;

import java.io.*;

import static com.newegg.ec.redis.util.RedisConfigUtil.REDIS_CONF;
import static com.newegg.ec.redis.util.SignUtil.SEMICOLON;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 8/15/2019
 */
public class SSH2Util {

    private SSH2Util() {
    }

    public static void wget(Machine machine, String targetPath, String fileName, String remoteUrl, boolean sudo) throws Exception {
        rm(machine, targetPath + fileName, sudo);
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        String template = "/usr/bin/wget -P %s %s";
        command.append(String.format(template, targetPath, remoteUrl));
        String execute = execute(machine, command.toString());
    }

    /**
     * copy file
     *
     * @param machine
     * @param originalPath
     * @param targetPath
     * @throws Exception
     */
    public static String copy(Machine machine, String originalPath, String targetPath, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        String template = "cp %s %s";
        command.append(String.format(template, originalPath, targetPath));
        String result = execute(machine, command.toString());
        return result;
    }

    /**
     * copy file
     *
     * @param machine
     * @param originalPath
     * @param targetPath
     * @throws Exception
     */
    public static String copy2(Machine machine, String originalPath, String targetPath, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        // 删除旧的数据，如果有旧数据的话
        String rmTemplate = "rm -rf %s;";
        command.append(String.format(rmTemplate, targetPath));
        if (sudo) {
            command.append("sudo ");
        }
        // create directory
        String mkdirTemplate = "mkdir -p %s;";
        command.append(String.format(mkdirTemplate, targetPath));
        if (sudo) {
            command.append("sudo ");
        }
        String template = "cp %s %s";
        command.append(String.format(template, originalPath, targetPath));
        String result = execute(machine, command.toString());
        return result;
    }

    public static String copyFileToRemote(Machine machine, String tempPath, String url, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        // 删除旧的数据，如果有旧数据的话
        String rmTemplate = "rm -rf %s;";
        command.append(String.format(rmTemplate, tempPath));
        if (sudo) {
            command.append("sudo ");
        }
        // create directory
        String mkdirTemplate = "mkdir -p %s;";
        command.append(String.format(mkdirTemplate, tempPath));
        if (sudo) {
            command.append("sudo ");
        }
        // 本机拷贝至安装机器节点
        String wgetTemplate = "/usr/bin/wget -P %s %s";
        command.append(String.format(wgetTemplate, tempPath, url));
        return SSH2Util.execute(machine, command.toString());
    }

    public static String rm(Machine machine, String filePath, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        String template = "rm -rf %s";
        command.append(String.format(template, filePath));
        String result = execute(machine, command.toString());
        return result;
    }

    public static String unzipToTargetPath(Machine machine, String filePath, String targetPath, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        String template = "tar -xzf %s -C %s --strip-components 2";
        command.append(String.format(template, filePath, targetPath));
        String result = execute(machine, command.toString());
        return result;
    }

    /**
     * mkdir -p
     *
     * @param machine
     * @param path
     * @throws Exception
     */
    public static String mkdir(Machine machine, String path, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        command.append("mkdir -p ").append(path);
        return execute(machine, command.toString());
    }

    /*public static void createFile(Machine machine, String path, boolean sudo) throws Exception {
        rm(machine, path + REDIS_CONF, sudo);
        StringBuffer command = new StringBuffer();
        command.append("cd ").append(path).append(";");
        if (sudo) {
            command.append("sudo ");
        }
        command.append("touch ").append("redis.conf;");
        if (sudo) {
            command.append("sudo ");
        }
        command.append("sh -c \"echo '#redis config' > redis.conf; echo 'bind 101.1.2.2' >> redis.conf\";");
        System.err.println(command.toString());
        String execute = execute(machine, command.toString());
        System.err.println(execute);
    }*/

    /**
     * 执行Shell脚本或命令
     *
     * @param machine
     * @param commands
     * @return
     */
    public static String execute(Machine machine, String commands) throws Exception {
        String result;
        Connection connection = null;
        try {
            connection = getConnection(machine);
            // 打开一个会话
            Session session = connection.openSession();
            session.execCommand(commands);
            InputStream in = session.getStdout();
            result = processStandardOutput(in);
            InputStream errorIn = session.getStderr();
            result += processStandardOutput(errorIn);
        } finally {
            close(connection);
        }
        return result;
    }

    @Deprecated
    public static String localExecute(String command) throws Exception {
        String result;
        String[] cmds = {"/bin/sh", "-c", command};
        Process ps = Runtime.getRuntime().exec(cmds);
        InputStream in = ps.getInputStream();
        result = processStandardOutput(in);
        InputStream errorIn = ps.getErrorStream();
        result += processStandardOutput(errorIn);
        return result;
    }

    /**
     * 解析流获取字符串信息
     *
     * @return
     */
    private static String processStandardOutput(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static Connection getConnection(Machine machine) throws Exception {
        String userName = machine.getUserName();
        String password = machine.getPassword();
        String host = machine.getHost();
        Connection connection = new Connection(host);
        connection.connect();
        boolean success = connection.authenticateWithPassword(userName, password);
        if (!success) {
            throw new RuntimeException(host + " login failed.");
        }
        return connection;
    }

    private static final void close(Connection connection) {
        if (connection != null) {
            connection.close();
        }
    }
}
