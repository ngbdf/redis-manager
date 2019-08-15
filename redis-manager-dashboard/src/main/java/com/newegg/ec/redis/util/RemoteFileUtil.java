package com.newegg.ec.redis.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import com.newegg.ec.redis.entity.Machine;

import java.io.File;
import java.io.IOException;

/**
 * @author Jay.H.Zou
 * @date 8/15/2019
 */
public class RemoteFileUtil {

    private RemoteFileUtil() {
    }

    public static void scp(Machine machine, String localPath, String targetPath) throws IOException {
        String userName = machine.getUserName();
        String password = machine.getPassword();
        String host = machine.getHost();
        Connection connection = new Connection(host);
        connection.connect();
        boolean success = connection.authenticateWithPassword(userName, password);
        if (!success) {
            throw new RuntimeException(host + " login failed.");
        }
        SCPClient scpClient = connection.createSCPClient();
        File file = new File(localPath);
        scpClient.put(localPath, file.length(), targetPath, "0644");
    }

}
