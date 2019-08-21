package com.newegg.ec.redis.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.plugin.install.service.impl.DockerInstallationOperation;
import com.newegg.ec.redis.plugin.install.service.impl.MachineInstallationOperation;

import java.io.*;

/**
 * @author Jay.H.Zou
 * @date 8/15/2019
 */
public class SSH2Util {

    private SSH2Util() {
    }

    public static void scp(Machine machine, String localPath, String targetPath) throws Exception {
        Connection connection = null;
        try {
            connection = getConnection(machine);
            SCPClient scpClient = connection.createSCPClient();
            File file = new File(localPath);
            scpClient.put(localPath, file.length(), targetPath, "0755");
        } finally {
            close(connection);
        }
    }

    /**
     * copy file
     *
     * @param machine
     * @param file
     * @param targetPath
     * @throws Exception
     */
    public static void copy(Machine machine, String file, String targetPath, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        command.append("cp ").append(file).append(SignUtil.SPACE).append(targetPath);
        System.err.println(command.toString());
        String result = execute(machine, command.toString());
        if (!Strings.isNullOrEmpty(result)) {
            throw new RuntimeException(result);
        }
    }

    public static void rm(Machine machine, String file, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        if (file.startsWith(DockerInstallationOperation.DOCKER_INSTALL_BASE_PATH)
                || file.startsWith(MachineInstallationOperation.MACHINE_INSTALL_BASE_PATH)) {
            command.append("rm -rf ").append(file);
            String result = execute(machine, command.toString());
            if (!Strings.isNullOrEmpty(result)) {
                throw new RuntimeException(result);
            }
        }

    }

    /**
     * mkdir -p
     *
     * @param machine
     * @param path
     * @throws Exception
     */
    public static void mkdir(Machine machine, String path, boolean sudo) throws Exception {
        StringBuffer command = new StringBuffer();
        if (sudo) {
            command.append("sudo ");
        }
        command.append("mkdir -p ").append(path);
        String result = execute(machine, command.toString());
        if (!Strings.isNullOrEmpty(result)) {
            throw new RuntimeException(result);
        }
    }

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
