package com.device.hk.opretions;

import com.device.hk.DeviceOperationStrategy;
import com.device.hk.common.AjaxResult;
import com.device.hk.common.DeviceListUtil;
import com.device.hk.module.DevicesModule;
import com.device.hk.po.DeviceInfo;

import java.util.Map;

/**
 * 获取设备状态的操作类，实现了 DeviceOperationStrategy 接口。
 * 该类用于处理获取设备状态的逻辑，包括解析设备配置信息和执行状态查询操作。
 *
 * 主要功能：
 * 1. 根据传入的配置参数，将设备信息转换为 DeviceInfo 对象。
 * 2. 打印设备的IP地址和端口号，模拟获取设备状态的具体逻辑。
 * 3. 返回操作成功的结果，表示设备状态获取完成。
 *
 * 注意事项：
 * - 配置参数中必须包含设备的相关信息（如IP地址、端口号等），否则可能导致转换失败。
 * - 当前实现仅打印设备信息，未包含实际的设备状态查询逻辑。
 * - 返回结果通过 AjaxResult 表示，调用方需根据返回的状态码判断操作是否成功。
 */
public class GetDeviceStatusOperation implements DeviceOperationStrategy {
    @Override
    public AjaxResult executeOperation(Map<String,Object> config) {
        Object deviceId = config.get("deviceId");

        DevicesModule devicesModule = DeviceListUtil.getDeviceModuleByDeviceId(String.valueOf(deviceId));
        if (devicesModule == null) {
            return AjaxResult.error("设备未注册");
        }
        if(!devicesModule.isOnline()){
            return AjaxResult.error("设备未在线");
        }
        DeviceInfo deviceInfo = devicesModule.getDeviceInfo();
        // 这里执行获取设备状态的具体逻辑
        System.out.println("获取设备状态，IP = " +deviceInfo.getDeviceIp() + ", 端口 = " + deviceInfo.getDevicePort());
        // 假设获取状态成功
        return AjaxResult.success();
    }
}
