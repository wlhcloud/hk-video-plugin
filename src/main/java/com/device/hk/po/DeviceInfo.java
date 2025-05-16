package com.device.hk.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备信息
 * @author wulihao
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInfo {
    /**
     * 设备ID
     */
    private String deviceId;

    /**
     *设备IP
     */
    private String deviceIp;

    /**
     *设备端口
     */
    private short devicePort;

    /**
     *设备用户名
     */
    private String deviceUser;

    /**
     *设备密码
     */
    private String devicePassword;
}
