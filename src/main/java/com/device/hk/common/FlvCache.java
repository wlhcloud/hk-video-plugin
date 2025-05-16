package com.device.hk.common;

import lombok.Data;

@Data
public class FlvCache {
    private static byte[] flvHeader;
    private static byte[] keyFrame;

    public static synchronized void setFlvHeader(byte[] header) {
        flvHeader = header;
    }

    public static synchronized byte[] getFlvHeader() {
        return flvHeader;
    }

    public static synchronized void setKeyFrame(byte[] frame) {
        keyFrame = frame;
    }

    public static synchronized byte[] getKeyFrame() {
        return keyFrame;
    }
}

