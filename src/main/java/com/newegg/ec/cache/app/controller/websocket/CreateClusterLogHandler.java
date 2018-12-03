package com.newegg.ec.cache.app.controller.websocket;

import net.sf.json.JSONObject;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by gl49 on 2018/4/22.
 */
public class CreateClusterLogHandler implements WebSocketHandler {
    private static final Map<String, BlockingDeque<String>> logMap = new ConcurrentHashMap<>();
    private static final Map<WebSocketSession, String> websocketLogMap = new ConcurrentHashMap<>();

    private static BlockingDeque<String> createLogQueueIfNotExist(String id, WebSocketSession webSocketSession) {
        if (null == logMap.get(id)) {
            logMap.put(id, new LinkedBlockingDeque<>());
        }
        if (null == websocketLogMap.get(webSocketSession)) {
            websocketLogMap.put(webSocketSession, id);
        }
        return logMap.get(id);
    }

    private static BlockingDeque<String> getLogQueue(String id) {
        return logMap.get(id);
    }

    public static void appendLog(String id, String msg) {
        BlockingDeque<String> blockingDeque = getLogQueue(id);
        if (null != blockingDeque) {
            blockingDeque.add(msg);
        }
    }

    public static void removeLogMap(WebSocketSession webSocketSession) {
        String id = websocketLogMap.get(webSocketSession);
        websocketLogMap.remove(webSocketSession);
        if (logMap.containsKey(id)) {
            logMap.remove(id);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        webSocketSession.getAttributes();
        webSocketSession.sendMessage(new TextMessage("connection success"));
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        JSONObject reqObject = JSONObject.fromObject(webSocketMessage.getPayload().toString());
        String id = reqObject.getString("id");
        createLogQueueIfNotExist(id, webSocketSession);
        BlockingDeque<String> logQueue;
        while (true) {
            logQueue = getLogQueue(id);
            String message = logQueue.poll();
            if (!StringUtils.isEmpty(message)) {
                webSocketSession.sendMessage(new TextMessage(message));
            } else {
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        if (webSocketSession.isOpen()) {
            webSocketSession.close();
        }
        removeLogMap(webSocketSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        removeLogMap(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
