package com.device.hk.stream;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 跳过了解析帧，降低cpu利用率
 *
 * @author wulihao
 */
@Slf4j
public class HandleStreamV1 {
    private PipedOutputStream outputStream;
    private ByteArrayOutputStream outputStreamPush;
    private PipedInputStream inputStream;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private volatile boolean running;
    public Thread thread;
    private int count;
    public String pushAddress;
    public String playKey;
    private final Consumer<byte[]> frameConsumer;

    private CompletableFuture<String> completableFutureString;

    public HandleStreamV1(String playKey, CompletableFuture<String> completableFuture,Consumer<byte[]> frameConsumer) {
        try {
            completableFutureString = completableFuture;
            this.playKey = playKey;
            this.frameConsumer = frameConsumer;
            outputStream = new PipedOutputStream();
            outputStreamPush = new ByteArrayOutputStream(4096 * 5);
            inputStream = new PipedInputStream(outputStream, 4096 * 5);
            running = true;
            System.out.println("创建视频流处理类对象" + outputStream.hashCode());
            startProcessing();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize piped streams", e);
        }
    }


    public void processStream(byte[] data) {
        try {
            outputStream.write(data, 0, data.length);
        } catch (Exception e) {
            log.error("Send stream error ,This service has been discontinued.{}", e.getMessage());
        }
    }


    private void startProcessing() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
//           打印FFmpeg日志可以帮助确定输入流的音视频编码格式帧率等信息,需要时可以取消注释
//            avutil.av_log_set_level(avutil.AV_LOG_INFO);
//            FFmpegLogCallback.set();
                    grabber = new FFmpegFrameGrabber(inputStream, 0);
                    grabber.setOption("rtsp_transport", "tcp"); // 设置RTSP传输协议为TCP
//            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 设置视频编解码器为H.264
//            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // 设置音频编解码器为ACC
                    grabber.setFormat("mpeg"); // 设置格式为MPEG
                    grabber.start();

                    // 获取输入格式上下文
                    AVFormatContext ifmt_ctx = grabber.getFormatContext();

                    System.out.println("视频宽度:" + grabber.getImageWidth());
                    System.out.println("视频高度:" + grabber.getImageHeight());
                    System.out.println("音频通道:" + grabber.getAudioChannels());

                    recorder = new FFmpegFrameRecorder(outputStreamPush, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
                    recorder.setInterleaved(true);  // 设置音视频交织方式
                    recorder.setVideoOption("crf", "23"); //画质参数
                    recorder.setFormat("flv");  // 设置推流格式为 FLV
//                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // 设置音频编码器为 AAC
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);  // 设置视频编码器为 H.264

                    recorder.setSampleRate(grabber.getSampleRate());  // 设置音频采样率
                    recorder.setFrameRate(grabber.getFrameRate()); //设置视频帧率
                    recorder.setVideoBitrate(3000000);  // 设置视频比特率为 3 Mbps（根据需要调整）
//                recorder.setVideoQuality(0);  // 设置视频质量参数（0为最高质量）
//                recorder.setAudioQuality(0);  // 设置音频质量参数（0为最高质量）
                    recorder.setGopSize((int) (grabber.getFrameRate() * 2));
                    recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                    recorder.setVideoOption("tune", "zerolatency"); // 降低编码延迟
                    recorder.setVideoOption("preset", "superfast"); // 提升编码速度

                    recorder.start(ifmt_ctx);  // 启动推流器
                    Frame frame;

                    count = 0;

                    AVPacket packet;

                    while (running) {
                        try {
                            packet = grabber.grabPacket();
                            if (packet == null) {
                                log.warn("No data packet received, waiting...");
                                Thread.sleep(100); // 等待 100 毫秒，防止 CPU 过高
                                continue;
                            }
                            if (packet.size() == 0) {
                                log.warn("Empty packet received, skipping...");
                                continue;
                            }

                            count++;
                            recorder.recordPacket(packet);
                            completableFutureString.complete("true");//运行到这说明推流成功了
                            if (count % 100 == 0) {
                                // 处理每帧
                                System.out.println("packet推流帧====>" + count);
                            }


                            byte[] flvData = outputStreamPush.toByteArray();
                            outputStreamPush.reset();

                            if (flvData.length > 0 && frameConsumer != null) {
                                // 设置 FLV header（通常只需要设置一次）
                                if (FlvCache.getFlvHeader() == null) {
                                    FlvCache.setFlvHeader(flvData); // 前 13 字节为标准 FLV 头（或更长，视情况）
                                }

                                if (FlvCache.getKeyFrame() == null && isKeyFrame(flvData)) {
                                    FlvCache.setKeyFrame(flvData);
                                }

                                frameConsumer.accept(flvData);  // 调用外部定义的 WebSocket 发送逻辑
                            }
                        } catch (Exception e) {
                            log.error("Recording error: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    completableFutureString.complete("false");//运行到这说明推流异常,需要反馈到前端
                    log.error(e.getMessage());
                } finally {
                    try {
                        if (grabber != null) {
                            grabber.stop();
                            grabber.release();
                        }
                        if (recorder != null) {
                            recorder.stop();
                            recorder.release();
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    } finally {
                        try {
                            inputStream.close();
                            outputStream.close();
                            outputStreamPush.close();
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public void stopProcessing() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
        System.out.println("已关闭javacv视频处理线程");
    }
    private boolean isKeyFrame(byte[] data) {
        return (data.length > 0 && (data[0] & 0xF0) == 0x10); // 视频Tag + I帧标记
    }

}

