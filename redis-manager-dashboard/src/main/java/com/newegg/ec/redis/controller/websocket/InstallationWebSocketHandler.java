package com.newegg.ec.redis.controller.websocket;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.sockjs.SockJsTransportFailureException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static com.newegg.ec.redis.util.TimeUtil.ONE_HOUR;

/**
 * Created by gl49 on 2018/4/22.
 */
@Component
public class InstallationWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(InstallationWebSocketHandler.class);

    private static Map<String, BlockingDeque<String>> CLUSTER_MESSAGE_MAP = new ConcurrentHashMap<>();

    private static Map<String, Long> CLUSTER_START = new ConcurrentHashMap<>();

    private static Map<WebSocketSession, String> WEB_SOCKET_CLUSTER_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        try {
            webSocketSession.sendMessage(new TextMessage("Start installation..."));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        try {
            String clusterName = webSocketMessage.getPayload().toString();
            createLogQueueIfNotExist(clusterName, webSocketSession);
            BlockingDeque<String> logQueue;
            while (true) {
                logQueue = getLogQueue(clusterName);
                if (logQueue == null) {
                    break;
                }
                if (logQueue.size() == 0) {
                    Thread.sleep(2000);
                    Long startTime = CLUSTER_START.get(clusterName);
                    if (System.currentTimeMillis() - startTime > ONE_HOUR) {
                        CLUSTER_MESSAGE_MAP.remove(clusterName);
                        CLUSTER_START.remove(clusterName);
                        break;
                    }
                }
                String message = logQueue.poll();
                if (!Strings.isNullOrEmpty(message)) {
                    System.err.println("Message: " + message);
                    try {
                        webSocketSession.sendMessage(new TextMessage(message));
                    } catch (SockJsTransportFailureException | IOException e) {
                        // ignore
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        try {
            if (webSocketSession.isOpen()) {
                webSocketSession.close();
            }
            removeLogMap(webSocketSession);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        removeLogMap(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public static void appendLog(String clusterName, String message) {
        BlockingDeque<String> blockingDeque = getLogQueue(clusterName);
        if (blockingDeque != null) {
            blockingDeque.add(message);
        }
    }

    public static void removeLogMap(WebSocketSession webSocketSession) {
        String clusterName = WEB_SOCKET_CLUSTER_MAP.get(webSocketSession);
        WEB_SOCKET_CLUSTER_MAP.remove(webSocketSession);
        if (CLUSTER_MESSAGE_MAP != null && !Strings.isNullOrEmpty(clusterName)) {
            CLUSTER_MESSAGE_MAP.remove(clusterName);
        }
    }

    private static BlockingDeque<String> createLogQueueIfNotExist(String clusterName, WebSocketSession webSocketSession) {
        CLUSTER_MESSAGE_MAP.putIfAbsent(clusterName, new LinkedBlockingDeque<>());
        CLUSTER_START.put(clusterName, System.currentTimeMillis());
        WEB_SOCKET_CLUSTER_MAP.putIfAbsent(webSocketSession, clusterName);
        return CLUSTER_MESSAGE_MAP.get(clusterName);
    }

    public static BlockingDeque<String> getLogQueue(String clusterName) {
        return CLUSTER_MESSAGE_MAP.get(clusterName);
    }

}