package com.device.hk.common;

import com.device.hk.module.DevicesModule;
import com.device.hk.po.DeviceInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
public class DeviceListUtil {
    private static List<DevicesModule> deviceList = new ArrayList<>();

    public static List<DevicesModule> getDeviceList() {
        return deviceList;
    }

    /**
     * 通过设备id获取设备信息
     * @param deviceId
     * @return
     */
    public static DevicesModule getDeviceModuleByDeviceId(String deviceId) {
        for (DevicesModule deviceModule : deviceList) {
            if (deviceModule.getDeviceInfo().getDeviceId().equals(deviceId)) {
                return deviceModule;
            }
        }
        return null;
    }

    /**
     * 通过ip和端口获取设备id
     * @param ip
     * @param port
     * @return
     */
    public static String getDeviceIDByIP(String ip, int port){
        for (DevicesModule deviceModule : deviceList) {
            if (deviceModule.getDeviceInfo().getDeviceIp().equals(ip) && deviceModule.getDeviceInfo().getDevicePort() == port) {
                return deviceModule.getDeviceInfo().getDeviceId();
            }
        }
        return null;
    }

    /**
     * 通过llAlarmHandle获取设备信息
     * @param lAlarmHandle
     * @return
     */
    public static DevicesModule getDeviceModuleByAlarmHandle(int lAlarmHandle) {
        for (DevicesModule deviceModule : deviceList) {
            int  alarmHandle = deviceModule.getLAlarmHandle();
            if (alarmHandle  == lAlarmHandle) {
                return deviceModule;
            }
        }
        return null;
    }


    /**
     * 通过ip和端口获取设备信息
     * @param ip
     * @param port
     * @return
     */
    public static DevicesModule getDeviceModuleByIpAndPort(String ip ,Integer port) {
        for (DevicesModule deviceModule : deviceList) {
            DeviceInfo deviceInfo = deviceModule.getDeviceInfo();
            if (ip.equals(deviceInfo.getDeviceIp()) && port.equals(deviceInfo.getDevicePort())) {
                return deviceModule;
            }
        }
        return null;
    }

    /**
     * 通过设备登录id获取设备信息
     * @param lUser
     * @return
     */
    public static DevicesModule getDeviceModuleBylUser(int lUser) {
        for (DevicesModule deviceModule : deviceList) {
            if(lUser == deviceModule.getLUserID()){
                return deviceModule;
            }
        }
        return null;
    }
}
