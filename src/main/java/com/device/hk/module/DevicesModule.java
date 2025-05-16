package com.device.hk.module;

import com.device.hk.po.DeviceInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备模块
 * @author wulihao
 */
@Slf4j
@Data
public class DevicesModule {
    /**
     * 登录句柄
     */
    private int lUserID = -1;

    /**
     * 设备信息
     */
    private DeviceInfo deviceInfo;

    /**
     * 告警句柄
     */
    private int lAlarmHandle;

    /**
     * 是否上线
     */
    private boolean isOnline;

    /**
     * websocket端口
     */
    private short wsPort;


    public DevicesModule(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

}
