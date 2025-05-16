package com.device.hk.common;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * 需要解析帧 cpu利用率太高，已弃用
 */
@Slf4j
public class HandleStream {
    private PipedOutputStream outputStream;
    private PipedInputStream inputStream;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private volatile boolean running;
    private Thread thread;
    private int count;
    public String pushAddress;

    public HandleStream(String address) {
        try {
            pushAddress=address;
            outputStream = new PipedOutputStream();
            inputStream = new PipedInputStream(outputStream, 4096 * 5);
            running = true;
            log.info("创建视频流处理类对象");
            startProcessing();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize piped streams", e);
        }
    }


    public void processStream(byte[] data) {
        try {
            outputStream.write(data, 0, data.length);
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    public void startProcessing() {
        thread = new Thread(() -> {
//           打印FFmpeg日志可以帮助确定输入流的音视频编码格式帧率等信息,需要时可以取消注释
//            avutil.av_log_set_level(avutil.AV_LOG_INFO);
//            FFmpegLogCallback.set();
            grabber = new FFmpegFrameGrabber(inputStream, 0);

            grabber.setOption("rtsp_transport", "tcp"); // 设置RTSP传输协议为TCP
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 设置视频编解码器为H.264
            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // 设置音频编解码器为ACC
            grabber.setFormat("mpeg"); // 设置格式为MPEG

            try {
                grabber.start();
                log.info("视频宽度:" + grabber.getImageWidth());
                log.info("视频高度:" + grabber.getImageHeight());
                log.info("音频通道:" + grabber.getAudioChannels());

                recorder = new FFmpegFrameRecorder(pushAddress, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
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
                recorder.setGopSize((int) (grabber.getFrameRate()*2));
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setVideoOption("tune", "zerolatency"); // 降低编码延迟
                recorder.setVideoOption("preset", "superfast"); // 提升编码速度

                recorder.start();  // 启动推流器
                Frame frame;
                count=0;
                while (running && (frame = grabber.grab()) != null) {
                    count++;
                    recorder.record(frame);  // 将每帧数据推送到 RTMP 服务器
                    if (count % 100 == 0) {
                        // 处理每帧
                        log.info("推流帧====>" + count);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                try {
                    if(grabber!=null){
                        grabber.stop();
                        grabber.release();
                    }
                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    try {
                        inputStream.close();
                        outputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
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
        log.info("已关闭javacv视频处理线程");
    }
}
