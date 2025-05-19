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
 * rtmp推流
 */
@Slf4j
public class RtmpOutputStrategy implements StreamOutputStrategy {
    private final String rtmpUrl;
    private FFmpegFrameRecorder recorder;

    public RtmpOutputStrategy(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    @Override
    public void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception {
        recorder = new FFmpegFrameRecorder(rtmpUrl,
                grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        initCommon(grabber, recorder, "flv");
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
            log.error("关闭RTMP推流器出错: {}", e.getMessage());
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
