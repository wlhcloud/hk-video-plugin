package com.device.hk.websocket;

import com.device.hk.stream.FlvCache;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

/**
 * websocket服务端
 */
public class WebSocketServer extends org.java_websocket.server.WebSocketServer {

    // 固定大小线程池，比如10个线程，数量可调节
    private final ExecutorService sendExecutor = Executors.newFixedThreadPool(10);
    // 每个连接对应一个发送队列
    private final ConcurrentHashMap<WebSocket, LinkedBlockingQueue<byte[]>> sendQueues = new ConcurrentHashMap<>();


    public WebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("连接打开：" + conn.getRemoteSocketAddress());
        LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(1000); // 队列容量1000
        sendQueues.put(conn, queue);
        // 建立连接时，立即发送FLV头部
        if (conn.isOpen()) {
            try {
                if (FlvCache.getFlvHeader() != null) {
                    conn.send(FlvCache.getFlvHeader()); // 发送 FLV 头
                }
                if (FlvCache.getKeyFrame() != null) {
                    conn.send(FlvCache.getKeyFrame()); // 发送关键帧
                }
                System.out.println("FLV头部发送成功");
            } catch (Exception e) {
                System.err.println("发送FLV头部失败: " + e.getMessage());
            }
        }

        sendExecutor.submit(() -> {
            try {
                while (conn.isOpen()) {
                    byte[] data = queue.take(); // 阻塞等待消息
                    try {
                        conn.send(data);
                    } catch (Exception e) {
                        System.err.println("发送失败，丢弃该帧，继续发送后续帧：" + e.getMessage());
                        // 不重试，直接丢弃，防止队列阻塞
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sendQueues.remove(conn);
            }
        });
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        sendQueues.remove(conn);
        System.out.println("连接关闭：" + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("收到消息：" + message);
        // 可选：处理客户端发来的指令
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("WebSocket 错误：" + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server 启动成功，监听端口：" + getPort());
    }

    public void sendToAll(byte[] data) {
        for (Map.Entry<WebSocket, LinkedBlockingQueue<byte[]>> entry : sendQueues.entrySet()) {
            LinkedBlockingQueue<byte[]> queue = entry.getValue();
            // 不要阻塞、不要重试，丢弃旧消息，保留最新消息
            if (!queue.offer(data)) {
                queue.poll();  // 丢弃最旧消息
                queue.offer(data);  // 放入最新消息
            }
        }
    }

    // 关闭时，记得优雅关闭线程池
    public void shutdown() {
        sendExecutor.shutdown();
    }

}
