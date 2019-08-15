package com.newegg.ec.redis.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import com.newegg.ec.redis.entity.Machine;

import java.io.IOException;

/**
 * @author Jay.H.Zou
 * @date 8/15/2019
 */
public class RemoteFeileUtil {

    private RemoteFeileUtil() {
    }

    public static boolean scp(Machine machine, String localPath, String targetPath) throws IOException {
        String userName = machine.getUserName();
        String password = machine.getPassword();
        String host = machine.getHost();
        Connection connection = new Connection(host);
        connection.connect();
        boolean success = connection.authenticateWithPassword(userName, password);
        if (!success) {
            return false;
        }
        SCPClient scpClient = connection.createSCPClient();
        scpClient.put(localPath, 100, targetPath, "0644");
        return true;
    }

}
