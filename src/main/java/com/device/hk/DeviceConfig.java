package com.device.hk;

import lombok.Data;

@Data
public class DeviceConfig {
    private String ip;
    private int port;
    private String userName;
    /**
     * 登录句柄
     */
    private int lUserID = -1;

    private int lAlarmHandle = -1;
}
