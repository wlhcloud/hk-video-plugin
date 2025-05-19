package com.device.hk.stream.strategy;

import com.device.hk.stream.FlvCache;
import com.device.hk.stream.StreamOutputStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

/**
 * WebSocket输出
 */
@Slf4j
public class WebSocketOutputStrategy implements StreamOutputStrategy {
    private final ByteArrayOutputStream outputStreamPush = new ByteArrayOutputStream(4096 * 5);
    private final Consumer<byte[]> frameConsumer;
    private FFmpegFrameRecorder recorder;
    private final String playKey;

    public WebSocketOutputStrategy(String playKey,Consumer<byte[]> frameConsumer) {
        this.frameConsumer = frameConsumer;
        this.playKey = playKey;
    }

    @Override
    public void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception {
        recorder = new FFmpegFrameRecorder(outputStreamPush,
                grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        initCommon(grabber, recorder, "flv");
        recorder.start(ifmtCtx);
    }

    @Override
    public void handlePacket(AVPacket packet) throws Exception {
        recorder.recordPacket(packet);
        byte[] flvData = outputStreamPush.toByteArray();
        outputStreamPush.reset();

        if (flvData.length > 0 && frameConsumer != null) {
            if (FlvCache.getFlvHeader(playKey) == null) {
                FlvCache.cacheFlvHeader(playKey, flvData);
            }
            if (FlvCache.getKeyFrame(playKey) == null && isKeyFrame(flvData)) {
                FlvCache.cacheKeyFrame(playKey,flvData);
            }
            frameConsumer.accept(flvData);
        }
    }

    @Override
    public void close() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            outputStreamPush.close();
        } catch (Exception e) {
            log.error("关闭WebSocket推流器出错: {}", e.getMessage());
        }
    }

    private boolean isKeyFrame(byte[] data) {
        return data.length > 0 && (data[0] & 0xF0) == 0x10;
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
