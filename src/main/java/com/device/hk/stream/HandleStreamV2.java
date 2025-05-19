package com.device.hk.stream;

import com.device.hk.stream.StreamOutputStrategy;
import com.device.hk.stream.strategy.HlsOutputStrategy;
import com.device.hk.stream.strategy.RtmpOutputStrategy;
import com.device.hk.stream.strategy.WebSocketOutputStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
public class HandleStreamV2 {
    private PipedOutputStream outputStream = new PipedOutputStream();
    public PipedInputStream inputStream;
    private List<StreamOutputStrategy> strategies = new ArrayList<>();
    private FFmpegFrameGrabber grabber;
    private volatile boolean running = false;
    public  CompletableFuture<String> completableFutureOne;
    private Thread thread;
    private int count;
    private boolean enableWebSocket;
    private String rtmpUrl;
    private String hlsDir;
    private Consumer<byte[]> frameConsumer;

    /**
     * 初始化推流
     * @param completableFutureOne 推流完成回调
     * @param frameConsumer 视频帧回调
     */
    public HandleStreamV2(
            CompletableFuture<String> completableFutureOne,
            Consumer<byte[]> frameConsumer
    ) {
        this(null,null,true, completableFutureOne,frameConsumer);
    }

    /**
     * 初始化推流
     * @param rtmpUrl rtmp地址
     * @param hlsDir hls地址
     * @param enableWebSocket 是否启用websocket
     * @param completableFutureOne 推流完成回调
     * @param frameConsumer 视频帧回调
     */
    public HandleStreamV2(
            String rtmpUrl,
            String hlsDir,
            boolean enableWebSocket,
            CompletableFuture<String> completableFutureOne,
            Consumer<byte[]> frameConsumer
    ) {
        try {
            this.enableWebSocket = enableWebSocket;
            this.rtmpUrl = rtmpUrl;
            this.hlsDir = hlsDir;
            this.frameConsumer = frameConsumer;
            this.completableFutureOne = completableFutureOne;
            inputStream = new PipedInputStream(outputStream, 4096 * 5);
            running = true;
            // 启动推流主循环
            pushLoop();
        } catch (Exception e) {
            log.error("HandleStreamV2 初始化异常：{}", e.getMessage(), e);
            close();
        }
    }

    /**
     * 推流主循环
     */
    private void pushLoop() {
        thread = new Thread(() -> {
            try {
                // 初始化 FFmpegFrameGrabber
                grabber = new FFmpegFrameGrabber(inputStream,0);
                grabber.setFormat("mpeg");
                grabber.start();

                AVFormatContext ifmtCtx = grabber.getFormatContext();

                // 根据配置添加推流策略
                if (enableWebSocket) {
                    strategies.add(new WebSocketOutputStrategy(frameConsumer));
                }
                if (rtmpUrl != null && !rtmpUrl.isEmpty()) {
                    strategies.add(new RtmpOutputStrategy(rtmpUrl));
                }
                if (hlsDir != null && !hlsDir.isEmpty()) {
                    strategies.add(new HlsOutputStrategy(hlsDir));
                }

                // 初始化所有推流策略
                for (StreamOutputStrategy strategy : strategies) {
                    strategy.init(grabber, ifmtCtx);
                }

                AVPacket pkt;
                count = 0;
                while (running) {
                    pkt = grabber.grabPacket();
                    if (pkt == null) {
                        log.warn("No data packet received, waiting...");
                        Thread.sleep(100); // 等待 100 毫秒，防止 CPU 过高
                        continue;
                    }
                    if (pkt.size() == 0) {
                        log.warn("Empty packet received, skipping...");
                        continue;
                    }
                    for (StreamOutputStrategy strategy : strategies) {
                        try {
                            strategy.handlePacket(pkt);
                        } catch (Exception ex) {
                            log.warn("推流处理异常：{}", ex.getMessage());
                        }
                    }
                    count++;

                    completableFutureOne.complete("true");//运行到这说明推流成功了
                    avcodec.av_packet_unref(pkt);
                }
            } catch (Exception e) {
                log.error("推流主循环异常：{}", e.getMessage(), e);
            } finally {
                close();
            }
        });
        thread.setName("javacv-video-push-thread");
        thread.start();
    }

    public void close() {
        try {
            for (StreamOutputStrategy strategy : strategies) {
                strategy.close();
            }
            if (grabber != null) {
                grabber.stop();
                grabber.release();
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            log.error("关闭 HandleStreamV2 出错：{}", e.getMessage());
        }
    }

    public void processStream(byte[] data) {
        try {
            outputStream.write(data, 0, data.length);
        } catch (Exception e) {
            log.error("Send stream error ,This service has been discontinued.{}", e.getMessage());
        }
    }

    public void stopProcessing() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
        System.out.println("已关闭javacv视频处理线程");
    }
}
