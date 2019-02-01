package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.core.logger.CommonLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Leo Fu on 2018/11/24.
 *
 * jedis客户端没有提供某些命令的API(如config rewrite等)，可以依据RESP协议简单实现这些的API
 * 开箱即用，用完关闭客户都即可
 */
public class RedisClient {

    public static final CommonLogger logger = new CommonLogger(RedisClient.class);

    public static final String REWRITE = "*2\r\n$6\r\nconfig\r\n$7\r\nrewrite\r\n";
    public static final String MEMORYPURGE = "*2\r\n$6\r\nmemory\r\n$5\r\npurge\r\n";
    public static final String MEMORYDOCTOR = "*2\r\n$6\r\nmemory\r\n$6\r\ndoctor\r\n";

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public RedisClient(String host, int port){
        try {

            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(host, port), 10000);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

        } catch (IOException e) {
            logger.error("Create RedisClient Error",e);
        }
    }

    /**
     * @param command
     * @return
     * @throws IOException
     */
    public String redisCommandOpt(String command) throws IOException {

        byte[] bytes= new byte[4096];
        outputStream.write(command.getBytes());
        inputStream.read(bytes);
        return new String(bytes);

    }

    /**
     * 密码认证redis操作
     * @param pssword
     * @param command
     * @return
     * @throws IOException
     */
    public String redisCommandOpt(String pssword,String command) throws IOException, InterruptedException {

        byte[] bytes= new byte[4096];
        StringBuilder auth = new StringBuilder();
        auth.append("*2").append("\r\n");
        auth.append("$4").append("\r\n");
        auth.append("AUTH").append("\r\n");
        auth.append("$").append(pssword.length()).append("\r\n");
        auth.append(pssword).append("\r\n");
        outputStream.write(auth.toString().getBytes());
        outputStream.write(command.getBytes());

        //经测试发现，在有密码的集群执行command时，需要延时一定的时间确保redis的response结果完全写入inputStream中
        Thread.sleep(20);
        inputStream.read(bytes);
        return new String(bytes);

    }

    public void closeClient() {

        try {
            if(socket != null && !socket.isClosed()){
                socket.close();
            }
            if(outputStream != null ){
                outputStream.close();
            }
            if(inputStream != null ){
                inputStream.close();
            }
        }catch (Exception e){
            logger.error("Close RedisClient Error",e);
        }

    }


}
