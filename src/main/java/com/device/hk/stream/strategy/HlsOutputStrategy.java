package com.device.hk.stream.strategy;

import com.device.hk.stream.StreamOutputStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

/**
 * hls输出策略
 */
@Slf4j
public class HlsOutputStrategy implements StreamOutputStrategy {
    private final String hlsDir;
    private FFmpegFrameRecorder recorder;

    public HlsOutputStrategy(String hlsDir) {
        this.hlsDir = hlsDir;
    }

    @Override
    public void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception {
        String output = hlsDir + "/stream.m3u8";
        recorder = new FFmpegFrameRecorder(output,
                grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        initCommon(grabber, recorder, "hls");
        recorder.setOption("hls_time", "4");
        recorder.setOption("hls_list_size", "5");
        recorder.setOption("hls_flags", "delete_segments");
        recorder.start(ifmtCtx);
    }

    @Override
    public void handlePacket(AVPacket packet) throws Exception {
        recorder.recordPacket(packet);
    }

    @Override
    public void close() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        } catch (Exception e) {
            log.error("关闭HLS推流器出错: {}", e.getMessage());
        }
    }

    private void initCommon(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, String format) {
        recorder.setInterleaved(true);
        recorder.setFormat(format);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setSampleRate(grabber.getSampleRate());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoBitrate(3000000);
        recorder.setGopSize((int) grabber.getFrameRate() * 2);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "superfast");
    }
}

