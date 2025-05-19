package com.device.hk.stream;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;

/**
 * 输出流策略
 */
public interface StreamOutputStrategy {
    void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception;
    void handlePacket(AVPacket packet) throws Exception;
    void close();
}
