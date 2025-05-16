package com.device.hk.po;

import lombok.Data;

/**
 * 通道信息
 * @author wulihao
 */
@Data
public class ChannelInfo {

    /**
     * 通道ID
     */
    private int nChannelID;

    /**
     * 设备ID
     */
    private String ip;

    /**
     * 设备端口，一般为37777
     */
    private int port;

    /**
     * 设备名称，一般为SN
     */
    private String deviceName;

}
