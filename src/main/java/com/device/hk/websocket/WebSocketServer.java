package com.device.hk.websocket;

import com.device.hk.stream.FlvCache;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
public class WebSocketServer extends org.java_websocket.server.WebSocketServer {

    private final ExecutorService sendExecutor = Executors.newFixedThreadPool(10);
    private final Map<WebSocket, LinkedBlockingQueue<byte[]>> sendQueues = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> connectionPlayKeyMap = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocket>> playKeyConnectionMap = new ConcurrentHashMap<>();

    public WebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String playKey = parsePlayKey(handshake);
        if (playKey == null) {
            conn.close(1008, "缺少 playKey 参数");
            return;
        }

        System.out.println("连接打开：" + conn.getRemoteSocketAddress() + " => playKey: " + playKey);

        connectionPlayKeyMap.put(conn, playKey);
        playKeyConnectionMap.computeIfAbsent(playKey, k -> ConcurrentHashMap.newKeySet()).add(conn);

        LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(1000);
        sendQueues.put(conn, queue);

        // 发送 FLV header + 关键帧
        if (conn.isOpen()) {
            try {
                if (FlvCache.getFlvHeader(playKey) != null) {
                    conn.send(FlvCache.getFlvHeader(playKey));
                }
                if (FlvCache.getKeyFrame(playKey) != null) {
                    conn.send(FlvCache.getKeyFrame(playKey));
                }
                System.out.println("FLV 头部发送成功");
            } catch (Exception e) {
                System.err.println("发送 FLV 头部失败: " + e.getMessage());
            }
        }

        sendExecutor.submit(() -> {
            try {
                while (conn.isOpen()) {
                    byte[] data = queue.take();
                    try {
                        conn.send(data);
                    } catch (Exception e) {
                        System.err.println("发送失败，跳过：" + e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sendQueues.remove(conn);
                String key = connectionPlayKeyMap.remove(conn);
                if (key != null) {
                    playKeyConnectionMap.getOrDefault(key, Collections.emptySet()).remove(conn);
                }
            }
        });
    }

    private String parsePlayKey(ClientHandshake handshake) {
        String resourceDesc = handshake.getResourceDescriptor(); // e.g., "/?playKey=device123"
        if (resourceDesc.contains("?")) {
            String[] parts = resourceDesc.split("\\?");
            if (parts.length > 1) {
                String[] params = parts[1].split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && kv[0].equals("playKey")) {
                        return kv[1];
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        sendQueues.remove(conn);
        String key = connectionPlayKeyMap.remove(conn);
        if (key != null) {
            playKeyConnectionMap.getOrDefault(key, Collections.emptySet()).remove(conn);
        }
        System.out.println("连接关闭：" + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("收到消息：" + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("WebSocket 错误：" + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server 启动成功，监听端口：" + getPort());
    }

    public void sendToPlayKey(String playKey, byte[] data) {
        Set<WebSocket> clients = playKeyConnectionMap.getOrDefault(playKey, Collections.emptySet());
        for (WebSocket conn : clients) {
            LinkedBlockingQueue<byte[]> queue = sendQueues.get(conn);
            if (queue != null) {
                if (!queue.offer(data)) {
                    queue.poll();
                    queue.offer(data);
                }
            }
        }
    }

    public void shutdown() {
        sendExecutor.shutdown();
    }
}

